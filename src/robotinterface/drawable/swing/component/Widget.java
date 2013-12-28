/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.swing.component;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;

/**
 *
 * @author antunes2
 */
public class Widget extends Component {

    public final JComponent widget;
    private Rectangle bounds;//TODO: USAR SEMPRE DOUBLE!
    private Rectangle tmpRect;
    private boolean visible = false;
    private boolean isStatic;// TODO: o que é isso?
    private boolean dynamic = false;

    public Widget(JComponent widget, Rectangle bounds) {
        tmpRect = new Rectangle();
        this.widget = widget;
        this.bounds = bounds;
        isStatic = false;
    }

    @Deprecated
    public Widget(JComponent widget, int x, int y, int width, int height) {
        this(widget, new Rectangle(x, y, width, height));
    }

    public Widget(JComponent widget, int width, int height) {
        this(widget, new Rectangle(0, 0, width, height));
    }

    public boolean isVisible() {
        return visible;
    }

    @Deprecated //usado apenas por MotableWidgetContainer
    public void setVisible(boolean visible) {
        this.visible = visible;
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

    @Override
    public void setTempLocation(int x, int y) {
        super.setTempLocation(x, y);
//        System.out.println("x:" + x + " -> " + this.widget);
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

    @Override
    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, Graphics2D g) {
        tmp.setRect(bounds);
        return tmp;
    }
}
