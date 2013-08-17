/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.drawable;

import algorithm.Command;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import gui.drawable.DrawingPanel.GraphicAttributes;
import gui.drawable.DrawingPanel.InputState;

/**
 *
 * @author antunes
 */
public interface Drawable {

    public void setX(int x);

    public void setY(int y);

    public int getX();

    public int getY();
    
    public int getWidth();

    public int getHeight();

    public Rectangle getBounds();

    public void setLocation(int x, int y);

    public void setSize(int width, int height);

    public void setBounds(int x, int y, int width, int height);

    public Shape getShape();

    public boolean isVisible();

    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in);

    public void draw(Graphics2D g, GraphicAttributes ga, InputState in);

    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in);

}
