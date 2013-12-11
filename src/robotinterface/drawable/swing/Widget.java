/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.swing;

import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 *
 * @author antunes2
 */
public class Widget {

    JComponent widget;
    private Rectangle bounds;
    private Rectangle tmpRect;
    private boolean isStatic;
    private boolean dynamic = false;
    private int x, y;

    public Widget(JComponent widget, Rectangle bounds) {
        tmpRect = new Rectangle();
        this.widget = widget;
        this.bounds = bounds;
        isStatic = false;
    }

    public Widget(JComponent widget, int x, int y, int width, int height) {
        this(widget, new Rectangle(x, y, width, height));
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public JComponent getJComponent() {
        return widget;
    }

    public Rectangle getBounds() {
        tmpRect.setBounds(bounds);
        tmpRect.x += x;
        tmpRect.y += y;
        return tmpRect;
    }

    public int getX() {
        return bounds.x + x;
    }

    public int getY() {
        return bounds.y + y;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public void setLocation(int x, int y) {
        bounds.setLocation(x, y);
        updateBounds();
    }

    public void setTempLocation(int x, int y) {
        this.x = x;
        this.y = y;
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
        widget.setBounds(getBounds());
    }
}
