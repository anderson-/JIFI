/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.device;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.robot.Robot;

/**
 *
 * @author antunes
 */
public class ReflectanceSensorArray extends Device implements Drawable {

    private int values[] = new int[5];

    @Override
    public void setState(ByteBuffer data) {
        for (int i = 0; i < 5; i++) {
            values[i] = data.getChar();
        }
    }

    @Override
    public String stateToString() {
        return "[" + values[0] + "," + values[1] + "]";
    }

    @Override
    public int getClassID() {
        return 5;//??
    }

    /*
     * Desenho:
     */
    private Rectangle2D.Double shape = new Rectangle.Double(0, 0, Robot.size, Robot.size);

    @Override
    public Shape getObjectShape() {
        return shape;
    }

    @Override
    public Rectangle2D.Double getObjectBouds() {
        return shape;
    }

    @Override
    public void setObjectBounds(double x, double y, double width, double height) {
        shape.setRect(x, y, width, height);
    }

    @Override
    public void setObjectLocation(double x, double y) {
        shape.x = x;
        shape.y = y;
    }

    @Override
    public int getDrawableLayer() {
        return Drawable.DEFAULT_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        
    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        int sw = (int) (Robot.size / 15);
        int sx = (int) (Robot.size * .8 / 2);
        int sy = -sw / 2;
        AffineTransform t = (AffineTransform) g.getTransform().clone();
        AffineTransform t2 = (AffineTransform) t;
        t2.rotate(-3 * Math.PI / 12);
        g.setTransform(t2);
        for (int si = 0; si < 5; si++) {
            g.setColor(Color.getHSBColor(.0f, 1, 1-(float)(values[si]/1024f)));
            t2.rotate(Math.PI / 12);
            g.setTransform(t2);
            g.fillOval(sx, sy, sw, sw);
        }
        g.setTransform(t);
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        
    }
}
