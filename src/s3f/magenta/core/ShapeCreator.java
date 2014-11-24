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
            return new Rectangle2D.Double(0, 0, bounds.width, bounds.height);
        }
    };

    public static final ShapeCreator DIAMOND = new ShapeCreator() {

        public static final int EXTENDED_HEIGHT = 15;
        public static final int SIMPLE_WIDTH = 22;

        @Override
        public Shape create(Rectangle2D.Double bounds) {
            Polygon myShape = new Polygon();

            if (true) {
                myShape.addPoint((int) bounds.width / 2, -EXTENDED_HEIGHT);
                myShape.addPoint((int) bounds.width, 0);
                myShape.addPoint((int) bounds.width, (int) bounds.height);
                myShape.addPoint((int) bounds.width / 2, (int) bounds.height + EXTENDED_HEIGHT);
                myShape.addPoint(0, (int) bounds.height);
                myShape.addPoint(0, EXTENDED_HEIGHT - EXTENDED_HEIGHT);
            } else {
                myShape.addPoint((int) bounds.width / 2 + SIMPLE_WIDTH, 0);
                myShape.addPoint((int) bounds.width + 2 * SIMPLE_WIDTH, (int) bounds.height / 2);
                myShape.addPoint((int) bounds.width / 2 + SIMPLE_WIDTH, (int) bounds.height);
                myShape.addPoint(0, (int) bounds.height / 2);
            }
            return myShape;
        }
    };

    public Shape create(Rectangle2D.Double bounds);

}
