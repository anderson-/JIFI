/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Stack;
import util.trafficsimulator.Clock;
import util.trafficsimulator.Timer;

/**
 *
 * @author Anderson
 */
public class DRobot {

    // desenho
    private final int bodyR = 70;
    public float prop = 100f; //[x/m] ex: prop = 10 -> 1m = 10x
    // posição e rotação
    private float x = 0f;
    private float y = 0f;
    private float theta = 0f;
    // movimento andar
    private boolean andando = false;
    private float velocidade = 1;//0.5f; // [m/s]
//    private float deslocamento = 0f;
    private float objetivo = 0f;
    // movimento girar
    private boolean girando = false;
    private float velocidadeAngular = 1;//0.5f; // [rad/s]
    private float objetivoAngular = 0f;
    // encoder
    //private long steps = 0;
    //private final float step = (float)Math.PI*5.0f;
    // etc
    private Clock clock;
    private AffineTransform t;
    private Stack<Line2D> caminhos;
    private ArrayList<Point> obstaculos;
    private float W; // comprimento do caminho atual
    private float X; // deslocamento no caminho atual

    public DRobot(Clock c) {
        caminhos = new Stack<>();
        obstaculos = new ArrayList<>();
        clock = c;

        clock.addTimer(new Timer(200) {
            @Override
            public void run() {
                addObstaculo(100);
            }
        });
    }

    public void draw(Graphics2D g) {
        AffineTransform old = g.getTransform();
        t = new AffineTransform(old);
        t.translate(x, y);
        t.rotate(theta);
        g.setTransform(t);
        g.setColor(Color.gray);
        g.drawOval(-5, -5, 10, 10);
        g.drawOval(-bodyR / 2, -bodyR / 2, bodyR, bodyR);
        g.setColor((andando || girando) ? (andando) ? Color.GREEN : Color.ORANGE : Color.RED);
        g.fillRect(bodyR / 2 - 5, -bodyR / 2 + 10, 5, bodyR - 20);
        g.setColor(Color.black);
        g.drawRect(bodyR / 2 - 5, -bodyR / 2 + 10, 5, bodyR - 20);
        g.fillRect(-10, -bodyR / 2, 20, 10);
        g.fillRect(-10, +bodyR / 2 - 9, 20, 10);
        g.setTransform(old);
        move();
    }

    public void drawCaminho(Graphics2D g) {
        g.setColor(Color.green);
        for (Line2D l : caminhos) {
            drawSeta(l.getP1(), l.getP2(), g);
        }
    }

    public void drawObstaculos(Graphics2D g) {
        g.setColor(Color.red);
        for (Point p : obstaculos) {
//            g.drawOval(p.x - 10, p.y - 10, 20, 20);
            g.fillRect(p.x - 1, p.y - 1, 2, 2);
        }
    }

    public void girar(float alpha) {
        objetivoAngular = alpha;
        girando = true;
    }

    public void mover(float metros) {
        System.out.println(objetivo);
        objetivo += metros;
        andando = true;
    }

    private void move() {


        if (andando) {
            //[x] = [m/s] * [x/m] * [s]
            float dx = velocidade * prop * (float) clock.getDt();
            int s = (objetivo > 0f)? 1 : -1;
//            System.out.println(objetivo * prop + " " + dx);
            
            if (s*objetivo * prop - dx <= 0f) {
                dx = s*objetivo * prop;
                objetivo = 0f;
                andando = false;
            }

            x += s*dx * (float) Math.cos(theta);
            y += s*dx * (float) Math.sin(theta);
            objetivo -= s*dx/prop;
        }

        if (girando) {

            float dw = velocidadeAngular * (float) clock.getDt();
            int s = (objetivoAngular > 0f)? 1 : -1;
            if (s*objetivoAngular - dw <= 0f) {
                dw = s*objetivoAngular;
                objetivoAngular = 0f;
                girando = false;
            }

            theta += s*dw;
            objetivoAngular -= s*dw;
        }
    }
    
    public boolean isAguardando (){
        return !(andando || girando);
    }

    public void addObstaculo(float distancia) {
        float tx = x + distancia * (float) Math.cos(theta);
        float ty = y + distancia * (float) Math.sin(theta);
        Point p = new Point((int) tx, (int) ty);
        obstaculos.add(p);
    }

    private void drawSeta(Point2D p1, Point2D p2, Graphics2D g) {
        g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
    }
}
