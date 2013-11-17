/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antunes
 */
public class Perception {

    private static final int MAX_ARRAY = 500;
    private final ArrayList<Point> path = new ArrayList<>();
    private final ArrayList<Point> distanceMap = new ArrayList<>();

    public static void paintPoints(Graphics2D g, List<Point> points, int size) {
        for (Point p : points) {
            g.fillOval(p.x - size / 2, p.y - size / 2, size, size);
        }
    }

    public void addObstacle(double x, double y, double theta, double d) {
        x += d * cos(theta);
        y += d * sin(theta);
        synchronized (distanceMap) {
            distanceMap.add(new Point((int) x, (int) y));
            while (distanceMap.size() > MAX_ARRAY) {
                distanceMap.remove(0);
            }
        }
    }

    public void addPathPoint(double x, double y) {
        path.add(new Point((int) x, (int) y));
        if (path.size() > MAX_ARRAY){
            path.remove(0);
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.red);
        synchronized (path) {
            paintPoints(g, path, 5);
        }

        g.setColor(Color.GREEN.brighter());
        synchronized (distanceMap) {
            paintPoints(g, distanceMap, 5);
        }
    }
}
