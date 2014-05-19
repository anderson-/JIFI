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
package robotinterface.algorithm.procedure;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.Variable;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.DrawableProcedureBlock;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.component.Component;
import robotinterface.drawable.swing.component.Space;
import robotinterface.drawable.swing.component.SubLineBreak;
import robotinterface.drawable.swing.component.TextLabel;
import robotinterface.drawable.swing.component.Widget;
import robotinterface.drawable.swing.component.WidgetLine;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.GUI;
import robotinterface.gui.panels.FlowchartPanel;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.Expression;
import robotinterface.interpreter.ResourceManager;
import robotinterface.plugin.Pluggable;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Comando genérico com suporte à variaveis.
 */
public class Procedure extends Command implements Classifiable, Pluggable {

    private static Object nill = new Object();
    private static Color myColor = Color.decode("#ACD630");
    private ArrayList<String> names;
    private ArrayList<Object> values;
    private ArrayList<Argument> myArgs;
    private String procedure;
    private boolean varArgs;
    private Pattern p = Pattern.compile("(\\S+)(\\+\\+)");

    public Procedure() { //tornar private 
        varArgs = true;
        procedure = "";
        names = new ArrayList<>();
        values = new ArrayList<>();
        myArgs = new ArrayList<>();
    }

    /**
     * Uso restrito ao parser.
     *
     * @param procedure
     * @deprecated
     */
    @Deprecated
    public Procedure(String procedure) {
        this();
        setProcedure(procedure);
    }

    public Procedure(Procedure p) {
        this(p.procedure);
    }

    protected Procedure(boolean varArgs) {
        this();
        this.varArgs = varArgs;
    }

    protected Procedure(Argument... args) {
        this();
        varArgs = true;
        this.myArgs.addAll(Arrays.asList(args));
    }

    public final String getProcedure() {
        return procedure;
    }

    public final void setProcedure(String procedure) {
        this.procedure = procedure;
        updateVariables();
    }

    @Override
    public boolean perform(ResourceManager rm) throws ExecutionException {
        evaluate(rm);
        return true;
    }

    private void updateVariables() {
        names.clear();
        values.clear();
        for (String str : procedure.split(";")) {
            str = str.trim();
            if (str.startsWith("var")) {
                str = str.replaceFirst("var ", "");
                String varName;
                Object varValue;
                for (String declaration : str.split(",")) {
                    int eq = declaration.indexOf("=");
                    if (eq > 0) {
                        varName = declaration.substring(0, eq).trim();
                        varValue = declaration.substring(eq + 1);
                    } else {
                        varName = declaration.trim();
                        varValue = null;
                    }
                    names.add(varName);
                    values.add(varValue);
                }
            }
        }
    }

    //usado pelos descendentes dessa classe para executar expressoes
    protected final Object execute(String procedure, ResourceManager rm) throws ExecutionException {
        Object o = null;

        updateVariables();

        JEP parser = rm.getResource(JEP.class);
        SymbolTable st = parser.getSymbolTable();
        for (int i = 0; i < names.size(); i++) {
            String varName = names.get(i);
            Object varValue = values.get(i);
            if (st.getVar(varName) != null && st.getVar(varName).hasValidValue()) {
                throw new ExecutionException("Variable already exists!");
            } else {
                if (!names.contains(varName)) {
                    names.add(varName);
                    values.add(varValue);
                }

                Object v = null;
                if (varValue != null) {
                    parser.parseExpression(varValue.toString());
                    v = parser.getValueAsObject();
                }

                Variable varOld;
                varOld = st.getVar(varName);
                FlowchartPanel flowcharPanel = GUI.getInstance().getFlowcharPanel();//rm.getResource TODO
                if (varOld != null){
                    flowcharPanel.popVar(varOld);
                }
                Variable var = st.makeVarIfNeeded(varName, v);
                flowcharPanel.pushVar(var);
            }
        }

        for (String str : procedure.split(";")) {
            str = str.trim();
            if (!(str.startsWith("var") || str.isEmpty())) {

                Matcher m = p.matcher(str);
                if (m.find()) {
                    String valid = m.group(1) + " = " + m.group(1) + " + 1";
                    if (this.procedure.equals(procedure)) {
                        this.procedure = procedure.replace(str, valid);
                    }
                    str = valid;
                }

                parser.parseExpression(str);
                o = parser.getValueAsObject();
                if (parser.hasError()) {
                    throw new ExecutionException(parser.getErrorInfo() + " in \"" + str + "\"");
                }
            }
        }

//        parser.parseExpression(procedure);
        return o;
    }

    //usado pelos descendentes dessa classe para executar expressoes simples (true/false)
    protected final boolean evaluate(String procedure, ResourceManager rm) throws ExecutionException {
        Object o = execute(procedure, rm);
        if (o instanceof Number) {
            Double d = ((Number) o).doubleValue();
            return (d != 0 && !d.isNaN());
        }
        return false;
    }

    //retorna o valor da expressão desse objeto
    protected final boolean evaluate(ResourceManager rm) throws ExecutionException {
        return evaluate(procedure, rm);
    }

//    protected final Variable newVariable(String name, Object value) {
//        return parser.getSymbolTable().makeVarIfNeeded(name, value);
//    }
    @Deprecated //ainda não funciona, falta criar a variavel na tabela de simbolos ^
    protected final void addVariable(String name, Object value) {
        names.add(name);
        values.add(value);
    }

    protected Collection<String> getVariableNames() {
        return names;
    }

    protected Collection<Object> getVariableValues() {
        return values;
    }

    public Collection<String> getDeclaredVariables() {
        ArrayList<String> vars = new ArrayList<>();
        Command it = this;
        while (it != null) {
            Command up = it.getPrevious();
            while (up != null) {
                if (up instanceof Procedure) {
                    vars.addAll(((Procedure) up).getVariableNames());
                }
                up = up.getPrevious();
            }
            it = it.getParent();
        }
        return vars;
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        if (!procedure.equals("0")) {
            for (String p : procedure.split(";")) {
                sb.append(ident).append(p.trim()).append(";\n");
            }
        } else {
            sb.append(ident).append(toString()).append(";\n");
        }
    }

    @Override
    public Item getItem() {
        Area myShape = new Area();
        myShape.add(new Area(new Rectangle2D.Double(0, 0, 20, 12)));
        myShape.subtract(new Area(new Rectangle2D.Double(4, 4, 12, 4)));
        return new Item("Procedimento", myShape, myColor, "Usado para declarar e atualizar o valor de variáveis, criando formulas e expreções algébricas");
    }

    @Override
    public Object createInstance() {
        return new Procedure("var x = 1");
    }

    public Argument addLineArg(int index, int type, Object data) {
        if (myArgs.size() > index) {
            if (index == -1) {
                throw new IndexOutOfBoundsException();
            }
        } else {
            while (myArgs.size() <= index) {
                myArgs.add(new Argument("", type));
            }
        }
        Argument arg = myArgs.get(index);
        if (data != nill) {
            arg.set(data, type);
        }
        return arg;
    }

    protected Argument addLineArg(int index, int type) {
        return addLineArg(index, type, nill);
    }

    @Deprecated
    protected Argument addLineArg(int index) {
        return addLineArg(index, Argument.UNDEFINED);
    }

    @Deprecated
    protected void resetArgs(Argument... args) {
        myArgs.clear();
        myArgs.addAll(Arrays.asList(args));
    }

    protected Argument getArg(int index) {
        return addLineArg(index);
    }

    protected Collection<Argument> getArgs() {
        return myArgs;
    }

    protected int getArgSize() {
        return myArgs.size();
    }

    protected void removeLineArg(int index) {
        if (index < myArgs.size()) {
            myArgs.remove(index);
        }
    }
    
    protected void removeLineArg() {
        if (myArgs.size() > 0) {
            myArgs.remove(myArgs.size() - 1);
        }
    }

    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawableProcedure(this);
        }
        return resource;
    }

    public static MutableWidgetContainer createDrawableProcedure(final Procedure p) {

        final int TEXTFIELD_WIDTH = 110;
        final int TEXTFIELD_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Procedimento:", true));
            }
        };

        //TEXTFIELD LINES
        final WidgetLine textFieldLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, MutableWidgetContainer container, int index) {
                JTextField textField = new JTextField();

                Widget wTextField = new Widget(textField, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);

                container.entangle(p.addLineArg(index - 1), wTextField);

                components.add(new Space(BUTTON_WIDTH));
                components.add(wTextField);

//                textField.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
////                            updateProcedure();
//                    }
//                });
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                if (arguments.size() > 0) {
                    String str = arguments.get(0).toString();
                    if (!str.endsWith(";")) {
                        str += ";";
                    }
                    sb.append(str);
                }
            }

//            @Override
//            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container) {
//                if (widgets.size() > 0) {
//                    Widget widget = widgets.iterator().next();
//                    JComponent jComponent = widget.getJComponent();
//                    if (jComponent instanceof JTextField) {
//                        String str = ((JTextField) jComponent).getText();
//                        if (!str.endsWith(";")) {
//                            str += ";";
//                        }
//                        return str;
//                    }
//                }
//                return "";
//            }
        };

        //END LINE
        final WidgetLine endLine = new WidgetLine(true) {
            private Widget addButton;
            private Widget remButton;

            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                JButton bTmp = new JButton(new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-add.png")));
                bTmp.setFocusable(false);

                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        container.addLine(textFieldLine);
                        //desconta headerLine e endLine
                        int size = container.getSize() - 2;
                        if (size > 1) {
                            JButton btn = (JButton) remButton.getJComponent();
                            btn.setEnabled(true);
                        }
                    }
                });

                addButton = new Widget(bTmp, BUTTON_WIDTH, TEXTFIELD_HEIGHT);

                bTmp = new JButton(new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-remove.png")));
                bTmp.setEnabled(false);
                bTmp.setFocusable(false);

                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //desconta headerLine e endLine
                        int size = container.getSize() - 2;
                        if (size > 1) {
                            container.removeLine(size);
                            p.removeLineArg();
                        }
                        if (size - 1 == 1) {
                            JButton btn = (JButton) remButton.getJComponent();
                            btn.setEnabled(false);
                        }
                    }
                });

                remButton = new Widget(bTmp, BUTTON_WIDTH, TEXTFIELD_HEIGHT);
                components.add(addButton);
                components.add(new Space(TEXTFIELD_WIDTH));
                components.add(remButton);
                components.add(new SubLineBreak(true));
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(p, myColor) {
            {
                boxLabel = p.getProcedure();
                updateStructure();
                getBoxLabel();
            }

            @Override
            public void updateStructure() {
                clear();

                addLine(headerLine);

                boolean empty = true;
                int index = 0;
                for (String str : boxLabel.split(";")) {
                    Argument arg = p.addLineArg(index);
                    arg.set(str, Argument.EXPRESSION);
                    index++;
                    addLine(textFieldLine);
                    empty = false;
                }

                if (empty) {
                    addLine(textFieldLine);
                }

                addLine(endLine);
            }

            @Override
            public String getBoxLabel() {
                String str = super.getBoxLabel();
                p.setProcedure(str);
                return str;
            }
        };

        return dcb;
    }

    public Procedure copy(Procedure copy) {
        if (copy != null) {
            copy.procedure = procedure;
            copy.names.addAll(names);
            copy.values.addAll(values);
            copy.myArgs.clear();
            for (int i = 0; i < myArgs.size(); i++) {
                copy.myArgs.add(new Argument(myArgs.get(i)));
            }
        }

        return copy;
    }

    public static Procedure copyAll(Procedure p) {
        Procedure start = p.copy((Procedure) p.createInstance());
        Procedure old = start;
        Procedure newCopy;

        Command it = p.getNext();
        int i = 0;

        while (it != null) {

            if (it instanceof Procedure) {
                newCopy = ((Procedure) it).copy((Procedure) ((Procedure) it).createInstance());

                i++;
//                System.out.println(it.getClass().getSimpleName() + " -> " + newCopy.getClass().getSimpleName());

                if (old != null) {
                    old.setNext(newCopy);
                    newCopy.setPrevious(old);
                }

                old = newCopy;

            } else {
                if (!(it instanceof Block.BlockEnd)) {
                    System.out.println("Erro ao copiar: ");
                    it.print();
                    break;
                }
            }

            it = it.getNext();
        }

        return start;
    }

    public static void main(String[] args) {
        Procedure p = new Procedure("var x,y,z; x = 2 + 2;");
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }

    public void append(String string) {
        if (procedure.endsWith(";")) {
            procedure += string;
        } else {
            procedure += "; " + string;
        }
    }
}
