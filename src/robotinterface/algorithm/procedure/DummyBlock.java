/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import robotinterface.algorithm.Command;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.DrawableProcedureBlock;
import robotinterface.drawable.swing.MutableWidgetContainer;

/**
 *
 * @author antunes
 */
public class DummyBlock extends Procedure {

    public static final int SHAPE_RECTANGLE = 0;
    public static final int SHAPE_ROUND_RECTANGLE = 1;
    public static final int SHAPE_CIRCLE = 2;

    public static GraphicObject createSimpleBlock(Command c, final String str, final Color strColor, final Color color, final int shapeType) {
        MutableWidgetContainer mwc = new DrawableProcedureBlock(c, color) {

            Shape shape;

            {
                if (shapeType == SHAPE_ROUND_RECTANGLE) {
                    shape = new RoundRectangle2D.Double();
                } else if (shapeType == SHAPE_CIRCLE) {
                    shape = new Ellipse2D.Double();
                } else {
                    shape = new Rectangle2D.Double();
                }
                super.boxLabelColor = strColor;
            }

            @Override
            public String getBoxLabel() {
                return str;
            }

            @Override
            public void splitBoxLabel(String original, Collection<String> splitted) {
                splitted.add(original);
            }

            @Override
            public Shape updateShape(Rectangle2D bounds) {

                if (shapeType == SHAPE_ROUND_RECTANGLE) {
                    RoundRectangle2D.Double s = ((RoundRectangle2D.Double) shape);
                    s.archeight = s.arcwidth = 20;
                    s.setFrame(bounds);
                } else if (shapeType == SHAPE_CIRCLE) {
                    Ellipse2D.Double s = ((Ellipse2D.Double) shape);
                    s.setFrame(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getWidth());
                } else {
                    Rectangle2D.Double s = ((Rectangle2D.Double) shape);
                    s.setRect(bounds);
                }

                return shape;
            }

        };
        mwc.setWidgetsEnebled(false);
        return mwc;
    }

    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createSimpleBlock(this, "      -      ", Color.LIGHT_GRAY, Color.LIGHT_GRAY, SHAPE_ROUND_RECTANGLE);
        }
        return resource;
    }

    @Override
    public Object createInstance() {
        return new DummyBlock();
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append("\n");
    }
}
