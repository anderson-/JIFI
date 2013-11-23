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
import robotinterface.algorithm.Command;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;
import robotinterface.drawable.DrawableCommandBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.MutableWidgetContainer;
import robotinterface.drawable.MutableWidgetContainer.WidgetLine;
import robotinterface.drawable.TextLabel;
import robotinterface.drawable.WidgetContainer.Widget;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.robot.Robot;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.Expression;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Comando genérico com suporte à variaveis.
 */
public class Procedure extends Command implements Expression, Classifiable {

    private static Color myColor = Color.decode("#ACD630");
    private ArrayList<String> names;
    private ArrayList<Object> values;
    private static JEP parser;
    private String procedure;
    public static final int TEXTFIELD_WIDTH = 110;
    public static final int TEXTFIELD_HEIGHT = 23;
    public static final int BUTTON_WIDTH = 20;
    public static final int INSET_X = 5;
    public static final int INSET_Y = 5;

    public Procedure() {
        parser = null;
        procedure = "0";
        names = new ArrayList<>();
        values = new ArrayList<>();
    }

    public Procedure(Procedure p) {
    }

    public Procedure(String procedure) {
        this();
        setProcedure(procedure);
    }

    protected final JEP getParser() {
        return parser;
    }

    @Override
    public final void setParser(JEP parser) {
        Procedure.parser = parser;
    }

    public final String getProcedure() {
        return procedure;
    }

    public final void setProcedure(String procedure) {
        this.procedure = procedure;
        updateVariables();
    }

    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        evaluate();
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

    //usado pelos descendentes dessa classe para executar expressoes simples
    protected final Object execute(String procedure) throws ExecutionException {
        if (parser == null) {
            throw new ExecutionException("Parser not found!");
        }

        Object o = null;

        updateVariables();

        SymbolTable st = getParser().getSymbolTable();
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

                st.makeVarIfNeeded(varName, v);
            }
        }

        for (String str : procedure.split(";")) {
            if (!str.startsWith("var")) {
                parser.parseExpression(str);
                o = parser.getValueAsObject();
            }
        }

//        parser.parseExpression(procedure);

        return o;
    }

    protected final boolean evaluate(String procedure) throws ExecutionException {
        Object o = execute(procedure);
        if (o instanceof Number) {
            Double d = ((Number) o).doubleValue();
            return (d != 0 && !d.isNaN());
        }
        return false;
    }

    protected final boolean evaluate() throws ExecutionException {
        return evaluate(procedure);
    }

//    protected final Variable newVariable(String name, Object value) {
//        return parser.getSymbolTable().makeVarIfNeeded(name, value);
//    }
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
        return new Item("Procedimento", myShape, myColor);
    }

    @Override
    public Object createInstance() {
        return new Procedure("var x = 1");
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
        final int INSET_X = 5;
        final int INSET_Y = 5;

        //HEADER LINE

        final WidgetLine headerLine = new WidgetLine(20) {
            @Override
            protected void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
                labels.add(new TextLabel("Procedimento:", 20, true));
            }
        };

        //TEXTFIELD LINES

        int textFieldLineWidth = 4 * INSET_X + 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH;
        int textFieldLineHeight = 2 * INSET_Y + TEXTFIELD_HEIGHT;
        final WidgetLine textFieldLine = new WidgetLine(textFieldLineWidth, textFieldLineHeight) {
            @Override
            protected void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
                JTextField textField = new JTextField((String) data);

                textField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
//                            updateProcedure();
                    }
                });

                int textFieldX = 2 * INSET_X + BUTTON_WIDTH;
                widgets.add(new Widget(textField, textFieldX, INSET_Y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
            }

            @Override
            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container) {
                if (widgets.size() > 0) {
                    Widget widget = widgets.iterator().next();
                    JComponent jComponent = widget.getJComponent();
                    if (jComponent instanceof JTextField) {
                        String str = ((JTextField) jComponent).getText();
                        if (!str.endsWith(";")) {
                            str += ";";
                        }
                        return str;
                    }
                }
                return "";
            }
        };

        //END LINE

        final WidgetLine endLine = new WidgetLine(true) {
            private Widget addButton;
            private Widget remButton;

            @Override
            protected void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
                JButton bTmp = new JButton("+");

                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        container.addLine(textFieldLine, "");
                        //desconta headerLine e endLine
                        int size = container.getSize() - 2;
                        if (size > 1) {
                            JButton btn = (JButton) remButton.getJComponent();
                            btn.setEnabled(true);
                        }
                    }
                });

                addButton = new Widget(bTmp, INSET_X, INSET_Y, BUTTON_WIDTH, TEXTFIELD_HEIGHT);

                bTmp = new JButton("-");
                bTmp.setEnabled(false);

                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //desconta headerLine e endLine
                        int size = container.getSize() - 2;
                        if (size > 1) {
                            container.removeLine(size);
                        }
                        if (size - 1 == 1) {
                            JButton btn = (JButton) remButton.getJComponent();
                            btn.setEnabled(false);
                        }
                    }
                });

                int remButtonX = 3 * INSET_X + BUTTON_WIDTH + TEXTFIELD_WIDTH;
                remButton = new Widget(bTmp, remButtonX, INSET_Y, BUTTON_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(addButton);
                widgets.add(remButton);
            }
        };

        DrawableCommandBlock dcb = new DrawableCommandBlock(p, myColor) {
            {
                string = p.getProcedure();
                updateLines();
                getString();
            }

            @Override
            public void updateLines() {
                clear();

                addLine(headerLine, null);

                boolean empty = true;
                for (String str : string.split(";")) {
                    addLine(textFieldLine, str);
                    empty = false;
                }

                if (empty) {
                    addLine(textFieldLine, "");
                }

                addLine(endLine, null);
            }

            @Override
            public String getString() {
                String str = super.getString();
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
