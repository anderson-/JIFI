package s3f.jifi.flowchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import s3f.jifi.core.Command;
import s3f.jifi.core.interpreter.ExecutionException;
import s3f.jifi.core.interpreter.ResourceManager;
import s3f.jifi.core.parser.parameterparser.Argument;
import s3f.magenta.DrawingPanel;
import s3f.magenta.GraphicObject;
import s3f.magenta.graphicresource.GraphicResource;
import s3f.magenta.graphicresource.SimpleContainer;
import s3f.magenta.sidepanel.Item;
import s3f.magenta.swing.DrawableProcedureBlock;
import s3f.magenta.swing.MutableWidgetContainer;
import s3f.magenta.swing.component.Component;
import s3f.magenta.swing.component.SubLineBreak;
import s3f.magenta.swing.component.TextLabel;
import s3f.magenta.swing.component.Widget;
import s3f.magenta.swing.component.WidgetLine;

/**
 *
 * @author antunes
 */
public class FunctionBlock extends Procedure {
    //talvez tenha que ser criado um clone completo da função (para recursividade)
    //o parser suporta isso? Acho que não...

    private Function function;

    public FunctionBlock() {
    }

    public FunctionBlock(Function function) {
        setFunction(function);
    }

    public final void setFunction(Function function) {
        if (function == null) {
            return;
        }
        this.function = function;
        System.out.println("selecionando: " + function.getCommandName());
        function.getEnd().setBlockBegin(this);
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public Command step(ResourceManager rm) throws ExecutionException {
        if (function == null) {
            return super.step(rm);
        }

        if (function.isDone()) {
            function.setDone(false);
            function.reset();
            return super.step(rm);
        } else {
            return function.step(rm);
        }
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append(function).append("(....);");
    }

    @Override
    public Item getItem() {
        return new Item("Função Externa", new Rectangle2D.Double(0, 0, 20, 15), Color.decode("#69CD87"));
    }

    @Override
    public Object createInstance() {
        return new FunctionBlock();
    }
    private GraphicObject d = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (d == null) {
            //HEADER LINE
            final WidgetLine headerLine = new WidgetLine() {
                @Override
                public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                    components.add(new TextLabel("Espera:", true));
                    components.add(new SubLineBreak());
                    Widget[] widgets = createGenericField(FunctionBlock.this, FunctionBlock.this.addLineArg(0, 0), "Tempo (ms):", 80, 25, components, container);
                    JSpinner spinner = (JSpinner) widgets[0].widget;
                    spinner.setModel(new SpinnerNumberModel((int) spinner.getValue(), 0, 10000, 50));
                    components.add(new SubLineBreak(true));
                }

                @Override
                public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                    sb.append("wait(");
                    for (int i = 0; i < arguments.size(); i++) {
                        sb.append(arguments.get(i));
                        if (i < arguments.size() - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");
                }
            };

            DrawableProcedureBlock dcb = new DrawableProcedureBlock(this, Color.BLUE) {
                @Override
                public void updateStructure() {
                    clear();
                    addLine(headerLine);
                    boxLabel = getBoxLabel();
                }
            };

            d = dcb;
        }
        return d;
    }
}
