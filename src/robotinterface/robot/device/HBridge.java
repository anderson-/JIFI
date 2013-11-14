/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.device;

import java.nio.ByteBuffer;
import robotinterface.robot.Robot;
import robotinterface.robot.simulation.VirtualDevice;

/**
 *
 * @author antunes
 */
public class HBridge extends Device {

    private byte[] msg;
    private int LeftWheelSpeed;
    private int RightWheelSpeed;

    public HBridge() {
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
        if (motor == 0){
            this.LeftWheelSpeed = speed;
        } else {
            this.RightWheelSpeed = speed;
        }
    }

    public void setFullState(byte speedM1, byte speedM2) {
        msg[0] = Robot.CMD_SET; //comando get
        msg[1] = getID(); //id
        msg[2] = 2; //tamanho da mensagem (2 bytes)
        // ... 
        msg[3] = 0;
        msg[4] = speedM1;
        send(msg);
        msg[3] = 1;
        msg[4] = speedM2;
        send(msg);
        this.LeftWheelSpeed = speedM1;
        this.RightWheelSpeed = speedM2;
    }

    public int getLeftWheelSpeed() {
        return LeftWheelSpeed/2;
    }

    public int getRightWheelSpeed() {
        return RightWheelSpeed/2;
    }

    @Override
    public String stateToString() {
        return "";
    }

    @Override
    public int getClassID() {
        return 2;
    }
    
    @Override
    public void updateRobot(Robot robot) {
        robot.setRightWheelSpeed(RightWheelSpeed);
        robot.setLeftWheelSpeed(LeftWheelSpeed);
    }

    @Override
    public String getName() {
        return "Motores";
    }
}
