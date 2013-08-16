/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.cmdpack.begginer;

import algorithm.procedure.Procedure;
import java.util.ArrayList;
import java.util.Arrays;
import org.nfunk.jep.Variable;
import robot.Device;
import robot.Robot;
import simulation.ExecutionException;
import util.Clock;
import util.Timer;

/**
 *
 * @author antunes
 */

public class ReadSensor extends Procedure {

    private Timer timer;
    private Device device;
    private Class<? extends Device> type;

    public ReadSensor(Class<? extends Device> type) {
        this.type = type;
        timer = new Timer(200);
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        device = robot.getDevice(type);
        if (device != null) {
            robot.getMainConnection().send(device.request());
        }
        timer.reset();
        clock.addTimer(timer);
    }
    
    @Override
    public boolean perform(Robot r, Clock clock) throws ExecutionException {
        if (timer.isConsumed()){
            if (device != null) {
                execute(device.getState());
            }
            return true;
        }
        return false;
    }
}
