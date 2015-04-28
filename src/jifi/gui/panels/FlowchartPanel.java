/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.gui.panels;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.nfunk.jep.Variable;
import jifi.algorithm.Command;
import jifi.algorithm.GraphicFlowchart;
import jifi.algorithm.procedure.Block;
import jifi.algorithm.procedure.DummyBlock;
import jifi.algorithm.procedure.Function;
import jifi.algorithm.procedure.Function.FunctionEnd;
import jifi.algorithm.procedure.If;
import jifi.algorithm.procedure.Procedure;
import jifi.drawable.Drawable;
import jifi.drawable.DrawingPanel;
import jifi.drawable.GraphicObject;
import jifi.drawable.graphicresource.GraphicResource;
import jifi.drawable.swing.WidgetContainer;
import jifi.drawable.util.QuickFrame;
import jifi.gui.panels.sidepanel.Item;
import jifi.gui.panels.sidepanel.SidePanel;
import jifi.interpreter.Interpreter;
import jifi.plugin.PluginManager;

/**
 *
 * @author antunes
 */
public class FlowchartPanel extends DrawingPanel implements Interpertable, Observer {

    private static final Font defaultFont;

    static {
        defaultFont = new Font("Dialog", Font.BOLD, 10);
    }

    private final Color selectionColor = new Color(0, .2f, .5f, .5f);
    private final Color executionColor = new Color(0, 1, 0, .5f);
    private final Color errorColor = new Color(1, 0, 0, .5f);
    public ArrayList<JPanel> tabs = new ArrayList<>();
    private final ArrayList<Command> selection = new ArrayList<>();
    private final ArrayList<Command> copy = new ArrayList<>();
    private final Stack<Function> undo = new Stack<>();
    private final Stack<Function> redo = new Stack<>();
    private final SidePanel sidePanel;
    private final Interpreter interpreter;
    private Function function;
    private boolean keyActionUsed = false;
    private Command newCommand = null;
    private Item itemSelected = null;
    private int clickDrop = 0;
    private GraphicObject executionCommand = null;
    private ArrayList<Variable> v = new ArrayList<>();
    private ArrayList<Color> c = new ArrayList<>();
    private ArrayList<Queue<UpdateVar>> q = new ArrayList<>();

    public FlowchartPanel(Function function, final Interpreter interpreter) {
        sidePanel = new SidePanel(this) {
            @Override
            public void itemSelected(Item item, Object ref) {
                Command currentCommand = interpreter.getCurrentCommand();
                Function function = interpreter.getMainFunction();
                if (interpreter.getInterpreterState() == Interpreter.PLAY || currentCommand != function) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null, "Atenção: A edição do fluxograma está suspensa\naté que o código termine ou seja parado.", "Atenção", JOptionPane.WARNING_MESSAGE);
                            if (itemSelected != null) {
                                itemSelected.setSelected(false);
                                itemSelected = null;
                            }
                        }
                    });
                }

                try {
                    if (itemSelected != null) {
                        itemSelected.setSelected(false);
                    }
                    itemSelected = item;
                    itemSelected.setSelected(true);
                    newCommand = SidePanel.newInstance(ref);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        sidePanel.setColor(Color.decode("#54A4A4"));
        sidePanel.addAllClasses(PluginManager.getPluginsAlpha("jifi/algorithm/plugin.txt", Procedure.class));
        sidePanel.addAllClasses(PluginManager.getPluginsAlpha("jifi/plugin/cmdpack/plugin.txt", Procedure.class));

        add(sidePanel);

        this.interpreter = interpreter;
        if (!interpreter.isAlive()) {
            this.interpreter.start();
        }
        setFunction(function);
        super.setName("Fluxograma");
        gridSize = -10;
        gridColor = new Color(0.95f, 0.95f, 0.95f);

    }

    public void hideSidePanel(boolean b) {
        sidePanel.setOpen(!b);
    }

    @Override
    public Interpreter getInterpreter() {
        return interpreter;
    }

    public Function getFunction() {
        return function;
    }

    public final void setFunction(Function function) {
        removeGraphicResources(this.function);
        this.function = function;

        ident(function);

        setName(function.toString());
        interpreter.setInterpreterState(Interpreter.STOP);
        interpreter.setMainFunction(function);
    }

    public void ident(Function f) {
        addDummyBlocks(function, this);
        ident(function, true);
        addAllDrawableResources(function, this);
    }

    private static void addDummyBlocks(Command c, FlowchartPanel fp) {
        if (c instanceof Block) {
            Block b = (Block) c;
            if (b.size() == 1) { //só tem o EndBlock
                DummyBlock db = new DummyBlock();
                b.add(db);
            }
            Command it = b.getStart();
            while (it != null) {
                addDummyBlocks(it, fp);
                if (it instanceof DummyBlock) {
                    //confirma
                    if (it.getParent() instanceof Block) {
                        if (((Block) it.getParent()).size() > 2) {
                            fp.removeGraphicResources(it);
                        }
                    }
                }
                it = it.getNext();
            }
        } else if (c instanceof If) {
            addDummyBlocks(((If) c).getBlockTrue(), fp);
            addDummyBlocks(((If) c).getBlockFalse(), fp);
        }
    }

    private static void hideAllWidgets(Command c, Command ign) {
        if (c != ign) {
            GraphicObject go = c.getDrawableResource();
            if (go != null) {
                if (go instanceof WidgetContainer) {
                    ((WidgetContainer) go).setWidgetVisible(false);
                }
            }
        }
        if (c instanceof Block) {
            Block b = (Block) c;
            if (b.size() == 1) { //só tem o EndBlock
                DummyBlock db = new DummyBlock();
                b.add(db);
            }
            Command it = b.getStart();
            while (it != null) {
                hideAllWidgets(it, ign);
                it = it.getNext();
            }
        } else if (c instanceof If) {
            hideAllWidgets(((If) c).getBlockTrue(), ign);
            hideAllWidgets(((If) c).getBlockFalse(), ign);
        }
    }

    private static void ident(Function f, boolean b) {
        f.ident(GraphicFlowchart.GF_X,
                GraphicFlowchart.GF_Y,
                GraphicFlowchart.GF_J,
                GraphicFlowchart.GF_K);
    }

    private boolean addCommand(Point p) {
        Command c = function.find(p);
        if (c != null) {
            boolean addNext = true;
            if (c instanceof FunctionEnd) {
                if (c.getPrevious() != null) {
                    c = c.getPrevious();
                } else {
                    c = c.getParent();
                }
            }

            //(***) descomentar para adicionar blocos antes 
            //se clicado na parte superior da seleção
            if (c instanceof Function) {
                c = ((Function) c).get(0);
                addNext = false; //(***)
            }

            if (c instanceof GraphicResource) { //(***)
                GraphicObject d = ((GraphicResource) c).getDrawableResource();
                if (d != null) {
                    //g.draw(d.getObjectShape());

                    //alterar usando fIx e fIy
                    if (c instanceof DummyBlock || p.y > d.getObjectBouds().getCenterY()) {
                        addNext = true;
                    } else {
                        addNext = false;
                    }

                }
            }
            Command n = newCommand;

//            interpreter.setInterpreterState(Interpreter.STOP);
            if (addNext) {
                c.addAfter(newCommand);
            } else {
                c.addBefore(newCommand);
            }

            if (n instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) n).getDrawableResource();
                if (d != null) {
                    this.add(d);
                }
            }

            pushUndo();
            redo.clear();

            if (c instanceof DummyBlock) {
                removeGraphicResources(c);
            }

            selection.clear();
            selection.add(newCommand);

            ident(function);
            return true;
        }
        return false;
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {

        Command cmd = interpreter.getCurrentCommand();

        if (interpreter.getInterpreterState() == Interpreter.STOP) {
            executionCommand = null;
//        } else if (interpreter.getTimestep() < 20) {
//            executionCommand = interpreter.getMainFunction().getDrawableResource();
        } else {
            if (cmd instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) cmd).getDrawableResource();
                if (d != null) {
                    executionCommand = d;
                }
            }
        }

        Command error = interpreter.getErrorCommand();

        if (error != null) {
            GraphicObject errorCommand = ((GraphicResource) error).getDrawableResource();
            AffineTransform o = g.getTransform();
            AffineTransform n = ga.getT(o);
            ga.applyGlobalPosition(n);
            ga.applyZoom(n);
            g.setTransform(n);
            g.setColor(errorColor);
            g.setStroke(new BasicStroke(5));
            g.fill(errorCommand.getObjectShape());
            g.draw(errorCommand.getObjectShape());
            g.setTransform(o);
            ga.done(n);
        } else if (executionCommand != null) {
            AffineTransform o = g.getTransform();
            AffineTransform n = ga.getT(o);
            ga.applyGlobalPosition(n);
            ga.applyZoom(n);
            g.setTransform(n);
            g.setColor(executionColor);
            g.setStroke(new BasicStroke(5));
            g.fill(executionCommand.getObjectShape());
            g.draw(executionCommand.getObjectShape());
            g.setTransform(o);
            ga.done(n);
        }

        if (newCommand != null) {
            if (itemSelected != null) {
                g.translate(in.getMouse().x - 10, in.getMouse().y - 10);
                g.setColor(itemSelected.getColor());
                g.fill(itemSelected.getIcon());
            }

            if (in.mouseGeneralClick()) {
                if (in.getMouseButton() == MouseEvent.BUTTON1) {
                    clickDrop++;
                    if (clickDrop == 2) {
                        Point p = in.getTransformedMouse();
                        //tenta adicionar na posição do mouse
                        if (!addCommand(p)) {
                            //tenta adicionar 15 px para baixo
                            p.y += 15;
                            if (!addCommand(p)) {
                                //tenta adicionar 15 px para cima
                                p.y -= 30;
                                if (!addCommand(p)) {

                                }
                            }
                        }
                        newCommand = null;
                        itemSelected.setSelected(false);
                        itemSelected = null;
                        clickDrop = 0;
                    }
                } else {
                    newCommand = null;
                    itemSelected.setSelected(false);
                    itemSelected = null;
                    clickDrop = 0;
                }
            }
        }
    }

    public void addAllDrawableResources(Command c, DrawingPanel p) {
        GraphicObject d = ((GraphicResource) c).getDrawableResource();
        add(d); //com verificação null e contains 
        if (c instanceof Block) {
            Block b = (Block) c;
            Command it = b.getStart();
            while (it != null) {
                addAllDrawableResources(it, p);
                it = it.getNext();
            }
        } else if (c instanceof If) {
            addAllDrawableResources(((If) c).getBlockTrue(), p);
            addAllDrawableResources(((If) c).getBlockFalse(), p);
        }
    }

    @Override
    public int getDrawableLayer() {
        return Drawable.BACKGROUND_LAYER | Drawable.DEFAULT_LAYER | Drawable.TOP_LAYER;
    }

    private void printBounds(Graphics2D g, Command c) {
        Rectangle2D.Double bounds = c.getBounds(null, GraphicFlowchart.GF_J, GraphicFlowchart.GF_K);
        g.draw(bounds);
        g.drawString(c.getCommandName(), (int) bounds.getMaxX(), (int) bounds.y);
        if (c instanceof Block) {
            Block b = (Block) c;
            Command it = b.getStart();
            while (it != null) {
                printBounds(g, it);
                it = it.getNext();
            }
        } else if (c instanceof If) {
            printBounds(g, ((If) c).getBlockTrue());
            printBounds(g, ((If) c).getBlockFalse());
        }
    }

    private void drawSelectedCommand(Graphics2D g, Command c) {
        if (c instanceof GraphicResource && c.getParent() != null) {
            GraphicObject d = ((GraphicResource) c).getDrawableResource();
            if (d != null) {
                g.setColor(selectionColor);
                if (d instanceof WidgetContainer) {
                    WidgetContainer wc = (WidgetContainer) d;
                    if (!wc.isWidgetVisible()) {
                        g.fill(d.getObjectShape());
                    }
                    g.draw(d.getObjectShape());
                } else {
                    g.fill(d.getObjectShape());
                    g.draw(d.getObjectShape());
                }
            }
        }
        if (c instanceof Block) {
            Block b = (Block) c;
            Command it = b.getStart();
            while (it != null) {
                drawSelectedCommand(g, it);
                it = it.getNext();
            }
        } else if (c instanceof If) {
            drawSelectedCommand(g, ((If) c).getBlockTrue());
            drawSelectedCommand(g, ((If) c).getBlockFalse());
        }
    }

    private void drawSelection(Graphics2D g) {
        g.setStroke(BOLD_STROKE);
        for (Command c : selection) {
            drawSelectedCommand(g, c);
        }
    }

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {

        ident(function);

//        g.setStroke(DEFAULT_STROKE);
//        g.setColor(Color.MAGENTA);
//        printBounds(g, function);
        drawSelection(g);

        if (in.isKeyPressed(KeyEvent.VK_DELETE) && !selection.isEmpty()) {
            pushUndo();
            redo.clear();

            for (Command c : selection) {
                if (!(c instanceof FunctionEnd)) {
                    removeGraphicResources(c);
                }
            }

            selection.clear();

            ident(function);
        }

        if (in.isKeyPressed(KeyEvent.VK_CONTROL)) {
            if (!keyActionUsed) {
                if (in.isKeyPressed(KeyEvent.VK_Z)) {
                    undo();
                    keyActionUsed = true;
                }

                if (in.isKeyPressed(KeyEvent.VK_Y)) {
                    redo();
                    keyActionUsed = true;
                }

                if (in.isKeyPressed(KeyEvent.VK_X)) {
                    if (isValidSelection()) {
                        copy.clear();
                        pushUndo();
                        redo.clear();

                        for (Command c : selection) {
                            if (c instanceof Procedure) {
                                Procedure p = (Procedure) c;
                                copy.add(p.copy((Procedure) p.createInstance()));
                                removeGraphicResources(c);
                            } else if (!(c instanceof FunctionEnd)) {
                                System.out.println("Erro de copia " + c);
                            }
                        }

                        selection.clear();
                        ident(function);
                    }
                    keyActionUsed = true;
                }

                if (in.isKeyPressed(KeyEvent.VK_C)) {
                    if (isValidSelection()) {
                        copy.clear();
                        for (Command c : selection) {
                            if (c instanceof Procedure) {
                                Procedure p = (Procedure) c;
                                copy.add(p.copy((Procedure) p.createInstance()));
                            } else if (!(c instanceof FunctionEnd)) {
                                System.out.println("Erro de copia " + c);
                            }
                        }
                    }
                    keyActionUsed = true;
                }

                if (in.isKeyPressed(KeyEvent.VK_V)) {
                    if (!selection.isEmpty() && !copy.isEmpty()) {
                        pushUndo();
                        Command s = selection.get(0);
                        selection.clear();
                        for (Command c : copy) {
                            if (c instanceof Procedure) {
                                Procedure p = (Procedure) c;
                                c = p.copy((Procedure) p.createInstance());
                            }
                            s.addAfter(c);
                            addAllDrawableResources(c, this);
                            selection.add(c);
                            s = c;
                        }
                        ident(function);
                    }
                    keyActionUsed = true;
                }
            }
        }
        if (in.numberOfKeysPressed() <= 1) {
            keyActionUsed = false;
        }

        if (in.mouseGeneralClick() && in.getMouseButton() == MouseEvent.BUTTON1) {
            Point p = in.getTransformedMouse();
            Command c = function.find(p);
            hideAllWidgets(function, c);

            if (c != null) {
                GraphicObject go = c.getDrawableResource();
                boolean ignore = false;
                if (go != null) {
                    if (go instanceof WidgetContainer) {
                        ignore = ((WidgetContainer) go).isWidgetVisible();
                    }
                }
                if (ignore) {
                    selection.clear();
                } else if (in.isKeyPressed(KeyEvent.VK_CONTROL)) {
                    if (selection.contains(c)) {
                        selection.remove(c);
                    } else {
                        //selection.add(0, c);
                        addToSelection(c);
                    }
                } else if (!selection.contains(c) && in.isKeyPressed(KeyEvent.VK_SHIFT)) {
                    //organizar seleção
                    if (!selection.isEmpty()) {
                        Command start = selection.get(0);
                        boolean ok = false;
                        Command it = start;
                        while (it != null) {
                            if (!selection.contains(it)) {
                                addToSelection(it);
                            }
                            it = it.getNext();

                            if (it == c) {
                                ok = true;
                                break;
                            }
                        }
                        if (!ok) {
                            selection.clear();
                            it = start;
                            while (it != null) {
                                if (!selection.contains(it)) {
                                    addToSelection(it);
                                }
                                it = it.getPrevious();

                                if (it == c) {
                                    ok = true;
                                    break;
                                }
                            }
                            if (!ok) {
                                //procurar para cima e para baixo :( agora não
                                selection.clear();
                            }
                        }
                    }
                    addToSelection(c);
                } else {
                    if (selection.contains(c)) {
                        selection.clear();
                    } else {
                        selection.clear();
                        addToSelection(c);
                    }
                }
            } else if (in.getSingleKey() == 0) {
                selection.clear();
            }
        }
    }

    private void addToSelection(Command c) {
        //verifica se estão no mesmo bloco
        int clevel = c.getLevel();
        Command cparent = c.getParent();
        for (Command i : selection) {
            if (i.getParent() != cparent || c.getLevel() != clevel) {
                return;
            }
        }
        selection.add(c);
    }

    private void removeGraphicResources(Command c) {
        if (c == null) {
            return;
        }

        if (c instanceof Block) {
            Command it = ((Block) c).getStart();
            while (it != null) {
                removeGraphicResources(it);
                it = it.getNext();
            }
        } else if (c instanceof If) {
            removeGraphicResources(((If) c).getBlockTrue());
            removeGraphicResources(((If) c).getBlockFalse());
        }

        super.remove(c.getDrawableResource());

        if (c.getParent() instanceof Block && c == ((Block) c.getParent()).getStart()) {
            ((Block) c.getParent()).shiftStart();
        }

        c.remove();
    }

    private boolean isValidSelection() {
        if (selection.isEmpty()) {
            return false;
        }

        Command start, end;

        start = selection.get(0);

        while (selection.contains(start.getPrevious())) {
            start = start.getPrevious();
        }

        end = start;

        int i = 1;

        while (selection.contains(end.getNext())) {
            i++;
            end = end.getNext();
        }

        if (i == selection.size()) {
            selection.clear();
            Command it = start;
            while (it != end.getNext()) {
                selection.add(it);
                it = it.getNext();
            }
            return true;
        }

        return false;
    }

    public static void main(String[] args) {
        QuickFrame.applyLookAndFeel();
        FlowchartPanel p = new FlowchartPanel(Interpreter.newTestFunction(), new Interpreter());
        QuickFrame.create(p, "Teste FlowcharPanel").addComponentListener(p);
    }

    public void pushUndo() {
        undo.add(function.copy());
        if (redo.size() > 10) {
            undo.remove(10);
        }
    }

    private void pushRedo() {
        redo.add(function.copy());
        if (redo.size() > 10) {
            redo.remove(10);
        }
    }

    public void undo() {
        if (undo.size() > 0) {
            pushRedo();
            setFunction(undo.pop());
            selection.clear();
        }
    }

    public void redo() {
        if (redo.size() > 0) {
            pushUndo();
            setFunction(redo.pop());
            selection.clear();
        }
    }

    public void popVar(Variable varOld) {
        int i = v.indexOf(varOld);
        v.remove(i);
        c.remove(i);
    }

    public void pushVar(Variable var) {
        var.addObserver(this);
        v.add(var);
        c.add(generateRandomColor());
        q.add(new LinkedList<UpdateVar>());
        update(var, null);
    }

    //:(
    public static class TmpVar extends Variable {

        public TmpVar() {
            super("");
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Command cmd = interpreter.getCurrentCommand();
        GraphicObject tmpGO = null;
        if (interpreter.getInterpreterState() != Interpreter.STOP) {
            if (cmd instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) cmd).getDrawableResource();
                if (d != null && cmd != function) {
                    tmpGO = d;
                }
            }
        }

        if (o instanceof Variable && tmpGO != null) {
            Variable variable = (Variable) o;
            int i = v.indexOf(variable);
            Object value = variable.getValue();
            if (value == null) {
                value = "0";
            }
            UpdateVar updateVar = new UpdateVar(tmpGO.getPosX(), tmpGO.getPosY(), ((variable.getName().isEmpty()) ? "" : variable.getName() + " = ") + value, c.get(i));
            this.add2(updateVar);
            Queue<UpdateVar> queue = q.get(i);
            queue.add(updateVar);
            while (queue.size() > 5) {
                this.remove(queue.poll());
            }
        }

    }

    float golden_ratio_conjugate = 0.618033988749895f;
    float h = (float) Math.random();

    public Color generateRandomColor() {
        h += golden_ratio_conjugate;
        h %= 1f;
        return Color.getHSBColor(h, 0.35f, 0.95f);
    }

    private class UpdateVar implements Drawable {

        private double x, y, a = 1;
        private String value;
        private Color color;

        public UpdateVar(double x, double y, String value) {
            this.x = x;
            this.y = y;
            this.value = value;
            this.color = Color.white;
        }

        private UpdateVar(double x, double y, String value, Color color) {
            this(x, y, value);
            this.color = color;
        }

        @Override
        public int getDrawableLayer() {
            return GraphicObject.TOP_LAYER;
        }

        @Override
        public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {

        }

        @Override
        public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {

        }

        @Override
        public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {

            g.setColor(Color.BLACK);

            if (a < .5) {
                a *= 0.85;
            } else {
                a *= 0.95;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) a));

            AffineTransform o = g.getTransform();
            AffineTransform n = ga.getT(o);
            ga.applyGlobalPosition(n);
            ga.applyZoom(n);
            g.setTransform(n);
            g.translate(x, y);

            g.setFont(defaultFont);
            g.setColor(color);
            g.drawString(value, .8f, .8f);
            g.drawString(value, .8f, -.8f);
            g.drawString(value, -.8f, .8f);
            g.drawString(value, -.8f, -.8f);
            g.setColor(Color.BLACK);
            g.drawString(value, 0, 0);

            g.setTransform(o);
            ga.done(n);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            if (a < .8) {
                y -= 0.8;
            }
            if (a < .1) {
                FlowchartPanel.this.remove(this);
            }
        }

        @Override
        public void setLocation(double x, double y) {

        }

        @Override
        public double getPosX() {
            return 0;
        }

        @Override
        public double getPosY() {
            return 0;
        }

    }
}
