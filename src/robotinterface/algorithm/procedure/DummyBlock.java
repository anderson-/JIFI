/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import robotinterface.algorithm.Command;
import robotinterface.drawable.Drawable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class DummyBlock extends Procedure {

//    @Override
//    public Item getItem() {
//        return null;
//    }

    @Override
    public Object createInstance() {
        return new DummyBlock();
    }

    @Override
    public Drawable getDrawableResource() {
        setProcedure("<DUMMY>");
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
    public void setNext(Command next) {
        if (next != null && !(next instanceof Block.BlockEnd)){
            super.remove();
        }
    }

    @Override
    public void setPrevious(Command previous) {
        if (previous != null && !(previous instanceof Block)){
            super.remove();
        }
    }

    @Override
    public void setParent(Command parent) {
        super.setParent(parent); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addBefore(Command c) {
        super.remove();
        return true;
    }

    @Override
    public boolean addAfter(Command c) {
        super.remove();
        return true;
    }

    @Override
    public void remove() {
        super.remove(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
