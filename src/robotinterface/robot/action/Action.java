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
    private boolean waitingMessage = false;
    private boolean autoSend = true;
    private boolean singleMessage = false;
    private boolean running = false;

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
        running = true;
        send(buffer);
        waitingMessage = false;
        if (!singleMessage){
            setWaiting(Long.MAX_VALUE); //espera terminar a ação
            run(this, robot); //espera confimação do comando 1
        }
    }

    public static void run(Action action, Robot robot) {
        while (!action.perform(robot)) {
            try {
                Thread.sleep(5);
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
        waitingMessage = true;
        running = true;
    }

    public final void setDone() {
        waitingMessage = false;
        running = false;
    }

    public boolean isWaiting() {
        return waitingMessage;
    }

    public boolean isRunning() {
        return running;
    }
}
