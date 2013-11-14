/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.device;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.nio.ByteBuffer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.DrawingPanel;
import robotinterface.robot.Robot;

/**
 *
 * @author antunes
 */
public class ReflectanceSensorArray extends Device implements Drawable {

    private int values[] = new int[5];
    private double x, y;

    @Override
    public void setState(ByteBuffer data) {
        System.out.println("R:" + data.remaining());
        byte b = data.get();
        for (int i = 0; i < 5; i++) {
            values[4 - i] = (b >> i) & 1;
        }
        
        System.out.println(b);
    }

    @Override
    public String stateToString() {
        return "[" + values[0] + "," + values[1] + "," + values[2] + "," + values[3] + "," + values[4] + "]";
    }

    @Override
    public int getClassID() {
        return 4;
    }
    
    @Override
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getPosX() {
        return x;
    }

    @Override
    public double getPosY() {
        return y;
    }

    @Override
    public int getDrawableLayer() {
        return GraphicObject.DEFAULT_LAYER;
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
            g.setColor(Color.getHSBColor(.0f, 1, 1 - (float) (values[si])));
            t2.rotate(Math.PI / 12);
            g.setTransform(t2);
            g.fillOval(sx, sy, sw, sw);
        }
        g.setTransform(t);
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }

    @Override
    public String getName() {
        return "Refletancia";
    }
}
