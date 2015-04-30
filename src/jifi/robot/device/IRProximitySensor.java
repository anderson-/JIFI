/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot.device;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
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

/**
 *
 * @author antunes
 */
public class IRProximitySensor extends Device implements VirtualDevice, Drawable, Selectable, Classifiable, Rotable, Configurable {

    public static final int MAX_DISTANCE = 500;
    private int dist = 0;
    private double x, y, theta;
    private boolean selected;
    private WidgetContainer resource;

    public IRProximitySensor() {
        name = new Argument("Distancia", Argument.STRING_LITERAL);
        pin = new Argument(17, Argument.NUMBER_LITERAL);
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
    public byte getClassID() {
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
        return name.getStringValue().replace(" ", "");
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

        Item item = new Item(getName(), tmpShape, Color.decode("#FF9700"), "");
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
        data.add(pin.getDoubleValue());
        data.add(x);
        data.add(y);
        data.add(getTheta());
        return data;
    }

    @Override
    public Device createDevice(List<Object> descriptionData) {
        IRProximitySensor ir  = new IRProximitySensor();
        ir.name.set((String) descriptionData.get(0), Argument.STRING_LITERAL);
        ir.pin.set((Double) descriptionData.get(1), Argument.NUMBER_LITERAL);
        ir.x = (double) descriptionData.get(2);
        ir.y = (double) descriptionData.get(3);
        ir.setTheta((double) descriptionData.get(4));
        return ir;
    }

    @Override
    public byte[] getBuilderMessageData() {
        return new byte[]{(byte) pin.getDoubleValue()};
    }

    @Override
    public WidgetContainer getConfigurationPanel() {
        if (resource == null) {
            resource = createConfigurationPanel(this);
        }
        return resource;
    }

    private Argument name;
    private Argument pin;

    public static MutableWidgetContainer createConfigurationPanel(final IRProximitySensor ir) {

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Configuração", true));
                components.add(new SubLineBreak());
                components.add(new TextLabel("Nome:  "));
                Widget wtextfield = new Widget(new JTextField("Refletancia"), 100, 25);
                components.add(wtextfield);
                container.softEntangle(ir.name, wtextfield);

                components.add(new SubLineBreak());
                components.add(new TextLabel("Pino:  "));
                Widget wspinner = new Widget(new JSpinner(new SpinnerNumberModel(2, 0, 20, 1)), 100, 25);
                components.add(wspinner);
                container.softEntangle(ir.pin, wspinner);

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
