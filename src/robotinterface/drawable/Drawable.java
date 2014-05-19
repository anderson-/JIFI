/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author antunes
 */
public interface Drawable extends Placeable{
    
    //constantes de desenho
    public static Stroke DEFAULT_STROKE  = new BasicStroke(1);
    public static Stroke BOLD_STROKE  = new BasicStroke(5);
    
    public static final int ABSOLUTE = 1;
    public static final int BACKGROUND_LAYER = 2;
    public static final int DEFAULT_LAYER = 4;
    public static final int TOP_LAYER = 8;
    public static final int BORDER = 16;
    
    /*
    * um painter para cada tipo de elemento (fundo, bordas, etc) do componente?
    * como desenhar as camadas? como desenhar os controles swing???
    */
    
    public int getDrawableLayer();

    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in);

    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in);

    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in);
}
