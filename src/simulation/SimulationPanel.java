/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

import algorithm.Command;
import gui.QuickFrame;
import gui.drawable.Drawable;
import gui.drawable.DrawingPanel;
import gui.drawable.SwingContainer;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import util.processing.PVector;
import util.trafficsimulator.Clock;
import util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class SimulationPanel extends DrawingPanel {

    private static final int MAX_ARRAY = 50;
    private PVector d;
    private ArrayList<Point> m1pos = new ArrayList<>();
    private ArrayList<Point> m2pos = new ArrayList<>();
    private ArrayList<Point> rpos = new ArrayList<>();
    private double rx = 0;
    private double ry = 0;
    
    private double v1 = 1;
    private double v2 = 1;

    public SimulationPanel() {
        d = PVector.random2D();
        
        //mapeia a posição a cada x ms
        Timer timer = new Timer(100){

            @Override
            public void run() {
                rpos.add(new Point((int)rx,(int)ry));
                while (rpos.size() > MAX_ARRAY){
                    rpos.remove(0);
                }
                 
                if (this.getCount()%10 == 0){
                   d.rotate(1f);
                }
            }
            
        };
        timer.setDisposable(false);
        clock.addTimer(timer);
        clock.setPaused(false);
    }
    
    public static void paintPoints (Graphics2D g, List<Point> points, int size){
        for (Point p : points){
            g.fillRect(p.x-size/2, p.y-size/2, size, size);
        }
    }
    
    public static void paintVector (Graphics2D g, PVector v, double x, double y, int size){
        AffineTransform o = g.getTransform();
        AffineTransform t = new AffineTransform(o);
        t.translate(x, y);
        t.rotate(v.heading()-Math.PI/4.0);
        System.out.println(v.heading());
        g.setTransform(t);
        g.drawLine(0, 0, size, size);
        g.setTransform(o);
    }
    
    public static double getAcceleration (double v1, double v2){
        return v1-v2;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
        
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
        
    }

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
        paintPoints(g,m1pos,2);
        paintPoints(g,m2pos,2);
        paintPoints(g,rpos,2);
        paintVector(g,d,rx,ry,10);
        PVector w = d.get();
        w.normalize();
        double dt = clock.getDt();
        double R = (v1*v1)/(dt*dt) + (v2*v2)/(dt*dt);
        if (R != 0){
            
        }
        
        
//        rx += .5*w.x;
//        ry += .5*w.y;
        g.fillOval(30, 30, 30, 30);
    }
    
    

    public static void main(String[] args) {
        SimulationPanel p = new SimulationPanel();
        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);
    }
}
