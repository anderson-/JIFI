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

    public final void setWaiting() {
        startReadingTime = System.currentTimeMillis();
        received = false;
        tmpTimeout = TIMEOUT;
    }
    
    public final void setWaiting(long tmpTimeout) {
        setWaiting();
        this.tmpTimeout = tmpTimeout;
    }
    
    public long getTimeout() {
        return tmpTimeout;
    }

    public final boolean isValidRead() throws TimeoutException {
        if (received) {
            receivedPackages++;
            time += (System.currentTimeMillis() - startReadingTime);
            return true;
        } else if (System.currentTimeMillis() - startReadingTime >= tmpTimeout) {
            lostPackages++;
            throw new TimeoutException();
        } else {
            return false;
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
