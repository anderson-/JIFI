/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugin.cmdpack.begginer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.HashMap;
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
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.MutableWidgetContainer;
import robotinterface.drawable.TextLabel;
import robotinterface.drawable.WidgetContainer;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.robot.RobotControlPanel;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.robot.connection.Connection;
import robotinterface.robot.Robot;
import robotinterface.robot.device.HBridge;
import robotinterface.interpreter.ExecutionException;
import static robotinterface.plugin.cmdpack.begginer.ReadDevice.createDrawableMove;
import robotinterface.robot.device.Device;
import robotinterface.util.trafficsimulator.Clock;
import robotinterface.util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class Wait extends Procedure implements Classifiable, FunctionToken<Wait> {

    private Timer timer;
    private int delay = 0;
    private String var = null;

    public Wait() {
        timer = new Timer(0);
    }

    public Wait(long ms) {
        this();
        timer.setTick(ms);
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {

        int d = delay;

        if (var != null) {
            Variable v = getParser().getSymbolTable().getVar(var);
            if (v != null && v.hasValidValue()) {
                Object o = v.getValue();
                if (o instanceof Number) {
                    Number n = (Number) o;
                    d = n.intValue();
                }
            }
        }

        timer.setTick(d);
        timer.reset();
        clock.addTimer(timer);
    }

    @Override
    public boolean perform(Robot robot, Clock clock) {
        return timer.isConsumed();
    }

    @Override
    public Item getItem() {
        return new Item("Wait", new RoundRectangle2D.Double(0, 0, 20, 20, 5, 5), Color.decode("#80DE71"));
    }

    @Override
    public Object createInstance() {
        return new Wait();
    }

    @Override
    public String getToken() {
        return "wait";
    }
    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawableMove(this);
        }
        return resource;
    }

    public static MutableWidgetContainer createDrawableMove(final Wait w) {

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
                labels.add(new TextLabel("Espera:", 20, true));

                final JSpinner spinner = new JSpinner();
                spinner.setModel(new SpinnerNumberModel(0, -128, 127, 2));
                JComboBox combobox = new JComboBox();
                boolean num = true;

                if (data != null) {
                    if (data instanceof Wait) {
                        Wait w = (Wait) data;

                        if (w.var != null) {
                            combobox.setSelectedItem(w.var);
                            num = false;
                        } else {
                            spinner.setValue((int) w.delay);
                        }
                    }
                }

                MutableWidgetContainer.setAutoFillComboBox(combobox, w);

                final JButton changeButton1 = new JButton();
                ImageIcon icon = new ImageIcon(getClass().getResource("/resources/tango/16x16/status/dialog-information.png"));
                changeButton1.setIcon(icon);

//                changeButton1.setEnabled(false);
//                changeButton2.setEnabled(false);

                int x = INSET_X;
                int y = INSET_Y + 40;
                int strLen = 65;
                labels.add(new TextLabel("Tempo:", x + 5, y));

                x += strLen;
                y -= 18;

                final WidgetContainer.Widget wspinner1 = new WidgetContainer.Widget(spinner, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                final WidgetContainer.Widget wcombobox1 = new WidgetContainer.Widget(combobox, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(wspinner1);
                widgets.add(wcombobox1);

                x += INSET_Y + TEXTFIELD_WIDTH;

                widgets.add(new WidgetContainer.Widget(changeButton1, x, y, BUTTON_WIDTH, BUTTON_WIDTH));

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

                wspinner1.setDynamic(true);
                wcombobox1.setDynamic(true);

                if (num) {
                    container.addWidget(wspinner1);
                } else {
                    container.addWidget(wcombobox1);
                }
            }

            @Override
            public String getString(Collection<WidgetContainer.Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container) {

                StringBuilder sb = new StringBuilder();

                sb.append("wait(");

                for (WidgetContainer.Widget w : widgets) {
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

        MutableWidgetContainer mwc = new MutableWidgetContainer(Color.decode("#FF6200")) {
            {
                string = w.getProcedure();
                updateLines();
            }

            @Override
            public void updateLines() {
                clear();
                if (string.length() <= 1) {
                    addLine(headerLine, w);
                } else {
                    String str = string.substring(string.indexOf("(") + 1, string.indexOf(")"));
                    updateWait(str, w);
                    addLine(headerLine, w);
                }
                string = getString();
            }
        };

        return mwc;
    }

    private static void updateWait(String args, Wait w) {
        String[] argv = args.split(",");
        if (argv.length == 0) {
            w.delay = 0;
        } else if (argv.length == 1) {
            argv[0] = argv[0].trim();
            if (Character.isLetter(argv[0].charAt(0))) {
                w.var = argv[0];
            } else {
                int v = Integer.parseInt(argv[0].trim());
                w.delay = v;
                w.var = null;
            }
        } else if (argv.length == 2) {
            argv[0] = argv[0].trim();
            if (Character.isLetter(argv[0].charAt(0))) {
                w.var = argv[0];
            } else {
                int v = Integer.parseInt(argv[0].trim());
                w.var = null;
            }
        }
//        w.updateProcedure();
    }

    @Override
    public Wait createInstance(String args) {
        Wait w = new Wait();
        if (!args.isEmpty()) {
            updateWait(args, w);
        }

        return w;
    }

    public static void main(String[] args) {
        Wait p = new Wait();
        Wait.updateWait("100", p);
        p.addBefore(new Procedure("var x, y;"));
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }
}
