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
