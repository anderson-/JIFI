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
    private Rectangle bounds;

    public SwingContainer() {
        parent = null;
        swingObjs = new ArrayList<>();
        bounds = new Rectangle();
    }

    public final void setX(int x) {
        bounds.x = x;
        updateSwing();
    }

    public final void setY(int y) {
        bounds.y = y;
        updateSwing();
    }

    public final int getX() {
        return bounds.x;
    }

    public final int getY() {
        return bounds.y;
    }
    
    public final int getWidth() {
        return bounds.width;
    }

    public final int getHeight() {
        return bounds.height;
    }

    public final Rectangle getBounds() {
        return bounds;
    }

    public final void setLocation(int x, int y) {
        bounds.setLocation(x, y);
        updateSwing();
    }

    public final void setSize(int width, int height) {
        bounds.setSize(width, height);
        updateSwing();
    }

    public final void setBounds(int x, int y, int width, int height) {
        bounds.setBounds(x, y, width, height);
        updateSwing();
    }

    public final void setBounds(Rectangle bounds) {
        this.bounds.setBounds(bounds);
        updateSwing();
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

    public abstract Shape getShape();

    public abstract Command getCommand();

    public abstract boolean hasBackground();

    public abstract boolean isVisible();

    public abstract boolean hasTopLayer();

    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
    }

    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
    }

    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
    }

    @Override
    public final Iterator<DynamicJComponent> iterator() {
        return swingObjs.iterator();
    }
}
