/**
 * @file .java
 * @author Fernando Padilha Ferreira <fpf.padilhaf@gmail.com>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Fernando Padilha Ferreira <fpf.padilhaf@gmail.com>
 *
 * RobotInterface is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * RobotInterface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RobotInterface. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package robotinterface.plugin.cmdpack.begginer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;


public class DrawableRobot extends JPanel {          
    public boolean girar = false;
    public boolean mover = false;

    @Override
    protected void paintComponent(Graphics g) {

      super.paintComponent(g);

      Graphics2D g2d = (Graphics2D) g;
      AffineTransform t = g2d.getTransform();
      int iSize = (int) this.getWidth();

      t.translate(iSize/2, iSize/2);
      g2d.setTransform(t);
      g2d.setColor(Color.gray);
      //body
      //g2d.drawOval(-5, -5, 10, 10);
      g2d.drawOval(-iSize / 2, -iSize / 2, iSize, iSize);
      //sensores de refletancia
      int sw = iSize / 15;
      int sx = (int)(iSize * .8 / 2);
      int sy = -sw/2;
      AffineTransform t2 = (AffineTransform) t.clone();
      t2.rotate(-3*Math.PI/12);
      g2d.setTransform(t2);
      for (int si = 0; si < 5; si++ ) {
        t2.rotate(Math.PI/12);
        g2d.setTransform(t2);
        g2d.fillOval(sx, sy, sw, sw);
      }
      g2d.setTransform(t);
      //bussola
      Polygon p = new Polygon();
      p.addPoint(-iSize/10, 0); p.addPoint(0, -iSize/5); p.addPoint(iSize/10, 0);
      t2 = (AffineTransform) t.clone();
      //g2d.drawOval(-iSize/5, -iSize/5, 2*iSize/5, 2*iSize/5);
      t2.rotate(Math.PI/2);
      g2d.setTransform(t2);
      g2d.setColor(Color.gray.brighter());
      g2d.fillPolygon(p);
      t2.rotate(Math.PI);
      g2d.setTransform(t2);
      g2d.setColor(Color.gray.darker());
      g2d.fillPolygon(p);
      g2d.setTransform(t);
      //rodas
      g2d.setColor(Color.gray);
      int ww = (int) (0.4 * iSize);
      int wh = (int) (0.2 * iSize);
      int wp = (int) (iSize / 2 - wh) + 1;
      g2d.fillRoundRect(-ww / 2, -iSize / 2 - 1, ww, wh, (int) (iSize * .1), (int) (iSize * .1));
      g2d.fillRoundRect(-ww / 2, wp, ww, wh, (int) (iSize * .1), (int) (iSize * .1));

      if (girar) {
        g2d.setColor(Color.red);
        g2d.drawArc((int)(-.6*iSize/2), (int)(-.6*iSize/2),
                    (int)(.6*iSize), (int)(.6*iSize),
                    0, 270);
        t.translate(0, .6*iSize/2);
        g2d.setTransform(t);
        p = new Polygon();
        p.addPoint(0, -iSize/15); p.addPoint(iSize/8, 0); p.addPoint(0, iSize/15);
        g2d.fillPolygon(p);
      }
      if (mover) {
        g2d.setColor(Color.red);
        t.translate(ww/2, -wp-wh/2);
        g2d.setTransform(t);
        g2d.drawLine(-ww, 0, 0, 0);
        p = new Polygon();
        p.addPoint(-iSize/8, -iSize/15); p.addPoint(0, 0); p.addPoint(-iSize/8, iSize/15);
        g2d.fillPolygon(p);
        t.translate(0, 2*wp+wh);
        g2d.setTransform(t);
        g2d.drawLine(-ww, 0, 0, 0);
        g2d.fillPolygon(p);
      }
    }

}


