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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.nfunk.jep.Variable;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.DrawableCommandBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.MutableWidgetContainer;
import robotinterface.drawable.TextLabel;
import robotinterface.drawable.WidgetContainer;
import robotinterface.drawable.WidgetContainer.Widget;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.plugin.cmdpack.util.PrintString;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Device;
import robotinterface.robot.device.HBridge;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Procedimento de mover o robô.
 */
public class Rotate extends Procedure implements GraphicResource, Classifiable, FunctionToken<Rotate> {

    private static Color myColor = Color.decode("#FF8533");
    private int angle;
    private String var = null;

    public Rotate() {
        angle = 0;
    }

    public Rotate(int angle) {
        super();
        this.angle = angle;
        updateProcedure();
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
        updateProcedure();
    }

    public void updateProcedure() {
        setProcedure("rotate(" + ((var != null) ? var : angle) + ")");
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        updateProcedure();
        super.toString(ident, sb);
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
//        hBridge = robot.getDevice(HBridge.class);
//        if (hBridge != null) {
//
//            byte t1 = angle;
//            byte t2 = m2;
//
//            if (var != null) {
//                Variable v = getParser().getSymbolTable().getVar(var);
//                if (v != null && v.hasValidValue()) {
//                    Object o = v.getValue();
//                    if (o instanceof Number) {
//                        Number n = (Number) o;
//                        t1 = n.byteValue();
//                    }
//                }
//            }
//
//            if (var2 != null) {
//                Variable v = getParser().getSymbolTable().getVar(var2);
//                if (v != null && v.hasValidValue()) {
//                    Object o = v.getValue();
//                    if (o instanceof Number) {
//                        Number n = (Number) o;
//                        t2 = n.byteValue();
//                    }
//                }
//            }
//
//            hBridge.setWaiting();
//            hBridge.setFullState(t1, t2);
//        }
    }

    @Override
    public boolean perform(Robot r, Clock clock) throws ExecutionException {
//        try {
//            if (hBridge != null && hBridge.isValidRead()) {
////                String deviceState = device.stateToString();
////                if (!deviceState.isEmpty()) {
////                    execute(var + " = " + deviceState);
////                }
//                return true;
//            }
//        } catch (Device.TimeoutException ex) {
////            System.err.println("RE-ENVIANDO hBridge");
//            begin(r, clock);
//        }
//        return false;
        return true;
    }
    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawableRotate(this);
        }
        return resource;
    }

    public static MutableWidgetContainer createDrawableRotate(final Rotate r) {

        final int TEXTFIELD_WIDTH = 80;
        final int TEXTFIELD_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;
        final int INSET_X = 5;
        final int INSET_Y = 5;

        //HEADER LINE

        int headerHeight = 3 * INSET_Y + TEXTFIELD_HEIGHT + 20;
        int headerWidth = 4 * INSET_X + BUTTON_WIDTH + TEXTFIELD_WIDTH + 64;
        final MutableWidgetContainer.WidgetLine headerLine = new MutableWidgetContainer.WidgetLine(headerWidth, headerHeight) {
            @Override
            protected void createRow(Collection<WidgetContainer.Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
                labels.add(new TextLabel("Girar:", 20, true));

                final JSpinner spinner1 = new JSpinner();
                spinner1.setModel(new SpinnerNumberModel(0, -360, 360, 2));
                JComboBox combobox1 = new JComboBox();
                boolean num1 = true;

                MutableWidgetContainer.setAutoFillComboBox(combobox1, r);

                if (data != null) {
                    if (data instanceof Rotate) {
                        Rotate m = (Rotate) data;

                        if (m.var != null) {
                            combobox1.setSelectedItem(m.var);
                            num1 = false;
                        } else {
                            spinner1.setValue(m.angle);
                        }
                    }
                }

                final JButton changeButton1 = new JButton();
                ImageIcon icon = new ImageIcon(getClass().getResource("/resources/tango/16x16/status/dialog-information.png"));
                changeButton1.setIcon(icon);

                int x = INSET_X;
                int y = INSET_Y + 40;
                int strLen = 64;
                labels.add(new TextLabel("Ângulo:", x + 5, y));

                x += strLen;
                y -= 18;

                final WidgetContainer.Widget wspinner1 = new WidgetContainer.Widget(spinner1, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                final WidgetContainer.Widget wcombobox1 = new WidgetContainer.Widget(combobox1, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(wspinner1);
                widgets.add(wcombobox1);

                x += INSET_Y + TEXTFIELD_WIDTH;

                widgets.add(new WidgetContainer.Widget(changeButton1, x, y, BUTTON_WIDTH, BUTTON_WIDTH));

                x -= INSET_Y + TEXTFIELD_WIDTH;

                x -= strLen;
                y += 50;

                changeButton1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (container.contains(wspinner1)) {
                            container.removeWidget(wspinner1);
                            container.addWidget(wcombobox1);
                        } else {
                            container.removeWidget(wcombobox1);
                            container.addWidget(wspinner1);
                        }
                    }
                });

                wspinner1.setDynamic(true);
                wcombobox1.setDynamic(true);

                if (num1) {
                    container.addWidget(wspinner1);
                } else {
                    container.addWidget(wcombobox1);
                }

            }

            @Override
            public String getString(Collection<WidgetContainer.Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container) {

                StringBuilder sb = new StringBuilder();

                sb.append("rotate(");

                for (Widget w : widgets) {
                    if (container.contains(w)) {
                        JComponent jc = w.getJComponent();
                        if (jc instanceof JComboBox) {
                            JComboBox cb = (JComboBox) jc;
                            Object o = cb.getSelectedItem();
                            if (o != null) {
                                sb.append(o.toString());
                            }
                        } else if (jc instanceof JSpinner) {
                            JSpinner s = (JSpinner) jc;
                            sb.append(s.getValue());
                        }
                    }
                }

                String str = sb.toString() + ")";
                return str;
            }
        };

        DrawableCommandBlock dcb = new DrawableCommandBlock(r, myColor) {
            {
                string = r.getProcedure();
                updateLines();
            }

            @Override
            public void updateLines() {
                clear();
                if (string.length() <= 1) {
                    addLine(headerLine, r);
                } else {
                    String str = string.substring(string.indexOf("(") + 1, string.indexOf(")"));
                    updateRotate(str, r);
                    addLine(headerLine, r);
                }
                string = getString();
            }
        };

        return dcb;
    }

    @Override
    public Item getItem() {
        return new Item("Girar", new Ellipse2D.Double(0, 0, 20, 20), myColor);
    }

    @Override
    public Object createInstance() {
        return new Rotate();
    }

    @Override
    public String getToken() {
        return "rotate";
    }

    private static void updateRotate(String str, Rotate m) {
        String[] argv = str.split(",");
        if (argv.length == 0) {
            m.angle = 0;
        } else if (argv.length == 1) {
            argv[0] = argv[0].trim();
            if (Character.isLetter(argv[0].charAt(0))) {
                m.var = argv[0];
            } else {
                int a = Integer.parseInt(argv[0].trim());
                m.angle = a;
                m.var = null;
            }
        }
        m.updateProcedure();
    }

    @Override
    public Rotate createInstance(String args) {
        Rotate r = new Rotate(0);
        if (!args.isEmpty()) {
            updateRotate(args, r);
        }

        return r;
        //return new ParseErrorProcedure(this, args);
    }

    public static void main(String[] args) {
        Rotate p = new Rotate();
        Rotate.updateRotate("x", p);
        p.addBefore(new Procedure("var x, y;"));
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }
}
