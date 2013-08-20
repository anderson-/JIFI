/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.drawable;

import algorithm.Command;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JComponent;
import gui.drawable.DrawingPanel.GraphicAttributes;
import gui.drawable.DrawingPanel.InputState;
import gui.drawable.SwingContainer.DynamicJComponent;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author antunes
 */
public abstract class SwingContainer implements Drawable, Iterable<DynamicJComponent> {

    public class DynamicJComponent {

        private JComponent component;
        private Rectangle bounds;

        public DynamicJComponent(JComponent component, Rectangle bounds) {
            this.component = component;
            this.bounds = bounds;
        }

        public JComponent getComponent() {
            return component;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public int getX() {
            return bounds.x;
        }

        public int getY() {
            return bounds.y;
        }

        public void setLocation(int x, int y) {
            bounds.setLocation(x, y);
            updateBounds();
        }

        public void setSize(int width, int height) {
            bounds.setSize(width, height);
            updateBounds();
        }

        public void setBounds(int x, int y, int width, int height) {
            bounds.setBounds(x, y, width, height);
            updateBounds();
        }

        public void setBounds(Rectangle bounds) {
            this.bounds.setBounds(bounds);
            updateBounds();
        }

        public void updateBounds() {
            component.setBounds(bounds);
        }
    }
    private DrawingPanel parent;
    private ArrayList<DynamicJComponent> swingObjs;
    protected Rectangle2D.Double bounds;
    protected boolean showSwing = false;

    public boolean isShowingSwing() {
        return showSwing;
    }

    public void showSwing(boolean showSwing) {
        this.showSwing = showSwing;
    }
    
    public SwingContainer() {
        parent = null;
        swingObjs = new ArrayList<>();
        bounds = new Rectangle2D.Double();
    }

    private void updateSwing() {
        Rectangle b;
        for (DynamicJComponent c : swingObjs) {
            b = c.bounds;
            b.setBounds(new Rectangle(b.x, b.y, b.width, b.height));
        }
    }

    public final void addJComponent(JComponent comp, int x, int y, int width, int height) {
        if (comp != null) {
            swingObjs.add(new DynamicJComponent(comp, new Rectangle(x, y, width, height)));
            updateSwing();
        }
        if (parent != null) {
            for (DynamicJComponent dc : swingObjs) {
                //remove possiveis duplicados
                parent.remove(dc.component);
                dc.component.removeMouseMotionListener(parent);
                //adiciona componente swing
                parent.add(dc.component);
                //permite receber ações de movimento do mouse no DrawingPanel
                dc.component.addMouseMotionListener(parent);
            }
        }
    }

    public final void addJComponent(JComponent comp, Rectangle bounds) {
        addJComponent(comp, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public final void addJComponent(JComponent comp) {
        Rectangle b = comp.getBounds();
        addJComponent(comp, b.x, b.y, b.width, b.height);
    }

    public Drawable appendTo(DrawingPanel dp) {
        if (parent == null) {
            parent = dp;
            addJComponent(null, 0, 0, 0, 0);
        }
        return this;
    }

    @Override
    public Shape getObjectShape() {
        return bounds;
    }

    @Override
    public final Rectangle2D.Double getObjectBouds() {
        return bounds;
    }

    @Override
    public void setObjectLocation(double x, double y) {
        bounds.x = x;
        bounds.y = y;
    }

    @Override
    public void setObjectBounds(double x, double y, double width, double height) {
        bounds.x = x;
        bounds.y = y;
        bounds.width = width;
        bounds.height = height;
    }

    @Override
    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
    }

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
    }

    @Override
    public final Iterator<DynamicJComponent> iterator() {
        return swingObjs.iterator();
    }
}
