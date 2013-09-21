/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels;

import robotinterface.gui.panels.sidepanel.SidePanel;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.procedure.Function;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.Interpreter;
import robotinterface.plugin.PluginManager;
import robotinterface.plugin.cmdpack.begginer.Move;
import robotinterface.plugin.cmdpack.begginer.ReadDevice;

/**
 *
 * @author antunes
 */
public class FlowchartPanel extends DrawingPanel {

    private Function function;
    private int fx = 200;
    private int fy = 60;
    private int fj = 25;
    private int fk = 60;
    private int fIx = 0;
    private int fIy = 1;
    private boolean fsi = false;
    Command tmp = null;
    Item itmp = null;

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
//        ArrayList<Class> classList = new ArrayList<>();
//        classList.add(ReadDevice.class);
//        sp.addAll(classList);
        sp.addAll(PluginManager.getPluginsAlpha("robotinterface/plugin/cmdpack/plugin.txt"));
        add(sp);
        function = Interpreter.newTestFunction();
        add(function);
        function.ident(fx, fy, fj, fk, fIx, fIy, fsi);
        function.wire(fj, fk, fIx, fIy, fsi);
        function.appendDCommandsOn(this);
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {

        if (tmp != null) {

            if (itmp != null) {
                g.translate(in.getMouse().x - 10, in.getMouse().y - 10);
                g.setColor(itmp.getColor());
                g.fill(itmp.getIcon());
            }

            if (in.mouseGeneralClick()) {
                Point p = in.getTransformedMouse();
                Command c = function.find(p);
                boolean addNext = true;
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
                    Command n = tmp;//new Move(1, 1);
                    if (n instanceof GraphicResource) {
                        Drawable d = ((GraphicResource) n).getDrawableResource();
                        if (d != null) {
                            this.add(d);
                        }
                    }

                    if (addNext) {
                        c.addAfter(tmp);
                    } else {
                        c.addBefore(tmp);
                    }

                    tmp = null;
                    itmp = null;

                    function.ident(fx, fy, fj, fk, fIx, fIy, fsi);
                    function.wire(fj, fk, fIx, fIy, fsi);
//                System.out.println(c);
                } else {
                    tmp = null;
                    itmp = null;
                }
            }
        }
    }

    public static void main(String[] args) {
        FlowchartPanel p = new FlowchartPanel();
        QuickFrame.create(p, "Teste FlowcharPanel").addComponentListener(p);
    }
}