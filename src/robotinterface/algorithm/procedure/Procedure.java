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
import robotinterface.algorithm.Command;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;
import robotinterface.drawable.DWidgetContainer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
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

//    /**
//     * Interface para a declaração de multiplas variaveis em algum comando.
//     *
//     * @see robotinterface.algorithm.procedure.Declaration
//     */
//    protected interface Declaration {
//
//        public Collection<String> getVariableNames();
//
//        public Collection<Object> getVariableValues();
//    }
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
        this.procedure = procedure;
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
    }

    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        evaluate();
        return true;
    }

    //usado pelos descendentes dessa classe para executar expressoes simples
    protected final Object execute(String procedure) throws ExecutionException {
        if (parser == null) {
            throw new ExecutionException("Parser not found!");
        }

        Object o = null;

        for (String str : procedure.split(";")) {
            if (str.startsWith("var")) {
                SymbolTable st = getParser().getSymbolTable();
                str = str.replaceFirst("var ", "");
                String varName;
                Object varValue;
                for (String declaration : str.split(",")) {
                    int eq = declaration.indexOf("=");
                    if (eq > 0) {
                        varName = declaration.substring(0, eq).trim();
                        varValue = Double.parseDouble(declaration.substring(eq + 1));
                    } else {
                        varName = declaration.trim();
                        varValue = null;
                    }
                    if (st.getVar(varName) != null && st.getVar(varName).hasValidValue()) {
                        throw new ExecutionException("Variable already exists!");
                    } else {
                        if (!names.contains(varName)) {
                            names.add(str);
                            values.add(varValue);
                        }
                        st.makeVarIfNeeded(varName, varValue);
                    }
                }
            } else {
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
            for (String p : procedure.split(";")){
                sb.append(ident).append(p).append(";\n");
            }
        } else {
            sb.append(ident).append(toString()).append(";\n");
        }
    }

    @Override
    public Item getItem() {
        return new Item("Procedimento", new Rectangle2D.Double(0, 0, 20, 15), Color.decode("#69CD87"));
    }

    @Override
    public Object createInstance() {
        return new Procedure("<insire um comando>");
    }
    private Drawable d = null;
    private static Font font = new Font("Dialog", Font.BOLD, 12);

    @Override
    public Drawable getDrawableResource() {
        if (d == null) {
            Shape s = new Rectangle2D.Double(0, 0, 150, 60);
            //cria um Losango (usar em IF)
            //s = SimpleContainer.createDiamond(new Rectangle(0,0,150,100));
            Color c = Color.decode("#69CD87");

            SimpleContainer sContainer = new SimpleContainer(s, c) {
                private ArrayList<Widget> wFields = new ArrayList<>();
                private Widget addButton;
                private Widget remButton;
                private boolean updateFields = false;

                {
                    createTextFields();

                    JButton b = new JButton("+");

                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            addTextField("");
                            if (wFields.size() > 1) {
                                JButton btn = (JButton) remButton.getJComponent();
                                btn.setEnabled(true);
                            }
                        }
                    });

                    addButton = addJComponent(b, 0, 0, BUTTON_WIDTH, TEXTFIELD_HEIGHT);

                    b = new JButton("-");

                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Widget w = wFields.get(wFields.size() - 1);
                            removeJComponent(w);
                            wFields.remove(w);
                            if (wFields.size() < 2) {
                                JButton btn = (JButton) remButton.getJComponent();
                                btn.setEnabled(false);
                            }
                        }
                    });

                    remButton = addJComponent(b, 0, 0, BUTTON_WIDTH, TEXTFIELD_HEIGHT);

                }

                private void addTextField(String str) {
                    JTextField textField = new JTextField(str);

                    textField.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            updateProcedure();
                        }
                    });

                    wFields.add(addJComponent(textField, 0, 0, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
                }

//                private void addTextFieldHTML(String str) {
//                    JTextPane textField = new JTextPane();
//                    textField.setContentType("text/html");
//                    textField.setText("<html><font color=red>The program performs</font></html>");
//
//                    textField.getDocument().addDocumentListener(new DocumentListener() {
//
//                        @Override
//                        public void insertUpdate(DocumentEvent e) {
//                            System.out.println("insert");
//                            try {
//                                e.getDocument().remove(0, e.getLength());
//                            } catch (BadLocationException ex) {
//                                System.out.println(":/");
//                            }
//                            
//                        }
//
//                        @Override
//                        public void removeUpdate(DocumentEvent e) {
//                            System.out.println("remove");
//                        }
//
//                        @Override
//                        public void changedUpdate(DocumentEvent e) {
//                            System.out.println("change");
//                        }
//                       
//                    });
//
////                    textField.addActionListener(new ActionListener() {
////                        @Override
////                        public void actionPerformed(ActionEvent e) {
////                            updateProcedure();
////                        }
////                    });
//
//                    wFields.add(addJComponent(textField, 0, 0, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
//                }
                private void updateProcedure() {
                    StringBuilder sb = new StringBuilder();

                    for (Widget w : wFields) {
                        JTextField tf = (JTextField) w.getJComponent();
                        String str = tf.getText();
                        sb.append(str);
                        if (!str.isEmpty() && !str.endsWith(";")) {
                            //APENAS ANTES DOS BLOCOS IF E WHILE ESTIVEREM PRONTOS!
                            if (Procedure.this instanceof While || Procedure.this instanceof If){
                                // NÂO USAR
                            } else {
                                sb.append(";");
                            }
                        }
                    }

                    procedure = sb.toString();
                }

                private void createTextFields() {
                    for (Iterator<DWidgetContainer.Widget> it = wFields.iterator(); it.hasNext();) {
                        Widget w = it.next();
                        removeJComponent(w);
                        it.remove();
                    }

                    for (String str : procedure.split(";")) {
                        addTextField(str);
                    }
                }

                private void drawLine(Graphics2D g) {
                    Command c = getNext();
                    if (c instanceof GraphicResource) {
                        Drawable d = ((GraphicResource) c).getDrawableResource();
                        if (d != null) {
                            Rectangle2D.Double bThis = getObjectBouds();
                            Rectangle2D.Double bNext = d.getObjectBouds();
                            Line2D.Double l = new Line2D.Double(bThis.getCenterX(), bThis.getMaxY(), bNext.getCenterX(), bNext.getMinY());
                            g.setStroke(new BasicStroke(2));
                            g.setColor(Color.red);
                            g.draw(l);
                        }
                    }
                }

                @Override
                protected void drawWJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
                    //escreve coisas quando os jcomponets estão visiveis
                    if (updateFields) {
                        createTextFields();
                        updateFields = false;
                    }

                    String str = "Procedimento:";

                    g.setFont(font);
                    FontMetrics fm = g.getFontMetrics();

                    double x;
                    double y;

                    double totalWidth = 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH + 4 * INSET_X;

                    double width = fm.stringWidth(str);
                    double height = fm.getHeight();

                    x = (totalWidth - width) / 2;
                    y = INSET_Y + fm.getAscent();

                    g.setColor(Color.black);
                    g.translate(x, y);
                    g.drawString(str, 0, 0);
                    g.translate(-x, -y);

                    x = BUTTON_WIDTH + 2 * INSET_X;

                    for (Widget w : wFields) {
                        y += INSET_Y;
                        w.setLocation((int) x, (int) y);
                        y += TEXTFIELD_HEIGHT;
                    }

                    ((Rectangle2D.Double) shape).width = totalWidth;
                    ((Rectangle2D.Double) shape).height = y + INSET_Y;
                    ((Rectangle2D.Double) bounds).width = totalWidth;
                    ((Rectangle2D.Double) bounds).height = y + INSET_Y;

                    y -= TEXTFIELD_HEIGHT;
                    x = INSET_X;

                    remButton.setLocation((int) x, (int) y);
                    addButton.setLocation((int) x + BUTTON_WIDTH + TEXTFIELD_WIDTH + 2 * INSET_X, (int) y);



//                    AffineTransform o = g.getTransform();
//                    System.out.println(o);
//                    ga.removeRelativePosition(o);
//                    ga.applyGlobalPosition(o);
//                    //ga.removeZoom(o);
//                    g.setTransform(o);


                    g.translate(-bounds.x, -bounds.y);
                    drawLine(g);
                }

                @Override
                protected void drawWoJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
                    //escreve coisas quando os jcomponets não estão visiveis

                    if (!updateFields) {
                        updateProcedure();
                        updateFields = true;
                    }


                    g.setFont(font);
                    FontMetrics fm = g.getFontMetrics();

                    double x = INSET_X;
                    double y = INSET_Y;

                    double width = 0;
                    double tmpWidth;

                    g.setColor(Color.black);

                    g.translate(x, 0);
                    for (String str : procedure.split(";")) {
                        str += ";";
                        str = str.trim();
                        tmpWidth = fm.stringWidth(str);
                        if (tmpWidth > width) {
                            width = tmpWidth;
                        }
                        y += fm.getAscent();

                        g.translate(0, y);
                        g.drawString(str, 0, 0);
                        g.translate(0, -y);

                    }
                    g.translate(-x, 0);

                    ((Rectangle2D.Double) shape).width = width + 2 * INSET_X;
                    ((Rectangle2D.Double) shape).height = y + 2 * INSET_Y;
                    ((Rectangle2D.Double) bounds).width = width + 2 * INSET_X;
                    ((Rectangle2D.Double) bounds).height = y + 2 * INSET_Y;

                    g.translate(-bounds.x, -bounds.y);
                    drawLine(g);

//                    double width = fm.stringWidth(procedure);
//                    double height = fm.getHeight();
//
//                    ((Rectangle2D.Double) shape).width = width + 2 * INSET_X;
//                    ((Rectangle2D.Double) shape).height = height + 2 * INSET_Y;
//
//                    double x;
//                    double y;
//
//                    x = INSET_X;
//                    y = (((Rectangle2D.Double) shape).height - height) / 2 + fm.getAscent();
//
//                    g.setColor(Color.black);
//                    g.translate(x, y);
//                    g.drawString(procedure, 0, 0);
//                    g.translate(-x, -y);

                }
            };

            d = sContainer;
        }
        return d;
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
                //System.out.println(it.getClass().getSimpleName() + " -> " + newCopy.getClass().getSimpleName());

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
        if (procedure.endsWith(";")){
            procedure += string;
        } else {
            procedure += "; " + string;
        }
    }
}
