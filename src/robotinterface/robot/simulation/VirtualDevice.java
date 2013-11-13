/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.simulation;

import java.nio.ByteBuffer;
import robotinterface.robot.Robot;

/**
 *
 * @author antunes
 */
public interface VirtualDevice {

    /**
     *
     * @param data
     * @param robot
     */
    public void setState(ByteBuffer data, Robot robot);

    /**
     * Isere dados no comando CMD_SET, emuando um robô real. Os valores
     * inseridos devem ser extraidos apenas do ambiente virtual da simulação.
     *
     * @param buffer
     * @param robot
     */
    public void getState(ByteBuffer buffer, Robot robot);
}
