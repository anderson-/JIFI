/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot.device;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import jifi.drawable.Drawable;
import jifi.drawable.DrawingPanel;
import jifi.drawable.GraphicObject;
import jifi.drawable.Rotable;
import jifi.gui.panels.RobotEditorPanel;
import jifi.gui.panels.sidepanel.Classifiable;
import jifi.gui.panels.sidepanel.Item;
import jifi.drawable.Selectable;
import jifi.robot.Robot;
import jifi.robot.simulation.VirtualDevice;

/**
 *
 * @author anderson
 */
public class Button extends Device implements VirtualDevice, Drawable, Selectable, Classifiable, Rotable {

    private final AffineTransform transform = new AffineTransform();
    private final int values[] = new int[5];
    private double x, y;
    private final Point2D.Double src = new Point2D.Double();
    private final Point2D.Double dst = new Point2D.Double();
    private boolean selected;

    @Override
    public boolean isActuator() {
        return false;
    }

    @Override
    public boolean isSensor() {
        return true;
    }

    @Override
    public byte[] defaultGetMessage() {
        return new byte[]{1, 0};
    }

    @Override
    public void setState(ByteBuffer data) {
        byte b = data.get();
        for (int i = 0; i < 5; i++) {
            values[4 - i] = (b >> i) & 1;
        }
    }

    @Override
    public void getState(ByteBuffer buffer, Robot robot) {
        byte value = 0;
        int sw = (int) (Robot.size / 15);
        int sx = (int) (Robot.size * .8 / 2);
        int sy = -sw / 2;
        transform.setToIdentity();
//        transform.translate(x, y);
        transform.rotate(robot.getTheta());
        transform.rotate(-3 * Math.PI / 12);
        src.setLocation(sx, sy);
        for (int si = 4; si >= 0; si--) {
            transform.rotate(Math.PI / 12);
            transform.deltaTransform(src, dst);
            values[si] = (robot.getEnvironment().isOver(dst.x + x + 2 + robot.getPosX(), dst.y + y + 2 + robot.getPosY())) ? 1 : 0;
            value |= (values[si] << si);
        }

        buffer.put((byte) 1);
        buffer.put(value);
    }

    @Override
    public void setState(ByteBuffer data, Robot robot) {
        setState(data);
//        int d = (int) robot.getEnvironment().beamDistance(robot.getPosX(), robot.getPosY(), robot.getTheta(), Robot.size / 2);
//        if (d < dist) {
//            dist = d;
//        }
    }

    @Override
    public void updateRobot(Robot robot) {
//        double d = dist * 2 + Robot.size / 2;
//        robot.getPerception().addObstacle(robot.getPosX(), robot.getPosY(), robot.getTheta(), d);
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

//        g.drawRect((int)dst.x, (int)dst.y, 3, 3);
        AffineTransform o = ga.getT(g.getTransform());
        o.translate(x, y);
        o.rotate(-3 * Math.PI / 12);
        g.setTransform(o);
        for (int si = 0; si < 5; si++) {
            if (selected) {
                g.setColor(RobotEditorPanel.SELECTED_COLOR);
                //selected = false;
            } else {
                g.setColor(Color.getHSBColor(.0f, 1, (float) (values[si])));
            }
            o.rotate(Math.PI / 12);
            g.setTransform(o);
            g.fillOval(sx, sy, sw, sw);
        }
        ga.done(o);
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }

    @Override
    public String getName() {
        return "Botão";
    }

    @Override
    public void resetState() {
        for (int i = 0; i < 5; i++) {
            values[i] = 0;
        }
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
        Area myShape = new Area();

        Polygon tmpShape = new Polygon();

        tmpShape.reset();
        tmpShape.addPoint(3, 0);
        tmpShape.addPoint(17, 0);
        tmpShape.addPoint(17, 14);
        tmpShape.addPoint(3, 14);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(0, 3);
        tmpShape.addPoint(0, 5);
        tmpShape.addPoint(20, 5);
        tmpShape.addPoint(20, 3);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(0, 10);
        tmpShape.addPoint(0, 12);
        tmpShape.addPoint(20, 12);
        tmpShape.addPoint(20, 10);
        myShape.add(new Area(tmpShape));

        myShape.subtract(new Area(new Ellipse2D.Double(5, 2, 10, 10)));

        myShape.add(new Area(new Ellipse2D.Double(7, 4, 6, 6)));

        Item item = new Item("Botão", myShape, Color.decode("#009C91"), "");
        item.setRef(this);
        return item;
    }

    @Override
    public void setTheta(double t) {

    }

    @Override
    public double getTheta() {
        return 0;
    }

    @Override
    public List<Object> getDescriptionData() {
        ArrayList<Object> data = new ArrayList<>();
        return data;
    }

    @Override
    public Device createDevice(List<Object> descriptionData) {
        return new Button();
    }

    @Override
    public byte[] getBuilderMessageData() {
        return new byte[]{};
    }
}
