/**
 * @file .java
 * @author Anderson Antunes <anderson.utf@gmail.com>
 *         *seu nome* <*seu email*>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Anderson Antunes <anderson.utf@gmail.com>
 *                       *seu nome* <*seu email*>
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
package robotinterface.drawable.graphicresource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import robotinterface.drawable.DWidgetContainer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;

/**
 * Implementação de DWidgetContainer para facilitar o desenho de objetos
 * gráficos.
 */
public class SimpleContainer extends DWidgetContainer {

    private Color color;

    public SimpleContainer(Shape shape, Color color) {
        super(shape);
        widgetVisible = false;
        this.color = color;
    }
    
    @Override
    public int getDrawableLayer() {
        return Drawable.DEFAULT_LAYER;
    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        if(in.mouseClicked()){
            super.widgetVisible = !super.widgetVisible;
        }

        g.setColor(color);
        g.fill(shape);
        
        if (widgetVisible){
            drawWJC(g, ga, in);
        } else {
            drawWoJC(g, ga, in);
        }
    }
    
    protected void drawWJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        
    }
    
    protected void drawWoJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        
    }
    
    boolean w = false;

    public static Shape createDiamond(Rectangle2D r) {
        Polygon p = new Polygon();

        p.addPoint((int) r.getCenterX(), 0);
        p.addPoint(0, (int) r.getCenterY());
        p.addPoint((int) r.getCenterX(), (int) r.getHeight());
        p.addPoint((int) r.getWidth(), (int) r.getCenterY());

        return p;
    }
}
