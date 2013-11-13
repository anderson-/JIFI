/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import robotinterface.algorithm.Command;
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

    @Override
    public GraphicObject getDrawableResource() {
        setProcedure("<VAZIO>");
        return super.getDrawableResource();
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        
    }

    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        return true;
    }
//
//    @Override
//    public void setNext(Command next) {
////        if (next != null && !(next instanceof Block.BlockEnd)){
////            super.remove();
////        }
//        super.setNext(next);
//    }
//
//    @Override
//    public void setPrevious(Command previous) {
////        if (previous != null && !(previous instanceof Block)){
////            super.remove();
////        }
//        super.setPrevious(previous);
//    }
//
//    @Override
//    public void setParent(Command parent) {
//        super.setParent(parent); //To change body of generated methods, choose Tools | Templates.
//    }
//
////    @Override
////    public boolean addBefore(Command c) {
////        super.addBefore(c);
////        super.remove();
////        return true;
////    }
////
////    @Override
////    public boolean addAfter(Command c) {
////        super.addAfter(c);
////        super.remove();
////        return true;
////    }
//
////    @Override
////    public void remove() {
////        super.remove(); //To change body of generated methods, choose Tools | Templates.
////    }
    
    @Override
    public Item getItem() {
        return new Item("Dummy", new Rectangle2D.Double(0, 0, 20, 15), Color.red);
    }

    @Override
    public Object createInstance() {
        return new DummyBlock();
    }
    
}
