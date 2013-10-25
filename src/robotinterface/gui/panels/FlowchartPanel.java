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
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.Parser;
import robotinterface.algorithm.procedure.Block;
import robotinterface.algorithm.procedure.Function;
import robotinterface.algorithm.procedure.If;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.Interpreter;
import robotinterface.plugin.PluginManager;
import robotinterface.robot.Robot;

/**
 *
 * @author antunes
 */
public class FlowchartPanel extends DrawingPanel implements TabController, Interpertable {

    public ArrayList<JPanel> tabs = new ArrayList<>();
    private Interpreter interpreter;
    private Function function;
    private int fx = 200;
    private int fy = 60;
    private int fj = 30;
    private int fk = 60;
    private int fIx = 0;
    private int fIy = 1;
    private boolean fsi = false;
    public Stack<Function> undo = new Stack<>();
    public Stack<Function> redo = new Stack<>();
    Command tmp = null;
    Item itmp = null;
    int tmpi = 0;

    public FlowchartPanel() {
        SidePanel sp = new SidePanel() {
            @Override
            protected void ItemSelected(Item item, Object ref) {
                try {
                    itmp = item;
                    tmp = SidePanel.newInstance(ref);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        sp.setColor(Color.decode("#54A4A4"));
        sp.addAllClasses(PluginManager.getPluginsAlpha("robotinterface/algorithm/plugin.txt"));
        sp.addAllClasses(PluginManager.getPluginsAlpha("robotinterface/plugin/cmdpack/plugin.txt"));
        add(sp);
//        try {
//            function = Parser.decode("func myFunc() {var x = 4; if (x > 2) print(\"ok\") else print(\"rodrigo\")}");
//        } catch (Exception ex) {
//            function = Interpreter.bubbleSort(10, true);
//        }

        function = Interpreter.bubbleSort(10, true);

//        Procedure p = (Procedure) function.getStart();
//        Procedure np = Procedure.copyAll(p);
//        function = new Function("ss", null);
//        function.addAll(np);

        function = (Function) function.copy((Procedure) new Function());

        //function = Interpreter.newTestFunction();
        add(function);
        interpreter = new Interpreter(new Robot());
        interpreter.setInterpreterState(Interpreter.STOP);
        interpreter.setMainFunction(function);
        interpreter.start();
//        function.ident(fx, fy, fj, fk, fIx, fIy, fsi);
//        function.wire(fj, fk, fIx, fIy, fsi);
        function.appendDCommandsOn(this);

//        JTextArea console = new JTextArea();

//        mc.redirectOut(null, System.out);

        setName(function.toString());
    }

    @Override
    public Interpreter getInterpreter() {
        return interpreter;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        removeGraphicResources(this.function);
        this.function = function;
        add(function);
        function.appendDCommandsOn(this);
        function.ident(fx, fy, fj, fk, fIx, fIy, fsi);
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {

        Command cmd = interpreter.getCurrentCommand();
//        System.out.println(cmd);
        if (cmd instanceof GraphicResource) {
            Drawable d = ((GraphicResource) cmd).getDrawableResource();
            if (d != null) {
                AffineTransform o = g.getTransform();
                AffineTransform n = (AffineTransform) o.clone();
                ga.applyGlobalPosition(n);
                ga.applyZoom(n);
                g.setTransform(n);
                g.setColor(Color.GREEN);

//                g.fill(d.getObjectShape());

                g.setStroke(new BasicStroke(5));
                g.draw(d.getObjectShape());

                g.setTransform(o);
            }
        }

//        if (in.mouseGeneralClick() && in.isKeyPressed(KeyEvent.VK_R)) {
//            Point p = in.getTransformedMouse();
//            Command c = function.find(p);
//            System.out.println("CLICK");
//            if (c != null) {
//                System.out.println("REMOVE");
//                c.remove();
//                return;
//            }
//        }



        if (tmp != null) {
            if (itmp != null) {
                g.translate(in.getMouse().x - 10, in.getMouse().y - 10);
                g.setColor(itmp.getColor());
                g.fill(itmp.getIcon());
            }

            if (in.mouseGeneralClick()) {
                tmpi++;
                Point p = in.getTransformedMouse();
                Command c = function.find(p);

                boolean addNext = true;
                if (tmpi == 2) {
                    if (c != null) {
                        if (c instanceof GraphicResource) {
                            Drawable d = ((GraphicResource) c).getDrawableResource();
                            if (d != null) {
                                g.draw(d.getObjectShape());

                                //alterar usando fIx e fIy
                                if (p.y > d.getObjectBouds().getCenterY()) {
                                    addNext = true;
                                } else {
                                    addNext = false;
                                }

                            }
                        }
                        Command n = tmp;
                        if (n instanceof GraphicResource) {
                            Drawable d = ((GraphicResource) n).getDrawableResource();
                            if (d != null) {
                                this.add(d);
                            }
                        }

                        pushUndo();
                        redo.clear();

                        if (addNext) {
                            c.addAfter(tmp);
                        } else {
                            c.addBefore(tmp);
                        }


                        function.ident(fx, fy, fj, fk, fIx, fIy, fsi);
//                    function.wire(fj, fk, fIx, fIy, fsi);
//                System.out.println(c);
                    }
                    tmp = null;
                    itmp = null;
                    tmpi = 0;
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
        if (in.isKeyPressed(KeyEvent.VK_CONTROL)) {
            Point p = in.getTransformedMouse();
            Command c = function.find(p);

            if (c != null) {
                if (c instanceof GraphicResource) {
                    Drawable d = ((GraphicResource) c).getDrawableResource();
                    if (d != null) {
                        g.setColor(Color.red);
                        g.draw(d.getObjectShape());
                    }
                }

//                c.print();

                if (in.mouseGeneralClick()) {
                    pushUndo();
                    redo.clear();

                    removeGraphicResources(c);

                    function.ident(fx, fy, fj, fk, fIx, fIy, fsi);
                }
            }
        }
    }

    private void removeGraphicResources(Command c) {
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

    public static void main(String[] args) {
        FlowchartPanel p = new FlowchartPanel();
        QuickFrame.create(p, "Teste FlowcharPanel").addComponentListener(p);
    }

    @Override
    public List<JPanel> getTabs() {
        return tabs;
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
        }
    }

    public void redo() {

        if (redo.size() > 0) {
            pushUndo();
            setFunction(redo.pop());
        }
    }
}