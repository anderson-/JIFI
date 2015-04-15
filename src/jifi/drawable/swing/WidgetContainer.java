/**
 * @file .java
 * @author Anderson Antunes <anderson.utf@gmail.com>
 *         *seu nome* <*seu email*>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Anderson Antunes <anderson.utf@gmail.com>
 *                       *seu nome* <*seu email*>
 *
 * JIFI is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * JIFI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JIFI. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jifi.drawable.swing;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JComponent;
import jifi.drawable.DrawingPanel.GraphicAttributes;
import jifi.drawable.DrawingPanel.InputState;
import jifi.drawable.swing.component.Widget;
import java.awt.geom.Rectangle2D;
import jifi.drawable.DrawingPanel;
import jifi.drawable.GraphicObject;

/**
 * Container desenhável com suporte a componentes Swing.
 */
public abstract class WidgetContainer implements GraphicObject, Iterable<Widget> {

    private DrawingPanel parent;
    private ArrayList<Widget> widgets;
    private AffineTransform transform;
    private boolean widgetVisible = false;
    protected Shape shape;
    protected Rectangle2D.Double bounds;

    public boolean isWidgetVisible() {
        return widgetVisible;
    }

    public void setWidgetVisible(boolean showSwing) {
        this.widgetVisible = showSwing;
    }

    public WidgetContainer() {
        parent = null;
        widgets = new ArrayList<>();
        transform = new AffineTransform();
        shape = null;
        bounds = new Rectangle2D.Double();
    }

    public WidgetContainer(Shape shape) {
        parent = null;
        widgets = new ArrayList<>();
        transform = new AffineTransform();
        this.shape = shape;
        bounds = new Rectangle2D.Double();
        bounds.setRect(shape.getBounds2D());
    }

    private void updateWidgets() {
        for (Widget c : widgets) {
            c.updateBounds();
        }
    }

    public void cleanRemoved() {
        if (parent != null) {
            for (Widget dc : widgets) {
                //remove possiveis duplicados
                parent.remove(dc.widget);
                dc.widget.removeMouseMotionListener(parent);
                //adiciona componente swing
                parent.add(dc.widget);
                //permite receber ações de movimento do mouse no DrawingPanel
                dc.widget.addMouseMotionListener(parent);
            }
        }
    }

    public boolean contains(Widget w) {
        return widgets.contains(w);
    }

    public void addWidget(Widget w) {
        if (w != null) {
            w.setVisible(true);
            widgets.add(w);
            updateWidgets();
            cleanRemoved();
        }
    }

    public final Widget addWidget(JComponent comp, int x, int y, int width, int height) {
        Widget w = null;
        if (comp != null) {
            w = new Widget(comp, new Rectangle(x, y, width, height));
            widgets.add(w);
            updateWidgets();
        }
        if (parent != null) {
            for (Widget dc : widgets) {
                //remove possiveis duplicados
                parent.remove(dc.widget);
                dc.widget.removeMouseMotionListener(parent);
                //adiciona componente swing
                parent.add(dc.widget);
                //permite receber ações de movimento do mouse no DrawingPanel
                dc.widget.addMouseMotionListener(parent);
            }
        }
        return w;
    }

    public final Widget addWidget(JComponent comp, Rectangle bounds) {
        Widget w = null;
        if (comp != null) {
            w = new Widget(comp, bounds);
            addWidget(w);
        }
        return w;
    }

    public final Widget addWidget(JComponent comp) {
        Widget w = null;
        if (comp != null) {
            w = new Widget(comp, comp.getBounds());
            addWidget(w);
        }
        return w;
    }

    public final void removeWidget(Widget w) {
        w.setVisible(false);
        widgets.remove(w);
        updateWidgets();
        if (parent != null) {
            parent.remove(w.widget);
            w.widget.removeMouseMotionListener(parent);
        }
    }

    public GraphicObject appendTo(DrawingPanel dp) {
        if (parent != dp) {
            parent = dp;
        }
        addWidget(null, 0, 0, 0, 0);
        return this;
    }

    @Override
    public final Shape getObjectShape() {
        if (shape != null) {
            transform.setToIdentity();
            transform.translate(bounds.x, bounds.y);
            return transform.createTransformedShape(shape);
        } else {
            return bounds;
        }
    }

    @Override
    public final Rectangle2D.Double getObjectBouds() {
        if (shape != null) {
            bounds.width = shape.getBounds2D().getWidth();
            bounds.height = shape.getBounds2D().getHeight();
        }
        return bounds;
    }

    @Override
    public void setLocation(double x, double y) {
        bounds.x = x;
        bounds.y = y;
    }

    @Override
    public double getPosX() {
        return bounds.x;
    }

    @Override
    public double getPosY() {
        return bounds.y;
    }

    @Override
    public void setObjectBounds(double x, double y, double width, double height) {
        throw new UnsupportedOperationException("Not supported yet.");
//        bounds.x = x;
//        bounds.y = y;
//        bounds.width = width;
//        bounds.height = height;
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
    public final Iterator<Widget> iterator() {
        return widgets.iterator();
    }

    public void setJComponentStatic(int i, boolean isStatic) {
        widgets.get(i).setStatic(isStatic);
    }
}
