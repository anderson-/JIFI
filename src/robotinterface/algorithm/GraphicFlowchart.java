/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 *
 * @author antunes
 */
public interface GraphicFlowchart {

    public static int GF_X = 0; //posição x
    public static int GF_Y = 0; //posição y
    public static int GF_J = 15; //deslocamento vertical
    public static int GF_K = 50; //deslocamento horizontal

    public void ident(double x, double y, double j, double k);

    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, double j, double k);
}
