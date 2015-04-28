/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot;

import jifi.robot.device.Device;
import jifi.robot.connection.Connection;
import jifi.drawable.GraphicObject;
import jifi.drawable.DrawingPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jifi.drawable.Drawable;
import jifi.drawable.Rotable;
import jifi.gui.panels.RobotEditorPanel;
import jifi.robot.action.Action;
import jifi.robot.action.system.AddNewDevice;
import jifi.robot.action.system.GenericAction;
import jifi.robot.action.system.ResetSystem;
import jifi.robot.action.system.StopAll;
import jifi.robot.action.system.UpdateAllDevices;
import jifi.robot.connection.message.Message;
import jifi.util.observable.Observer;
import jifi.robot.simulation.Environment;
import jifi.robot.simulation.Perception;
import jifi.robot.simulation.VirtualConnection;
import jifi.robot.simulation.VirtualDevice;

/**
 *
 * @author antunes
 */
public class Robot implements Observer<ByteBuffer, Connection>, GraphicObject, Rotable {

    public static final double SIZE_CM = 20;
    public static final double size = 60;
    public static final byte CMD_END = 0;
    public static final byte CMD_STOP = 1;
    public static final byte CMD_ECHO = 2;
    public static final byte CMD_PRINT = 3;
    public static final byte CMD_GET = 4;
    public static final byte CMD_SET = 5;
    public static final byte CMD_ADD = 6;
    public static final byte CMD_RESET = 7;
    public static final byte CMD_DONE = 8;
    public static final byte CMD_RUN = 9;
    public static final byte CMD_NO_OP = 10;
    public static final byte CMD_FAIL = 11;
    public static final byte XTRA_ALL = (byte) 222;
    public static final byte XTRA_FREE_RAM = (byte) 223;
    public static final byte XTRA_SYSTEM = (byte) 224;
    public static final byte XTRA_BEGIN = (byte) 225;
    public static final byte XTRA_END = (byte) 226;
    public boolean LOG = false;
    public static final Action REQUEST_FREE_RAM = new GenericAction(true, 0, new byte[]{4, (byte) 223, 0});
    public static final Action REMOVE_ALL_DEVICES = new GenericAction(true, 0, new byte[]{7, (byte) 222});
    public static final Action STOP_ALL = new StopAll();
    public static final Action RESET_SYSTEM = new ResetSystem();
    public static final Action UPDATE_ALL_DEVICES = new UpdateAllDevices();
    public static final AddNewDevice ADD_NEW_DEVICE = new AddNewDevice();
    private boolean DEBUG;
    private boolean moveDisabled = false;
    private boolean selected;

    public void disableMove(boolean d) {
        moveDisabled = d;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public class InternalClock extends Device {

        private float stepTime = 0;

        @Override
        public void setState(ByteBuffer data) {
            stepTime = data.getFloat();
//            System.out.println("Tempo do ciclo: " + stepTime);
        }

        @Override
        public boolean isActuator() {
            return false;
        }

        @Override
        public boolean isSensor() {
            return true;
        }

        @Override
        public String stateToString() {
            return "" + stepTime;
        }

        @Override
        public int getClassID() {
            return 0;
        }

        @Override
        public String getName() {
            return "Ciclo";
        }

        @Override
        public void resetState() {
            stepTime = 0;
        }
    }
    private Environment environment;
    private final Perception perception;
    private final ArrayList<Device> devices;
    private final ArrayList<Action> actions;
    private final ArrayList<Connection> connections;
    private int freeRam = 0;
    private double x, y;
    private double theta;
    private double rightWheelSpeed, leftWheelSpeed;
    private final Rectangle2D.Double bounds = new Rectangle.Double();
    private final ArrayList<Observer<Device, Robot>> observers = new ArrayList<>();
    private final ByteBuffer buffer = ByteBuffer.allocate(256);

    public Robot() {
        devices = new ArrayList<>();
        actions = new ArrayList<>();
        connections = new ArrayList<>();
        perception = new Perception();
        add(new InternalClock());

        x = 0;
        y = 0;
        theta = 0;
        rightWheelSpeed = 0;
        leftWheelSpeed = 0;
    }

    public void reset() {
        x = 0;
        y = 0;
        theta = 0;
        perception.clearPath();
        for (Device d : devices) {
            d.resetState();
        }
        stop();
    }

    @Deprecated//hbridge para o robo na função stopAll()
    public void stop() {
        rightWheelSpeed = 0;
        leftWheelSpeed = 0;
        for (Action a : actions) {
            if (a.isWaiting()) {
                a.markUnread();
                a.setDone();
            } else if (a.isRunning()) {
                //System.out.println("reset");
                //resetSystem();
                a.markUnread();
                a.setDone();
            }
        }
    }

    public void updateObservers(Device d) {
        for (Observer<Device, Robot> o : observers) {
            o.update(d, this);
        }
    }

    public final int getFreeRam() {
        return freeRam;
    }

    public final int requestFreeRam() {
        Message.setConnection(getMainConnection());
        REQUEST_FREE_RAM.begin(this);
        Action.run(REQUEST_FREE_RAM, this);
        return freeRam;
    }

    public final void removeAllDevices() {
        Message.setConnection(getMainConnection());
        REMOVE_ALL_DEVICES.begin(this);
        Action.run(REMOVE_ALL_DEVICES, this);
    }

    public final void add(Device d) {
        devices.add(d);
        d.setID(devices.size() - 1);
    }

    public final void remove(Device d) {
        devices.remove(d);
        d.setID(-1);
    }

    public final <T> T getDevice(Class<? extends Device> c) {
        Message.setConnection(getMainConnection());
        for (Device d : devices) {
            if (c.isInstance(d)) {
                return (T) d;
            }
        }
        return null;
    }

    public final Device getDevice(int index) {
        Message.setConnection(getMainConnection());
        if (index < 0 || index >= devices.size()) {
            return null;
        }
        return devices.get(index);
    }

    public final List<Device> getDevices() {
        Message.setConnection(getMainConnection());
        return devices;
    }

    public final int getDeviceListSize() {
        return devices.size();
    }

    public final void add(Action a) {
        actions.add(a);
        a.setID(actions.size() - 1);
    }

    public final void remove(Action a) {
        actions.remove(a);
        a.setID(-1);
    }

    public final <T> T getAction(Class<? extends Action> c) {
        Message.setConnection(getMainConnection());
        for (Action a : actions) {
            if (c.isInstance(a)) {
                return (T) a;
            }
        }
        return null;
    }

    public final Action getAction(int index) {
        if (index < 0 || index >= actions.size()) {
            return null;
        }
        return actions.get(index);
    }

    public final List<Action> getAction() {
        Message.setConnection(getMainConnection());
        return actions;
    }

    public final int getActionListSize() {
        return actions.size();
    }

    public final void add(Connection c) {
        c.attach(this);
        connections.add(c);
    }

    public final void remove(Connection c) {
        c.detach(this);
        connections.remove(c);
    }

    public final Connection getConnection(Class<? extends Connection> c) {
        for (Connection con : connections) {
            if (c.isInstance(con)) {
                return con;
            }
        }
        return null;
    }

    public final Connection getConnection(int index) {
        if (index < 0 || index >= connections.size()) {
            return null;
        }
        return connections.get(index);
    }

    public void setMainConnection(Connection c) {
        if (c != null) {
            c.attach(this);
            connections.add(0, c);
        } else {
            connections.clear();
        }
    }

    public final Connection getMainConnection() {
        if (connections.isEmpty()) {
            return null;
        }
        return getConnection(0);
    }

    public final List<Connection> getConnections() {
        return connections;
    }

    public final int getConnectionListSize() {
        return connections.size();
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Perception getPerception() {
        return perception;
    }

    public void updateVirtualPerception() {
        perception.addPathPoint(x, y);
    }

    public void updatePerception() {
        Message.setConnection(getMainConnection());
        UPDATE_ALL_DEVICES.setInfiniteSend(false);
        UPDATE_ALL_DEVICES.begin(this);
        Action.run(UPDATE_ALL_DEVICES, this);
    }

    public void resetSystem() {
        Message.setConnection(getMainConnection());
        RESET_SYSTEM.begin(this);
        Action.run(RESET_SYSTEM, this);
    }

    public void stopAll() {
        Message.setConnection(getMainConnection());
        STOP_ALL.setNumberOfTrials(10);
        STOP_ALL.begin(this);
        Action.run(STOP_ALL, this);
    }

    public void addDevice(byte[] data) {
        Message.setConnection(getMainConnection());
        ADD_NEW_DEVICE.setDeviceData(data);
        ADD_NEW_DEVICE.begin(this);
        Action.run(ADD_NEW_DEVICE, this);
    }

    public final void virtualRobot(ByteBuffer message, Connection connection) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
        }
        try {
            loop:
            while (message.remaining() > 0) {
                buffer.clear();
                if (DEBUG) {
                    System.out.println("C " + buffer);
                }
                byte cmd = message.get();
                switch (cmd) {
                    case CMD_STOP: {
                        //skip bytes
                        STOP_ALL.markUnread();
                        message.get();
                        break;
                    }
//
//                    case CMD_ECHO: {
//                        byte length = message.get();
//                        byte[] bytestr = new byte[length];
//                        message.get(bytestr);
//                        connection.send(bytestr);
//                        break;
//                    }
//
//                    case CMD_PRINT: {
//                        byte connectionID = message.get();
//                        byte length = message.get();
//                        byte[] bytestr = new byte[length];
//                        System.out.println("receiving:" + length);
//                        message.get(bytestr);
//                        System.out.println(new String(bytestr)); //TODO: stdout
////                    if (connectionID == XTRA_ALL) {
////                        for (Connection c : getConnections()) {
////                            if (c != null) {
////                                c.send(bytestr);
////                            }
////                        }
////                    } else {
////                        Connection c = getConnection(connectionID);
////                        if (c != null) {
////                            c.send(bytestr);
////                        }
////                    }
//                        break;
//                    }

                    case CMD_GET: {
                        byte id = message.get();
                        byte length = message.get();
                        if (id == XTRA_FREE_RAM) {
                            freeRam = 0;
                            REQUEST_FREE_RAM.markUnread();
                        } else {
                            if (message.remaining() >= length) {
                                byte[] args = new byte[length];
                                message.get(args);

                                Device d = getDevice(id);
                                if (d != null && d instanceof VirtualDevice) {
                                    if (DEBUG) {
                                        System.out.println(".r:" + buffer.remaining() + ",p:" + buffer.position() + ",l:" + buffer.limit());
                                    }
                                    buffer.put(CMD_SET);
                                    if (DEBUG) {
                                        System.out.println(".r:" + buffer.remaining() + ",p:" + buffer.position() + ",l:" + buffer.limit());
                                    }
                                    buffer.put(id);
                                    if (DEBUG) {
                                        System.out.println(".r:" + buffer.remaining() + ",p:" + buffer.position() + ",l:" + buffer.limit());
                                    }
                                    ((VirtualDevice) d).getState(buffer, this);
                                    if (DEBUG) {
                                        System.out.println(".r:" + buffer.remaining() + ",p:" + buffer.position() + ",l:" + buffer.limit());
                                    }
                                }
                            } else if (LOG) {
                                System.err.println("1mesagem muito curta:" + id + "[" + length + "] de " + message.remaining());
                            }
                        }
                        break;
                    }

                    case CMD_SET: {
                        byte id = message.get();
                        byte length = message.get();
                        if (message.remaining() >= length) {
                            byte[] args = new byte[length];
                            message.get(args);
                            ByteBuffer tmp = ByteBuffer.wrap(args).asReadOnlyBuffer();
                            tmp.order(ByteOrder.LITTLE_ENDIAN);
                            if (id == XTRA_FREE_RAM) {
                                freeRam = tmp.getChar();
                                System.out.println("FreeRam: " + freeRam);
                            } else {
                                Device d = getDevice(id);
                                if (d != null) {
                                    if (d instanceof VirtualDevice) {
                                        ((VirtualDevice) d).setState(tmp, this);
                                    } else {
                                        d.setState(tmp);
                                    }
                                    d.updateRobot(this);
                                    d.markUnread();
                                    updateObservers(d);
                                }
                            }
                        } else if (LOG) {
                            System.err.println("2mesagem muito curta:" + id + "[" + length + "] de " + message.remaining());
                        }
                        break;
                    }

                    case CMD_ADD: {
                        //skip bytes
                        message.get();
                        message.get();
                        byte length = message.get();
                        byte[] args = new byte[length];
                        message.get(args);
                        ADD_NEW_DEVICE.markUnread();
                        break;
                    }
//
                    case CMD_RESET: {
                        //skip bytes
                        REMOVE_ALL_DEVICES.markUnread();
                        message.get();
                        break;
                    }
//
//                    case CMD_DONE: {
//                        byte cmdDone = message.get();
//                        byte id = message.get();
//                        if (cmdDone == CMD_RUN) {
//                            byte len = message.get();
//                            byte[] status = new byte[len];
//                            message.get(status);
//                            if (len > 0) {
//                                if (status[0] == XTRA_BEGIN) {
//                                    System.out.println("cmd begin:" + id);
//                                } else if (status[0] == XTRA_END) {
//                                    System.out.println("cmd end:" + id);
//                                }
//                            }
//                        } else if (cmdDone == CMD_RESET) {
//                            switch (id) {
//                                case XTRA_ALL:
//                                    System.out.println("Dispositivos e funções resetados...");
//                                    break;
//                                case XTRA_SYSTEM:
//                                    System.out.println("Sistema resetado...");
//                                    break;
//                                default:
//                                    Device d = getDevice(id);
//                                    System.out.println("Dispositivo [" + d + "] resetado...");
//                                    break;
//                            }
//
//                        } else {
//                            message.get(); //tamanho da mensagem rebida pelo robô e não
//                            //o tamanho da mensagem a ser lida agora.
//                            switch (cmdDone) {
//                                case CMD_SET: {
//                                    Device d = getDevice(id);
//                                    if (d != null) {
//                                        //define que o valor do dispositivo é novo
//                                        //e ainda não foi lido
//                                        d.markUnread();
//                                    }
//                                    break;
//                                }
//                            }
//                        }
//                        //TODO: confirmação do comando enviado
//                        break;
//                    }
//
//
//
//                    case CMD_NO_OP: {
//                        break;
//                    }
//
//                    case CMD_END: {
//                        break loop;
//                    }

                    default:
                        if (LOG && cmd != 0) {
                            System.err.println("Erro: Comando invalido: " + cmd);
                        }
                }
                if (DEBUG) {
                    System.out.println("flip");
                }
                buffer.flip();
                if (DEBUG) {
                    System.out.println("update");
                }
                update(buffer, connection);
            }
        } catch (BufferUnderflowException e) {
            if (LOG) {
                System.err.println("mensagem pela metade");
            }
            e.printStackTrace();
        } catch (BufferOverflowException e) {
            e.printStackTrace();
            System.err.println("r:" + buffer.remaining() + ",p:" + buffer.position() + ",l:" + buffer.limit());
            //System.out.println("??");
        }
    }

    @Override
    public final void update(ByteBuffer message, Connection connection) {
        message.order(ByteOrder.LITTLE_ENDIAN);
        try {
            loop:
            while (message.remaining() > 0) {
                byte cmd = message.get();
                switch (cmd) {
                    case CMD_STOP: {
                        //skip bytes	
                        STOP_ALL.markUnread();
                        message.get();
                        break;
                    }

                    case CMD_ECHO: {
                        byte length = message.get();
                        byte[] bytestr = new byte[length];
                        message.get(bytestr);
                        connection.send(bytestr);
                        break;
                    }

                    case CMD_PRINT: {
                        byte connectionID = message.get();
                        byte length = message.get();
                        byte[] bytestr = new byte[length];
                        System.out.println("receiving:" + length);
                        message.get(bytestr);
                        System.out.println(new String(bytestr)); //TODO: stdout
//                    if (connectionID == XTRA_ALL) {
//                        for (Connection c : getConnections()) {
//                            if (c != null) {
//                                c.send(bytestr);
//                            }
//                        }
//                    } else {
//                        Connection c = getConnection(connectionID);
//                        if (c != null) {
//                            c.send(bytestr);
//                        }
//                    }
                        break;
                    }

                    case CMD_GET: {
                        //skip bytes
                        message.get();
                        byte length = message.get();
                        byte[] args = new byte[length];
                        message.get(args);
                        break;
                    }

                    case CMD_SET: {
                        byte id = message.get();
                        byte length = message.get();
                        if (message.remaining() >= length) {
                            byte[] args = new byte[length];
                            message.get(args);
                            ByteBuffer tmp = ByteBuffer.wrap(args).asReadOnlyBuffer();
                            tmp.order(ByteOrder.LITTLE_ENDIAN);
                            if (id == XTRA_FREE_RAM) {
                                freeRam = tmp.getChar();
                                System.out.println("FreeRam: " + freeRam);
                                REQUEST_FREE_RAM.markUnread();
                            } else {
                                Device d = getDevice(id);
                                if (d != null) {
                                    if (connection instanceof VirtualConnection
                                            && d instanceof VirtualDevice
                                            && ((VirtualConnection) connection).serial()) {
                                        //robo real com ambiente virtual
                                        ((VirtualDevice) d).setState(tmp, this);
                                    } else {
                                        //robo real (sem ambiente virtual) ou somente virtual
                                        d.setState(tmp);
                                    }
                                    d.markUnread();
                                    d.updateRobot(this);
                                    updateObservers(d);
                                }
                            }
                        } else if (LOG) {
                            System.err.println("mesagem muito curta:" + id + "[" + length + "] de " + message.remaining());
                        }
                        break;
                    }

                    case CMD_ADD: {
                        //skip bytes
                        message.get();
                        byte length = message.get();
                        byte[] args = new byte[length];
                        message.get(args);
                        break;
                    }

                    case CMD_RESET: {
                        //skip bytes
                        message.get();
                        break;
                    }

                    case CMD_DONE: {
                        byte cmdDone = message.get();
                        byte id = message.get();
                        if (cmdDone == CMD_RUN) {
                            byte len = message.get();
                            if (message.remaining() >= len) {
                                byte[] status = new byte[len];
                                message.get(status);
                                if (len > 0) {
                                    Action a = getAction(id);
                                    if (a != null) {
                                        if (status[0] == XTRA_BEGIN) {
                                            a.markUnread();
                                            a.setRunning();
//                                            System.out.println("cmd begin:" + id);
                                        } else if (status[0] == XTRA_END) {
                                            a.markUnread();
                                            a.setDone();
//                                            System.out.println("cmd end:" + id);
                                        }
                                    }
                                }
                            } else {
                                System.err.println("3mesagem muito curta:" + id + "[" + len + "] de " + message.remaining());
                            }
                        } else if (cmdDone == CMD_RESET) {
                            switch (id) {
                                case XTRA_ALL:
                                    System.out.println("Dispositivos removidos...");
                                    REMOVE_ALL_DEVICES.markUnread();
                                    break;
                                case XTRA_SYSTEM:
                                    System.out.println("Sistema resetado...");
                                    break;
                                default:
                                    Device d = getDevice(id);
                                    System.out.println("Dispositivo [" + id + "] resetado...");
                                    break;
                            }

                        } else {
                            message.get(); //tamanho da mensagem rebida pelo robô e não
                            //o tamanho da mensagem a ser lida agora.
                            switch (cmdDone) {
                                case CMD_ADD: {
                                    ADD_NEW_DEVICE.markUnread();
                                    break;
                                }
                                case CMD_SET: {
                                    Device d = getDevice(id);
                                    if (d != null) {
                                        //define que o valor do dispositivo é novo
                                        //e ainda não foi lido
                                        d.markUnread();
                                    }
                                    break;
                                }
                                case CMD_STOP: {
                                    switch (id) {
                                        case XTRA_ALL:
                                            STOP_ALL.markUnread();
                                            break;
                                        case XTRA_SYSTEM:
                                            //STOP_ALL.markUnread();
                                            break;
                                        default:
                                            System.out.println("stop?" + id);
                                    }
                                }
                            }
                        }
                        //TODO: confirmação do comando enviado
                        break;
                    }

                    case CMD_NO_OP: {
                        break;
                    }

                    case CMD_END: {
                        break loop;
                    }

                    default:
                        if (LOG && cmd != 0) {
                            System.err.println("Erro1: Comando invalido: " + cmd);
                        }
                }
            }
        } catch (BufferUnderflowException e) {
            if (LOG) {
                e.printStackTrace();
                System.err.println("mensagem pela metade");
            }
        }
    }

    @Override
    public double getTheta() {
        return theta;
    }

    @Override
    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getRightWheelSpeed() {
        return rightWheelSpeed;
    }

    public void setRightWheelSpeed(double rightWheelSpeed) {
        this.rightWheelSpeed = rightWheelSpeed;
    }

    public double getLeftWheelSpeed() {
        return leftWheelSpeed;
    }

    public void setLeftWheelSpeed(double leftWheelSpeed) {
        this.leftWheelSpeed = leftWheelSpeed;
    }

    private void move(double dt) {
        double pf = rightWheelSpeed + leftWheelSpeed;
        double mf = leftWheelSpeed - rightWheelSpeed;
        double hf = pf / 2;
        double a = size / 2 * pf / mf;
        double b = theta + mf * dt / size;
        double sin_theta = sin(theta);
        double cos_theta = cos(theta);

        if (leftWheelSpeed != rightWheelSpeed) {
            if (b < 0) {
                b += 2 * Math.PI;
            }
            if (b >= 2 * Math.PI) {
                b -= 2 * Math.PI;
            }
            theta = b;
            x = x + a * (sin(b) - sin_theta);
            y = y - a * (cos(b) - cos_theta);
        } else {
            x += hf * cos(theta) * dt;
            y += hf * sin(theta) * dt;
        }
    }

    @Override
    public final Rectangle2D.Double getObjectBouds() {
        bounds.x = x;
        bounds.y = y;
        bounds.width = bounds.height = size;
        return bounds;
    }

    @Override
    public Shape getObjectShape() {
        return getObjectBouds();
    }

    @Override
    public void setLocation(double x, double y) {
        bounds.x = this.x = x;
        bounds.y = this.y = y;
    }

    @Override
    public double getPosX() {
        return x;
    }

    @Override
    public double getPosY() {
        return y;
    }

    @Override
    public void setObjectBounds(double x, double y, double width, double height) {
        if (width == height) {
            bounds.x = x;
            bounds.y = y;
            bounds.width = width;
            bounds.height = height;
        }
    }

    @Override
    public int getDrawableLayer() {
        return DrawingPanel.DEFAULT_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {

        AffineTransform o = g.getTransform();
        AffineTransform t = ga.getT(o);
        ga.removeRelativePosition(t);
        g.setTransform(t);

        perception.draw(g);

        t.setTransform(o);

        //t.translate(x, y); DrawingPanel se encarrega de definir a posiçãos
        t.rotate(theta);
        g.setTransform(t);

        int iSize = (int) size;
        g.setColor(Color.white);
        g.fillOval(-iSize / 2, -iSize / 2, iSize, iSize);
        if (!selected) {
            g.setColor(Color.gray);
        } else {
            g.setColor(RobotEditorPanel.SELECTED_COLOR);
            selected = false;
        }
        //body
        g.drawOval(-5, -5, 10, 10);
        g.drawOval(-iSize / 2, -iSize / 2, iSize, iSize);
        //frente
//        g.fillRect(iSize / 2 - 5, -iSize / 2 + 10, 5, iSize - 20);
        //contorno
        //g.setColor(Color.black);
//        g.drawRect(iSize / 2 - 5, -iSize / 2 + 10, 5, iSize - 20);
        //rodas
        int ww = (int) (0.4 * size);
        int wh = (int) (0.2 * size);
        int wp = (int) (size / 2 - wh) + 1;

        g.fillRoundRect(-ww / 2, -iSize / 2 - 1, ww, wh, (int) (size * .1), (int) (size * .1));
        g.fillRoundRect(-ww / 2, wp, ww, wh, (int) (size * .1), (int) (size * .1));

//        AffineTransform td = ga.getT();
        for (Device d : devices) {
//            td.setTransform(t);
            g.setTransform(t);
            if (d instanceof Drawable) {
                ((Drawable) d).draw(g, ga, in);
            }
        }

        g.setTransform(o);
        ga.done(t);
        if (!moveDisabled) {
            move(ga.getClock().getDt());
        }
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }
}
