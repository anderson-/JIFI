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
 * JIFI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * JIFI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jifi.algorithm.parser.parameterparser.Argument;
import jifi.drawable.Drawable;
import jifi.drawable.GraphicObject;
import jifi.drawable.DrawingPanel;
import jifi.drawable.Rotable;
import jifi.gui.panels.RobotEditorPanel;
import jifi.gui.panels.sidepanel.Classifiable;
import jifi.gui.panels.sidepanel.Item;
import jifi.drawable.Selectable;
import jifi.drawable.swing.MutableWidgetContainer;
import jifi.drawable.swing.WidgetContainer;
import jifi.drawable.swing.component.Component;
import jifi.drawable.swing.component.SubLineBreak;
import jifi.drawable.swing.component.TextLabel;
import jifi.drawable.swing.component.Widget;
import jifi.drawable.swing.component.WidgetLine;
import jifi.gui.panels.sidepanel.Configurable;
import jifi.robot.Robot;
import jifi.robot.simulation.VirtualDevice;

public class ReflectanceSensorArray extends Device implements VirtualDevice, Drawable, Selectable, Classifiable, Rotable, Configurable {

    private final AffineTransform transform = new AffineTransform();
    private final int values[] = new int[5];
    private double x, y, theta;
    private final Point2D.Double src = new Point2D.Double();
    private final Point2D.Double dst = new Point2D.Double();
    private boolean selected;
    private WidgetContainer resource;

    public ReflectanceSensorArray() {
        name = new Argument("Refletancia", Argument.STRING_LITERAL);
        threshold = new Argument(200, Argument.NUMBER_LITERAL);
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
    public boolean isActuator() {
        return false;
    }

    @Override
    public boolean isSensor() {
        return true;
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
    public byte getClassID() {
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
        return name.getStringValue().replace(" ", "");
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

        Item item = new Item(getName(), myShape, Color.decode("#C00086"), "");
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

    @Override
    public List<Object> getDescriptionData() {
        ArrayList<Object> data = new ArrayList<>();
        data.add(getName());
        data.add(threshold.getDoubleValue());
        data.add(x);
        data.add(y);
        data.add(getTheta());
        return data;
    }

    @Override
    public Device createDevice(List<Object> descriptionData) {
        ReflectanceSensorArray ir = new ReflectanceSensorArray();
        ir.name.set((String) descriptionData.get(0), Argument.STRING_LITERAL);
        ir.threshold.set((Double) descriptionData.get(1), Argument.NUMBER_LITERAL);
        ir.x = (double) descriptionData.get(2);
        ir.y = (double) descriptionData.get(3);
        ir.setTheta((double) descriptionData.get(4));
        return ir;
    }

    @Override
    public byte[] getBuilderMessageData() {
        int t = (int) threshold.getDoubleValue();
        return new byte[]{14, 4, 15, 16, (byte) (t & 0xFF), (byte) ((t >> 8) & 0xFF)};
    }

    @Override
    public WidgetContainer getConfigurationPanel() {
        if (resource == null) {
            resource = createConfigurationPanel(this);
        }
        return resource;
    }

    private Argument name;
    private Argument threshold;

    public static MutableWidgetContainer createConfigurationPanel(final ReflectanceSensorArray r) {

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Configuração", true));
                components.add(new SubLineBreak());
                components.add(new TextLabel("Nome:  "));
                Widget wtextfield = new Widget(new JTextField("led"), 100, 25);
                components.add(wtextfield);
                container.softEntangle(r.name, wtextfield);

                components.add(new SubLineBreak());
                components.add(new TextLabel("Limite:  "));
                Widget wspinner = new Widget(new JSpinner(new SpinnerNumberModel(200, 0, 1023, 50)), 100, 25);
                components.add(wspinner);
                container.softEntangle(r.threshold, wspinner);

                components.add(new SubLineBreak(true));
            }
        };

        MutableWidgetContainer c = new MutableWidgetContainer(Color.BLACK) {
            @Override
            public void updateStructure() {
                clear();
                addLine(headerLine);
                boxLabel = getBoxLabel();
            }
        };

        c.updateStructure();

        return c;
    }
}
