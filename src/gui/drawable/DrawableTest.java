/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.drawable;

import algorithm.Command;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import util.trafficsimulator.ColorChanger;

/**
 *
 * @author antunes
 */
public class DrawableTest extends SwingContainer {

    public static class Circle implements Drawable {
        
        Rectangle bounds = new Rectangle();
        
        @Override
        public void setX(int x) {
            bounds.x = x;
        }

        @Override
        public void setY(int y) {
            bounds.y = y;
        }

        @Override
        public int getX() {
            return bounds.x;
        }

        @Override
        public int getY() {
            return bounds.y;
        }

        @Override
        public int getWidth() {
            return bounds.width;
        }

        @Override
        public int getHeight() {
            return bounds.height;
        }

        @Override
        public Rectangle getBounds() {
            return bounds;
        }

        @Override
        public void setLocation(int x, int y) {
            bounds.setLocation(x, y);
        }

        @Override
        public void setSize(int width, int height) {
            bounds.setSize(width, height);
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            bounds.setBounds(x, y, width, height);
        }

        @Override
        public Shape getShape() {
            return bounds;
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
            
        }

        @Override
        public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
            g.fillRect(-10, -10, 100, 100);
            g.setColor(Color.gray);
            g.fillOval(0, 0, 30, 30);
        }

        @Override
        public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
            
        }
    }
    
    private ColorChanger cc = new ColorChanger(Color.CYAN, 0.5f);
    
    public DrawableTest() {
        setBounds(100, 100, 300, 300);
        JButton b = new JButton("HelloWorld");
        addJComponent(b, 50,0, 200,100);
        b = new JButton("HelloWorld2");
        addJComponent(b, 50,200, 200,100);
    }

    @Override
    public Shape getShape() {
        return getBounds();
    }

    @Override
    public Command getCommand() {
        return null;
    }

    @Override
    public boolean hasBackground() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean hasTopLayer() {
        return false;
    }

    @Override
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        if (in.isMouseOver()){
            g.setColor(Color.RED);
        } else {
            if (in.isKeyPressed(KeyEvent.VK_4)){
                g.setColor(Color.YELLOW);
            } else {
                switch (in.getSingleKey()){
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
        g.fillRect(0, 0, 1300, 1300);
        DrawingPanel.drawBallOrbitingCenter(g, 300, 300);
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }
    
}
