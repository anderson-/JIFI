/**
 * @file .java
 * @author Anderson Antunes <anderson.utf@gmail.com>
 *         *seu nome* <*seu email*>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Anderson Antunes <anderson.utf@gmail.com>
 *                       *seu nome* <*seu email*>
 *
 * RobotInterface is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * RobotInterface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RobotInterface. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package robotinterface.plugin.cmdpack.begginer;

import robotinterface.algorithm.procedure.Function;
import robotinterface.algorithm.procedure.If;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.algorithm.procedure.While;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.WidgetContainer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.drawable.DrawableCommandBlock;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.MutableWidgetContainer;
import robotinterface.drawable.TextLabel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.gui.panels.robot.RobotControlPanel;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.plugin.cmdpack.serial.Start;
import robotinterface.plugin.cmdpack.util.PrintString;
import robotinterface.robot.device.Device;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;
import robotinterface.interpreter.ExecutionException;
import static robotinterface.plugin.cmdpack.begginer.Move.createDrawableMove;
import robotinterface.robot.device.Device.TimeoutException;
import robotinterface.util.trafficsimulator.Clock;
import robotinterface.util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class ReadDevice extends Procedure implements GraphicResource, Classifiable, FunctionToken<ReadDevice> {

    private Device device;
    private Class<? extends Device> type;
    private String deviceName = "";
    private String var;

    public ReadDevice() {
    }

    public ReadDevice(Class<? extends Device> type, String var) {
        this.type = type;
        this.var = var;
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        device = robot.getDevice(type);
        if (device != null) {
            //mensagem get padrão 
            byte[] msg = device.defaultGetMessage();
            device.setWaiting();
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
    }

    @Override
    public boolean perform(Robot r, Clock clock) throws ExecutionException {
        try {
            if (device != null && device.isValidRead()) {
                String deviceState = device.stateToString();
                if (!deviceState.isEmpty()) {
                    execute(var + " = " + deviceState);
                }
                return true;
            }
        } catch (TimeoutException ex) {
//            System.err.println("RE-ENVIANDO");
            begin(r, clock);
        }
        return false;
    }
    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawableMove(this);
        }
        return resource;
    }

    public static MutableWidgetContainer createDrawableMove(final ReadDevice rd) {

        final JComboBox comboboxDev = new JComboBox();
        final JComboBox comboboxVar = new JComboBox();
        final HashMap<String, Class<? extends Device>> deviceMap = new HashMap<>();
        for (Class<? extends Device> c : RobotControlPanel.getAvailableDevices()) {
            String str = c.getSimpleName();
            try {
                str = c.newInstance().getName();
            } catch (Exception ex) {
            }
            deviceMap.put(str, c);
            comboboxDev.addItem(str);
        }

//        comboboxDev.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JComboBox cb = (JComboBox) e.getSource();
//                String devName = (String) cb.getSelectedItem();
//                m.deviceName = devName;
//                m.type = deviceMap.get(devName);
//                System.out.println(m.type);
//            }
//        });
//
//        comboboxVar.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JComboBox cb = (JComboBox) e.getSource();
//                String varName = (String) cb.getSelectedItem();
//                m.var = varName;
//            }
//        });

        final int TEXTFIELD_WIDTH = 100;
        final int TEXTFIELD_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;
        final int INSET_X = 5;
        final int INSET_Y = 5;

        //HEADER LINE

        int headerHeight = 4 * INSET_Y + 2 * TEXTFIELD_HEIGHT + 20;
        int headerWidth = 4 * INSET_X + TEXTFIELD_WIDTH + 64;
        final MutableWidgetContainer.WidgetLine headerLine = new MutableWidgetContainer.WidgetLine(headerWidth, headerHeight) {
            @Override
            protected void createRow(Collection<WidgetContainer.Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
                labels.add(new TextLabel("Ler Sensor:", 20, true));

                MutableWidgetContainer.setAutoFillComboBox(comboboxVar, rd);

                if (data != null) {
                    if (data instanceof ReadDevice) {
                        ReadDevice rd = (ReadDevice) data;

                        if (rd.var != null && !rd.var.isEmpty()) {
                            comboboxVar.setSelectedItem(rd.var);
                        }

                        comboboxDev.setSelectedItem(rd.deviceName);
                    }
                }

                int x = INSET_X;
                int y = INSET_Y + 40;
                int strlen = 68;
                labels.add(new TextLabel("Sensor:", x + 5, y));

                x += strlen;
                y -= 18;

                final WidgetContainer.Widget wcombobox1 = new WidgetContainer.Widget(comboboxDev, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(wcombobox1);

                x -= strlen;
                y += 50;

                labels.add(new TextLabel("Variavel:", x + 5, y));

                x += strlen;
                y -= 18;

                final WidgetContainer.Widget wcombobox2 = new WidgetContainer.Widget(comboboxVar, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(wcombobox2);
            }
        };

        DrawableCommandBlock dcb = new DrawableCommandBlock(rd, Color.decode("#FF6200")) {
            {
                string = rd.getProcedure();
                updateLines();
            }

            @Override
            public void updateLines() {
                clear();
                addLine(headerLine, rd);
                string = getString();
            }

            @Override
            public String getString() {
                String devName = (String) comboboxDev.getSelectedItem();
                rd.deviceName = devName;
                rd.type = deviceMap.get(devName);

                String varName = (String) comboboxVar.getSelectedItem();
                rd.var = varName;

                return "read(" + rd.deviceName + "," + rd.var + ")";
            }
        };

        return dcb;
    }

    private static void updateReadDevice(String args, ReadDevice rd) {
        String[] argv = args.split(",");
        if (argv.length == 2) {
            argv[0] = argv[0].trim();
            argv[1] = argv[1].trim();
            rd.deviceName = argv[0];
            rd.var = argv[1];
        }
    }

    @Override
    public String toString() {
        if (var != null && type != null) {
            return var + " = Robot." + type.getSimpleName();
        } else {
            return getCommandName();
        }
    }

    @Override
    public Item getItem() {
        return new Item("Read Device", new RoundRectangle2D.Double(0, 0, 20, 20, 5, 5), Color.decode("#C05480"));
    }

    @Override
    public Object createInstance() {
        return new ReadDevice();
    }

    public static void main(String[] args) {
        ReadDevice p = new ReadDevice();
        updateReadDevice("Distancia, y", p);
        p.addBefore(new Procedure("var x, y;"));
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }

    @Override
    public String getToken() {
        return "read";
    }

    @Override
    public ReadDevice createInstance(String args) {
        ReadDevice rd = new ReadDevice();
        if (!args.isEmpty()) {
            updateReadDevice(args, rd);
        }
        return rd;
    }
}
