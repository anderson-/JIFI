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
import robotinterface.drawable.DrawingPanel.GraphicAttributes;
import robotinterface.drawable.DrawingPanel.InputState;
import java.awt.geom.Rectangle2D;

/**
 * Interface que torna uma classe desenhável por um {@link DrawingPanel}.
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
