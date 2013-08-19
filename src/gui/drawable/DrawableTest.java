/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.JButton;
import util.trafficsimulator.ColorChanger;

/**
 *
 * @author antunes
 */
public class DrawableTest extends SwingContainer {

    public static class Circle implements Drawable {

        Rectangle2D.Double bounds = new Rectangle2D.Double();

        @Override
        public Shape getObjectShape() {
            return bounds;
        }

        @Override
        public Rectangle2D.Double getObjectBouds() {
            return bounds;
        }

        @Override
        public void setObjectLocation(double x, double y) {
            bounds.x = x;
            bounds.y = y;
        }

        @Override
        public void setObjectBounds(double x, double y, double width, double height) {
            bounds.x = x;
            bounds.y = y;
            bounds.width = width;
            bounds.height = height;
        }

        @Override
        public int getDrawableLayer() {
            return DrawingPanel.DEFAULT_LAYER;
        }

        @Override
        public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        }

        @Override
        public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
            //desenha uma bolinha
            g.setColor(Color.gray);
            g.fillOval(0, 0, 30, 30);
        }

        @Override
        public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        }
    }
    private ColorChanger cc = new ColorChanger(Color.CYAN, 0.5f);

    public DrawableTest() {
        setObjectBounds(100, 100, 300, 300);
        JButton b = new JButton("Botão1");
        addJComponent(b, 50, 0, 200, 100);
        b = new JButton("Botão2");
        addJComponent(b, 50, 200, 200, 100);
    }

    @Override
    public int getDrawableLayer() {
        return DrawingPanel.DEFAULT_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        if (in.isMouseOver()) {
            g.setColor(Color.RED);
        } else {
            if (in.isKeyPressed(KeyEvent.VK_4)) {
                g.setColor(Color.YELLOW);
            } else {
                switch (in.getSingleKey()) {
                    case KeyEvent.VK_1:
                        g.setColor(Color.ORANGE);
                        break;
                    case KeyEvent.VK_2:
                        g.setColor(Color.GREEN);
                        break;
                    case KeyEvent.VK_3:
                        g.setColor(Color.BLUE);
                        break;
                    default:

                        g.setColor(cc.getColor());
                }
            }
        }
        g.fillRect(0, 0, (int)getObjectBouds().width, (int)getObjectBouds().height);
        g.setColor(Color.white);
        drawBallOrbitingCenter(g, 300, 300);
    }
    
    public static void drawBallOrbitingCenter(Graphics2D g, int width, int height) {
        double time = 2 * Math.PI * (System.currentTimeMillis() % 10000) / 10000.;
        g.fillOval((int) (Math.sin(time) * width / 3 + width / 2 - 20), (int) (Math.cos(time) * height / 3 + height / 2) - 20, 40, 40);
    }


    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }
}
