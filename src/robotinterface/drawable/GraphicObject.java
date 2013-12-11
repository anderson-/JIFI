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
package robotinterface.drawable;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import robotinterface.drawable.DrawingPanel.GraphicAttributes;
import robotinterface.drawable.DrawingPanel.InputState;
import java.awt.geom.Rectangle2D;

/**
 * Interface que torna uma classe desenhável por um {@link DrawingPanel}.
 */
public interface GraphicObject extends Drawable {

    public static class SimpleDrawableObject implements GraphicObject {

        private static final AffineTransform transform = new AffineTransform();;
        protected Shape shape;
        protected Rectangle2D.Double bounds;

        public SimpleDrawableObject(Shape shape) {
            this.shape = shape;
            bounds = new Rectangle2D.Double();
            bounds.setRect(shape.getBounds2D());
        }

        @Override
        public Shape getObjectShape() {
            transform.setToIdentity();
            transform.translate(bounds.x, bounds.y);
            return transform.createTransformedShape(shape);
        }

        @Override
        public Rectangle2D.Double getObjectBouds() {
            return bounds;
        }

        @Override
        public void setObjectBounds(double x, double y, double width, double height) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLocation(double x, double y) {
            bounds.x = x;
            bounds.y = y;
        }
        

        @Override
        public double getPosX() {
            return bounds.x;
        }

        @Override
        public double getPosY() {
            return bounds.y;
        }

        @Override
        public int getDrawableLayer() {
            return GraphicObject.DEFAULT_LAYER;
        }

        @Override
        public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
            
        }

        @Override
        public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
            
        }

        @Override
        public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
            
        }
    }

    public Shape getObjectShape();

    public Rectangle2D.Double getObjectBouds();

    public void setObjectBounds(double x, double y, double width, double height);

}
