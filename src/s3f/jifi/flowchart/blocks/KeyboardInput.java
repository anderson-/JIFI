/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.flowchart.blocks;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComboBox;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import s3f.jifi.flowchart.syntax.FunctionToken;
import s3f.jifi.flowchart.parameterparser.Argument;
import s3f.magenta.GraphicObject;
import s3f.magenta.swing.DrawableProcedureBlock;
import s3f.magenta.swing.MutableWidgetContainer;
import s3f.magenta.swing.component.Component;
import s3f.magenta.swing.component.SubLineBreak;
import s3f.magenta.swing.component.TextLabel;
import s3f.magenta.swing.component.Widget;
import s3f.magenta.swing.component.WidgetLine;
import s3f.magenta.sidepanel.Item;

/**
 *
 * @author antunes
 */
public class KeyboardInput extends Procedure implements FunctionToken<KeyboardInput> {

    private static Color myColor = Color.decode("#996622");
    private Argument arg0;
    private Argument arg1;

    public KeyboardInput() {
        arg0 = new Argument("", Argument.SINGLE_VARIABLE);
        arg1 = new Argument("", Argument.TEXT);
    }

    private KeyboardInput(Argument[] args) {
        this();
        arg0.set(args[0]);
        if (args.length > 1) {
            arg1.set(args[1]);
        }
    }

    @Override
    public Item getItem() {
        Area myShape = new Area();
        Polygon tmpShape = new Polygon();
        tmpShape.addPoint(4, 0);
        tmpShape.addPoint(16, 0);
        tmpShape.addPoint(12, 16);
        tmpShape.addPoint(0, 16);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(6, 4);
        tmpShape.addPoint(12, 4);
        tmpShape.addPoint(10, 12);
        tmpShape.addPoint(4, 12);
        myShape.subtract(new Area(tmpShape));

        return new Item("Entrada", myShape, myColor, "Mantém o robô se movendo ou faz o programa esperar por alguns milissegundos");
    }

    @Override
    public Object createInstance() {
        return new KeyboardInput();
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "kin(v);", null);
        fc.setShortDescription("...");
        return fc;
    }

    @Override
    public String getToken() {
        return "kin";
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        if (!arg1.getStringValue().isEmpty()) {
            setProcedure("kin(" + arg0 + ",\"" + arg1 + "\")");
        } else {
            setProcedure("kin(" + arg0 + ")");
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

    public static MutableWidgetContainer createDrawableMove(final KeyboardInput k) {

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Entrada:", true));
                components.add(new SubLineBreak());
                JComboBox comboboxVar = new JComboBox();
                MutableWidgetContainer.setAutoFillComboBox(comboboxVar, k, false);
                Widget wcomboboxvar = new Widget(comboboxVar, 100, 25);
                components.add(new TextLabel("Variavel:"));
                components.add(wcomboboxvar);
                container.entangle(k.arg0, wcomboboxvar);
                components.add(new SubLineBreak());
                createGenericField(k, k.arg1, "Texto:", 130, 25, components, container, WidgetLine.ARG_TEXTFIELD);
                components.add(new SubLineBreak(true));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                sb.append("kin(");
                sb.append(arguments.get(0));

                String text = arguments.get(1).getStringValue();

                if (!text.isEmpty()) {
                    sb.append(",\"").append(text).append("\"");
                }
                sb.append(")");
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(k, myColor) {
            private GeneralPath myShape = new GeneralPath();

            {
                super.shapeStartX = 10;
            }

            @Override
            public void updateStructure() {
                clear();
                addLine(headerLine);
                boxLabel = getBoxLabel();
            }

            @Override
            public Shape updateShape(Rectangle2D bounds) {
                double w = bounds.getWidth();
                double h = bounds.getHeight();

                int x = 10;

                myShape.reset();
                myShape.moveTo(x, 0);
                myShape.lineTo(w + 2 * x, 0);
                myShape.lineTo(w + x, h);
                myShape.lineTo(0, h);
                myShape.closePath();

                return myShape;
            }
        };

        return dcb;
    }

    @Override
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof KeyboardInput) {
            ((KeyboardInput) copy).arg0.set(arg0);
            ((KeyboardInput) copy).arg1.set(arg1);
        }
        return copy;
    }

    @Override
    public int getParameters() {
        return -2;
    }

    @Override
    public KeyboardInput createInstance(Argument[] args) {
        KeyboardInput w = new KeyboardInput(args);
        return w;
    }
}
