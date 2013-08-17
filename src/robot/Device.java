/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot;

import java.nio.ByteBuffer;

/**
 *
 * @author antunes
 */
public abstract class Device {
    
    /*
     * Cada dispositivo deve implementar suas proprias funções para
     * enviar comandos para o robô. Veja as funções implementadas em 
     * HBridge.
     */
    
    private byte id;
    private static Connection connection;


    public static void setConnection(Connection connection) {
        Device.connection = connection;
    }
    
    public void setID(int id){
        this.id = (byte) id;
    }
    
    public byte getID(){
        return id;
    }

    public abstract void setState(ByteBuffer data);
    
    public abstract String stateToString ();
    
    /**
     * Define a mensagem padrão a ser enviada para o comando GET.
     * 
     * @param msg Mensagem a ser enviada
     */
    public byte[] defaultGetMessage(){
        return new byte []{};
    }
    
    /**
     * Envia uma mensagem pela interface de comunicação padrão do robô.
     * 
     * @param msg Mensagem a ser enviada
     */
    protected final void send (ByteBuffer msg){
        connection.send(msg);
    }
    
    /**
     * Envia uma mensagem pela interface de comunicação padrão do robô.
     * 
     * @param msg Mensagem a ser enviada
     */
    protected final void send (byte [] msg){
        connection.send(msg);
    }
    
}
