/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot;

import gui.drawable.Drawable;
import gui.drawable.DrawingPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import observable.Observer;
import simulation.Interpreter;

/**
 *
 * @author antunes
 */
public class Robot implements Observer<ByteBuffer, Connection>, Drawable {

    public static final double SIZE_CM = 20;
    private double size = 60;
    private double x, y;
    private double theta;
    private double rightWheelSpeed, leftWheelSpeed;
    private Rectangle2D.Double bounds = new Rectangle.Double();

    public class InternalClock extends Device {

        private float stepTime = 0;

        @Override
        public void setState(ByteBuffer data) {
            stepTime = data.getFloat();
            System.out.println("Tempo do ciclo: " + stepTime);
        }

        @Override
        public String stateToString() {
            return "" + stepTime;
        }

        @Override
        public int getClassID() {
            return 0;
        }
    }
    private Interpreter interpreter;
    private ArrayList<Device> devices;
    private ArrayList<Connection> connections;
    private int freeRam = 0;
    public static final byte CMD_STOP = 1;
    public static final byte CMD_ECHO = 2;
    public static final byte CMD_PRINT = 3;
    public static final byte CMD_GET = 4;
    public static final byte CMD_SET = 5;
    public static final byte CMD_ADD = 6;
    public static final byte CMD_RESET = 7;
    public static final byte CMD_DONE = 8;
    public static final byte CMD_NO_OP = 9;
    public static final byte XTRA_ALL = (byte) 222;
    public static final byte XTRA_FREE_RAM = (byte) 223;

    public Robot() {
        devices = new ArrayList<>();
        connections = new ArrayList<>();
        add(new InternalClock());

        x = 0;
        y = 0;
        theta = 0;
        rightWheelSpeed = 0;
        leftWheelSpeed = 0;

    }

    public final int getFreeRam() {
        return freeRam;
    }

    public final void add(Device d) {
        devices.add(d);
        d.setID(devices.size() - 1);
    }

    public final void add(Connection c) {
        c.attach(this);
        connections.add(c);
    }

    public final <T> T getDevice(Class<? extends Device> c) {
        for (Device d : devices) {
            if (c.isInstance(d)) {
                return (T) d;
            }
        }
        return null;
    }

    public final Connection getConnection(Class<? extends Connection> c) {
        for (Connection con : connections) {
            if (c.isInstance(con)) {
                return con;
            }
        }
        return null;
    }

    public final Device getDevice(int index) {
        if (index < 0 || index >= devices.size()) {
            return null;
        }
        return devices.get(index);
    }

    public final Connection getConnection(int index) {
        if (index < 0 || index >= connections.size()) {
            return null;
        }
        return connections.get(index);
    }

    public final Connection getMainConnection() {
        return getConnection(0);
    }

    public final List<Device> getDevices() {
        return devices;
    }

    public final List<Connection> getConnections() {
        return connections;
    }

    public final int getDeviceListSize() {
        return devices.size();
    }

    public final int getConnectionListSize() {
        return connections.size();
    }

    public final Interpreter getInterpreter() {
        return interpreter;
    }

    @Override
    public final void update(ByteBuffer message, Connection connection) {
        message.order(ByteOrder.LITTLE_ENDIAN);
        while (message.remaining() > 0) {
            byte cmd = message.get();
            switch (cmd) {
                case CMD_STOP: {
                    //skip bytes
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
                    message.get(bytestr);
                    System.out.println(new String(bytestr)); //TODO: stdout
                    if (connectionID == XTRA_ALL) {
                        for (Connection c : getConnections()) {
                            if (c != null) {
                                c.send(bytestr);
                            }
                        }
                    } else {
                        Connection c = getConnection(connectionID);
                        if (c != null) {
                            c.send(bytestr);
                        }
                    }
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
                            d.setState(tmp);
                        }
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
                    message.get(); //tamanho da mensagem rebida pelo robô e não
                    //o tamanho da mensagem a ser lida agora.
                    switch (cmdDone) {
                        case CMD_SET: {
                            Device d = getDevice(id);
                            if (d != null) {
                                //define que o valor do dispositivo é novo
                                //e ainda não foi lido
                                d.markUnread();
                            }
                            break;
                        }
                    }
                    //TODO: confirmação do comando enviado
                    break;
                }

                case CMD_NO_OP: {
                    break;
                }
                default:
                    if (cmd != 0) {
                        System.out.println("Erro: Comando invalido: " + cmd);
                    }
            }
        }
    }

    public double getTheta() {
        return theta;
    }

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
    public void setObjectLocation(double x, double y) {
        bounds.x = x;
        bounds.y = y;
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
        AffineTransform t = new AffineTransform(o);
        //t.translate(x, y); DrawingPanel se encarrega de definir a posiçãos
        t.rotate(theta);
        g.setTransform(t);
        g.setColor(Color.gray);
        int iSize = (int) size;
        //body
        g.drawOval(-5, -5, 10, 10);
        g.drawOval(-iSize / 2, -iSize / 2, iSize, iSize);
        //frente
        g.fillRect(iSize / 2 - 5, -iSize / 2 + 10, 5, iSize - 20);
        //contorno
        //g.setColor(Color.black);
        g.drawRect(iSize / 2 - 5, -iSize / 2 + 10, 5, iSize - 20);
        //rodas
        int ww = (int) (0.4 * size);
        int wh = (int) (0.2 * size);
        int wp = (int) (size / 2 - wh) + 1;

        g.fillRoundRect(-ww / 2, -iSize / 2 - 1, ww, wh, (int) (size * .1), (int) (size * .1));
        g.fillRoundRect(-ww / 2, wp, ww, wh, (int) (size * .1), (int) (size * .1));
        g.setTransform(o);

        move(ga.getClock().getDt());
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }
}
