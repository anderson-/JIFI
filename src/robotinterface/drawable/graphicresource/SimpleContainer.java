/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.graphicresource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import robotinterface.drawable.DWidgetContainer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;

/**
 *
 * @author antunes
 */
public class SimpleContainer extends DWidgetContainer {

    private Shape shape;
    private Color color;
    private AffineTransform transform;

    public SimpleContainer(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
        transform = new AffineTransform();
        Rectangle2D r = shape.getBounds2D();
        super.setObjectBounds(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public Shape getObjectShape() {
        transform.setToIdentity();
        transform.translate(super.bounds.x, super.bounds.y);
        return transform.createTransformedShape(shape);
    }

    @Override
    public int getDrawableLayer() {
        return Drawable.DEFAULT_LAYER;
    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
//        if(in.mouseClicked()){
//            super.widgetVisible = !super.widgetVisible;
//        }
        
        g.setColor(color);
        g.fill(shape);
    }

    boolean w = false;
    
    public static Shape createPoli(Rectangle2D r) {
        Polygon p = new Polygon();

        p.addPoint((int) r.getCenterX(), 0);
        p.addPoint(0, (int) r.getCenterY());
        p.addPoint((int) r.getCenterX(), (int) r.getHeight());
        p.addPoint((int) r.getWidth(), (int) r.getCenterY());

        return p;
    }
}
