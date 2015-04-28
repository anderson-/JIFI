/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.gui.panels.robot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import jifi.gui.GUI;
import jifi.interpreter.Interpreter;
import jifi.robot.Robot;
import jifi.robot.action.Action;
import jifi.robot.action.RotateAction;
import jifi.robot.connection.Connection;
import jifi.robot.connection.SimpleSerial;
import jifi.robot.device.Button;
import jifi.robot.device.Compass;
import jifi.robot.device.Device;
import jifi.robot.device.HBridge;
import jifi.robot.device.IRProximitySensor;
import jifi.robot.device.LED;
import jifi.robot.device.ReflectanceSensorArray;
import jifi.robot.simulation.VirtualConnection;

/**
 *
 * @author antunes
 */
public class RobotControlPanel extends JPanel {

    private class ConnectionStatusGraph extends JPanel {

        private final float size = 5;
        private ArrayList<Integer> sendedArray = new ArrayList<>();
        private ArrayList<Integer> lostArray = new ArrayList<>();
        private ArrayList<Integer> receivedArray = new ArrayList<>();
        private int sended = 0;
        private int lost = 0;
        private int received = 0;
        private Color sendedColor = Color.decode("#4ecdc4");
        private Color lostColor = Color.decode("#ff6b6b");
        private Color receivedColor = Color.decode("#c7f464");

        public ConnectionStatusGraph() {
        }

        public void step() {
            int nbars = (int) (getWidth() / size);
            int newSended = 0;
            int newReceived = 0;
            int newLost = 0;
            if (serial != null && serial.isConnected()) {
                int s = serial.getSendedPackages();
                int r = serial.getReceivedPackages();
                int l = Device.getLostPackages();

                newSended = s - sended;
                newReceived = r - received;
                newLost = l - lost;

                sendedArray.add(newSended);
                receivedArray.add(newReceived);
                lostArray.add(newLost);

                sended = s;
                received = r;
                lost = l;
            }

            //atualiza botão
            if (button != null) {
                if (connection == null || !connection.isConnected()) {
                    button.setIcon(ICON_OFFLINE);
                } else {
                    boolean oldLost = true;

                    for (int i = lostArray.size() - 1; i > lostArray.size() - 6 && i >= 0; i--) {
                        oldLost &= (lostArray.get(i) > 0 || sendedArray.get(i) == 0);
                        oldLost &= (receivedArray.get(i) == 0);
                    }

                    if (newReceived == 0 && newLost >= 0) {
                        if (newSended > 0) {
                            if (oldLost) {
                                button.setIcon(ICON_ERROR);
                            } else {
                                button.setIcon(ICON_TRANSMIT);
                            }
                        } else {
                            button.setIcon(ICON_IDLE);
                        }
                    } else if (newSended == 0) {
                        if (newReceived > 0) {
                            button.setIcon(ICON_RECEIVE);
                        } else {
                            if (virtual) {
                                button.setIcon(ICON_RECEIVE_TRANSMITV);
                            } else {
                                button.setIcon(ICON_IDLE);
                            }
                        }
                    } else {
                        if (virtual) {
                            button.setIcon(ICON_RECEIVE_TRANSMITV);
                        } else {
                            button.setIcon(ICON_RECEIVE_TRANSMIT);
                        }
                    }
                }
            }

            float ping = Device.getPingEstimative();
            if (!Float.isNaN(ping)) {
                statusLabel2.setText("Ping: " + (int) ping + " ms");
            } else {
                statusLabel2.setText(" - ");
            }

            //statusLabel3.setText("Lost: " + lost);
            statusLabel3.setText(sended + "|" + received + "|" + lost);
            statusLabel3.setToolTipText("Enviado|Recebido|Perdido");

            while (sendedArray.size() > nbars) {
                sendedArray.remove(0);
                lostArray.remove(0);
                receivedArray.remove(0);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {

            int maxS = 0;
            int maxL = 0;
            int maxR = 0;
            int max;

            for (int i : sendedArray) {
                if (i > maxS) {
                    maxS = i;
                }
            }

            for (int i : lostArray) {
                if (i > maxR) {
                    maxR = i;
                }
            }

            for (int i : receivedArray) {
                if (i > maxR) {
                    maxR = i;
                }
            }

            max = maxS + maxL + maxR;
            float height = getHeight();
            float c = height / max;
            float h, t;

            if (g == null) {
                return;
            }

            g.setColor(Color.black);
            g.fillRect(0, 0, getWidth(), getHeight());

            for (int i = 0; i < sendedArray.size(); i++) {
                t = 0;
                h = lostArray.get(i) * c;
                if (h > 0) {
                    g.setColor(lostColor);
                    g.fillRect((int) (i * size), (int) (height - h - t), (int) size, (int) h + 3);
                }
                t += h;
                h = receivedArray.get(i) * c;
                if (h > 0) {
                    g.setColor(receivedColor);
                    g.fillRect((int) (i * size), (int) (height - h - t), (int) size, (int) h + 3);
                }
                t += h;
                h = sendedArray.get(i) * c;
                if (h > 0) {
                    g.setColor(sendedColor);
                    g.fillRect((int) (i * size), (int) (height - h - t), (int) size, (int) h + 3);
                }
            }
        }
    }

    private static final ImageIcon ICON_OFFLINE;
    private static final ImageIcon ICON_IDLE;
    private static final ImageIcon ICON_ERROR;
    private static final ImageIcon ICON_FULL_ERROR;
    private static final ImageIcon ICON_RECEIVE;
    private static final ImageIcon ICON_TRANSMIT;
    private static final ImageIcon ICON_RECEIVE_TRANSMIT;
    private static final ImageIcon ICON_RECEIVEV;
    private static final ImageIcon ICON_TRANSMITV;
    private static final ImageIcon ICON_RECEIVE_TRANSMITV;

    static {
        ICON_OFFLINE = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/network-offline.png"));
        ICON_IDLE = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/network-idle.png"));
        ICON_ERROR = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/network-error.png"));
        ICON_FULL_ERROR = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/software-update-urgent.png"));

        ICON_RECEIVE = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/network-receive.png"));
        ICON_TRANSMIT = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/network-transmit.png"));
        ICON_RECEIVE_TRANSMIT = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/network-transmit-receive.png"));

        ICON_RECEIVEV = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/virtual-network-receive.png"));
        ICON_TRANSMITV = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/virtual-network-transmit.png"));
        ICON_RECEIVE_TRANSMITV = new ImageIcon(RobotControlPanel.class.getResource("/resources/tango/32x32/status/virtual-network-transmit-receive.png"));
    }

    public static final String VIRTUAL_CONNECTION = "Virtual";
    public static int INSTANCE = 0;
    private TitledBorder border;
    private SimpleSerial serial;
    private Connection connection = null;
    private RobotManager robotManager;
    private boolean connected = false;
    private static Robot robot;
    private MouseListener ml;
    private JButton button = null;
    private boolean virtual = false;

    public RobotControlPanel(RobotManager rm) {
        INSTANCE++;
        serial = new SimpleSerial(57600);
        robot = new Robot();
        robot.add(new HBridge());
        robot.add(new Compass());
        robot.add(new IRProximitySensor());
        robot.add(new ReflectanceSensorArray());
        robot.add(new LED());
        robot.add(new Button());
        robot.add(new Action() { //ação 0
            @Override
            public void putMessage(ByteBuffer data, Robot robot) {

            }
        });
        robot.add(new RotateAction());//ação 1 (como na biblioteca em cpp)

        initComponents();
        border = javax.swing.BorderFactory.createTitledBorder("Robô " + INSTANCE);
//        setBorder(border);
        robotManager = rm;
        connectionStatusGraph.setVisible(false);
        removeButton.setVisible(false);

        statusLabel2.setText("");
        statusLabel3.setText("");

        new Thread("Repaint Thread- " + Thread.activeCount()) {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (connectionStatusGraph instanceof ConnectionStatusGraph) {
                            ((ConnectionStatusGraph) connectionStatusGraph).step();
                        }
                        connectionStatusGraph.repaint();
                        int m = freememPB.getMaximum();
                        int fr = robot.getFreeRam();
                        if (fr == 0) {
                            freememPB.setVisible(false);
                        } else {
                            if (m < fr) {
                                freememPB.setMaximum(fr);
                            }
                            freememPB.setValue(fr);
                            freememPB.setStringPainted(true);
                            freememPB.setString("RAM: " + fr + "/" + m);
                            freememPB.setVisible(true);
                        }

                        Thread.sleep(500);
                    }
                } catch (InterruptedException ex) {
                }

            }
        }.start();

        ml = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!connected) {
                    int i = connectionComboBox.getSelectedIndex();
                    connectionComboBox.removeAllItems();
                    connectionComboBox.addItem(VIRTUAL_CONNECTION);
                    Collection<String> availableDevices = serial.getAvailableDevices();
                    for (String str : availableDevices) {
                        connectionComboBox.addItem(str);
                    }
                    if (i != -1) {
                        connectionComboBox.setSelectedIndex(i);
                    }

                    if (button != null) {
                        if (e == null || e.getSource() == button) {
                            if (availableDevices.size() > 0) {
                                button.setEnabled(true);
                            } else {
                                button.setEnabled(false);
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };

        ml.mouseEntered(null);

        for (Component c : connectionComboBox.getComponents()) {
            c.addMouseListener(ml);
        }

    }

    public void setConnectButton(JButton c) {
        c.addMouseListener(ml);
        button = c;
        ml.mouseEntered(null);
    }

    public static Collection<Class> getAvailableDevices() {
        ArrayList<Class> devices = new ArrayList<>();

//        devices.add(HBridge.class);
        devices.add(Compass.class);
        devices.add(IRProximitySensor.class);
        devices.add(ReflectanceSensorArray.class);
        devices.add(LED.class);
        devices.add(Button.class);

        return devices;
    }

    public static Robot getRobot() {
        return robot;
    }

    @Override
    public String toString() {
        if (border == null) {
            return super.getName();
        } else {
            return border.getTitle();
        }
    }

    public void setConnection(int index) {
        connectionComboBox.setSelectedIndex(index);
    }

    public JComboBox getConnectionComboBox() {
        return connectionComboBox;
    }

    public boolean tryConnect() {
        if (connected) {
            connected = false;

            if (connection != null) {
                GUI.getInstance().getInterpreter().setInterpreterState(Interpreter.STOP);
                connection.closeConnection();
                connection = null;
            }

            connectionStatusGraph.setVisible(false);
            connectionComboBox.setEnabled(true);
            statusLabel.setForeground(Color.black);
            statusLabel.setText("Desconectado");
            statusLabel2.setText("");
            statusLabel3.setText("");

            robot.setMainConnection(null);

            if (button != null) {
                button.setIcon(ICON_OFFLINE);
                button.setToolTipText("Conectar");
            }

            connectButton.setForeground(Color.black);
            connectButton.setText("Conectar");
            connected = false;

            return true;
        } else {
            String str = (String) connectionComboBox.getSelectedItem();
            if (str.equals(VIRTUAL_CONNECTION)) {
                connection = new VirtualConnection();
                virtual = true;
            } else {
                serial.setDefaultPort(str);
                connectionStatusGraph.setVisible(true);
                connection = new VirtualConnection(serial);
                virtual = false;
            }
            statusLabel.setForeground(Color.gray);
            statusLabel.setText("Conectando...");
            statusLabel2.setText("");
            connected = connection.establishConnection();
            if (connected) {
                statusLabel.setForeground(Color.green.darker());
                statusLabel.setText("Conectado");

                if (button != null) {
                    button.setIcon(ICON_IDLE);
                    button.setToolTipText("Desconectar");
                }

                connectButton.setForeground(Color.red.darker());
                connectButton.setText("Desconectar");
                connectionComboBox.setEnabled(false);
            } else {
                statusLabel.setForeground(Color.red.darker());
                statusLabel.setText("Falha");
                statusLabel2.setText("");
                statusLabel3.setText("");
                if (button != null) {
                    button.setIcon(ICON_FULL_ERROR);
                    button.setText("Tentar Novamente");
                }
                connectionComboBox.setEnabled(true);
                return false;
            }

            robot.setMainConnection(connection);

            if (connection instanceof VirtualConnection) {
                VirtualConnection v = (VirtualConnection) connection;
                v.setRobot(robot);
            }
            return true;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        connectionComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        connectButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        connectionStatusGraph = new ConnectionStatusGraph();
        statusLabel2 = new javax.swing.JLabel();
        statusLabel3 = new javax.swing.JLabel();
        freememPB = new javax.swing.JProgressBar();

        jLabel3.setText("jLabel3");

        jLabel1.setText("Conexão:");

        connectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setText("Status:");

        statusLabel.setText("Desconectado");

        connectButton.setText("Conectar");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        removeButton.setText("Remover");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout connectionStatusGraphLayout = new javax.swing.GroupLayout(connectionStatusGraph);
        connectionStatusGraph.setLayout(connectionStatusGraphLayout);
        connectionStatusGraphLayout.setHorizontalGroup(
            connectionStatusGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        connectionStatusGraphLayout.setVerticalGroup(
            connectionStatusGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );

        statusLabel2.setText("-");

        statusLabel3.setText("-");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(connectionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(connectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(connectionStatusGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusLabel)
                            .addComponent(statusLabel2))))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusLabel3)
                    .addComponent(freememPB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addComponent(freememPB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionStatusGraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(connectButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int val = JOptionPane.showConfirmDialog(null, "Deseja realmente remover o robô " + "X" + "?");
        if (val == JOptionPane.YES_OPTION) {
            robotManager.remove(this);
            robotManager.updateUI();
            if (connected) {
                tryConnect(); //desconecta
            }
            INSTANCE--;
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    public void simpleConectButtonActionPerformed() {
        if (connectionComboBox.getItemCount() == 2) {
            if (connected) {
                if (virtual) {
                    tryConnect();
                    connectionComboBox.setSelectedIndex(1);
                    tryConnect();
                } else {
                    tryConnect();
                    connectionComboBox.setSelectedIndex(0);
                    tryConnect();
                }
            } else {
                connectionComboBox.setSelectedIndex(1);
                tryConnect();
            }
        } else if (connectionComboBox.getItemCount() == 1) {
            connectionComboBox.setSelectedIndex(0);
            tryConnect();
        } else {

        }
    }

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        tryConnect();
    }//GEN-LAST:event_connectButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton connectButton;
    private javax.swing.JComboBox connectionComboBox;
    private javax.swing.JPanel connectionStatusGraph;
    private javax.swing.JProgressBar freememPB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusLabel2;
    private javax.swing.JLabel statusLabel3;
    // End of variables declaration//GEN-END:variables
}
