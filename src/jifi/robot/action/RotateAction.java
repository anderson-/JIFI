/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot.action;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import jifi.robot.Robot;
import jifi.robot.device.Compass;
import jifi.robot.device.HBridge;

/**
 *
 * @author antunes
 */
public class RotateAction extends Action {

    private int angle = 0;

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }
    
    @Override
    public void putMessage(ByteBuffer data, Robot robot) {
        data.order(ByteOrder.LITTLE_ENDIAN);
        data.put(Robot.CMD_RUN); //comando executar ação
        data.put(getID()); //id da ação
        data.put((byte) 2);//numero de dispositivos utilizados
        HBridge hbridge = robot.getDevice(HBridge.class);
        Compass compass = robot.getDevice(Compass.class);
        if (hbridge == null || compass == null){
            System.out.println("ERRO: Dispositivo não encontrado!");
            return;
        }
        data.put(hbridge.getID());//dispositivo 1
        data.put(compass.getID());//dispositivo 2
        data.put((byte) 3);//tamanho do vetor de dados
        data.putChar((char) angle); //2bytes->int = angulo
        data.put((byte) 2); //precisão
    }
}
