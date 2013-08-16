/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.cmdpack.begginer;

import algorithm.Command;
import robot.Connection;
import robot.Robot;
import robot.impl.HBridge;
import simulation.ExecutionException;
import util.Clock;
import util.Timer;

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
