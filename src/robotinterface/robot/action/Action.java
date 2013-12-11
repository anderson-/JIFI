/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.action;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import robotinterface.robot.Robot;
import robotinterface.robot.connection.message.Message;

/**
 *
 * @author antunes
 */
public abstract class Action extends Message {

    private static final ByteBuffer buffer = ByteBuffer.allocate(256);
    protected boolean running = false;
    protected boolean autoSend = true;
    protected boolean singleMessage = false;

    public Action (){
        
    }
    
    public Action (boolean singleMessage){
        this.singleMessage = singleMessage;
    }
    
    public void setAutoSend(boolean autoSend) {
        this.autoSend = autoSend;
    }

    public abstract void putMessage(ByteBuffer data, Robot robot);

    public void begin(Robot robot) {
        buffer.clear();
        putMessage(buffer, robot);
        buffer.flip();
        setWaiting();
        send(buffer);
        running = false;
        if (!singleMessage){
            setWaiting(Long.MAX_VALUE); //espera terminar a ação
            run(this, robot); //espera confimação do comando
        }
    }

    public static void run(Action action, Robot robot) {
        while (!action.perform(robot)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
            }
        }
    }

    public final boolean perform(Robot robot) {
        try {
            if (isValidRead()) {
                return true;
            }
        } catch (Message.TimeoutException ex) {
            if (autoSend) {
                begin(robot);
            } else {
                return true;
            }
        }
        return false;
    }

    public final void setRunning() {
        running = true;
    }

    public final void setDone() {
        running = false;
    }
}
