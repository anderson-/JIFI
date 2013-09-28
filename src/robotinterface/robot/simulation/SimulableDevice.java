/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.simulation;

import java.nio.ByteBuffer;
import robotinterface.gui.panels.SimulationPanel;
import robotinterface.robot.Robot;

/**
 *
 * @author antunes
 */
public interface SimulableDevice {
    
    public ByteBuffer get (ByteBuffer buffer, Robot robot, SimulationPanel panel);
    
}
