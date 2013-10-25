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
public class IRProximitySensor extends Device implements Drawable {

    int dist = 0;
    
    @Override
    public void setState(ByteBuffer data) {
        dist = data.getChar();
        System.out.println("Distancia: " + dist);
    }
    
    @Override
    public String stateToString() {
        return "" + dist;
    }

    @Override
    public int getClassID() {
        return 5;
    }
    
    public int getDist(){
        return dist;
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
        
        AffineTransform t = (AffineTransform) g.getTransform().clone();
        AffineTransform t2 = (AffineTransform) t;
//        t2.rotate(-3 * Math.PI / 12);
        g.setTransform(t2);
        
        g.setColor(new Color(.1f, 1f, .1f, 0.5f));
        g.fillRect(30, -5, (int)(Math.random()*100), 10);
        
        g.setTransform(t);
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        
    }
}
