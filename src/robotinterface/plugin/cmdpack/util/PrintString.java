/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugin.cmdpack.util;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import robotinterface.algorithm.procedure.Procedure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.nfunk.jep.Variable;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.MutableWidgetContainer;
import robotinterface.drawable.MutableWidgetContainer.WidgetLine;
import robotinterface.drawable.TextLabel;
import robotinterface.drawable.WidgetContainer.Widget;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;

/**
 *
 */
public class PrintString extends Procedure implements FunctionToken<PrintString> {

    private String format;
    private ArrayList<String> varNames;

    public PrintString() {
        varNames = new ArrayList<>();
        format = "";
    }

    public PrintString(String str, boolean b) {
        updateVariables(str);
        setProcedure(toString());
    }

    private void updateVariables(String str) {
        int l = str.lastIndexOf("\"");

        String w = str.substring(l + 1, str.length());

        varNames = new ArrayList<>();

        for (String var : w.split(",")) {
            String vart = var.trim();
            if (vart.endsWith(")")) {
                vart = vart.replace(")", "");
            }
            if (!vart.isEmpty()) {
                varNames.add(vart);
            }
        }

        int s = str.indexOf("\"") + 1;

        this.format = str.substring(s, l);
    }

    private void setFormat(String format) {
        this.format = format;
    }

    private String getFormat() {
        return format;
    }

    private ArrayList<String> getVariables() {
        return varNames;
    }

    public PrintString(String str, String... vars) {
        if (vars != null) {
            varNames = new ArrayList<>();
            varNames.addAll(Arrays.asList(vars));
        }
        this.format = str;
        setProcedure(toString());
    }

    @Override
    public boolean perform(Robot r, Clock clock) {
        String out = new String(format);
        for (String varName : varNames) {
            Variable v = getParser().getSymbolTable().getVar(varName);
            if (v != null && v.hasValidValue()) {
                out = out.replaceFirst("%v", v.getValue().toString());
            } else {
                out = out.replaceFirst("%v", "¿" + varName + "?");
            }
        }

        System.out.println(out);
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("print(\"").append(format.replaceAll("\n", "/n")).append("\"");

        for (String s : varNames) {
            sb.append(", ").append(s);
        }

        sb.append(")");
        return sb.toString();
    }

    @Override
    public Item getItem() {
        return new Item("Exibir", new Rectangle2D.Double(0, 0, 20, 15), color);
    }

    @Override
    public Object createInstance() {
        return new PrintString("Hello Worlld!");
    }
    private Color color = Color.decode("#6693BC");

    public static MutableWidgetContainer createDrawablePrintString(final PrintString p) {

        final int TEXTFIELD_WIDTH = 110;
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
            private int varN = 0;

            @Override
            protected void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
                JComboBox combobVar = new JComboBox();

                MutableWidgetContainer.setAutoFillComboBox(combobVar, p);

                if (data != null) {
                    combobVar.setSelectedItem(data);
                }

//                combobVar.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        JComboBox cb = (JComboBox) e.getSource();
//                        var = (String) cb.getSelectedItem();
//                        if (var != nullvar.equals(RELOAD_VARS_ITEM)) {
//                            cb.removeAllItems();
//                            cb.addItem(RELOAD_VARS_ITEM);
////                            for (String str : ReadDevice.super.getDeclaredVariables()) {
////                                cb.addItem(str);
////                            }
//                        }
//                    }
//                });

                int x = BEGIN_X + INSET_X;
                int y = INSET_Y;

                varN++;
                labels.add(new TextLabel("v" + varN + ":", x, y + 18));
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
                            p.getVariables().add(varName);
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
//                        container.updateLines();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        container.setString(tfName.getText());
//                        container.updateLines();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        container.setString(tfName.getText());
//                        container.updateLines();
                    }
                });

                widgets.add(new Widget(tfName, BEGIN_X + 2 * INSET_X + 65, INSET_Y + 20, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
                JButton bTmp = new JButton(">");

                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        container.setString(tfName.getText());
                        container.updateLines();
                    }
                });

                widgets.add(new Widget(bTmp, BEGIN_X + 3 * INSET_X + 65 + TEXTFIELD_WIDTH, INSET_Y + 20, BUTTON_WIDTH, TEXTFIELD_HEIGHT));
            }

            @Override
            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container) {
                if (widgets.size() > 0) {
                    try {
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
                            if (!str.isEmpty()) {
                                sb.append(str);
                            }
                        }
                        p.setFormat(sb.toString());
                        p.getVariables().clear();

                        return "\"" + sb.toString() + "\"";
                    } catch (NoSuchElementException e) {
                        e.printStackTrace();
                    }
                }
                return "";
            }
        };

        MutableWidgetContainer mwc = new MutableWidgetContainer(Color.decode("#08B9AC")) {
            private GeneralPath myShape = new GeneralPath();

            {
                string = p.getProcedure();
                updateLines();

                super.shapeStartX = 16;
            }

            @Override
            public void updateLines() {

                if (string.startsWith("print")) {
                    p.updateVariables(string);

                    clear();

                    addLine(headerLine, p.getFormat());
                    for (String str : p.getVariables()) {
                        addLine(varSelectiteonLine, str);
                    }
                } else {
                    if (getSize() < 1) {
                        clear();

                        addLine(headerLine, null);
                    }

                    String subStr = "%v";
                    int ocorrences = (string.length() - string.replace(subStr, "").length()) / subStr.length();;
                    ocorrences -= getSize() - 1;
                    for (int i = 0; i < ocorrences; i++) {
                        addLine(varSelectiteonLine, null);
                    }
                }
//
////                System.out.println(super.getString());
//
//                String form = p.getFormat();
////                if (form.startsWith("")){
////                    form = form.substring(1, form.length()-1);
////                }
//
//                System.out.println(form);
//
//                addLine(headerLine, form);
//                for (String str : p.getVariables()) {
//                    System.out.println(str + "*");
//                    addLine(varSelectiteonLine, str);
//                }
//                
//                
//                if (getSize() < 1) {
//                    clear();
//
//                    addLine(headerLine, null);
//                }
//
//                String subStr = "%v";
//                int ocorrences = (string.length() - string.replace(subStr, "").length()) / subStr.length();;
//                ocorrences -= getSize() - 1;
//                for (int i = 0; i < ocorrences; i++) {
//                    addLine(varSelectiteonLine, null);
//                }

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
                return str;
            }
        };

        return mwc;
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
        Procedure p = super.copy(copy);

        if (copy instanceof PrintString) {
            ((PrintString) copy).format = format;
            ((PrintString) copy).varNames.addAll(varNames);
        } else {
        }

        return p;
    }

    public static void main(String[] args) {
        Procedure p = new PrintString("ANDERSON");
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }

    @Override
    public String getToken() {
        return "print";
    }

    @Override
    public PrintString createInstance(String args) {
        return new PrintString(args, true);
    }
}
