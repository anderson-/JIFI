/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugin.cmdpack.serial;

import robotinterface.algorithm.Command;
import robotinterface.robot.Robot;
import robotinterface.interpreter.ExecutionException;
import robotinterface.util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class Start extends Command {
    
    private int attempts = 0;
    private static final int MAX_ATTEMPTS = 5;
    
    public Start(){
        super();
    }

    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        if (robot.getMainConnection().establishConnection()){
            return true;
        } else if (attempts < MAX_ATTEMPTS){
            attempts++;
            //implementar delay!!
            return false;
        } else {
            attempts = 0;
            throw new ExecutionException("Communication Start Fail");
        }
    }
    
    
    
}
