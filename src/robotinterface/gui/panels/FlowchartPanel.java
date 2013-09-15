/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import robotinterface.algorithm.procedure.Function;
import robotinterface.drawable.DWidgetContainer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.interpreter.Interpreter;

/**
 *
 * @author antunes
 */
public class FlowchartPanel extends DrawingPanel {

    private class SelectionPanel extends DWidgetContainer {

        public static final int panelWidth = 200;
        private boolean open = true;
        private boolean animOpen = false;
        private boolean animClose = false;
        private RoundRectangle2D.Double closeBtn;

        public SelectionPanel() {
            this.closeBtn = new RoundRectangle2D.Double(-15, 15, 20, 20, 10, 10);
            bounds.x = 0;
        }

        @Override
        public int getDrawableLayer() {
            return Drawable.TOP_LAYER;
        }

        @Override
        public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
            g.setStroke(new BasicStroke());
            g.setColor(Color.cyan.darker().darker());
            if (in.mouseGeneralClick() && closeBtn.contains(in.getRelativeMouse())) {
                if (open && !(animOpen || animClose)) {
                    animClose = true;
                } else {
                    animOpen = true;
                }
            }
            g.draw(closeBtn);
//            g.drawRoundRect(-15, 15, 30, 20, 10, 10);
//            System.out.println(in.getRelativeMouse());
            g.fillRect(0, 0, panelWidth, ga.getHeight());
            //super.setObjectLocation(0, x);
            if (in.isKeyPressed(KeyEvent.VK_1)) {
                animOpen = !animOpen;
            } else if (in.isKeyPressed(KeyEvent.VK_2)) {
                animClose = !animClose;
            }

            if (animOpen) {
                if (bounds.x > ga.getWidth() - panelWidth) {
                    bounds.x -= 2;
                } else {
                    bounds.x = ga.getWidth() - panelWidth;
                    animOpen = false;
                    open = true;
                }
            } else if (animClose) {
                open = false;
                if (bounds.x < ga.getWidth()) {
                    bounds.x += 2;
                } else {
                    bounds.x = ga.getWidth();
                    animClose = false;
                }
            } else if (open) {
                bounds.x = ga.getWidth() - panelWidth;
                bounds.y = 0;
                bounds.width = panelWidth;
                bounds.height = ga.getHeight();
            }
        }
    }
    private Function function;
    private int fx = 200;
    private int fy = 60;
    private int fj = 25;
    private int fk = 30;
    private int fIx = 0;
    private int fIy = 1;
    private boolean fsi = true;

    public FlowchartPanel() {
        add(new SelectionPanel());
        function = Interpreter.newTestFunction();
        add(function);
        function.ident(fx, fy, fj, fk, fIx, fIy, fsi);
        function.wire(fj, fk, fIx, fIy, fsi);
        function.appendDCommandsOn(this);
    }

    public static void main(String[] args) {
        FlowchartPanel p = new FlowchartPanel();
        QuickFrame.create(p, "Teste FlowcharPanel").addComponentListener(p);
    }
}