/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.device;

import robotinterface.robot.connection.Connection;
import java.nio.ByteBuffer;
import robotinterface.robot.Robot;

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
    public class TimeoutException extends Exception {
    }
    public static long TIMEOUT = 25; //alterado para 25 se utilizar librxtxSerial.so recompilado
    private static long time = 0;
    private static int receivedPackages = 0;
    private static int lostPackages = 0;
    private byte id;
    private static Connection connection;
    private boolean received = false;
    private long startReadingTime;

    @Deprecated
    public final void markUnread() { //só usado por Robot.update(...)
        received = true;
    }

    public final void setWaiting() {
        startReadingTime = System.currentTimeMillis();
        received = false;
    }

    public final boolean isValidRead() throws TimeoutException {
        if (received) {
            receivedPackages++;
            time += (System.currentTimeMillis() - startReadingTime);
            return true;
        } else if (System.currentTimeMillis() - startReadingTime >= TIMEOUT) {
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

    public static void setConnection(Connection connection) {
        Device.connection = connection;
    }

    public void setID(int id) {
        this.id = (byte) id;
    }

    public byte getID() {
        return id;
    }

    public abstract String getName();

    public abstract int getClassID();

    public abstract void setState(ByteBuffer data);

    public abstract String stateToString();

    /**
     * Define a mensagem padrão a ser enviada para o comando GET.
     *
     * @param msg Mensagem a ser enviada
     */
    public byte[] defaultGetMessage() {
        return new byte[]{};
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
