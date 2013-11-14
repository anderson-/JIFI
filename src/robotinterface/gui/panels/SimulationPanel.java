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

import java.awt.BasicStroke;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.DrawingPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import robotinterface.robot.connection.Connection;
import robotinterface.robot.device.Device;
import robotinterface.util.trafficsimulator.Timer;
import robotinterface.robot.Robot;
import static java.lang.Math.*;
import java.util.Iterator;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.gui.panels.sidepanel.SidePanel;
import robotinterface.plugin.PluginManager;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.IRProximitySensor;
import robotinterface.robot.simulation.Environment;
import robotinterface.robot.simulation.Perception;
import robotinterface.util.LineIterator;
import robotinterface.util.observable.Observer;

/**
 * Painel da simulação do robô. <### EM DESENVOLVIMENTO ###>
 */
public class SimulationPanel extends DrawingPanel implements Serializable {

    public static final Item ITEM_LINE = new Item("Fita Adesiva", new Rectangle(0, 0, 20, 5), Color.DARK_GRAY);
    public static final Item ITEM_OBSTACLE_LINE = new Item("Parede", new Rectangle(0, 0, 20, 5), Color.decode("#9B68C0"));
    public static final Item ITEM_CILINDER = new Item("Cilindro", new Ellipse2D.Double(0, 0, 20, 20), Color.decode("#9B68C0"));
    public static final Item ITEM_REMOVE_LINE = new Item("Remover", new Rectangle(0, 0, 20, 20), Color.decode("#D24545"));
    private static final int MAX_ARRAY = 5000;
    private final ArrayList<Robot> robots = new ArrayList<>();
    private Environment env = new Environment();
    private Item itemSelected;
    private Point2D.Double point = null;
    SidePanel sp;

    public SimulationPanel() {

        sp = new SidePanel() {
            @Override
            protected void ItemSelected(Item item, Object ref) {
                try {
                    itemSelected = item;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
//        sp.setOpen(false);
//        sp.setColor(Color.decode("#FF7070"));//FF7070
        sp.add(ITEM_LINE);
        sp.add(ITEM_OBSTACLE_LINE);
        sp.add(ITEM_CILINDER);
        sp.add(ITEM_REMOVE_LINE);
        add(sp);

        //mapeia a posição a cada x ms
        Timer timer = new Timer(100) {
            ArrayList<Robot> tmpBots = new ArrayList<>();

            @Override
            public void run() {
                tmpBots.clear();
                synchronized (robots) {
                    tmpBots.addAll(robots);
                }

                for (Robot robot : tmpBots) {
                    robot.updatePerception();

                    if (this.getCount() % 20 == 0) {
//                        robot.setRightWheelSpeed(30);
//                        robot.setLeftWheelSpeed(30);
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

    private SimulationPanel(Environment e) {
        env = e;
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }
    
    public void addRobot(Robot robot) {
        synchronized (robots) {
            robots.add(robot);
        }
        add(robot);
    }
    
    public ArrayList<Robot> getRobots() {
        return robots;
    }

//    private void addObstacle(Robot robot, double d) {
//        double tx = robot.getObjectBouds().x + d * cos(robot.getTheta());
//        double ty = robot.getObjectBouds().y + d * sin(robot.getTheta());
//        synchronized (obstacle) {
//            obstacle.add(new Point((int) tx, (int) ty));
//            while (obstacle.size() > MAX_ARRAY) {
//                obstacle.remove(0);
//            }
//        }
//    }
//    public final void add(Robot r) {
//        add((GraphicObject) r);
//        for (Device d : r.getDevices()) {
//            if (d instanceof GraphicObject) {
//                add((GraphicObject) d);
//            }
//        }
//        for (Connection c : r.getConnections()) {
//            add((GraphicObject) c);
//        }
//    }
    private void paintPoints(Graphics2D g, List<Point> points, int size) {
        for (Point p : points) {
            g.fillOval(p.x - size / 2, p.y - size / 2, size, size);
        }
    }

    @Override
    public int getDrawableLayer() {
        return DrawingPanel.BACKGROUND_LAYER | DrawingPanel.DEFAULT_LAYER | DrawingPanel.TOP_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
        super.drawBackground(g, ga, in);
        env.draw(g);
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
        if (sp.getObjectBouds().contains(in.getMouse())) {
            return;
        }
        if (itemSelected != null) {
            if (itemSelected == ITEM_LINE || itemSelected == ITEM_OBSTACLE_LINE || itemSelected == ITEM_REMOVE_LINE) {
                if (in.mouseClicked()) {
                    if (in.getMouseButton() == MouseEvent.BUTTON1) {
                        if (point != null) {
                            Line2D.Double line = new Line2D.Double(point, in.getTransformedMouse());

                            if (itemSelected == ITEM_LINE) {
                                env.addFollowLine(line);
                            } else if (itemSelected == ITEM_OBSTACLE_LINE) {
                                env.addObstacle(line);
                            } else if (itemSelected == ITEM_REMOVE_LINE) {
                                for (Iterator<Shape> it = env.linesIterator(); it.hasNext();) {
                                    Shape s = it.next();
                                    if (s instanceof Line2D.Double) {
                                        if (((Line2D.Double) s).intersectsLine(line)) {
                                            it.remove();
                                        }
                                    } else {
                                        Point2D p;
                                        for (Iterator<Point2D> iter = new LineIterator(line); iter.hasNext();) {
                                            p = iter.next();
                                            if (s.contains(p)) {
                                                it.remove();
                                                break;
                                            }
                                        }
                                    }
                                }

                                for (Iterator<Shape> it = env.obstaclesIterator(); it.hasNext();) {
                                    Shape s = it.next();
                                    if (s instanceof Line2D.Double) {
                                        if (((Line2D.Double) s).intersectsLine(line)) {
                                            it.remove();
                                        }
                                    } else {
                                        Point2D p;
                                        for (Iterator<Point2D> iter = new LineIterator(line); iter.hasNext();) {
                                            p = iter.next();
                                            if (s.contains(p)) {
                                                it.remove();
                                                break;
                                            }
                                        }
                                    }
                                }
                                point = null;
                                return;
                            }

                        }
                        point = new Point2D.Double(in.getTransformedMouse().x, in.getTransformedMouse().y);
                    } else {
                        point = null;
                    }
                }
            } else if (itemSelected == ITEM_CILINDER) {
                if (in.mouseClicked()) {
                    if (in.getMouseButton() == MouseEvent.BUTTON1) {
                        if (point != null) {
                            double r = point.distance(in.getTransformedMouse());
                            double x = point.x - r;
                            double y = point.y - r;
                            r *= 2;
                            env.addObstacle(new Ellipse2D.Double(x, y, r, r));
                            point = null;
                            return;
                        }
                        point = new Point2D.Double(in.getTransformedMouse().x, in.getTransformedMouse().y);
                    } else {
                        point = null;
                    }
                }
            }
//            g.translate(in.getMouse().x - 10, in.getMouse().y - 10);
//            g.setColor(itemSelected.getColor());
//            g.fill(itemSelected.getIcon());
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
//                System.out.println("eee");
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
                    g.drawOval((int) (x - r), (int) (y - r), (int) r * 2, (int) r * 2);
                    //desenha o raio
                    g.setColor(Color.magenta);
                    g.drawLine((int) robot.getObjectBouds().x, (int) robot.getObjectBouds().y, (int) x, (int) y);
                    g.setStroke(defaultStroke); //fim da linha pontilhada
                    //desenha o centro
                    g.fillOval((int) (x - 3), (int) (y - 3), 6, 6);
//                    System.out.println("asd");
                }
            }
        }

//        g.setColor(Color.red);
//        synchronized (rpos) {
//            paintPoints(g, rpos, 5);
//        }
//        g.setColor(Color.GREEN.brighter());
//        synchronized (obstacle) {
//            paintPoints(g, obstacle, 5);
//        }

//        per.draw(g);

        g.setStroke(new BasicStroke(5));

        if (point != null) {
            if (itemSelected == ITEM_LINE || itemSelected == ITEM_OBSTACLE_LINE || itemSelected == ITEM_REMOVE_LINE) {
                g.drawLine((int) point.x, (int) point.y, (int) in.getTransformedMouse().x, (int) in.getTransformedMouse().y);
            } else {
                double r = point.distance(in.getTransformedMouse());
                double x = point.x - r;
                double y = point.y - r;
                r *= 2;
                g.drawOval((int) x, (int) y, (int) r, (int) r);
            }
        }

//        g.setColor(Color.BLACK);
//        for (Shape s : obstacles) {
//            g.draw(s);
//        }

        g.setStroke(defaultStroke);
    }

    public static void main(String[] args) {
        
        SimulationPanel p = new SimulationPanel();
        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);

        Robot r = new Robot();
        r.add(new IRProximitySensor());
        r.setEnvironment(p.getEnv());
        p.addRobot(r);
        
        r = new Robot();
        r.add(new IRProximitySensor());
        r.setEnvironment(p.getEnv());
        p.addRobot(r);
    }

}
