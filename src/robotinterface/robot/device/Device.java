/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.device;

import robotinterface.robot.connection.Connection;
import java.nio.ByteBuffer;
import robotinterface.robot.Robot;
import robotinterface.robot.connection.message.Message;

/**
 *
 * @author antunes
 */
public abstract class Device extends Message {

    

    public abstract String getName();
    /*
     * Cada dispositivo deve implementar suas proprias funções para
     * enviar comandos para o robô. Veja as funções implementadas em 
     * HBridge.
     */
    public abstract int getClassID();

    public abstract void setState(ByteBuffer data);

    public abstract String stateToString();
    
    public abstract void resetState();

    /**
     * Define a mensagem padrão a ser enviada para o comando GET.
     *
     * @param msg Mensagem a ser enviada
     */
    public byte[] defaultGetMessage() {
        return new byte[]{};
    }
}
