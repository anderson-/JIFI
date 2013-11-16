/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.util.LineIterator;

/**
 *
 * @author antunes
 */
public class Environment {

    private static final int MAX_DISTANCE = 500;
    private final ArrayList<Shape> followLines = new ArrayList<>();
    private final ArrayList<Shape> obstacles = new ArrayList<>();
    private final ArrayList<Shape> followLinesTmp = new ArrayList<>();
    private final ArrayList<Shape> obstaclesTmp = new ArrayList<>();

    public void addObstacle(Shape s) {
        obstacles.add(s);
    }

    public void addFollowLine(Shape s) {
        followLines.add(s);
    }

    public double beamDistance(double x, double y, double theta, double d) {
        double df = Double.MAX_VALUE;
        double dt;
        double x2 = x + MAX_DISTANCE * cos(theta);
        double y2 = y + MAX_DISTANCE * sin(theta);
        Line2D.Double line = new Line2D.Double(x, y, x2, y2);
        boolean insideShape = false;
        boolean isInsideShape = false;
        boolean end = false;
        Point2D p;

        obstaclesTmp.clear();
        obstaclesTmp.addAll(obstacles);
        System.out.println("1");
        for (Iterator<Shape> shapeIt = obstaclesTmp.iterator(); shapeIt.hasNext();) {
            System.out.println("2");
            Shape s = shapeIt.next();
            if (s instanceof Line2D.Double) {
                Line2D.Double obstacleLine = ((Line2D.Double) s);
                Line2D.Double tmpLine = new Line2D.Double();
                for (Iterator<Point2D> lineIt = new LineIterator(line); lineIt.hasNext();) {
                    p = lineIt.next();
                    tmpLine.setLine(x, y, p.getX(), p.getY());

                    if (tmpLine.intersectsLine(obstacleLine)) {
                        dt = sqrt(pow(x - p.getX(), 2) + pow(y - p.getY(), 2)) - d;
                        //procura o ponto mais perto
                        if (df > dt) {
                            df = dt;
                        }
                    }
                }
            } else {
                insideShape = s.contains(x, y);
                if (insideShape) {
                    if (end) {
                        continue;
                    } else {
                        df = 0;
                    }
                }
                for (Iterator<Point2D> lineIt = new LineIterator(line); lineIt.hasNext();) {
                    p = lineIt.next();
                    if (s.contains(p)) {
                        dt = sqrt(pow(x - p.getX(), 2) + pow(y - p.getY(), 2)) - d;

                        if (insideShape) {
                            //procura o ponto mais longe
                            if (df < dt) {
                                df = dt;
                            }
                            isInsideShape = true;
                        } else {
                            //procura o ponto mais perto
                            if (df > dt) {
                                df = dt;
                            }
                        }
                    }
                }
                
                if (isInsideShape && !shapeIt.hasNext()) {
                    shapeIt = obstaclesTmp.iterator();
                    System.out.println("3");
                    end = true;
                }
            }
        }

        return df / 2;
    }

    public boolean isOver(double x, double y) {
        followLinesTmp.clear();
        followLinesTmp.addAll(followLines);

        for (Shape s : followLinesTmp) {
            if (s.intersects(x - 3, y - 3, 6, 6)) {
                return true;
            }
        }
        return false;
    }

    public void draw(Graphics2D g) {

        Stroke str = g.getStroke();

        g.setStroke(new BasicStroke(5));

        g.setColor(Color.BLACK);
        for (Shape s : followLines) {
            g.draw(s);
        }

        g.setColor(Color.ORANGE.darker().darker());
        for (Shape s : obstacles) {
            g.draw(s);
        }

        g.setStroke(str);
    }

    public Iterator<Shape> linesIterator() {
        return followLines.iterator();
    }

    public Iterator<Shape> obstaclesIterator() {
        return obstacles.iterator();
    }
}
