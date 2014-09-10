/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.magenta.core;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author anderson
 */
public interface ShapeCreator {

    public static final ShapeCreator DEFAULT = new ShapeCreator() {

        @Override
        public Shape create(Rectangle2D.Double bounds) {
            return new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    };

    public static final ShapeCreator DIAMOND = new ShapeCreator() {

        public static final int EXTENDED_HEIGHT = 15;
        public static final int SIMPLE_HEIGHT = 18;
        public static final int SIMPLE_WIDTH = 22;

        @Override
        public Shape create(Rectangle2D.Double bounds) {
            Polygon myShape = new Polygon();

            int shapeStartX = 0;
            int shapeStartY = 0;

            if (true) {
                shapeStartX = 0;
                shapeStartY = EXTENDED_HEIGHT;
                myShape.addPoint((int) bounds.getCenterX(), 0);
                myShape.addPoint((int) bounds.getMaxX(), EXTENDED_HEIGHT);
                myShape.addPoint((int) bounds.getMaxX(), (int) bounds.getMaxY() + EXTENDED_HEIGHT);
                myShape.addPoint((int) bounds.getCenterX(), (int) bounds.getMaxY() + 2 * EXTENDED_HEIGHT);
                myShape.addPoint(0, (int) bounds.getMaxY() + EXTENDED_HEIGHT);
                myShape.addPoint(0, EXTENDED_HEIGHT);
            } else {
                shapeStartX = SIMPLE_WIDTH;
                shapeStartY = SIMPLE_HEIGHT;

                myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, 0);
                myShape.addPoint((int) bounds.getMaxX() + 2 * SIMPLE_WIDTH, (int) bounds.getCenterY());
                myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, (int) bounds.getMaxY());
                myShape.addPoint(0, (int) bounds.getCenterY() + SIMPLE_HEIGHT);
            }
            return myShape;
        }
    };

    public Shape create(Rectangle2D.Double bounds);

}
