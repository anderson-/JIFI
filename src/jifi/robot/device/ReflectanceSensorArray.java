/**
 * @file .java
 * @author Fernando Padilha Ferreira <fpf.padilhaf@gmail.com>
 * Anderson de Oliveira Antunes <anderson.utf@gmail.com>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Fernando Padilha Ferreira <fpf.padilhaf@gmail.com>
 * Anderson de Oliveira Antunes <anderson.utf@gmail.com>
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
package jifi.robot.device;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import jifi.drawable.Drawable;
import jifi.drawable.GraphicObject;
import jifi.drawable.DrawingPanel;
import jifi.drawable.Rotable;
import jifi.gui.panels.RobotEditorPanel;
import jifi.gui.panels.sidepanel.Classifiable;
import jifi.gui.panels.sidepanel.Item;
import jifi.drawable.Selectable;
import jifi.robot.Robot;
import jifi.robot.simulation.VirtualDevice;

public class ReflectanceSensorArray extends Device implements VirtualDevice, Drawable, Selectable, Classifiable, Rotable {

    private final AffineTransform transform = new AffineTransform();
    private final int values[] = new int[5];
    private double x, y, theta;
    private final Point2D.Double src = new Point2D.Double();
    private final Point2D.Double dst = new Point2D.Double();
    private boolean selected;

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
        transform.rotate(robot.getTheta() + theta);
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
        o.rotate(-3 * Math.PI / 12 + theta);
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
        return "Refletancia";
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
        tmpShape.addPoint(0, 4);
        tmpShape.addPoint(0, 0);
        tmpShape.addPoint(10, 15);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(10, 2);
        tmpShape.addPoint(0, 20);
        tmpShape.addPoint(20, 20);
        myShape.exclusiveOr(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(20, 0);
        tmpShape.addPoint(20, 5);
        tmpShape.addPoint(10, 15);
        myShape.exclusiveOr(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(20, 8);
        tmpShape.addPoint(20, 13);
        tmpShape.addPoint(10, 15);
        myShape.exclusiveOr(new Area(tmpShape));

        Item item = new Item("Sensor Refletivo", myShape, Color.decode("#C00086"), "");
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
