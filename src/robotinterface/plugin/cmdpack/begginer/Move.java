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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Variable;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.swing.DrawableCommandBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.component.TextLabel;
import robotinterface.drawable.swing.WidgetContainer;
import robotinterface.drawable.swing.component.Widget;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.drawable.swing.component.WidgetLine;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.ResourceManager;
import robotinterface.plugin.cmdpack.util.PrintString;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Device;
import robotinterface.robot.device.HBridge;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Procedimento de mover o robô.
 */
public class Move extends Procedure implements Classifiable, FunctionToken<Move> {

    private static Color myColor = Color.decode("#47B56C");
    private Argument arg0;
    private Argument arg1;
    private HBridge hBridge = null;

    public Move() {
        arg0 = new Argument("0", Argument.NUMBER_LITERAL);
        arg1 = new Argument("0", Argument.NUMBER_LITERAL);
    }

    public Move(int m1, int m2) {
        arg0 = new Argument(m1, Argument.NUMBER_LITERAL);
        arg1 = new Argument(m2, Argument.NUMBER_LITERAL);
        updateProcedure();
    }

    public Move(Argument[] args) {
        this();
        arg0.set(args[0]);
        arg1.set(args[1]);
    }

    public void updateProcedure() {
        setProcedure("move(" + arg0 + "," + arg1 + ")");
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        updateProcedure();
        super.toString(ident, sb);
    }

    @Override
    public void begin(ResourceManager rm) throws ExecutionException {
        Robot robot = rm.getResource(Robot.class);
        hBridge = robot.getDevice(HBridge.class);

        if (hBridge != null) {
            JEP parser = rm.getResource(JEP.class);
            arg0.parse(parser);
            arg1.parse(parser);

            byte t1 = (byte) arg0.getDoubleValue();
            byte t2 = (byte) arg1.getDoubleValue();

            hBridge.setWaiting();
            hBridge.setFullState(t1, t2);
            robot.setRightWheelSpeed(t2);
            robot.setLeftWheelSpeed(t1);
        }
    }

    @Override
    public boolean perform(ResourceManager rm) throws ExecutionException {
        try {
            if (hBridge != null && hBridge.isValidRead()) {
//                String deviceState = device.stateToString();
//                if (!deviceState.isEmpty()) {
//                    execute(var + " = " + deviceState);
//                }
                return true;
            }
        } catch (Device.TimeoutException ex) {
//            System.err.println("RE-ENVIANDO hBridge");
            begin(rm);
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

    public static MutableWidgetContainer createDrawableMove(final Move m) {

        final int TEXTFIELD_WIDTH = 80;
        final int TEXTFIELD_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;
        final int INSET_X = 5;
        final int INSET_Y = 5;

        //HEADER LINE
        int headerHeight = 4 * INSET_Y + 2 * TEXTFIELD_HEIGHT + 20;
        int headerWidth = 4 * INSET_X + 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH;
        final WidgetLine headerLine = new WidgetLine(headerWidth, headerHeight) {
            @Override
            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
                labels.add(new TextLabel("Mover:", 20, true));

                final JSpinner spinner1 = new JSpinner();
                final JSpinner spinner2 = new JSpinner();
                spinner1.setModel(new SpinnerNumberModel(0, -128, 127, 2));
                spinner2.setModel(new SpinnerNumberModel(0, -128, 127, 2));
                JComboBox combobox1 = new JComboBox();
                JComboBox combobox2 = new JComboBox();
                boolean num1 = true, num2 = true;

                MutableWidgetContainer.autoUpdateValue(spinner1);
                MutableWidgetContainer.autoUpdateValue(spinner2);

                MutableWidgetContainer.setAutoFillComboBox(combobox1, m);
                MutableWidgetContainer.setAutoFillComboBox(combobox2, m);

                if (data != null) {
                    if (data instanceof Move) {
                        Move m = (Move) data;

                        if (m.arg0.isVariable()) {
                            combobox1.setSelectedItem(m.arg0.getVariableName());
                            num1 = false;
                        } else {
                            spinner1.setValue((int) m.arg0.getDoubleValue());
                        }

                        if (m.arg1.isVariable()) {
                            combobox2.setSelectedItem(m.arg1.getVariableName());
                            num2 = false;
                        } else {
                            spinner2.setValue((int) m.arg1.getDoubleValue());
                        }
                    }
                }

                final JButton changeButton1 = new JButton();
                final JButton changeButton2 = new JButton();
                ImageIcon icon = new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/system-search.png"));
                changeButton1.setIcon(icon);
                changeButton1.setToolTipText("Selecionar variável");
                changeButton2.setIcon(icon);
                changeButton1.setToolTipText("Selecionar variável");

//                changeButton1.setEnabled(false);
//                changeButton2.setEnabled(false);
                int x = INSET_X;
                int y = INSET_Y + 40;
                labels.add(new TextLabel("V1:", x + 5, y));

                x += 26;
                y -= 18;

                final Widget wspinner1 = new Widget(spinner1, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                final Widget wcombobox1 = new Widget(combobox1, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(wspinner1);
                widgets.add(wcombobox1);

                x += INSET_Y + TEXTFIELD_WIDTH;

                widgets.add(new Widget(changeButton1, x, y, BUTTON_WIDTH, BUTTON_WIDTH));

                x -= INSET_Y + TEXTFIELD_WIDTH;

                x -= 26;
                y += 50;

                labels.add(new TextLabel("V2:", x + 5, y));

                x += 26;
                y -= 18;

                final Widget wspinner2 = new Widget(spinner2, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                final Widget wcombobox2 = new Widget(combobox2, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(wspinner2);
                widgets.add(wcombobox2);

                x += INSET_Y + TEXTFIELD_WIDTH;

                widgets.add(new Widget(changeButton2, x, y, BUTTON_WIDTH, BUTTON_WIDTH));

                x -= INSET_Y + TEXTFIELD_WIDTH;

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

                changeButton2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (container.contains(wspinner2)) {
                            container.removeWidget(wspinner2);
                            container.addWidget(wcombobox2);
                        } else {
                            container.removeWidget(wcombobox2);
                            container.addWidget(wspinner2);
                        }
                    }
                });

                wspinner1.setDynamic(true);
                wcombobox1.setDynamic(true);
                wspinner2.setDynamic(true);
                wcombobox2.setDynamic(true);

                if (num1) {
                    container.addWidget(wspinner1);
                } else {
                    container.addWidget(wcombobox1);
                }

                if (num2) {
                    container.addWidget(wspinner2);
                } else {
                    container.addWidget(wcombobox2);
                }
            }

            @Override
            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container) {

                StringBuilder sb = new StringBuilder();

                sb.append("move(");

                boolean firstArg = true;

                for (Widget w : widgets) {
                    if (container.contains(w)) {
                        JComponent jc = w.getJComponent();
                        if (jc instanceof JComboBox) {
                            JComboBox cb = (JComboBox) jc;
                            Object o = cb.getSelectedItem();
                            if (o != null) {
                                sb.append(o.toString());
                                sb.append(" ");
                                if (firstArg) {
                                    m.arg0.set(o.toString(), Argument.SINGLE_VARIABLE);
                                    firstArg = false;
                                } else {
                                    m.arg1.set(o.toString(), Argument.SINGLE_VARIABLE);
                                }
                            }
                        } else if (jc instanceof JSpinner) {
                            JSpinner s = (JSpinner) jc;
                            sb.append(s.getValue());
                            sb.append(" ");
                            if (firstArg) {
                                m.arg0.set(s.getValue(), Argument.NUMBER_LITERAL);
                                firstArg = false;
                            } else {
                                m.arg1.set(s.getValue(), Argument.NUMBER_LITERAL);
                            }
                        }
                    }
                }

                String str = sb.toString().trim().replace(" ", ",") + ")";
//                updateMove(str.substring(str.indexOf("(") + 1, str.indexOf(")")), m);
                return str;
            }
        };

        DrawableCommandBlock dcb = new DrawableCommandBlock(m, myColor) {
            {
                string = m.getProcedure();
                updateLines();
            }

            @Override
            public void updateLines() {
                clear();
                if (string.length() <= 1) {
                    addLine(headerLine, m);
                } else {
//                    String str = string.substring(string.indexOf("(") + 1, string.indexOf(")"));
//                    updateMove(str, m);
                    addLine(headerLine, m);
                }
                string = getString();
            }
        };

        return dcb;
    }

    @Override
    public Item getItem() {
        Area myShape = new Area();
        Polygon tmpPoli = new Polygon();
        tmpPoli.addPoint(0, 0);
        tmpPoli.addPoint(13, 10);
        tmpPoli.addPoint(0, 20);
        myShape.add(new Area(tmpPoli));
        tmpPoli.reset();
        tmpPoli.addPoint(6, 0);
        tmpPoli.addPoint(20, 10);
        tmpPoli.addPoint(6, 20);
        myShape.exclusiveOr(new Area(tmpPoli));
        return new Item("Mover", myShape, myColor);
    }

    @Override
    public Object createInstance() {
        return new Move();
    }

    @Override
    public int getParameters() {
        return 2;
    }

    @Override
    public Move createInstance(Argument[] args) {
        return new Move(args);
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "move(r1,r2);", null);
        fc.setShortDescription("Move o robô, sendo r1 e r2 a roda esquerda e direita respectivamente. Cada roda recebe um valor\n"
                + "inteiro de velocidade relativa, sendo 0 a roda parada e 127 a velocidade máxima do motor.\n"
                + "Utilizando valores negativos em r1 ou r2 faz a respectiva roda girar no sentido oposto.");
        return fc;
    }

    @Override
    public String getToken() {
        return "move";
    }

    @Override
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof Move) {
            ((Move) copy).arg0 = arg0;
            ((Move) copy).arg1 = arg1;
        }
        return copy;
    }

//    private static void updateMove(String str, Move m) {
//        String[] argv = str.split(",");
//        if (argv.length == 0) {
//            m.m1 = (byte) 0;
//            m.m2 = (byte) 0;
//        } else if (argv.length == 1) {
//            argv[0] = argv[0].trim();
//            if (Character.isLetter(argv[0].charAt(0))) {
//                m.var1 = argv[0];
//                m.var2 = argv[0];
//            } else {
//                int v = Integer.parseInt(argv[0].trim());
//                m.m1 = (byte) v;
//                m.m2 = (byte) v;
//                m.var1 = null;
//                m.var2 = null;
//            }
//        } else if (argv.length == 2) {
//            argv[0] = argv[0].trim();
//            if (Character.isLetter(argv[0].charAt(0))) {
//                m.var1 = argv[0];
//            } else {
//                int v = Integer.parseInt(argv[0].trim());
//                m.m1 = (byte) v;
//                m.var1 = null;
//            }
//
//            argv[1] = argv[1].trim();
//            if (Character.isLetter(argv[1].charAt(0))) {
//                m.var2 = argv[1];
//            } else {
//                int v = Integer.parseInt(argv[1].trim());
//                m.m2 = (byte) v;
//                m.var2 = null;
//            }
//        }
//        m.updateProcedure();
//    }

//    @Override
//    public Move createInstance(String args) {
//        Move m = new Move(0, 0);
//        if (!args.isEmpty()) {
//            updateMove(args, m);
//        }
//
//        return m;
//        //return new ParseErrorProcedure(this, args);
//    }
    public static void main(String[] args) {
        Move p = new Move();
//        Move.updateMove("x", p);
        p.addBefore(new Procedure("var x, y;"));
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }
}
