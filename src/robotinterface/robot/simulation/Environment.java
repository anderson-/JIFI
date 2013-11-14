/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
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
        Point2D p;
//        boolean out = true;
        for (Shape s : obstacles) {
            if (s instanceof Line2D.Double) {
            } else {
                for (Iterator<Point2D> iter = new LineIterator(line); iter.hasNext();) {
                    p = iter.next();
                    if (s.contains(p)) {
                        dt = sqrt(pow(x - p.getX(), 2) + pow(y - p.getY(), 2)) - d;
                        System.out.println(dt + " " + df);
                        if (dt < df) {
                            df = dt;
                        }
//                        if (df <= 0) {
//                            out = false;
//                        }
//                        if (out) {
//                            if (dt < df) {
//                                df = dt;
//                            }
//                        } else {
//                            if (dt > df) {
//                                df = dt;
//                            }
//                        }
//                        out = false;
                    }
//                    if (out) {
//                    }
                }
//                if (!out) {
//                    out = true;
//                }
            }
        }

        return df / 2;
    }

    public boolean isOver(double x, double y) {
        for (Shape s : obstacles) {
            if (s.contains(x - 3, y - 3, 6, 6)) {
                return true;
            }
        }
        return false;
    }

    public void draw(Graphics2D g) {

        g.setColor(Color.BLACK);
        for (Shape s : followLines) {
            g.draw(s);
        }

        g.setColor(Color.ORANGE.darker().darker());
        for (Shape s : obstacles) {
            g.draw(s);
        }
    }

    public Iterator<Shape> linesIterator() {
        return followLines.iterator();
    }

    public Iterator<Shape> obstaclesIterator() {
        return obstacles.iterator();
    }
}
