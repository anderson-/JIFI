/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot.action.system;

import java.nio.ByteBuffer;
import jifi.robot.Robot;
import jifi.robot.action.Action;

/**
 *
 * @author antunes
 */
public class AddNewDevice extends Action {

    private byte [] deviceData;
    
    public void setDeviceData(byte[] deviceData) {
        this.deviceData = deviceData;
    }
    
    public AddNewDevice() {
        super(true); //uma só mensagem de confimação
    }

    @Override
    public void putMessage(ByteBuffer data, Robot robot) {
        data.put(Robot.CMD_ADD);
        data.put(deviceData);
    }
}
