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
public class ResetSystem extends Action {

    public ResetSystem() {
        super(true); //uma só mensagem de confimação
    }

    @Override
    public void putMessage(ByteBuffer data, Robot robot) {
        data.put(Robot.CMD_RESET);
        data.put(Robot.XTRA_SYSTEM);
    }
    
}
