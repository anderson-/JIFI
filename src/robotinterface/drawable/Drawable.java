/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable;

import java.awt.Graphics2D;
import java.awt.Shape;
import robotinterface.drawable.DrawingPanel.GraphicAttributes;
import robotinterface.drawable.DrawingPanel.InputState;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author antunes
 */
public interface Drawable {

    public static final int BACKGROUND_LAYER = 1;
    public static final int DEFAULT_LAYER = 2;
    public static final int TOP_LAYER = 4;

    public Shape getObjectShape();

    public Rectangle2D.Double getObjectBouds();

    public void setObjectBounds(double x, double y, double width, double height);
    
    public void setObjectLocation(double x, double y);

    public int getDrawableLayer();

    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in);

    public void draw(Graphics2D g, GraphicAttributes ga, InputState in);

    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in);
}
