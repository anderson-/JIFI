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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jifi.algorithm.parser.parameterparser.Argument;
import jifi.drawable.Drawable;
import jifi.drawable.DrawingPanel;
import jifi.drawable.GraphicObject;
import jifi.drawable.swing.DrawableProcedureBlock;
import jifi.drawable.swing.MutableWidgetContainer;
import jifi.drawable.swing.WidgetContainer;
import jifi.drawable.swing.component.Component;
import jifi.drawable.swing.component.SubLineBreak;
import jifi.drawable.swing.component.TextLabel;
import jifi.drawable.swing.component.Widget;
import jifi.drawable.swing.component.WidgetLine;
import jifi.gui.panels.sidepanel.Configurable;
import jifi.gui.panels.RobotEditorPanel;
import jifi.gui.panels.sidepanel.Classifiable;
import jifi.gui.panels.sidepanel.Item;
import jifi.drawable.Selectable;
import jifi.plugin.cmdpack.begginer.Wait;
import jifi.robot.Robot;
import jifi.robot.simulation.VirtualDevice;

/**
 *
 * @author anderson
 */
public class LED extends Device implements VirtualDevice, Drawable, Selectable, Classifiable, Configurable {

    private final AffineTransform transform = new AffineTransform();
    private boolean state = false;
    private double x, y;
    private final Point2D.Double src = new Point2D.Double();
    private final Point2D.Double dst = new Point2D.Double();
    private boolean selected;
    private Color color = Color.RED;
    private WidgetContainer resource;
    private final HashMap<String, Color> colorMap = new HashMap<>();

    public LED() {
        colorMap.put("Vermelho", Color.RED);
        colorMap.put("Azul", Color.BLUE);
        colorMap.put("Verde", Color.GREEN);
        colorMap.put("Amarelo", Color.YELLOW);
        name = new Argument("LED", Argument.STRING_LITERAL);
        pin = new Argument(2, Argument.NUMBER_LITERAL);
        colorArg = new Argument("Vermelho", Argument.SINGLE_VARIABLE);
    }

    @Override
    public boolean isActuator() {
        return true;
    }

    @Override
    public boolean isSensor() {
        return false;
    }

    @Override
    public byte[] defaultGetMessage() {
        return new byte[]{1, 0};
    }

    @Override
    public void setState(ByteBuffer data) {
        state = (data.get() != 0);
    }

    @Override
    public void getState(ByteBuffer buffer, Robot robot) {
        buffer.put((byte) 1);
        buffer.put((byte) (state ? 1 : 0));
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
        return "" + (state ? 1 : 0);
    }

    @Override
    public byte getClassID() {
        return 1;
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
        if (state) {
            g.setColor(colorMap.get(colorArg.toString()));
        } else {
            g.setColor(colorMap.get(colorArg.toString()).darker().darker().darker().darker());
        }

        g.fillOval((int) x - 3, (int) y - 3, 6, 6);
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
        state = false;
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

        myShape.add(new Area(new Ellipse2D.Double(2, 0, 12, 12)));

        tmpShape.reset();
        tmpShape.addPoint(2, 4);
        tmpShape.addPoint(2, 13);
        tmpShape.addPoint(14, 13);
        tmpShape.addPoint(14, 4);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(0, 13);
        tmpShape.addPoint(0, 15);
        tmpShape.addPoint(16, 15);
        tmpShape.addPoint(16, 13);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(7, 9);
        tmpShape.addPoint(7, 18);
        tmpShape.addPoint(4, 18);
        tmpShape.addPoint(4, 15);
        myShape.exclusiveOr(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(9, 9);
        tmpShape.addPoint(9, 18);
        tmpShape.addPoint(12, 18);
        tmpShape.addPoint(12, 15);
        myShape.exclusiveOr(new Area(tmpShape));

        Item item = new Item(getName(), myShape, Color.decode("#67E200"), "");
        item.setRef(this);
        return item;
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
    private Argument colorArg;

    public static MutableWidgetContainer createConfigurationPanel(final LED led) {

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Configuração", true));
                components.add(new SubLineBreak());
                components.add(new TextLabel("Nome:  "));
                Widget wtextfield = new Widget(new JTextField("led"), 100, 25);
                components.add(wtextfield);
                container.softEntangle(led.name, wtextfield);
                components.add(new SubLineBreak());

                components.add(new TextLabel("Pino:  "));
                Widget wspinner = new Widget(new JSpinner(new SpinnerNumberModel(2, 0, 20, 1)), 100, 25);
                components.add(wspinner);
                container.softEntangle(led.pin, wspinner);

                components.add(new SubLineBreak());
                components.add(new TextLabel("Cor:  "));

                JComboBox combobox = new JComboBox();
                for (String str : led.colorMap.keySet()) {
                    combobox.addItem(str);
                }
                Widget wcombobox = new Widget(combobox, 100, 25);
                components.add(wcombobox);

                container.softEntangle(led.colorArg, wcombobox);
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

    @Override
    public List<Object> getDescriptionData() {
        ArrayList<Object> data = new ArrayList<>();
        data.add(getName());
        data.add(pin.getDoubleValue());
        data.add(x);
        data.add(y);
        data.add(colorArg.toString());
        return data;
    }

    @Override
    public Device createDevice(List<Object> descriptionData) {
        LED led = new LED();
        led.name.set((String) descriptionData.get(0), Argument.STRING_LITERAL);
        led.pin.set((Double) descriptionData.get(1), Argument.NUMBER_LITERAL);
        led.x = (double) descriptionData.get(2);
        led.y = (double) descriptionData.get(3);
        led.colorArg.set((String) descriptionData.get(4), Argument.SINGLE_VARIABLE);
        return led;
    }

    @Override
    public byte[] getBuilderMessageData() {
        return new byte[]{(byte) pin.getDoubleValue()};
    }
}
