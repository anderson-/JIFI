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
import robotinterface.drawable.DrawingPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import robotinterface.util.trafficsimulator.Timer;
import robotinterface.robot.Robot;
import static java.lang.Math.*;
import java.util.Iterator;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.gui.panels.sidepanel.SidePanel;
import robotinterface.robot.device.IRProximitySensor;
import robotinterface.robot.simulation.Environment;
import robotinterface.util.LineIterator;

/**
 * Painel da simulação do robô. <### EM DESENVOLVIMENTO ###>
 */
public class SimulationPanel extends DrawingPanel implements Serializable {

    public static final Item ITEM_LINE;
    public static final Item ITEM_LINE_POLI;
    public static final Item ITEM_OBSTACLE_LINE;
    public static final Item ITEM_OBSTACLE_POLI;
    public static final Item ITEM_REMOVE_LINE;

    static {
        Area myShape = new Area();
        Polygon tmpShape = new Polygon();
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

        ITEM_REMOVE_LINE = new Item("Remover", myShape, Color.red, "Remove elementos na interseção com esta linha");

        myShape = new Area();
        tmpShape = new Polygon();
        tmpShape.reset();
        tmpShape.addPoint(0, 20);
        tmpShape.addPoint(10, 0);
        tmpShape.addPoint(20, 20);
        myShape.add(new Area(tmpShape));
        tmpShape.reset();
        tmpShape.addPoint(5, 17);
        tmpShape.addPoint(10, 6);
        tmpShape.addPoint(15, 17);
        myShape.exclusiveOr(new Area(tmpShape));

        ITEM_OBSTACLE_POLI = new Item("Parede Fechada", myShape, Environment.getObstacleColor(), "Cria paredes em forma de um polígono");
        ITEM_LINE = new Item("Linha", new Rectangle(0, 0, 20, 4), Color.DARK_GRAY, "Linha preta colocada no chão, detectada pelo sensor de refletância");
        ITEM_LINE_POLI = new Item("Linha Fechada", myShape, Color.DARK_GRAY, "Cria linhas no chão em forma de um polígono");
        ITEM_OBSTACLE_LINE = new Item("Parede", new Rectangle(0, 0, 20, 4), Environment.getObstacleColor(), "Parede ou obstáculo, detectado pelo sensor de distância");
    }
    private final ArrayList<Robot> robots = new ArrayList<>();
    private Environment env = new Environment();
    private Item itemSelected;
    private Point2D.Double point = null;
    private final Ellipse2D.Double circle = new Ellipse2D.Double();
    private final Ellipse2D.Double dot = new Ellipse2D.Double();
    private final Line2D.Double radius = new Line2D.Double();
    private int poliSegments = 6;
    SidePanel sidePanel;

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

    public SimulationPanel() {

        super.midMouseButtonResetView = false;

        sidePanel = new SidePanel() {
            @Override
            protected void ItemSelected(Item item, Object ref) {
                try {
                    if (itemSelected == item) {
                        itemSelected.setSelected(false);
                        itemSelected = null;
                    } else {
                        if (itemSelected != null) {
                            itemSelected.setSelected(false);
                            itemSelected = null;
                        }
                        itemSelected = item;
                        itemSelected.setSelected(true);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        sidePanel.setColor(Color.decode("#9DCA1D"));//FF7070
        sidePanel.add(ITEM_LINE);
        sidePanel.add(ITEM_LINE_POLI);
        sidePanel.add(ITEM_OBSTACLE_LINE);
        sidePanel.add(ITEM_OBSTACLE_POLI);
//        sp.add(ITEM_CILINDER);
        sidePanel.add(ITEM_REMOVE_LINE);
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

    private SimulationPanel(Environment e) {
        env = e;
    }

    public void hideSidePanel(boolean b) {
        sidePanel.setOpen(!b);
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
        env.draw(g);
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
        if (sidePanel.getObjectBouds().contains(in.getMouse())) {
            return;
        }

        if (in.isKeyPressed(KeyEvent.VK_CONTROL)) {
            if (in.mouseClicked() && in.getMouseButton() == MouseEvent.BUTTON2) {
                resetView();
            }

            if (itemSelected == ITEM_LINE_POLI || itemSelected == ITEM_OBSTACLE_POLI) {
                zoomEnabled = false;
                int wr = -in.getMouseWheelRotation();
                if (poliSegments + wr >= 3 && poliSegments + wr < 16) {
                    poliSegments += wr;
                }
            } else {
                Robot r = null;
                int d = Integer.MAX_VALUE;
                synchronized (robots) {
                    for (Robot robot : robots) {
                        int x = (int) (robot.getPosX() - in.getTransformedMouse().x);
                        int y = (int) (robot.getPosY() - in.getTransformedMouse().y);
                        int tmpD = (int) (Math.sqrt(x * x + y * y));
                        if (tmpD < d) {
                            d = tmpD;
                            r = robot;
                        }
                    }
                }
                r.setSelected(true);
                if (in.isKeyPressed(KeyEvent.VK_R)) {
                    zoomEnabled = false;
                    int wr = -in.getMouseWheelRotation();
                    r.setTheta(r.getTheta() + wr / 3.0);
                } else if (in.mouseClicked() && in.getMouseButton() == MouseEvent.BUTTON1) {
                    r.setLocation(in.getTransformedMouse().x, in.getTransformedMouse().y);
                }
            }
        } else {
            zoomEnabled = true;
        }

        if (itemSelected != null) {
            if (itemSelected == ITEM_LINE || itemSelected == ITEM_OBSTACLE_LINE || itemSelected == ITEM_REMOVE_LINE) {
                if (in.mouseClicked()) {
                    if (in.getMouseButton() == MouseEvent.BUTTON1) {
                        if (point != null) {
                            Line2D.Double line = new Line2D.Double(point, in.getTransformedMouse());

                            if (itemSelected == ITEM_LINE) {
                                env.addFollowLine(new double[]{line.x1, line.y1, line.x2, line.y2});
                            } else if (itemSelected == ITEM_OBSTACLE_LINE) {
                                env.addWall(new double[]{line.x1, line.y1, line.x2, line.y2});
                            } else if (itemSelected == ITEM_REMOVE_LINE) {
                                for (Iterator<Line2D.Double> it = env.linesIterator(); it.hasNext();) {
                                    Line2D.Double s = it.next();
                                    if (s.intersectsLine(line)) {
                                        env.removeFollowLine(s);
                                        it.remove();
                                    }
                                }
                                for (Iterator<Line2D.Double> it = env.obstaclesIterator(); it.hasNext();) {
                                    Line2D.Double s = it.next();
                                    if (s.intersectsLine(line)) {
                                        env.removeWall(s);
                                        it.remove();
                                    }
                                }
                            }

                        }
                        point = new Point2D.Double(in.getTransformedMouse().x, in.getTransformedMouse().y);
                    } else {
                        point = null;
                    }
                }
            } else if (itemSelected == ITEM_LINE_POLI || itemSelected == ITEM_OBSTACLE_POLI) {
                boolean obstacle = (itemSelected == ITEM_OBSTACLE_POLI);
                if (in.mouseClicked()) {
                    if (in.getMouseButton() == MouseEvent.BUTTON1) {
                        if (point != null) {
                            Point point2 = in.getTransformedMouse();
                            double r = point.distance(point2);
                            double alpha = Math.atan2(point2.y - point.y, point2.x - point.x);
                            double theta = (2 * Math.PI) / poliSegments;
                            double ix = point.x + r * cos(alpha);
                            double iy = point.y + r * sin(alpha);
                            double ox = ix;
                            double oy = iy;
                            double tx;
                            double ty;

                            for (int i = 0; i < poliSegments; i++) {
                                alpha += theta;
                                tx = point.x + r * cos(alpha);
                                ty = point.y + r * sin(alpha);
                                if (obstacle) {
                                    env.addWall(new double[]{ox, oy, tx, ty});
                                } else {
                                    env.addFollowLine(new double[]{ox, oy, tx, ty});
                                }
                                ox = tx;
                                oy = ty;
                            }
                            point = null;
                            return;
                        }
                        point = new Point2D.Double(in.getTransformedMouse().x, in.getTransformedMouse().y);
                    } else {
                        point = null;
                    }
                }
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
        g.setColor(Color.green);
        if (point != null) {
            if (itemSelected == ITEM_LINE || itemSelected == ITEM_OBSTACLE_LINE || itemSelected == ITEM_REMOVE_LINE) {
                g.drawLine((int) point.x, (int) point.y, (int) in.getTransformedMouse().x, (int) in.getTransformedMouse().y);
            } else if (itemSelected == ITEM_LINE_POLI || itemSelected == ITEM_OBSTACLE_POLI) {
                Point point2 = in.getTransformedMouse();
                double r = point.distance(point2);
                double alpha = Math.atan2(point2.y - point.y, point2.x - point.x);
                double theta = (2 * Math.PI) / poliSegments;
                double ix = point.x + r * cos(alpha);
                double iy = point.y + r * sin(alpha);
                double ox = ix;
                double oy = iy;
                double tx;
                double ty;

                for (int i = 0; i < poliSegments; i++) {
                    alpha += theta;
                    tx = point.x + r * cos(alpha);
                    ty = point.y + r * sin(alpha);
                    g.drawLine((int) ox, (int) oy, (int) tx, (int) ty);
                    ox = tx;
                    oy = ty;
                }
//                g.drawLine((int) ix, (int) iy, (int) ox, (int) oy);
            }
        }

//        g.setColor(Color.BLACK);
//        for (Shape s : obstacles) {
//            g.draw(s);
//        }
        g.setStroke(defaultStroke);
    }

    public void resetSimulation() {
        // remove as percepções dos robos e retorna-os para a posicao (0,0)
        for (Robot r : robots) {
            r.reset();
        }
        // limpa o ambiente
        env.clearEnvironment();
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
