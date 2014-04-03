/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class DummyBlock extends Procedure {

    public static GraphicObject createSimpleBlock(final String str, final Color strColor, final Color color) {
        MutableWidgetContainer mwc = new MutableWidgetContainer(color) {

            {
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

        };
        mwc.setWidgetsEnebled(false);
        return mwc;
    }
    
    private GraphicObject resource = null;
    
    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createSimpleBlock("      -      ", Color.LIGHT_GRAY, Color.LIGHT_GRAY);
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
