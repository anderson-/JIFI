/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugins.cmdpack.begginer;

import robotinterface.algorithm.Command;
import robotinterface.robot.connection.Connection;
import robotinterface.robot.Robot;
import robotinterface.robot.device.HBridge;
import robotinterface.interpreter.ExecutionException;
import robotinterface.util.trafficsimulator.Clock;
import robotinterface.util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class Wait extends Command {
    
    private Timer timer;
    
    public Wait (long ms){
        timer = new Timer(ms);
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        timer.reset();
        clock.addTimer(timer);
    }
    
    @Override
    public boolean perform(Robot robot, Clock clock) {
        return timer.isConsumed();
    }
    
}
