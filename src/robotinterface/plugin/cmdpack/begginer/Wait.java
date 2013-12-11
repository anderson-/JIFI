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
import org.nfunk.jep.JEP;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.swing.DrawableCommandBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.Widget;
import robotinterface.drawable.swing.component.TextLabel;
import robotinterface.drawable.swing.WidgetContainer;
import robotinterface.drawable.swing.component.WidgetLine;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
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
    private Argument arg0;

    public Wait() {
        arg0 = new Argument("0", Argument.NUMBER_LITERAL);
        timer = new Timer(0);
    }

    public Wait(long ms) {
        this();
        timer.setTick(ms);
    }

    private Wait(Argument[] args) {
        this();
        arg0.set(args[0]);
    }

    @Override
    public void begin(ResourceManager rm) throws ExecutionException {
        Clock clock = rm.getResource(Clock.class);
        JEP parser = rm.getResource(JEP.class);
        arg0.parse(parser);
        timer.setTick((long) arg0.getDoubleValue());
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
        setProcedure("wait(" + arg0 + ")");
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

    public static MutableWidgetContainer createDrawableMove(final Wait W) {

        final int TEXTFIELD_WIDTH = 70;
        final int TEXTFIELD_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;
        final int INSET_X = 5;
        final int INSET_Y = 5;

        //HEADER LINE

        int headerHeight = 3 * INSET_Y + TEXTFIELD_HEIGHT + 20;
        int headerWidth = 4 * INSET_X + BUTTON_WIDTH + TEXTFIELD_WIDTH + 87;
        final WidgetLine headerLine = new WidgetLine(headerWidth, headerHeight) {
            @Override
            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
                labels.add(new TextLabel("Espera:", 20, true));

                final JSpinner spinner = new JSpinner();
                spinner.setModel(new SpinnerNumberModel(500, 0, 10000, 50));
                JComboBox combobox = new JComboBox();
                boolean num = true;

                MutableWidgetContainer.autoUpdateValue(spinner);
                MutableWidgetContainer.setAutoFillComboBox(combobox, W);
                
                if (data != null) {
                    if (data instanceof Wait) {
                        Wait w = (Wait) data;

                        if (w.arg0.isVariable()) {
                            combobox.setSelectedItem(w.arg0.toString());
                            num = false;
                        } else {
                            spinner.setValue((int) w.arg0.getDoubleValue());
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

                final Widget wspinner1 = new Widget(spinner, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                final Widget wcombobox1 = new Widget(combobox, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(wspinner1);
                widgets.add(wcombobox1);

                x += INSET_Y + TEXTFIELD_WIDTH;

                widgets.add(new Widget(changeButton1, x, y, BUTTON_WIDTH, BUTTON_WIDTH));

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
            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container) {

                StringBuilder sb = new StringBuilder();

                sb.append("wait(");

                for (Widget w : widgets) {
                    if (container.contains(w)) {
                        JComponent jc = w.getJComponent();
                        if (jc instanceof JComboBox) {
                            JComboBox cb = (JComboBox) jc;
                            Object o = cb.getSelectedItem();
                            if (o != null) {
                                sb.append(o.toString());
                                W.arg0.set(o.toString(), Argument.SINGLE_VARIABLE);
                            }
                        } else if (jc instanceof JSpinner) {
                            JSpinner s = (JSpinner) jc;
                            sb.append(s.getValue());
                            W.arg0.set(s.getValue(), Argument.NUMBER_LITERAL);
                        }
                    }
                }

                String str = sb.toString() + ")";
                return str;
            }
        };

        DrawableCommandBlock dcb = new DrawableCommandBlock(W, myColor) {
            {
                string = W.getProcedure();
                updateLines();
            }

            @Override
            public void updateLines() {
                clear();
                if (string.length() <= 1) {
                    addLine(headerLine, W);
                } else {
                    addLine(headerLine, W);
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
            ((Wait)copy).arg0 = arg0;
        }
        return copy;
    }

//    private static void updateWait(String args, Wait w) {
//        String[] argv = args.split(",");
//        if (argv.length == 0) {
//            w.delay = 0;
//        } else if (argv.length == 1) {
//            argv[0] = argv[0].trim();
//            if (Character.isLetter(argv[0].charAt(0))) {
//                w.var = argv[0];
//            } else {
//                int v = Integer.parseInt(argv[0].trim());
//                w.delay = v;
//                w.var = null;
//            }
//        } else if (argv.length == 2) {
//            argv[0] = argv[0].trim();
//            if (Character.isLetter(argv[0].charAt(0))) {
//                w.var = argv[0];
//            } else {
//                int v = Integer.parseInt(argv[0].trim());
//                w.var = null;
//            }
//        }
////        w.updateProcedure();
//    }


    public static void main(String[] args) {
        Wait p = new Wait();
//        Wait.updateWait("100", p);
        p.addBefore(new Procedure("var x, y;"));
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }

    @Override
    public int getParameters() {
        return 1;
    }

    @Override
    public Wait createInstance(Argument[] args) {
        Wait w = new Wait(args);
        return w;
    }
}
