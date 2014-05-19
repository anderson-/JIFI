/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.device;

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
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.DrawableProcedureBlock;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.WidgetContainer;
import robotinterface.drawable.swing.component.Component;
import robotinterface.drawable.swing.component.SubLineBreak;
import robotinterface.drawable.swing.component.TextLabel;
import robotinterface.drawable.swing.component.Widget;
import robotinterface.drawable.swing.component.WidgetLine;
import robotinterface.gui.panels.sidepanel.Configurable;
import robotinterface.gui.panels.RobotEditorPanel;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.drawable.Selectable;
import robotinterface.plugin.cmdpack.begginer.Wait;
import robotinterface.robot.Robot;
import robotinterface.robot.simulation.VirtualDevice;

/**
 *
 * @author anderson
 */
public class LED extends Device implements VirtualDevice, Drawable, Selectable, Classifiable, Configurable {
    
    private final AffineTransform transform = new AffineTransform();
    private final int values[] = new int[5];
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
        arg0 = new Argument("Vermelho", Argument.SINGLE_VARIABLE);
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
        
        g.setColor(colorMap.get(arg0.toString()));
        
        g.fillOval((int) x - 3, (int) y - 3, 6, 6);
    }
    
    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        
    }
    
    @Override
    public String getName() {
        return "LED";
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
        
        Item item = new Item("LED", myShape, Color.decode("#67E200"), "");
        item.setRef(this);
        return item;
    }
    
    public WidgetContainer getConfigurationPanel() {
        if (resource == null) {
            resource = createConfigurationPanel(this);
        }
        return resource;
    }
    
    private Argument arg0;
    
    public static MutableWidgetContainer createConfigurationPanel(final LED led) {

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Configuração", true));
                components.add(new SubLineBreak());
                components.add(new TextLabel("Cor:  "));
                
                JComboBox combobox = new JComboBox();
                for (String str : led.colorMap.keySet()) {
                    combobox.addItem(str);
                }
                Widget wcombobox = new Widget(combobox, 100, 25);
                components.add(wcombobox);
                
                container.softEntangle(led.arg0, wcombobox);
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
