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
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import robotinterface.algorithm.Command;
import static robotinterface.algorithm.Command.identChar;
import static robotinterface.algorithm.procedure.If.createDrawableIf;
import static robotinterface.algorithm.procedure.Procedure.INSET_X;
import static robotinterface.algorithm.procedure.Procedure.INSET_Y;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.MutableWidgetContainer;
import robotinterface.drawable.TextLabel;
import robotinterface.drawable.WidgetContainer;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;

/**
 * Laço de repetição simples.
 */
public class While extends Block {

    private GraphicObject resource = null;

    public While() {
    }

    public While(String procedure) {
        setProcedure(procedure);
    }

    @Override
    public Command step() throws ExecutionException {
        if (evaluate()) {
            return start;
        }
        return super.step();
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append("while (").append(getProcedure()).append(")").append("{\n");
        Command it = start;
        while (it != null) {
            it.toString(ident + identChar, sb);
            it = it.getNext();
        }
        sb.append(ident).append("}\n");
    }

    @Override
    public Item getItem() {
        return super.getItem(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object createInstance() {
        return new While();
    }

    public static MutableWidgetContainer createDrawableWhile(final While w) {

        final String[] comparadores = {"==", "!=", "<", "<=", ">", ">="};
        final String[] proximos = {" ", "&&", "||"};
        final int TEXTFIELD_WIDTH = 80;
        final int TEXTFIELD_HEIGHT = 25;
        final int COMBOBOX_WIDTH = 55;
        final int COMBOBOX_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;
        //HEADER LINE
        final MutableWidgetContainer.WidgetLine headerLine = new MutableWidgetContainer.WidgetLine(20) {
            @Override
            protected void createRow(Collection<WidgetContainer.Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
                labels.add(new TextLabel("If:", 20, true));
            }
        };
        //LINES
        int textFieldLineWidth = 4 * INSET_X + 2 * TEXTFIELD_WIDTH + COMBOBOX_WIDTH;
        int textFieldLineHeight = 3 * INSET_Y + TEXTFIELD_HEIGHT + COMBOBOX_HEIGHT;
        final MutableWidgetContainer.WidgetLine conditionalLine = new MutableWidgetContainer.WidgetLine(textFieldLineWidth, textFieldLineHeight) {
            @Override
            protected void createRow(Collection<WidgetContainer.Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
                JTextField primeiro = new JTextField();
                JTextField segundo = new JTextField();
                JComboBox comparacao = new JComboBox(comparadores);
                JComboBox proximo = new JComboBox(proximos);

                if (data instanceof Object[]) {
                    if (((Object[]) data)[0] instanceof String[]) {
                        String[] strArray = (String[]) ((Object[]) data)[0];
                        if (strArray.length == 1) {
                            primeiro.setText(strArray[0]);
                        } else if (strArray.length == 2) {
                            primeiro.setText(strArray[0]);
                            segundo.setText(strArray[1]);
                        }
                    }
                    if (((Object[]) data)[1] instanceof Integer) {
                        int cmp = (Integer) ((Object[]) data)[1];
                        if (cmp >= 0) {
                            comparacao.setSelectedIndex(cmp);
                        }
                    }
                    if (((Object[]) data)[2] instanceof Integer) {
                        int op = (Integer) ((Object[]) data)[2];
                        if (op >= 0) {
                            proximo.setSelectedIndex(op);
                        }
                    }
                }

                final MutableWidgetContainer.WidgetLine thisConditionalLine = this;

                proximo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        container.addLine(thisConditionalLine, "");
                    }
                });

                int x = INSET_X;
                int y = INSET_Y;

                widgets.add(new WidgetContainer.Widget(primeiro, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
                x += TEXTFIELD_WIDTH + INSET_X;
                widgets.add(new WidgetContainer.Widget(comparacao, x, y, COMBOBOX_WIDTH, COMBOBOX_HEIGHT));
                x += COMBOBOX_WIDTH + INSET_X;
                widgets.add(new WidgetContainer.Widget(segundo, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
                x = TEXTFIELD_WIDTH + 2 * INSET_X;
                y += COMBOBOX_HEIGHT + INSET_Y;
                widgets.add(new WidgetContainer.Widget(proximo, x, y, COMBOBOX_WIDTH, COMBOBOX_HEIGHT));
            }

            @Override
            public String getString(Collection<WidgetContainer.Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container) {
                System.out.println("PARSING");
                if (widgets.size() >= 4) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        Iterator<WidgetContainer.Widget> iterator = widgets.iterator();
                        String str;
                        WidgetContainer.Widget tmpWidget;
                        JComponent jComponent;
                        //JTextField 1
                        tmpWidget = iterator.next();
                        jComponent = tmpWidget.getJComponent();
                        if (jComponent instanceof JTextField) {
                            str = ((JTextField) jComponent).getText();
                            if (!str.isEmpty()) {
                                sb.append(" ");
                                sb.append(str);
                                sb.append(" ");
                            }
                        }
                        //JComboBox 1
                        tmpWidget = iterator.next();
                        jComponent = tmpWidget.getJComponent();
                        if (jComponent instanceof JComboBox) {
                            str = ((JComboBox) jComponent).getSelectedItem().toString();
                            if (!str.isEmpty()) {
                                sb.append(str);
                                sb.append(" ");
                            }
                        }
                        //JTextField 2
                        tmpWidget = iterator.next();
                        jComponent = tmpWidget.getJComponent();
                        if (jComponent instanceof JTextField) {
                            str = ((JTextField) jComponent).getText();
                            if (!str.isEmpty()) {
                                sb.append(str);
                                sb.append(" ");
                            }
                        }
                        //JComboBox 2
                        tmpWidget = iterator.next();
                        jComponent = tmpWidget.getJComponent();
                        if (jComponent instanceof JComboBox) {
                            str = ((JComboBox) jComponent).getSelectedItem().toString();
                            if (!str.isEmpty()) {
                                sb.append(str);
                            }
                        }
                        System.out.println(sb.toString());
                        return sb.toString();
                    } catch (NoSuchElementException e) {
                        System.out.println("ERROR!");
                    }
                }
                return "";
            }
        };
        
        MutableWidgetContainer mwc = new MutableWidgetContainer(Color.decode("#1281BD")) {
            private Polygon myShape = new Polygon();
            public static final int EXTENDED_HEIGHT = 15;
            public static final int SIMPLE_HEIGHT = 20;
            public static final int SIMPLE_WIDTH = 20;

            {
                string = w.getProcedure();
                updateLines();
                center = true;
            }

            @Override
            public void updateLines() {
                //exclui todas as linhas
                clear();

                //adiciona cabeçalho
                addLine(headerLine, null);

                //tenta decodificar string
                try {
                    //aqui é onde vai ser armazendado os dados de cada linha
                    Object[] data = null;
                    int op = -1;
                    int cmp = -1;
                    String[] expressions;
                    boolean and;
                    boolean andEnd;
                    boolean or;
                    boolean orEnd;

                    for (String str0 : string.split("&&")) {
                        and = (str0.length() == string.length());
                        andEnd = string.endsWith(str0);
                        for (String str : str0.split("\\|\\|")) {
                            or = (str.length() == str0.length());
                            orEnd = str0.endsWith(str);

                            System.out.println(str);

                            if (!or && !orEnd) {
                                // ||
                                op = 2;
                            } else if (!and && !andEnd) {
                                // &&
                                op = 1;
                            } else if (andEnd && orEnd) {
                                // ??
                                op = 0;
                            } else {
                                System.err.println("IF parse Error!");
                            }

                            for (int i = 0; i < comparadores.length; i++) {
                                if (str.contains(comparadores[i])) {
                                    cmp = i;
                                }
                            }

                            expressions = str.split(comparadores[cmp]);

                            data = new Object[]{expressions, cmp, op};
                            //adiciona uma nova linha com dados
                            addLine(conditionalLine, data);
                        }
                    }
                    return;
                } catch (Exception e) {
                }

                //adiciona uma nova linha sem dados
                addLine(conditionalLine, null);
            }

            @Override
            public Shape updateShape(Rectangle2D bounds) {
                myShape.reset();

                if (widgetVisible) {
                    shapeStartX = 0;
                    shapeStartY = EXTENDED_HEIGHT;
                    myShape.addPoint((int) bounds.getCenterX(), 0);
                    myShape.addPoint((int) bounds.getMaxX(), EXTENDED_HEIGHT);
                    myShape.addPoint((int) bounds.getMaxX(), (int) bounds.getMaxY() + EXTENDED_HEIGHT);
                    myShape.addPoint((int) bounds.getCenterX(), (int) bounds.getMaxY() + 2 * EXTENDED_HEIGHT);
                    myShape.addPoint(0, (int) bounds.getMaxY() + EXTENDED_HEIGHT);
                    myShape.addPoint(0, EXTENDED_HEIGHT);
                } else {
                    shapeStartX = SIMPLE_WIDTH;
                    shapeStartY = SIMPLE_HEIGHT;

                    myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, 0);
                    myShape.addPoint((int) bounds.getMaxX() + 2 * SIMPLE_WIDTH, (int) bounds.getCenterY() + SIMPLE_HEIGHT);
                    myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, (int) bounds.getMaxY() + 2 * SIMPLE_HEIGHT);
                    myShape.addPoint(0, (int) bounds.getCenterY() + SIMPLE_HEIGHT);
                }
                return myShape; //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getString() {
                String str = super.getString();
                return str;
            }

            @Override
            public void splitString(String original, Collection<String> splitted) {
                //TODO:
//                String[] split = original.split("&&|\\|\\|");
//                splitted.add("if");
//                for (String str : split) {
//                    str = str.trim();
//                    splitted.add(str);
//                }
//                splitted.add(")");

                boolean and;
                boolean andEnd;
                boolean or;
                boolean orEnd;

                for (String str0 : string.split("&&")) {
                    and = (str0.length() == string.length());
                    andEnd = string.endsWith(str0);
                    for (String str : str0.split("\\|\\|")) {
                        or = (str.length() == str0.length());
                        orEnd = str0.endsWith(str);
                        str = str.trim();
                        splitted.add(str);

                        if (!or && !orEnd) {
                            splitted.add("||");
                        } else if (!and && !andEnd) {
                            splitted.add("&&");
                        }
                    }
                }
            }
        };
        return mwc;
    }

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawableWhile(this);
        }
        return resource;
    }
}
