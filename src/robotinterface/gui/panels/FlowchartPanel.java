/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import robotinterface.gui.panels.sidepanel.SidePanel;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.JPanel;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.GraphicFlowchart;
import robotinterface.algorithm.procedure.Block;
import robotinterface.algorithm.procedure.DummyBlock;
import robotinterface.algorithm.procedure.Function;
import robotinterface.algorithm.procedure.Function.FunctionEnd;
import robotinterface.algorithm.procedure.If;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.WidgetContainer;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.Interpreter;
import robotinterface.plugin.PluginManager;

/**
 *
 * @author antunes
 */
public class FlowchartPanel extends DrawingPanel implements Interpertable {

    private final Color selectionColor = new Color(0, .2f, .5f, .5f);
    private final Color executionColor = new Color(0, 1, 0, .5f);
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

    public FlowchartPanel(Function function) {
        sidePanel = new SidePanel() {
            @Override
            protected void ItemSelected(Item item, Object ref) {
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
        sidePanel.addAllClasses(PluginManager.getPluginsAlpha("robotinterface/algorithm/plugin.txt", Procedure.class));
        sidePanel.addAllClasses(PluginManager.getPluginsAlpha("robotinterface/plugin/cmdpack/plugin.txt", Procedure.class));

        add(sidePanel);

        interpreter = new Interpreter();
        interpreter.start();
        setFunction(function);
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

            if (n instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) n).getDrawableResource();
                if (d != null) {
                    this.add(d);
                }
            }

            pushUndo();
            redo.clear();

            interpreter.setInterpreterState(Interpreter.STOP);

            if (addNext) {
                c.addAfter(newCommand);
            } else {
                c.addBefore(newCommand);
            }

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
                if (d != null && cmd != function) {
                    executionCommand = d;
                }
            }
        }

        if (executionCommand != null) {
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
        return Drawable.DEFAULT_LAYER | Drawable.TOP_LAYER;
    }

    private void printBounds(Graphics2D g, Command c) {
        Rectangle2D.Double bounds = c.getBounds(null, GraphicFlowchart.GF_J, GraphicFlowchart.GF_K);
        g.draw(bounds);
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
        FlowchartPanel p = new FlowchartPanel(Interpreter.bubbleSort(10, true));
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
}
