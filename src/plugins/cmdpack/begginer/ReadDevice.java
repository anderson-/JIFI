/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.cmdpack.begginer;

import algorithm.procedure.Procedure;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import org.nfunk.jep.Variable;
import robot.Device;
import robot.Robot;
import simulation.ExecutionException;
import util.trafficsimulator.Clock;
import util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class ReadDevice extends Procedure {

    private Timer timer;
    private Device device;
    private Class<? extends Device> type;
    private String var;

    public ReadDevice(Class<? extends Device> type, String var) {
        this.type = type;
        this.var = var;
        timer = new Timer(200);
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        device = robot.getDevice(type);
        if (device != null) {
            //mensagem get padrão 
            byte[] msg = device.defaultGetMessage();
            if (msg.length > 0) {
                //cria um buffer para a mensagem
                ByteBuffer GETmessage = ByteBuffer.allocate(64);
                //header do comando set
                GETmessage.put(Robot.CMD_GET);
                //id
                GETmessage.put(device.getID());
                //tamanho da mensagem
                GETmessage.put((byte) msg.length);
                //mensagem
                GETmessage.put(msg);
                //flip antes de enviar
                GETmessage.flip();
                robot.getMainConnection().send(GETmessage);
            } else {
                msg = new byte[]{Robot.CMD_GET, device.getID(), 0};
                robot.getMainConnection().send(msg);
            }
        }
        timer.reset();
        clock.addTimer(timer);
    }

    @Override
    public boolean perform(Robot r, Clock clock) throws ExecutionException {
        if (timer.isConsumed()) { //espera 200ms antes de ler o valor do dispositivo
            if (device != null) {
                String deviceState = device.stateToString();
                if (!deviceState.isEmpty()) {
                    execute(var + " = " + deviceState);
                }
            }
            return true;
        }
        return false;
    }
}
