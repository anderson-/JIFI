/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.connection.message;

import java.nio.ByteBuffer;
import robotinterface.robot.Robot;
import robotinterface.robot.connection.Connection;
import robotinterface.robot.device.Device;

/**
 *
 * @author antunes
 */
public abstract class Message {

    public class TimeoutException extends Exception {
    }
    
    public static long TIMEOUT = 30;
    public static long MAX_TIMEOUT = 300;//Long.MAX_VALUE
    private static long time = 0;
    private static int receivedPackages = 0;
    private static int lostPackages = 0;
    private boolean received = false;
    private long startReadingTime;
    protected static Connection connection = null;
    private byte id;
    private long tmpTimeout;
    
    public void setID(int id) {
        this.id = (byte) id;
    }

    public byte getID() {
        return id;
    }

    public static void setConnection(Connection connection) {
        Message.connection = connection;
    }

    @Deprecated
    public final void markUnread() { //só usado por Robot.update(...)
        received = true;
    }

    /**
     * Define que esta mensagem está aguardando resposta.
     * A mensagem será aguardada por <code>TIMEOUT</code> antes de ser considerada perdida.
     */
    public final void setWaiting() {
        startReadingTime = System.currentTimeMillis();
        received = false;
        tmpTimeout = TIMEOUT;
    }
    
    /**
     * Define que esta mensagem está aguardando resposta.
     * O Parametro <code>tmpTimeout</code> define o tempo de espera antes que
     * a mensagem seja considerada perdida.
     */
    public final void setWaiting(long tmpTimeout) {
        setWaiting();
        this.tmpTimeout = tmpTimeout;
    }
    
    public long getTimeout() {
        return tmpTimeout;
    }

    /**
     * Define se a mensagem está em um estado válido para a leitura.
     * 
     * 
     * @return
     * @throws robotinterface.robot.connection.message.Message.TimeoutException 
     */
    public final boolean isValidRead() throws TimeoutException {
        if (received) {
            receivedPackages++;
            time += (System.currentTimeMillis() - startReadingTime);
            return true; //retorna com sucesso
        } else if (System.currentTimeMillis() - startReadingTime >= tmpTimeout) {
            lostPackages++;
            throw new TimeoutException(); //notifica a perda da mensagem
        } else {
            return false; //aguarda mais para receber a mensagem
        }
    }

    public static int getLostPackages() {
        return lostPackages;
    }

    public static float getPingEstimative() {
        return (float) time / receivedPackages;
    }
    
    
    /**
     * Envia uma mensagem pela interface de comunicação padrão do robô.
     *
     * @param msg Mensagem a ser enviada
     */
    protected final void send(ByteBuffer msg) {
        connection.send(msg);
    }

    /**
     * Envia uma mensagem pela interface de comunicação padrão do robô.
     *
     * @param msg Mensagem a ser enviada
     */
    protected final void send(byte[] msg) {
        connection.send(msg);
    }
    
    public void updateRobot(Robot robot) {
    }
}
