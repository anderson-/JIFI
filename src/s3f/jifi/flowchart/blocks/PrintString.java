/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.flowchart.blocks;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import s3f.jifi.flowchart.parser.syntax.FunctionToken;
import s3f.jifi.flowchart.parser.parameterparser.Argument;
import s3f.magenta.swing.DrawableProcedureBlock;
import s3f.magenta.GraphicObject;
import s3f.magenta.swing.MutableWidgetContainer;
import s3f.magenta.swing.component.Component;
import s3f.magenta.swing.component.SubLineBreak;
import s3f.magenta.swing.component.WidgetLine;
import s3f.magenta.swing.component.TextLabel;
import s3f.magenta.swing.component.Widget;
import s3f.magenta.util.QuickFrame;
import s3f.magenta.sidepanel.Item;
import s3f.util.trafficsimulator.Timer;

/**
 *
 */
public class PrintString extends Procedure implements FunctionToken<PrintString> {

    private static Color myColor = Color.decode("#08B9AC");
    private final Timer timer = new Timer(5);

    public PrintString() {
        super(new Argument("", Argument.TEXT));
    }

    @Deprecated
    public PrintString(String str, String... vars) {
        this();
    }

    private PrintString(Argument[] args) {
        super(args);
        setProcedure(toString()); //TODO: não usar procedimento, implementar void toString(String ident, StringBuilder sb)
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("print(");
        boolean format = true;
        for (Argument arg : getArgs()) {
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

        return new Item("Exibir", myShape, myColor, "Quando executado exibe uma mensagem no Terminal, útil para saber o valor de variáveis em alguns pontos do programa");
    }

    public static MutableWidgetContainer createDrawablePrintString(final PrintString p) {

        //LINES
        final WidgetLine varSelectiteonLine = new WidgetLine() {

            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                Argument a = p.addLineArg(index, Argument.SINGLE_VARIABLE);
                createGenericField(p, a, "v" + index + ":", 80, 25, components, container, WidgetLine.ARG_COMBOBOX | WidgetLine.ARG_TEXTFIELD, false);
                //components.add(new LineBreak(false));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                if (arguments.size() > 0) {
                    String varName = arguments.get(0).toString();
                    sb.append(", ").append(varName);
                }
            }
        };

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Exibir:", true));
                components.add(new SubLineBreak());

                Widget wtfName = createGenericField(p, p.addLineArg(0, Argument.TEXT), "Formato:", 130, 25, components, container, WidgetLine.ARG_TEXTFIELD)[0];
                final JTextField tfName = (JTextField) wtfName.getJComponent();

                tfName.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        container.setString(tfName.getText());
                        container.updateStructure();
                        tfName.requestFocusInWindow();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        container.setString(tfName.getText());
                        container.updateStructure();
                        tfName.requestFocusInWindow();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        container.setString(tfName.getText());
                        container.updateStructure();
                        tfName.requestFocusInWindow();
                    }
                });

                JButton bTmp = new JButton(new ImageIcon(getClass().getResource("/resources/fugue/asterisk.png")));
                components.add(new Widget(bTmp, 25, 25));
                bTmp.setToolTipText("Adicionar referencia");
                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tfName.setText(tfName.getText() + " %v");
                    }
                });
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                try {
                    sb.append("\"");
                    //JTextField 1
                    String str = arguments.iterator().next().toString();
//                    p.resetArgs(new Argument(str, Argument.TEXT));
                    if (!str.isEmpty()) {
                        sb.append(str);
                    }
                    sb.append("\"");
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                }
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(p, myColor) {
            private GeneralPath myShape = new GeneralPath();

            {
                super.shapeStartX = 16;
            }

            @Override
            public void updateStructure() {

                if (boxLabel.startsWith("print")) {
                    clear();
                }

                if (getSize() == 0) {
                    addLine(headerLine);
                }

                String subStr = "%v";
                int ocorrences = (boxLabel.length() - boxLabel.toLowerCase().replace(subStr, "").length()) / subStr.length();;

                ocorrences -= getSize() - 1;
                for (int i = 0; i < ocorrences; i++) {
                    addLine(varSelectiteonLine);
                }

                for (int i = ocorrences; i < 0; i++) {
                    removeLine(getSize() - 1);
                }
            }

            @Override
            public Shape updateShape(Rectangle2D bounds) {
                double mx = bounds.getWidth();
                double my = bounds.getHeight();
                int a = 10;
                int b = 15;
                int c = 20;

                myShape.reset();
                myShape.moveTo(a, 0);
                myShape.lineTo(mx + a, 0);
                myShape.curveTo(mx + a + c, 0, mx + a + c, my, mx + a, my);
                myShape.lineTo(a, my);
                myShape.lineTo(a - b, my / 2);
                myShape.closePath();

                return myShape;
            }

            @Override
            public String getBoxLabel() {
                String str = "print(" + super.getBoxLabel() + ")";
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
