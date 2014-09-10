/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.magenta.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import s3f.magenta.Drawable;
import s3f.magenta.DrawingPanel;
import s3f.magenta.GraphicObject;

/**
 *
 * @author anderson
 */
public class ShapeRender implements Render {

    private Color color;
    public static Stroke BORDER_STROKE = new BasicStroke(5);
    public static Stroke SHADOW_STROKE = new BasicStroke(5);

    public ShapeRender(Color color) {
        this.color = color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public int getDrawableLayer() {
        return Drawable.DEFAULT_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {

    }

    int l = 0;

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        if (ga.getCurrentObject() instanceof GraphicObject) {
            GraphicObject graphicObject = (GraphicObject) ga.getCurrentObject();
            Shape shape = graphicObject.getObjectShape();

            //shadow
            AffineTransform t = ga.getT();
            t.translate(3, 2);
            g.setColor(color.darker());
            g.setStroke(SHADOW_STROKE);
            g.draw(t.createTransformedShape(shape));
            ga.done(t);

            int w = shape.getBounds().width;
            if (w != l) {
                System.out.println(w/(l - w));
                l = w;
            }

            //white background
            g.setColor(Color.white);
            g.fill(shape);

            //border
            g.setColor(color);
            g.setStroke(BORDER_STROKE);
            g.draw(shape);

        }
//        if (ga.getCurrentShape() != null){
//            g.setColor(Color.magenta);
//            g.fill(ga.getCurrentShape());
//        }
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {

    }

    @Override
    public void setLocation(double x, double y) {

    }

    @Override
    public double getPosX() {
        return 0;
    }

    @Override
    public double getPosY() {
        return 0;
    }

}
