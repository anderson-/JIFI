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

    public interface Wireable {

        public void wire();
    }

    public void ident(double x, double y, double j, double k, double Ix, double Iy, boolean a);

    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, double j, double k, double Ix, double Iy, boolean a);
}
