/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot.action;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jifi.robot.Robot;
import jifi.robot.connection.message.Message;

/**
 *
 * @author antunes
 */
public abstract class Action extends Message {

    private static final ByteBuffer buffer = ByteBuffer.allocate(256);
    /**
     *
     */
    private boolean waitingMessage = false;
    /**
     * Reenvia mensagens perdidas até receber uma rsposta.
     */
    private boolean infiniteSend = true;

    /**
     *
     */
    private int numberOfTrials = -1;
    /**
     *
     */
    private int trials = 0;
    /**
     * Define que apenas uma mensagem será recebida.
     */
    private boolean singleMessage = false;
    /**
     *
     */
    private boolean running = false;

    public Action() {

    }

    /**
     *
     * Define a quantidade de respostas que esta ação irá receber. Caso seja
     * passado o parametro <code>singleMessage = true<\code> a ação irá esperar
     * por um comando de confirmação por tempo indeterminado (reenvindo a
     * mensagem após MAX_TIMEOUT) e depois espera pela mensagem de termino do comando.
     *
     *
     * @param singleMessage
     */
    public Action(boolean singleMessage) {
        this.singleMessage = singleMessage;
    }

    public void setInfiniteSend(boolean infiniteSend) {
        this.infiniteSend = infiniteSend;
        numberOfTrials = -1;
        trials = 0;
    }

    public void setNumberOfTrials(int numberOfTrials) {
        this.numberOfTrials = numberOfTrials;
        trials = 0;
        infiniteSend = false;
    }

    public abstract void putMessage(ByteBuffer data, Robot robot);

    /**
     * Envia a mensagem
     *
     * @param robot
     */
    public synchronized void begin(Robot robot) {
        //coloca mensagem no buffer
        buffer.clear();
        putMessage(buffer, robot);
        buffer.flip();
        //estado: esperando por mensagem
        setWaiting();
        //envia a mensagem
        send(buffer);
        if (!singleMessage) {
            setWaiting(MAX_TIMEOUT); //espera terminar a ação
            run(this, robot); //espera confimação do comando 1
        }
    }

    /**
     * Executa a ação, bloqueando a thread que o chamou.
     *
     * @param action
     * @param robot
     */
    public static void run(Action action, Robot robot) {
        while (!action.perform(robot)) {
            try {
                Thread.sleep(5); //Thread do interpretador
            } catch (InterruptedException ex) {
            }
        }
    }

    public final boolean perform(Robot robot) {
        try {
            if (isValidRead()) {
                return true; //termina sem problemas
            }
        } catch (Message.TimeoutException ex) {
            if (infiniteSend || trials < numberOfTrials) {
                begin(robot); //reenvia a mensagem em caso de perda
                trials++;
            } else {
                return true; //termina de forma inválida
            }
        }
        if (!connection.isConnected()){
            System.out.println("problema serial");
            return true; //problema na conexao serial
        }
        return false; //ainda não terminou
    }

    /**
     * Confirma que a ação está em execução pelo robô.
     *
     * @deprecated Uso apenas pela classe Robot.
     */
    @Deprecated
    public final void setRunning() {
        waitingMessage = true;
        running = true;
    }

    /**
     * Define que a ação foi concluída com sucesso pelo robô.
     *
     * @deprecated Uso apenas pela classe Robot.
     */
    @Deprecated
    public final void setDone() {
        waitingMessage = false;
        running = false;
    }

    /**
     * Retorna <code>true</code> se a ação está esperando uma mensagem de
     * confirmação de incio/fim.
     *
     * @return
     */
    public boolean isWaiting() {
        return waitingMessage;
    }

    /**
     * Retorna <code>true</code> se a ação está eviando/esperando uma mensagem
     * ativamente (sobrecarregando a respectiva thread).
     *
     * @return
     */
    public boolean isRunning() {
        return running;
    }
}
