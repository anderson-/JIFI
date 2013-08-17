/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.cmdpack.serial;

import algorithm.Command;
import robot.Robot;
import simulation.ExecutionException;
import util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class Stop extends Command {

    public Stop() {
        super();
    }

    @Override
    public boolean perform(Robot robot, Clock clock) {
        robot.getMainConnection().closeConnection();
        return true;
    }
}
