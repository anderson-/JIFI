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

    private Shape shape;
    private Color color;
    private AffineTransform transform;

    public SimpleContainer(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
        transform = new AffineTransform();
        Rectangle2D r = shape.getBounds2D();
        super.setObjectBounds(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public SimpleContainer() {
        this(new Rectangle2D.Double(), Color.white);
    }

    @Override
    public Shape getObjectShape() {
        transform.setToIdentity();
        transform.translate(super.bounds.x, super.bounds.y);
        return transform.createTransformedShape(shape);
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
        
        if (!widgetVisible){
            draw2(g, ga, in);
        }
    }
    
    protected void draw2(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        
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
