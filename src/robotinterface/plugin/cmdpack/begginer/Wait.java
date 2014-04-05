/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugin.cmdpack.begginer;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.nfunk.jep.JEP;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.swing.DrawableProcedureBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.component.TextLabel;
import robotinterface.drawable.swing.component.Component;
import robotinterface.drawable.swing.component.SubLineBreak;
import robotinterface.drawable.swing.component.Widget;
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
        return new Item("Esperar", myShape, myColor, "Mantém o robô se movendo ou faz o programa esperar por alguns milissegundos");
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

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Espera:", true));
                components.add(new SubLineBreak());
                Widget[] widgets = createGenericField(W, W.arg0, "Tempo (ms):", 80, 25, components, container);
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

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(W, myColor) {
            @Override
            public void updateStructure() {
                clear();
                addLine(headerLine);
                boxLabel = getBoxLabel();
            }
        };

        return dcb;
    }
    
    @Override
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof Wait){
            ((Wait)copy).arg0.set(arg0);
        }
        return copy;
    }


    public static void main(String[] args) {
        Wait p = new Wait();
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
