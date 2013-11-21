/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.action.system;

import java.nio.ByteBuffer;
import robotinterface.robot.Robot;
import robotinterface.robot.action.Action;

/**
 *
 * @author antunes
 */
public class UpdateAllDevices extends Action {

    public UpdateAllDevices() {
        super(true); //uma só mensagem de confimação
    }

    @Override
    public void putMessage(ByteBuffer data, Robot robot) {
        data.put(Robot.CMD_GET);
        data.put(Robot.XTRA_ALL);
        data.put((byte) 0);
    }
}
