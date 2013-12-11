/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugin.cmdpack.util;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import robotinterface.algorithm.procedure.Procedure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Variable;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.drawable.DrawableCommandBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.MutableWidgetContainer;
import robotinterface.drawable.MutableWidgetContainer.WidgetLine;
import robotinterface.drawable.TextLabel;
import robotinterface.drawable.WidgetContainer.Widget;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.ResourceManager;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;
import robotinterface.util.trafficsimulator.Timer;

/**
 *
 */
public class PrintString extends Procedure implements FunctionToken<PrintString> {

    private static Color myColor = Color.decode("#08B9AC");
    private ArrayList<Argument> myArgs;
    private final Timer timer = new Timer(2);

    public PrintString() {
        myArgs = new ArrayList<>();
        myArgs.add(new Argument("", Argument.STRING_LITERAL));
    }

    @Deprecated
    public PrintString(String str, String... vars) {
        this();
    }

    private PrintString(Argument[] args) {
        myArgs = new ArrayList<>();
        myArgs.addAll(Arrays.asList(args));
        setProcedure(toString());
    }

//    private void updateVariables(String str) {
//        int l = str.lastIndexOf("\"");
//
//        String w = str.substring(l + 1, str.length());
//
//        if (varNames == null) {
//            varNames = new ArrayList<>();
//        }
//
//        varNames.clear();
//
//        for (String var : w.split(",")) {
//            String vart = var.trim();
//            if (vart.endsWith(")")) {
//                vart = vart.replace(")", "");
//            }
//            if (!vart.isEmpty()) {
//                varNames.add(vart);
//            }
//        }
//
//        int s = str.indexOf("\"") + 1;
//
//        this.format = str.substring(s, l);
//    }
//    private void setFormat(String format) {
//        myArgs.get(0).set(format, Argument.STRING_LITERAL);
//    }

//    private String getFormat() {
//        return myArgs.get(0).getStringValue();
//    }
//
//    private ArrayList<Argument> getVariables() {
//        return myArgs;
//    }

    @Override
    public void begin(ResourceManager rm) throws ExecutionException {
        Clock clock = rm.getResource(Clock.class);
        JEP parser = rm.getResource(JEP.class);
        String out = myArgs.get(0).getStringValue();

        String padps = "%V"; //printAllDecimalPlacesStr
        String ptdps = "%v"; //printTwoDecimalPlacesStr
        String replace;
        DecimalFormat df = new DecimalFormat("#.00");

        for (Argument arg : myArgs) {
            arg.parse(parser);
            int padpi = out.indexOf(padps); //printAllDecimalPlacesIndex
            int ptdpi = out.indexOf(ptdps); //printTwoDecimalPlacesIndex
            boolean printTwoDecimalPlaces = ((padpi == -1) ? true
                    : ((ptdpi == -1) ? false
                    : (ptdpi < padpi)));
            replace = (printTwoDecimalPlaces) ? ptdps : padps;
//            System.out.println(padpi);
//            System.out.println(ptdpi);
//            System.out.println(printTwoDecimalPlaces);
//            System.out.println(replace);

            Object value = arg.getValue();
            if (printTwoDecimalPlaces && value instanceof Double) {
                out = out.replaceFirst(replace, df.format((Double) value));
            } else {
                out = out.replaceFirst(replace, value.toString());
            }
        }

        System.out.println(out);
        timer.reset();
        clock.addTimer(timer);
    }

    @Override
    public boolean perform(ResourceManager rm) {
        return timer.isConsumed();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("print(");
        boolean format = true;
        for (Argument arg : myArgs) {
            if (format) {
                sb.append("\"");
                sb.append(arg.getStringValue());
                sb.append("\"");
                format = false;
            } else {
                sb.append(", ").append(arg);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Item getItem() {
        GeneralPath tmpShape = new GeneralPath();
        double mx = 10;
        double my = 12;
        double a = 4;
        double b = 8;

        tmpShape.reset();
        tmpShape.moveTo(a, 0);
        tmpShape.lineTo(mx + a, 0);
        tmpShape.curveTo(mx + b + a, 0, mx + b + a, my, mx + a, my);
        tmpShape.lineTo(a, my);
        tmpShape.lineTo(0, my / 2);
        tmpShape.closePath();

        Area myShape = new Area();
        myShape.add(new Area(tmpShape));
        myShape.subtract(new Area(new Ellipse2D.Double(5, 3, 11, 6)));
        //myShape.exclusiveOr(new Area( new Ellipse2D.Double(8, 3, 6, 6))); //oh hell no!

        return new Item("Exibir", myShape, myColor);
    }

    public static MutableWidgetContainer createDrawablePrintString(final PrintString p) {

        final int TEXTFIELD_WIDTH = 130;
        final int TEXTFIELD_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;
        final int BEGIN_X = 20;
        final int INSET_X = 5;
        final int INSET_Y = 5;

        //LINES
        int varSelectiteonLineWidth = 4 * INSET_X + 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH;
        int varSelectiteonLineHeight = 2 * INSET_Y + TEXTFIELD_HEIGHT;
        final WidgetLine varSelectiteonLine = new WidgetLine(varSelectiteonLineWidth, varSelectiteonLineHeight) {
            private String var;

            @Override
            protected void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
                JComboBox combobVar = new JComboBox();
                combobVar.setFocusable(false);

                MutableWidgetContainer.setAutoFillComboBox(combobVar, p);

                if (data != null) {
                    combobVar.setSelectedItem(data);
                }

                int x = BEGIN_X + INSET_X;
                int y = INSET_Y;

                labels.add(new TextLabel("v" + container.getSize() + ":", x, y + 18));
                x += 25;
                //x += 2 * INSET_X + BUTTON_WIDTH;
                widgets.add(new Widget(combobVar, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
            }

            @Override
            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container) {
                if (widgets.size() > 0) {
                    Widget widget = widgets.iterator().next();
                    JComponent jComponent = widget.getJComponent();
                    if (jComponent instanceof JComboBox) {
                        Object o = ((JComboBox) jComponent).getSelectedItem();
                        if (o != null) {
                            String varName = o.toString();
                            p.myArgs.add(new Argument(varName, Argument.SINGLE_VARIABLE));
                            return ", " + varName;
                        }
                    }
                }
                return "";
            }
        };

        //HEADER LINE
        int headerHeight = 2 * INSET_Y + TEXTFIELD_HEIGHT + 20;
        int headerWidth = BEGIN_X + 4 * INSET_X + 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH;
        final WidgetLine headerLine = new WidgetLine(headerWidth, headerHeight) {
            @Override
            protected void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
                labels.add(new TextLabel("Exibir:", 20, true));
                labels.add(new TextLabel("Formato:", BEGIN_X + INSET_X, 3 * INSET_Y + 28));
                final JTextField tfName = new JTextField();

                if (data != null) {
                    tfName.setText(data.toString());
                }

                tfName.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        container.setString(tfName.getText());
                        container.updateLines();
                        tfName.requestFocusInWindow();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        container.setString(tfName.getText());
                        container.updateLines();
                        tfName.requestFocusInWindow();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        container.setString(tfName.getText());
                        container.updateLines();
                        tfName.requestFocusInWindow();
                    }
                });

                widgets.add(new Widget(tfName, BEGIN_X + 65, INSET_Y + 20, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
                JButton bTmp = new JButton(new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/system-search.png")));
                bTmp.setToolTipText("Selecionar variável");
                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tfName.setText(tfName.getText() + " %v");
                    }
                });

                widgets.add(new Widget(bTmp, BEGIN_X + INSET_X + 65 + TEXTFIELD_WIDTH, INSET_Y + 20, BUTTON_WIDTH, TEXTFIELD_HEIGHT));
            }

            @Override
            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container) {
                if (widgets.size() > 0) {
                    try {
                        p.myArgs.clear();
                        StringBuilder sb = new StringBuilder();
                        Iterator<Widget> iterator = widgets.iterator();
                        String str;
                        Widget tmpWidget;
                        JComponent jComponent;
                        //JTextField 1
                        tmpWidget = iterator.next();
                        jComponent = tmpWidget.getJComponent();
                        if (jComponent instanceof JTextField) {
                            str = ((JTextField) jComponent).getText();
                            p.myArgs.add(new Argument(str, Argument.STRING_LITERAL));
                            if (!str.isEmpty()) {
                                sb.append(str);
                            }
                        }

                        return "\"" + sb.toString() + "\"";
                    } catch (NoSuchElementException e) {
                        e.printStackTrace();
                    }
                }
                return "";
            }
        };

        DrawableCommandBlock dcb = new DrawableCommandBlock(p, myColor) {
            private GeneralPath myShape = new GeneralPath();

            {
                string = p.getProcedure();
                updateLines();

                super.shapeStartX = 16;
            }

            @Override
            public void updateLines() {

                if (string.startsWith("print")) {
//                    p.updateVariables(string);??????????????????????
                    

                    clear();
                } else {
//                    p.myArgs.clear();
                }

                if (getSize() == 0) {
                    clear();
                    addLine(headerLine, p.myArgs.get(0).getStringValue());
                }

                String subStr = "%v";
                int ocorrences = (string.length() - string.toLowerCase().replace(subStr, "").length()) / subStr.length();;
                if (p.myArgs.size() > 1) {
                    ocorrences -= (p.myArgs.size() -1);
                    for (int i = 1; i < p.myArgs.size(); i++) {
                        addLine(varSelectiteonLine, p.myArgs.get(i).getVariableName());
                    }
                } else {
                    ocorrences -= getSize() - 1;
                    for (int i = 0; i < ocorrences; i++) {
                        addLine(varSelectiteonLine, null);
                    }
                }

                for (int i = ocorrences; i < 0; i++) {
                    removeLine(getSize() - 1);
                }
            }

            @Override
            public Shape updateShape(Rectangle2D bounds) {
                double mx = bounds.getWidth();
                double my = bounds.getHeight();
                double a = 15;
                double b = 20;

                myShape.reset();
                myShape.moveTo(a, 0);
                myShape.lineTo(mx + a, 0);
                myShape.curveTo(mx + b + a, 0, mx + b + a, my, mx + a, my);
                myShape.lineTo(a, my);
                myShape.lineTo(0, my / 2);
                myShape.closePath();

                return myShape;
            }

            @Override
            public String getString() {
                String str = "print(" + super.getString() + ")";
                p.setProcedure(str);
                return str;
            }
        };

        return dcb;
    }
    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawablePrintString(this);
        }
        return resource;
    }

    @Override
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof PrintString) {
            ((PrintString) copy).myArgs.clear();
            ((PrintString) copy).myArgs.addAll(myArgs);
        }
        return copy;
    }

    @Override
    public Object createInstance() {
        return new PrintString("");
    }

    @Override
    public int getParameters() {
        return -2;
    }

    @Override
    public PrintString createInstance(Argument[] args) {
        return new PrintString(args);
    }

    public static void main(String[] args) {
        Procedure p = new PrintString("ANDERSON");
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "print(", null);
        fc.setShortDescription("Função exibir.");
        ArrayList<ParameterizedCompletion.Parameter> params = new ArrayList<>();
        params.add(new ParameterizedCompletion.Parameter("var", "format", true));
        fc.setParams(params);
        return fc;
    }

    @Override
    public String getToken() {
        return "print";
    }
}
