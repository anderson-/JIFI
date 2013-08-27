/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels;

import java.awt.BasicStroke;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import robotinterface.robot.connection.Connection;
import robotinterface.robot.device.Device;
import robotinterface.util.trafficsimulator.Timer;
import robotinterface.robot.Robot;
import static java.lang.Math.*;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.IRProximitySensor;
import robotinterface.util.TextStroke;
import robotinterface.util.observable.Observer;

/**
 *
 * @author antunes
 */
public class SimulationPanel extends DrawingPanel implements Serializable, Observer<Device, Robot> {

    private static final int MAX_ARRAY = 500;
    private final ArrayList<Robot> robots = new ArrayList<>();
    private final ArrayList<Point> rpos = new ArrayList<>();
    private final ArrayList<Point> obstacle = new ArrayList<>();
    private boolean stop = false;

    public SimulationPanel() {
//        robot.setRightWheelSpeed(50);
//        robot.setLeftWheelSpeed(50);
        //mapeia a posição a cada x ms
        Timer timer = new Timer(100) {
            @Override
            public void run() {
                for (Robot robot : robots) {
                    //posição
                    synchronized (rpos) {
                        rpos.add(new Point((int) robot.getObjectBouds().x, (int) robot.getObjectBouds().y));
                        while (rpos.size() > MAX_ARRAY) {
                            rpos.remove(0);
                        }
                    }
                    synchronized (obstacle) {
                        while (obstacle.size() > MAX_ARRAY) {
                            obstacle.remove(0);
                        }
                    }
//                    if (this.getCount() % 20 == 0) {
//                        robot.setRightWheelSpeed(Math.random() * 100);
//                        robot.setLeftWheelSpeed(Math.random() * 100);
//                    }
                }
            }
        };
        timer.setDisposable(false);
        clock.addTimer(timer);
        clock.setPaused(false);
    }

    public void addRobot(Robot robot) {
        synchronized (robots) {
            robots.add(robot);
        }
        robot.attach(this);
        add(robot);
    }

    private void addObstacle(Robot robot, double d) {
        double tx = robot.getObjectBouds().x + d * cos(robot.getTheta());
        double ty = robot.getObjectBouds().y + d * sin(robot.getTheta());
        synchronized (obstacle) {
            obstacle.add(new Point((int) tx, (int) ty));
        }
    }

    public final void add(Robot r) {
        add((Drawable) r);
        for (Device d : r.getDevices()) {
            if (d instanceof Drawable) {
                add((Drawable) d);
            }
        }
        for (Connection c : r.getConnections()) {
            add((Drawable) c);
        }
    }

    public static void paintPoints(Graphics2D g, List<Point> points, int size) {
        for (Point p : points) {
            g.fillOval(p.x - size / 2, p.y - size / 2, size, size);
        }
    }

    public static double getAcceleration() {
        return 10;
    }

    @Override
    public int getDrawableLayer() {
        return DrawingPanel.BACKGROUND_LAYER | DrawingPanel.DEFAULT_LAYER | DrawingPanel.TOP_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
        g.setColor(Color.gray);
        drawGrade(g, 4, (float) ((Robot.size * 100) / Robot.SIZE_CM), getBounds());
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
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
                }
            }
        }
        g.setColor(Color.red);
        synchronized (rpos) {
            paintPoints(g, rpos, 5);
        }
        g.setColor(Color.GREEN.brighter());
        synchronized (obstacle) {
            paintPoints(g, obstacle, 5);
        }
    }

    public static void main(String[] args) {
        SimulationPanel p = new SimulationPanel();
        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);
        p.addRobot(new Robot());
        p.addRobot(new Robot());
    }

    @Override
    public void update(Device device, Robot robot) {
        if (device instanceof IRProximitySensor) {
            addObstacle(robot, ((IRProximitySensor) device).getDist()*2);
        }
        if (device instanceof Compass) {
            robot.setTheta(Math.toRadians(((Compass) device).getAlpha()));
        }
    }
}
