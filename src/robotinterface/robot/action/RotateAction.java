/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.action;

import java.nio.ByteBuffer;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;

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
        data.put(Robot.CMD_RUN); //comando executar ação
        data.put((byte)1); //id da ação getID()
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
        
        ByteBuffer tmpBuffer = ByteBuffer.allocate(2);
        tmpBuffer.putChar((char) angle);
        byte[] tmp = new byte[2];
        tmpBuffer.flip();
        tmpBuffer.get(tmp);
        
        data.put(tmp[1]);
        data.put(tmp[0]);
        
        data.put((byte) 10); //precisão
    }
}
