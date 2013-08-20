/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels;

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

/**
 *
 * @author antunes
 */
public class SimulationPanel extends DrawingPanel implements Serializable {

    private static final int MAX_ARRAY = 500;
    private ArrayList<Point> rpos = new ArrayList<>();
    private Robot robot;

    public SimulationPanel() {
        robot = new Robot();
        add(robot);
        robot.setRightWheelSpeed(50);
        robot.setLeftWheelSpeed(50);
        //mapeia a posição a cada x ms
        Timer timer = new Timer(100) {
            @Override
            public void run() {
                rpos.add(new Point((int)robot.getObjectBouds().x, (int)robot.getObjectBouds().y));
                while (rpos.size() > MAX_ARRAY) {
                    rpos.remove(0);
                }
                if (this.getCount() % 20 == 0) {
                    robot.setRightWheelSpeed(Math.random() * 80);
                    robot.setLeftWheelSpeed(Math.random() * 80);
                }
            }
        };
        timer.setDisposable(false);
        clock.addTimer(timer);
        clock.setPaused(false);
    }

    public final void add(Robot r) {
        add((Drawable)r);
        for (Device d : r.getDevices()){
            if (d instanceof Drawable){
                add((Drawable)d);
            }
        }
        for (Connection c : r.getConnections()){
            add((Drawable)c);
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
        drawGrade(g, 4, (float) ((robot.getObjectBouds().height*100)/Robot.SIZE_CM), getBounds());
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
    }

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
        g.setColor(Color.red);
        paintPoints(g, rpos, 5);
    }

    public static void main(String[] args) {
        SimulationPanel p = new SimulationPanel();
        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);
    }
}
