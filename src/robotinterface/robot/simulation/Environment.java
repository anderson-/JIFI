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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
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
    private final ArrayList<double[]> obstacleCircles = new ArrayList<>();
    private final ArrayList<double[]> obstacleLines = new ArrayList<>();
    private final ArrayList<double[]> followLineLines = new ArrayList<>();

    public void addObstacleLine(double[] line) {
        obstacleLines.add(line);

        obstacles.add(new Line2D.Double(line[0], line[1], line[2], line[3]));
    }

    public void addObstacleCircle(double[] circle) {
        obstacleCircles.add(circle);
        obstacles.add(new Ellipse2D.Double(circle[0], circle[1], circle[2], circle[2]));
    }

    public void addFollowLine(double[] line) {
        followLineLines.add(line);
        followLines.add(new Line2D.Double(line[0], line[1], line[2], line[3]));
    }

    public void saveFile(File file) throws IOException {
        FileWriter fw = new FileWriter(file.getAbsoluteFile());

        StringBuilder sb = new StringBuilder();
        sb.append("# Environment ").append(System.currentTimeMillis()).append(" #\n");

        if (!obstacleCircles.isEmpty() || !obstacleLines.isEmpty()) {
            sb.append("# Obstacles #\n");
            sb.append("\n");
        }

        if (!obstacleCircles.isEmpty()) {
            for (double[] data : obstacleCircles) {
                sb.append("circle(").append(data[0]).append(", ").append(data[1]).append(", ").append(data[2]).append(")\n");
            }
            sb.append("\n");
        }

        if (!obstacleLines.isEmpty()) {
            for (double[] data : obstacleLines) {
                sb.append("wall(").append(data[0]).append(", ").append(data[1]).append(", ").append(data[2]).append(", ").append(data[3]).append(")\n");
            }
            sb.append("\n");
        }

        if (!followLineLines.isEmpty()) {
            sb.append("# Followable Lines #\n");
            sb.append("\n");
            for (double[] data : followLineLines) {
                sb.append("line(").append(data[0]).append(", ").append(data[1]).append(", ").append(data[2]).append(", ").append(data[3]).append(")\n");
            }
        }

        sb.append("\n");

        fw.write(sb.toString());

        fw.close();
    }

    public void loadFile(InputStream input) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                String str = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                String[] argv = str.split(",");
                if (line.contains("circle") && argv.length == 3) {
                    argv[0] = argv[0].trim();
                    argv[1] = argv[1].trim();
                    argv[2] = argv[2].trim();

                    double x = Double.parseDouble(argv[0]);
                    double y = Double.parseDouble(argv[1]);
                    double r = Double.parseDouble(argv[2]);

                    addObstacleCircle(new double[]{x, y, r});

                } else if (line.contains("wall") && argv.length == 4) {
                    argv[0] = argv[0].trim();
                    argv[1] = argv[1].trim();
                    argv[2] = argv[2].trim();
                    argv[3] = argv[3].trim();

                    double x1 = Double.parseDouble(argv[0]);
                    double y1 = Double.parseDouble(argv[1]);
                    double x2 = Double.parseDouble(argv[2]);
                    double y2 = Double.parseDouble(argv[3]);

                    addObstacleLine(new double[]{x1, y1, x2, y2});

                } else if (line.contains("line") && argv.length == 4) {
                    argv[0] = argv[0].trim();
                    argv[1] = argv[1].trim();
                    argv[2] = argv[2].trim();
                    argv[3] = argv[3].trim();

                    double x1 = Double.parseDouble(argv[0]);
                    double y1 = Double.parseDouble(argv[1]);
                    double x2 = Double.parseDouble(argv[2]);
                    double y2 = Double.parseDouble(argv[3]);

                    addFollowLine(new double[]{x1, y1, x2, y2});
                }
            }
        }
        reader.close();
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
        for (Iterator<Shape> shapeIt = obstaclesTmp.iterator(); shapeIt.hasNext();) {
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
