/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import robotinterface.drawable.GraphicObject;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class DummyBlock extends Procedure {

    {
        setProcedure("<VAZIO>");
    }
    
    @Override
    public GraphicObject getDrawableResource() {
        return super.getDrawableResource();
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        
    }

    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        return true;
    }
    
    @Override
    public Item getItem() {
        return new Item("Dummy", new Rectangle2D.Double(0, 0, 20, 15), Color.red);
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
