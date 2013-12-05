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
 * RobotInterface is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * RobotInterface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RobotInterface. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package robotinterface.robot.device;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.DrawingPanel;
import robotinterface.robot.Robot;
import robotinterface.robot.simulation.VirtualDevice;

public class ReflectanceSensorArray extends Device implements VirtualDevice, Drawable {

    private final AffineTransform transform = new AffineTransform();
    private final int values[] = new int[5];
    private double x, y;
    private final Point2D.Double src = new Point2D.Double();
    private final Point2D.Double dst = new Point2D.Double();

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
        transform.rotate(robot.getTheta());
        transform.rotate(-3 * Math.PI / 12);
        src.setLocation(sx, sy);
        for (int si = 0; si < 5; si++) {
            transform.rotate(Math.PI / 12);
            transform.deltaTransform(src, dst);
            values[si] = (robot.getEnvironment().isOver(dst.x + robot.getPosX(), dst.y + robot.getPosY())) ? 1 : 0;
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
        o.setTransform(o);
        o.rotate(-3 * Math.PI / 12);
        g.setTransform(o);
        for (int si = 0; si < 5; si++) {
            g.setColor(Color.getHSBColor(.0f, 1, (float) (values[si])));
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
}
