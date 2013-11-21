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
import robotinterface.robot.simulation.Environment;
import robotinterface.robot.simulation.VirtualDevice;

/**
 *
 * @author antunes
 */
public class IRProximitySensor extends Device implements VirtualDevice, Drawable {

    public static final int MAX_DISTANCE = 500;
    private int dist = 0;
    private double x, y;

    @Override
    public void setState(ByteBuffer data) {
        dist = data.getChar();
//        System.out.println("Distancia: " + dist);
    }

    @Override
    public void getState(ByteBuffer buffer, Robot robot) {
        buffer.put((byte) 2);
        char d = (char) robot.getEnvironment().beamDistance(robot.getPosX(), robot.getPosY(), robot.getTheta(), Robot.size / 2);
        buffer.putChar(d);
    }

    @Override
    public void setState(ByteBuffer data, Robot robot) {
        setState(data);
        int d = (int) robot.getEnvironment().beamDistance(robot.getPosX(), robot.getPosY(), robot.getTheta(), Robot.size / 2);
        if (d < dist) {
            dist = d;
        }
    }

    @Override
    public void updateRobot(Robot robot) {
        double d = dist * 2 + Robot.size / 2;
        robot.getPerception().addObstacle(robot.getPosX(), robot.getPosY(), robot.getTheta(), d);
    }

    @Override
    public String stateToString() {
        return "" + dist;
    }

    @Override
    public int getClassID() {
        return 5;
    }

    public int getDist() {
        return dist;
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
        AffineTransform t = ga.getT(0, g.getTransform());
        g.setTransform(t);
        g.setColor(new Color(.1f, 1f, .1f, 0.5f));
        g.fillRect(30, -5, (int) (dist * 2), 10);
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }

    @Override
    public String getName() {
        return "Distancia";
    }
}
