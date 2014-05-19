/**
 * @file .java
 * @author Anderson Antunes <anderson.utf@gmail.com>
 *         *seu nome* <*seu email*>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Anderson Antunes <anderson.utf@gmail.com>
 *                       *seu nome* <*seu email*>
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
package robotinterface.gui.panels;

import robotinterface.gui.panels.sidepanel.Configurable;
import java.awt.BasicStroke;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.drawable.DrawingPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import robotinterface.util.trafficsimulator.Timer;
import robotinterface.robot.Robot;
import static java.lang.Math.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import robotinterface.algorithm.procedure.If;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.Rotable;
import robotinterface.gui.panels.robot.RobotControlPanel;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.drawable.Selectable;
import robotinterface.gui.panels.sidepanel.SidePanel;
import robotinterface.plugin.cmdpack.begginer.Move;
import robotinterface.plugin.cmdpack.begginer.Read;
import robotinterface.robot.device.Button;
import robotinterface.robot.device.Device;
import robotinterface.robot.device.IRProximitySensor;
import robotinterface.robot.device.LED;
import robotinterface.robot.device.ReflectanceSensorArray;
import robotinterface.robot.simulation.Environment;
import robotinterface.util.LineIterator;

/**
 * Painel da simulação do robô. <### EM DESENVOLVIMENTO ###>
 */
public class RobotEditorPanel extends DrawingPanel implements Serializable {

    private final Item ITEM_ADD_DEVICE;
    private final Item ITEM_GO_BACK;
    private final Item ITEM_REMOVE;
    public static Color SELECTED_COLOR = Color.decode("#6BE400");

    {
        Area myShape = new Area();
        Polygon tmpShape = new Polygon();
        tmpShape.addPoint(16, 6);
        tmpShape.addPoint(16, 10);
        tmpShape.addPoint(0, 10);
        tmpShape.addPoint(0, 6);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(6, 16);
        tmpShape.addPoint(10, 16);
        tmpShape.addPoint(10, 0);
        tmpShape.addPoint(6, 0);
        myShape.add(new Area(tmpShape));

        ITEM_ADD_DEVICE = new Item("Novo Dispositivo", myShape, Color.decode("#0969A2"), "");

        tmpShape.reset();
        tmpShape.addPoint(0, 10);
        tmpShape.addPoint(10, 0);
        tmpShape.addPoint(10, 5);
        tmpShape.addPoint(20, 5);

        tmpShape.addPoint(20, 8);
        tmpShape.addPoint(9, 8);
        tmpShape.addPoint(9, 5);

        tmpShape.addPoint(4, 10);

        tmpShape.addPoint(9, 15);
        tmpShape.addPoint(9, 12);
        tmpShape.addPoint(20, 12);

        tmpShape.addPoint(20, 15);
        tmpShape.addPoint(10, 15);
        tmpShape.addPoint(10, 20);

        ITEM_GO_BACK = new Item("Voltar", tmpShape, Color.decode("#FF7800"), "");

        myShape = new Area();
        tmpShape = new Polygon();
        tmpShape.addPoint(2, 0);
        tmpShape.addPoint(20, 18);
        tmpShape.addPoint(18, 20);
        tmpShape.addPoint(0, 2);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(18, 0);
        tmpShape.addPoint(20, 2);
        tmpShape.addPoint(2, 20);
        tmpShape.addPoint(0, 18);
        myShape.add(new Area(tmpShape));

        ITEM_REMOVE = new Item("Remover", new Area(myShape), Color.red, "");

    }
    private final ArrayList<Robot> robots = new ArrayList<>();
    private Item itemSelected;
    private Point2D.Double point = null;
    private final Ellipse2D.Double circle = new Ellipse2D.Double();
    private final Ellipse2D.Double dot = new Ellipse2D.Double();
    private final Line2D.Double radius = new Line2D.Double();
    private int poliSegments = 6;
    SidePanel sidePanel;
    Robot r;

    public static Shape create(int i, double x, double y, double r, Path2D.Double poly) {

        if (poly == null) {
            poly = new Path2D.Double();
        } else {
            poly.reset();
        }

        double alpha = 1;
        double theta = (2 * Math.PI) / i;
        double tx = x + r * cos(alpha);
        double ty = y + r * sin(alpha);

        poly.moveTo(tx, ty);

        for (int j = 0; j < i; j++) {
            alpha += theta;
            tx = x + r * cos(alpha);
            ty = y + r * sin(alpha);
            poly.lineTo((int) tx, (int) ty);
        }

        return poly;
    }

    public RobotEditorPanel(Robot r) {
        super.midMouseButtonResetView = false;

        this.r = r;
        addRobot(r);
        gridSize = 30;
        zoom = 3.2;
        sidePanel = new SidePanel(this) {
            
            void goBack() {
                if (itemSelected != null && itemSelected.getRef() instanceof Selectable) {
                    itemSelected.setSelected(false);
                    ((Selectable) itemSelected.getRef()).setSelected(false);
                }
                itemSelected = null;
                sidePanel.clearPanel();
                for (Device d : RobotEditorPanel.this.r.getDevices()) {
                    if (d instanceof Classifiable) {
                        Item newItem = ((Classifiable) d).getItem().copy();
                        newItem.setName("[" + (d.getID()) + "] " + newItem.getName());
                        sidePanel.add(newItem);
                    }
                }

                sidePanel.add(ITEM_ADD_DEVICE);
                sidePanel.switchAnimLeft();
            }

            @Override
            public void itemSelected(Item item, Object ref) {
                if (item == null) {

                } else if (item == ITEM_ADD_DEVICE) {
                    sidePanel.clearTempPanel();
                    sidePanel.addTmp(ITEM_GO_BACK);
                    for (Class c : RobotControlPanel.getAvailableDevices()) {
                        if (Classifiable.class.isAssignableFrom(c)) {
                            try {
                                Item newItem = ((Classifiable) c.newInstance()).getItem();
                                newItem.setRef(c);
                                sidePanel.addTmp(newItem);
                            } catch (InstantiationException ex) {

                            } catch (IllegalAccessException ex) {

                            }
                        }
                    }
                    sidePanel.switchAnimRight();

                } else if (item == ITEM_GO_BACK) {
                    goBack();
                } else if (item == ITEM_REMOVE) {
                    RobotEditorPanel.this.r.remove((Device) itemSelected.getRef());
                    goBack();
                } else {
                    if (item.getRef() instanceof Class) {
                        try {
                            RobotEditorPanel.this.r.add((Device) (((Class) item.getRef()).newInstance()));
                        } catch (Exception ex) {
                        }
                        
                        goBack();
                    } else {
                        sidePanel.clearTempPanel();
                        sidePanel.addTmp(ITEM_GO_BACK);

                        if (item.getRef() instanceof Configurable) {
                            Configurable c = (Configurable) item.getRef();
                            sidePanel.addTmp(c.getConfigurationPanel());
                        }

                        sidePanel.addTmp(ITEM_REMOVE);
                        sidePanel.switchAnimRight();

                        if (item.getRef() instanceof Selectable) {
                            item.setSelected(true);
                            ((Selectable) item.getRef()).setSelected(true);
                        }
                    }

                }
                itemSelected = item;
            }
        };

        sidePanel.itemSelected(ITEM_GO_BACK, null);
        

        sidePanel.setColor(Color.decode("#4D4388"));//FF7070

        add(sidePanel);

        //mapeia a posição a cada x ms
        Timer timer = new Timer(300) {
            ArrayList<Robot> tmpBots = new ArrayList<>();

            @Override
            public void run() {
                tmpBots.clear();
                synchronized (robots) {
                    tmpBots.addAll(robots);
                }

                for (Robot robot : tmpBots) {
                    if (!(robot.getLeftWheelSpeed() == 0 && robot.getRightWheelSpeed() == 0)) {
                        robot.updateVirtualPerception();
                    }

//                    robot.setRightWheelSpeed(30);
//                    robot.setLeftWheelSpeed(-30);
                    if (this.getCount() % 20 == 0) {
//                        robot.setRightWheelSpeed(Math.random() * 100);
//                        robot.setLeftWheelSpeed(Math.random() * 100);
                    }
                }
            }
        };
        timer.setDisposable(false);
        clock.addTimer(timer);
        clock.setPaused(false);
    }
    
    public void hideSidePanel(boolean b) {
        sidePanel.setOpen(!b);
    }

    public void addRobot(Robot robot) {
        synchronized (robots) {
            robots.add(robot);
        }
        add(robot);
    }

    public void removeRobot(Robot robot) {
        synchronized (robots) {
            robots.remove(robot);
        }
        remove(robot);
    }

    public ArrayList<Robot> getRobots() {
        return robots;
    }

    @Override
    public int getDrawableLayer() {
        return DrawingPanel.BACKGROUND_LAYER | DrawingPanel.DEFAULT_LAYER | DrawingPanel.TOP_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
        super.drawBackground(g, ga, in);
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
        r.setTheta(0);
        r.setLocation(0, 0);
        if (itemSelected != null && itemSelected.getRef() instanceof Drawable) {
            //desenha a origem
            Drawable d = ((Drawable) itemSelected.getRef());
            if (d != null) {
                g.setColor(Color.MAGENTA);
                AffineTransform transform = g.getTransform();
                ga.applyGlobalPosition(transform);
                ga.applyZoom(transform);
                g.setTransform(transform);
                g.drawOval((int) d.getPosX() - 1, (int) d.getPosY() - 1, 2, 2);
                //g.fillRect((int) d.getPosX() - 2, (int) d.getPosY() - 2, 4, 4);
            }
        }
        if (sidePanel.getObjectBouds().contains(in.getMouse())) {
            return;
        }

        if (itemSelected != null && itemSelected.getRef() instanceof Drawable) {
            //desenha a origem
            Drawable d = ((Drawable) itemSelected.getRef());

            if (in.mousePressed() && in.getMouseButton() == MouseEvent.BUTTON1) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                if (in.isKeyPressed(KeyEvent.VK_R) && d instanceof Rotable) {
                    Rotable rotable = (Rotable) d;
                    zoomEnabled = false;

                    double theta = Math.atan2((in.getTransformedMouse().y - d.getPosY()), (in.getTransformedMouse().x - d.getPosX()));
                    g.setColor(Color.MAGENTA);
                    g.drawLine((int) d.getPosX(), (int) d.getPosY(), in.getTransformedMouse().x, in.getTransformedMouse().y);
                    rotable.setTheta(theta);
                } else if (in.isKeyPressed(KeyEvent.VK_M)) {
                    d.setLocation(in.getTransformedMouse().x, in.getTransformedMouse().y);
                }
            } else {
                this.setCursor(Cursor.getDefaultCursor());
            }

            if (in.isKeyPressed(KeyEvent.VK_CONTROL)) {
                if (in.mouseClicked() && in.getMouseButton() == MouseEvent.BUTTON2) {
                    resetView();
                }

//                if (in.isKeyPressed(KeyEvent.VK_R) && d instanceof Rotable) {
//                    Rotable rotable = (Rotable) d;
//                    zoomEnabled = false;
//                    int wr = -in.getMouseWheelRotation();
//                    rotable.setTheta(rotable.getTheta() + wr / 5.0);
//                } else if (in.mousePressed() && in.getMouseButton() == MouseEvent.BUTTON1) {
//                    d.setLocation(in.getTransformedMouse().x, in.getTransformedMouse().y);
//                }
            } else {
                zoomEnabled = true;
            }
        }
    }
    public static final BasicStroke defaultStroke = new BasicStroke();
    public static final BasicStroke dashedStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_MITER,
            10.0f, new float[]{5}, 0.0f);

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
        synchronized (robots) {
            for (Robot robot : robots) {

                if (in.mouseClicked() && in.getMouseButton() == MouseEvent.BUTTON2) {
                    if (!in.isKeyPressed(KeyEvent.VK_CONTROL)) {
                        robot.setLocation(0, 0);
                        robot.setTheta(0);
                    }
                }

                double v1 = robot.getLeftWheelSpeed();
                double v2 = robot.getRightWheelSpeed();

                //desenha o caminho
                if (v1 != v2) {
                    //calcula o raio
                    double r = Robot.size / 2 * ((v1 + v2) / (v1 - v2));
                    //calcula o centro (ortogonal à direção atual do robô)
                    double x, y;
                    if (r < 0) {
                        r *= -1;
                        x = (cos(-robot.getTheta() + PI / 2) * r + robot.getObjectBouds().x);
                        y = (-sin(-robot.getTheta() + PI / 2) * r + robot.getObjectBouds().y);
                    } else {
                        x = (cos(-robot.getTheta() - PI / 2) * r + robot.getObjectBouds().x);
                        y = (-sin(-robot.getTheta() - PI / 2) * r + robot.getObjectBouds().y);
                    }

                    g.setStroke(dashedStroke); //linha pontilhada
                    //desenha o circulo
                    g.setColor(Color.gray);
                    circle.setFrame(x - r, y - r, r * 2, r * 2);
                    try {
                        g.draw(circle);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(circle);
                        System.exit(0);
                    }
                    //desenha o raio
                    g.setColor(Color.magenta);
                    radius.setLine(robot.getObjectBouds().x, robot.getObjectBouds().y, x, y);
                    g.draw(radius);
                    g.setStroke(defaultStroke); //fim da linha pontilhada
                    //desenha o centro
                    dot.setFrame(x - 3, y - 3, 6, 6);
                    g.fill(dot);
                }
            }
        }

        g.setStroke(new BasicStroke(5));
        g.setColor(Color.green);
        g.setStroke(defaultStroke);
    }

    public static void main(String[] args) {

        Robot r = new Robot();
        r.add(new IRProximitySensor());
        r.add(new ReflectanceSensorArray());
        r.add(new LED());
        r.add(new Button());
        r.add(new Button());
        r.add(new Button());

        RobotEditorPanel p = new RobotEditorPanel(r);
        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);

    }
}
