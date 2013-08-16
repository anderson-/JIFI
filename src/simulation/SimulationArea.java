/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import gui.drawable.DRobot;
import util.Clock;

/**
 *
 * @author Anderson
 */
public class SimulationArea extends JPanel implements Runnable, KeyListener, MouseWheelListener, java.io.Serializable{

    private Clock clock;
    private DRobot robo;
    private int comando;
    private final ArrayList<Integer> keys;

    public SimulationArea() {
        super(true);
        clock = new Clock();
        robo = new DRobot(clock);
        robo.mover(2*4*(float)Math.PI);
        robo.girar(2*4*(float)Math.PI);
        comando = 1;
        keys = new ArrayList<>();
        this.setPreferredSize(new Dimension(640, 480));
        //this.addKeyListener(this);
        new Thread(this).start();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Percurso do Robo 1.0");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SimulationArea area = new SimulationArea();

        frame.add(area);
        frame.addKeyListener(area);
        frame.addMouseWheelListener(area);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

//        new Thread(area).start();
    }

    @Override
    public void run() {
        clock.setPaused(false);
        while (!Thread.interrupted()) {
            repaint();
            try {
                Thread.sleep(2);
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected synchronized void paintComponent(Graphics g1) {
        clock.increase();
        Graphics2D g = (Graphics2D) g1;

        if (comando != 0) {
            switch (comando) {
                case 1:
                    g.translate(this.getWidth() / 2, this.getHeight() / 2);
                    break;
            }
            comando = 0;// velocidade[m/s] * 1/100[s/ms] * prop[x/m] * dt[ms] = dX [x]
//        float dX = (float) (velocidade * 0.01f * prop * clock.getDt());
//        X += dX;
//        // X[x] / W[x] = parametro[](0.0f->1.0f)
//        float parametro = X / W;
//        x = (1.0f - parametro) * (float) caminho.getP1().getX() + parametro * (float) caminho.getP2().getX();
//        y = (1.0f 
        }


        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(Color.LIGHT_GRAY);
        g.translate(this.getWidth() / 2, this.getHeight() / 2);
        drawGrade(g);
        robo.drawCaminho(g);
        robo.drawObstaculos(g);
        robo.draw(g);

        synchronized (keys) {
            if (!keys.isEmpty()) {
                for (int key : keys) {
                    switch (key) {
                        case KeyEvent.VK_UP:
                            robo.mover(1e-3f);
                            break;
                        case KeyEvent.VK_DOWN:
                            robo.mover(-1e-3f);
                            break;
                        case KeyEvent.VK_RIGHT:
                            robo.girar(0.09f);
                            break;
                        case KeyEvent.VK_LEFT:
                            robo.girar(-0.09f);
                            break;
                    }
                }
            }
        }

        g.dispose();
    }
    int grid = 3;

    public void drawGrade(Graphics2D g) {
        if (grid > 0) {

            int prop = (int) robo.prop / grid;

            for (int x = -(this.getWidth() / prop) / 2; x <= (this.getWidth() / prop) / 2; x++) {
                g.drawLine(x * prop, -this.getHeight() / 2, x * prop, this.getHeight() / 2);
            }

            for (int y = -(this.getHeight() / prop) / 2; y <= (this.getHeight() / prop) / 2; y++) {
                g.drawLine(-this.getWidth() / 2, y * prop, this.getWidth() / 2, y * prop);
            }

        } else if (grid < 0) {
            int prop = (int) robo.prop / -grid;

            for (int x = -(this.getWidth() / prop) / 2; x <= (this.getWidth() / prop) / 2; x++) {
                for (int y = -(this.getHeight() / prop) / 2; y <= (this.getHeight() / prop) / 2; y++) {
                    g.fillRect(x * prop - 1, y * prop - 1, 2, 2);
                }
            }

        } else {
            return;
        }
        String str = "grade: " + Math.abs(1.0f/grid)*100 + " cm";
        int sx = g.getFontMetrics().stringWidth(str);
        int sy = g.getFontMetrics().getHeight();
        int px = this.getWidth()/2-10-sx;
        int py = this.getHeight()/2-20;
        g.setColor(Color.lightGray);
        g.fillRect(px, py-11, sx, sy);
        g.setColor(Color.white);
        g.drawString(str, px, py);
        
    }

    @Override
    public void keyTyped(KeyEvent e) {
        synchronized (keys) {
            if (!keys.contains(e.getKeyCode())) {
                keys.add(e.getKeyCode());
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (keys) {
            if (!keys.contains(e.getKeyCode())) {
                keys.add(e.getKeyCode());
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (keys) {
            keys.remove((Integer) e.getKeyCode());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        grid += e.getWheelRotation();
    }
}
