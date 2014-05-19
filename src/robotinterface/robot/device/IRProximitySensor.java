/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.device;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.nio.ByteBuffer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.Rotable;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.drawable.Selectable;
import robotinterface.robot.Robot;
import robotinterface.robot.simulation.Environment;
import robotinterface.robot.simulation.VirtualDevice;

/**
 *
 * @author antunes
 */
public class IRProximitySensor extends Device implements VirtualDevice, Drawable, Selectable, Classifiable, Rotable {

    public static final int MAX_DISTANCE = 500;
    private int dist = 0;
    private double x, y, theta;
    private boolean selected;

    @Override
    public void setState(ByteBuffer data) {
        dist = data.getChar();
//        System.out.println("Distancia: " + dist);
    }

    @Override
    public void getState(ByteBuffer buffer, Robot robot) {
        buffer.put((byte) 2);
        double c = Math.cos(robot.getTheta());
        double s = Math.sin(robot.getTheta());
        double nx = x * c - y * s;
        double ny = x * s + y * c;
        char d = (char) robot.getEnvironment().beamDistance(robot.getPosX() + nx, robot.getPosY() + ny, robot.getTheta() + theta);
        buffer.putChar(d);
    }

    @Override
    public void setState(ByteBuffer data, Robot robot) {
        setState(data);
        double c = Math.cos(robot.getTheta());
        double s = Math.sin(robot.getTheta());
        double nx = x * c - y * s;
        double ny = x * s + y * c;
        int d = (int) robot.getEnvironment().beamDistance(robot.getPosX() + nx, robot.getPosY() + ny, robot.getTheta() + theta);
        if (d < dist) {
            dist = d;
        }
    }

    @Override
    public void updateRobot(Robot robot) {
        double c = Math.cos(robot.getTheta());
        double s = Math.sin(robot.getTheta());
        double nx = x * c - y * s;
        double ny = x * s + y * c;
        robot.getPerception().addObstacle(robot.getPosX() + nx, robot.getPosY() + ny, robot.getTheta() + theta, dist * 2);
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
        AffineTransform t = ga.getT(g.getTransform());
        g.translate(x, y);
        g.rotate(theta);
        g.setColor(new Color(.1f, 1f, .1f, 0.5f));
        if (selected) {
            g.fillRect(0, -5, (int) 50, 10);
        } else {
            g.fillRect(0, -5, (int) (dist * 2), 10);
        }
        g.setTransform(t);
        ga.done(t);
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }

    @Override
    public String getName() {
        return "Distancia";
    }

    @Override
    public void resetState() {
        dist = 0;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean s) {
        selected = s;
    }

    @Override
    public Item getItem() {
        Polygon tmpShape = new Polygon();

        tmpShape.reset();
//        tmpShape.addPoint(7, 0);
//        tmpShape.addPoint(10, 0);
//        tmpShape.addPoint(10, 3);
//        tmpShape.addPoint(9, 2);
//        tmpShape.addPoint(1, 8);
//        tmpShape.addPoint(0, 7);
//        tmpShape.addPoint(0, 10);
//        tmpShape.addPoint(3, 10);
//        tmpShape.addPoint(2, 9);
//        tmpShape.addPoint(8, 1);
        
//        tmpShape.addPoint(6, 0);
//        tmpShape.addPoint(10, 0);
//        tmpShape.addPoint(10, 4);
//        tmpShape.addPoint(9, 3);
//        tmpShape.addPoint(1, 7);
//        tmpShape.addPoint(0, 6);
//        tmpShape.addPoint(0, 10);
//        tmpShape.addPoint(4, 10);
//        tmpShape.addPoint(3, 9);
//        tmpShape.addPoint(7, 1);
        
        tmpShape.addPoint(10, 0);
        tmpShape.addPoint(20, 0);
        tmpShape.addPoint(20, 10);
        tmpShape.addPoint(18, 6);
        tmpShape.addPoint(2, 14);
        tmpShape.addPoint(0, 10);
        tmpShape.addPoint(0, 20);
        tmpShape.addPoint(10, 20);
        tmpShape.addPoint(6, 18);
        tmpShape.addPoint(14, 2);

        Item item = new Item("Distancia", tmpShape, Color.decode("#FF9700"), "");
        item.setRef(this);
        return item;
    }

    @Override
    public void setTheta(double t) {
        theta = t;
    }

    @Override
    public double getTheta() {
        return theta;
    }
}
