/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugin.cmdpack.begginer;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
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
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.DrawableCommandBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.MutableWidgetContainer;
import robotinterface.drawable.TextLabel;
import robotinterface.drawable.WidgetContainer;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.robot.Robot;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.ResourceManager;
import robotinterface.util.trafficsimulator.Clock;
import robotinterface.util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class Wait extends Procedure implements Classifiable, FunctionToken<Wait> {

    private static Color myColor = Color.decode("#9966FF");
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
    public void begin(ResourceManager rm) throws ExecutionException {
        Clock clock = rm.getResource(Clock.class);
        int d = delay;

        if (var != null) {
            JEP parser = rm.getResource(JEP.class);
            Variable v = parser.getSymbolTable().getVar(var);
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
    public boolean perform(ResourceManager rm) {
        return timer.isConsumed();
    }

    @Override
    public Item getItem() {
        Area myShape = new Area();
        Polygon tmpShape = new Polygon();
        tmpShape.addPoint(0, 0);
        tmpShape.addPoint(7, 0);
        tmpShape.addPoint(7, 18);
        tmpShape.addPoint(0, 18);
        myShape.add(new Area(tmpShape));
        
        tmpShape.reset();
        tmpShape.addPoint(11, 0);
        tmpShape.addPoint(18, 0);
        tmpShape.addPoint(18, 18);
        tmpShape.addPoint(11, 18);
        myShape.add(new Area(tmpShape));
        return new Item("Esperar", myShape, myColor);
    }

    @Override
    public Object createInstance() {
        return new Wait();
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "wait(t);", null);
        fc.setShortDescription("Serve para controlar o tempo de duração de uma atividade do robô. O tempo é regulado pelo\n" +
"parâmetro inteiro t, e é medido em milissegundos. Ao executar um comando e depois usar o wait(t)\n" +
"o robô mantém o seu “estado de máquina” por um determinado tempo, ou seja, ele fica em stand-by\n" +
"durante esse período, sem receber novos comandos."
                + "<p><b>Exemplo:\n" +
"<p>move (127, 127);" +
"<p>wait (1000);" +
"<p>move (0, 0); <\b>\n" +
"<p><p>Faz o robô acionar os motores em velocidade máxima para frente por 1 segundo e depois parar");
        return fc;
    }
    
    @Override
    public String getToken(){
        return "wait";
    }
    
    @Override
    public void toString(String ident, StringBuilder sb) {
        if (var == null){
            setProcedure("wait(" + delay + ")");
        } else {
            setProcedure("wait(" + var + ")");
        }
        super.toString(ident, sb);
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

        final int TEXTFIELD_WIDTH = 70;
        final int TEXTFIELD_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;
        final int INSET_X = 5;
        final int INSET_Y = 5;

        //HEADER LINE

        int headerHeight = 3 * INSET_Y + TEXTFIELD_HEIGHT + 20;
        int headerWidth = 4 * INSET_X + BUTTON_WIDTH + TEXTFIELD_WIDTH + 87;
        final MutableWidgetContainer.WidgetLine headerLine;
        headerLine = new MutableWidgetContainer.WidgetLine(headerWidth, headerHeight) {
            @Override
            protected void createRow(Collection<WidgetContainer.Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
                labels.add(new TextLabel("Espera:", 20, true));

                final JSpinner spinner = new JSpinner();
                spinner.setModel(new SpinnerNumberModel(500, 0, 10000, 50));
                JComboBox combobox = new JComboBox();
                boolean num = true;

                MutableWidgetContainer.autoUpdateValue(spinner);
                MutableWidgetContainer.setAutoFillComboBox(combobox, w);
                
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

                final JButton changeButton1 = new JButton();
                ImageIcon icon = new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/system-search.png"));
                changeButton1.setIcon(icon);
                changeButton1.setToolTipText("Selecionar variável");

//                changeButton1.setEnabled(false);
//                changeButton2.setEnabled(false);

                int x = INSET_X;
                int y = INSET_Y + 40;
                int strLen = 88;
                labels.add(new TextLabel("Tempo (ms):", x + 5, y));

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
                updateWait(str.substring(str.indexOf("(") + 1, str.indexOf(")")), w);
                return str;
            }
        };

        DrawableCommandBlock dcb = new DrawableCommandBlock(w, myColor) {
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

        return dcb;
    }
    
    @Override
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof Wait){
            ((Wait)copy).delay = delay;
            ((Wait)copy).var = var;
        }
        return copy;
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
