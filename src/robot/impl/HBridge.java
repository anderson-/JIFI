/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot.impl;

import java.nio.ByteBuffer;
import robot.Connection;
import robot.Device;
import robot.Robot;

/**
 *
 * @author antunes
 */
public class HBridge extends Device {

    private byte[] msg;

    public HBridge(int id) {
        msg = new byte[5];
    }

    @Override
    public void setState(ByteBuffer data) {
    }

    public void setMotorState(int motor, byte speed) {
        msg[0] = Robot.CMD_SET; //comando get
        msg[1] = getID(); //id
        msg[2] = 2; //tamanho da mensagem (2 bytes)
        msg[3] = (byte) motor; //byte 1 - motor
        msg[4] = speed; //byte 2 - velocidade
        send(msg); //envia mensagem
    }

    public void setFullState(byte speedM1, byte speedM2) {
        msg[0] = Robot.CMD_SET; //comando get
        msg[1] = getID(); //id
        // ... 
        msg[3] = 0;
        msg[4] = speedM1;
        send(msg);
        msg[3] = 1;
        msg[4] = speedM2;
        send(msg);
    }

    @Override
    public String stateToString() {
        return "";
    }
}
