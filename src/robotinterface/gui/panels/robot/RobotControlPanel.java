/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.robot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import robotinterface.drawable.DrawingPanel;
import robotinterface.robot.Robot;
import robotinterface.robot.action.RotateAction;
import robotinterface.robot.connection.Connection;
import robotinterface.robot.connection.Serial;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.Device;
import robotinterface.robot.device.HBridge;
import robotinterface.robot.device.IRProximitySensor;
import robotinterface.robot.device.ReflectanceSensorArray;
import robotinterface.robot.simulation.VirtualConnection;

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

        @Override
        protected void paintComponent(Graphics g) {
            int nbars = (int) (getWidth() / size);
            {
                if (serial != null && serial.isConnected()) {
                    int s = serial.getSendedPackages();
                    int r = serial.getReceivedPackages();
                    sendedArray.add(s - sended);
                    receivedArray.add(r - received);
                    sended = s;
                    received = r;
                }

                int l = Device.getLostPackages();
                lostArray.add(l - lost);
                lost = l;

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
    public static final String VIRTUAL_CONNECTION = "Virtual";
    public static int INSTANCE = 0;
    private TitledBorder border;
    private Serial serial;
    private Connection connection = null;
    private RobotManager robotManager;
    private boolean connected = false;
    private Robot robot;

    public RobotControlPanel(RobotManager rm) {
        INSTANCE++;
        serial = new Serial(57600);
        robot = new Robot();
        robot.add(new HBridge());
        robot.add(new Compass());
        robot.add(new IRProximitySensor());
        robot.add(new ReflectanceSensorArray());
        robot.add(new RotateAction());

        initComponents();
        border = javax.swing.BorderFactory.createTitledBorder("Robô " + INSTANCE);
        setBorder(border);
        robotManager = rm;
        connectionStatusGraph.setVisible(false);

        statusLabel2.setText("");
        statusLabel3.setText("");

        new Thread("Repaint Thread- " + Thread.activeCount()) {
            @Override
            public void run() {
                try {
                    while (true) {
                        connectionStatusGraph.repaint();
                        Thread.sleep(500);
                    }
                } catch (InterruptedException ex) {
                }
            }
        }.start();

        MouseListener ml = new MouseListener() {
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
                    for (String str : serial.getAvailableDevices()) {
                        connectionComboBox.addItem(str);
                    }
                    if (i != -1) {
                        connectionComboBox.setSelectedIndex(i);
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

    public static Collection<Class> getAvailableDevices() {
        ArrayList<Class> devices = new ArrayList<>();

        devices.add(HBridge.class);
        devices.add(Compass.class);
        devices.add(IRProximitySensor.class);
        devices.add(ReflectanceSensorArray.class);

        return devices;
    }

    public Robot getRobot() {
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

    public boolean tryConnect() {
        if (connected) {
            connected = false;

            if (connection != null) {
                connection.closeConnection();
                connection = null;
            }

            connectionStatusGraph.setVisible(false);
            connectionComboBox.setEnabled(true);
            statusLabel.setForeground(Color.black);
            statusLabel.setText("Desconectado");
            statusLabel2.setText("");
            statusLabel3.setText("");

            connectButton.setForeground(Color.black);
            connectButton.setText("Conectar");
            connected = false;

            return true;
        } else {
            String str = (String) connectionComboBox.getSelectedItem();
            if (str.equals(VIRTUAL_CONNECTION)) {
                connection = new VirtualConnection();
            } else {
                serial.setDefaultPort(str);
                connectionStatusGraph.setVisible(true);
                connection = new VirtualConnection(serial);
            }
            statusLabel.setForeground(Color.gray);
            statusLabel.setText("Conectando...");
            statusLabel2.setText("");
            connected = connection.establishConnection();
            if (connected) {
                statusLabel.setForeground(Color.green.darker());
                statusLabel.setText("Conectado");

                connectButton.setForeground(Color.red.darker());
                connectButton.setText("Desconectar");
                connectionComboBox.setEnabled(false);
            } else {
                statusLabel.setForeground(Color.red.darker());
                statusLabel.setText("Falha");
                statusLabel2.setText("");
                statusLabel3.setText("");
                connectionComboBox.setEnabled(true);
                return false;
            }

            robot.add(connection);

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

        jLabel3.setText("jLabel3");

        setBorder(null);

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
            .addGap(0, 145, Short.MAX_VALUE)
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
                .addGap(0, 31, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel3)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        tryConnect();
    }//GEN-LAST:event_connectButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton connectButton;
    private javax.swing.JComboBox connectionComboBox;
    private javax.swing.JPanel connectionStatusGraph;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusLabel2;
    private javax.swing.JLabel statusLabel3;
    // End of variables declaration//GEN-END:variables
}
