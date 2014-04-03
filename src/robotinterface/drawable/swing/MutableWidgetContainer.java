/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.swing;

import robotinterface.drawable.swing.component.Widget;
import robotinterface.drawable.swing.component.WidgetLine;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.component.Component;
import robotinterface.drawable.swing.component.SubLineBreak;
import robotinterface.drawable.swing.component.TextLabel;
import robotinterface.drawable.util.QuickFrame;

/**
 *
 * @author antunes
 */
public class MutableWidgetContainer extends WidgetContainer {

    protected static Font defaultFont;
    public static final int INSET_Xd = 5;
    public static final int INSET_Yd = 5;

    static {
        defaultFont = new Font("Dialog", Font.BOLD, 12);
    }

    protected String boxLabel = "";
    protected Color color;
    protected int shapeStartX = 0;
    protected int shapeStartY = 0;
    protected boolean center = false;
    protected boolean widgetsEnabled = true;
    protected Color boxLabelColor = Color.BLACK;
    protected Font stringFont = defaultFont;
    protected Rectangle2D.Double shapeBounds;
    private String name = "";
    private double stringWidth = 0;
    private int firstShapeUpdate = 2;
    private boolean updateStructure = false;
    private boolean updateShape = false;
    private boolean updateHeight = false;
    private final ArrayList<String> splittedBoxLabel;
    private final ArrayList<WidgetLine> rowTypes;
    private final ArrayList<ArrayList<Component>> rows;
    private final ArrayList<Argument> tmpArguments;
    private final HashMap<Widget, Argument> eMap;

    public MutableWidgetContainer(Color color) {
        super(new Rectangle(0, 0, 1, 1));
        splittedBoxLabel = new ArrayList<>();
        rowTypes = new ArrayList<>();
        rows = new ArrayList<>();
        tmpArguments = new ArrayList<>();
        eMap = new HashMap<>();
        shapeBounds = new Rectangle2D.Double();
        setWidgetVisible(false);
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setString(String string) {
        this.boxLabel = string;
    }

    public boolean isWidgetsEnebled() {
        return widgetsEnabled;
    }

    public void setWidgetsEnebled(boolean widgetsEnebled) {
        this.widgetsEnabled = widgetsEnebled;
    }

    public Widget entangle(Argument arg, Widget... ws) {
        for (Widget w : ws) {
            w.setDynamic(true);
            eMap.put(w, arg);
        }
        Widget chosen = arg.setValueOf(ws);
        addWidget(chosen);
        return chosen;
    }

    public String getBoxLabel() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rowTypes.size(); i++) {
            tmpArguments.clear();
            WidgetLine type = rowTypes.get(i);

            for (Component c : rows.get(i)) {
                if (c instanceof Widget) {
                    Widget w = (Widget) c;
                    if (w.isVisible()) {
                        Argument arg = eMap.get(w);
                        if (arg != null) {
                            arg.getValueFrom(w);
                            tmpArguments.add(arg);
                        }
                    }
                }
            }

            type.toString(sb, tmpArguments, this);
        }
        return sb.toString();
    }

    public void splitBoxLabel(String original, Collection<String> splitted) {
        String[] split = original.split(";");
        for (String str : split) {
            str += ";";
            str = str.trim();
            splitted.add(str);
        }
    }

    public void addLine(WidgetLine line) {
        ArrayList<Component> newRowComponents = new ArrayList<>();

        int index = rowTypes.size() - 1;
        for (; index >= 0; index--) {
            if (!rowTypes.get(index).isOnPageEnd()) {
                index++;
                break;
            }
        }
        index = (index < 0) ? 0 : index;

        line.createRow(newRowComponents, this, index);
        resetY();

//        line.setIndex(index); TODO
        rowTypes.add(index, line);
        rows.add(index, newRowComponents);

        for (Component c : newRowComponents) {
            if (c instanceof Widget) {
                Widget w = (Widget) c;
                if (!w.isDynamic()) {
                    super.addWidget(w);
                }
            }
        }
        updateHeight = true;
    }

    public boolean hasLine(int index) {
        return (rowTypes.size() > index);
    }

    public int getLineIndex(Widget w) {
        return rows.indexOf(w);
    }

    public int getLineIndex(WidgetLine wl) {
        return rowTypes.indexOf(wl);
    }

    public void removeLine(int index) {
        if (rowTypes.size() > index) {
            rowTypes.remove(index);
            for (Component c : rows.get(index)) {
                if (c instanceof Widget) {
                    Widget w = (Widget) c;
                    super.removeWidget(w);
                }
            }
            rows.remove(index);
            //força a atualização do tamanho desse objeto
            shapeBounds.setRect(0, 0, 0, 0);
            updateHeight = true;
        }
    }

    public int getSize() {
        return rowTypes.size();
    }

    public void clear() {
        for (ArrayList<Component> ac : rows) {
            for (Component c : ac) {
                if (c instanceof Widget) {
                    Widget w = (Widget) c;
                    super.removeWidget(w);
                }
            }
        }

        rowTypes.clear();
        rows.clear();
        eMap.clear();

        //força a atualização do tamanho desse objeto
        shapeBounds.setRect(0, 0, 0, 0);
        updateHeight = true;
    }

    public void updateStructure() {
    }

    public Shape updateShape(Rectangle2D bounds) {
        Rectangle2D.Double s = new Rectangle2D.Double();
        s.setRect(bounds);
        return s;
    }

    @Override
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {

        //linhas
        g.setColor(color.darker());
        g.setStroke(BOLD_STROKE);
        g.translate(-bounds.x, -bounds.y);
        backDraw(g);
        g.translate(bounds.x, bounds.y);

    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {

        if (widgetsEnabled & in.mouseClicked() && in.getMouseClickCount() == 2) {
            setWidgetVisible(!isWidgetVisible());
            shapeBounds.setRect(0, 0, 0, 0);
            in.getMouseClickConsume();
        }

        //sombra
        AffineTransform t = ga.getT();
        t.translate(3, 2);
        g.setColor(color.darker());
        g.setStroke(new BasicStroke(5));
        g.draw(t.createTransformedShape(shape));
        ga.done(t);

        //fundo branco
        g.setColor(Color.white);
        g.fill(shape);
        g.setStroke(new BasicStroke(5));
        g.setColor(color);
        g.draw(shape);

        AffineTransform o = g.getTransform();

        //componente
        if (isWidgetVisible()) {
            drawOpenBox(g, ga, in);
        } else {
            drawClosedBox(g, ga, in);
        }

        g.setStroke(new BasicStroke(4));

        g.setTransform(o);

    }

    /**
     * Calcula o tamanho de uma sub-linha de um MutableWidgetContainer.
     *
     * @param g
     * @param tmp
     * @param tmp2
     * @param components
     * @return
     */
    private static double getSubLineHeight(Graphics2D g, Rectangle2D.Double tmp, Rectangle2D.Double tmp2, Collection<Component> components) {
        double lineHeight = 0;

        for (Component c : components) {
            tmp.setRect(0, 0, 0, 0);
            tmp2.setRect(0, 0, 0, 0);
            c.getBounds(tmp, g);
            c.getInsets(tmp2);
            double tmpHeight = tmp.height + tmp2.y + tmp2.height;
            lineHeight = (tmpHeight > lineHeight) ? tmpHeight : lineHeight;
        }

        return lineHeight;
    }

    public double drawLine(Graphics2D g, double y, double width, Rectangle2D.Double bounds, Rectangle2D.Double tmp, Rectangle2D.Double tmp2, Collection<Component> components) {

        int x = 0 + shapeStartX;
        int subLineHeight = 0;

        for (Component c : components) {

            //Pula os JComponents não visiveis (não pertencentes)
            if (c instanceof Widget && !contains((Widget) c)) {
                continue;
            }

            //inicializa os retangulos temporarios com o tamanho e 
            //moldura do objeto atual
            tmp.setRect(0, 0, 0, 0);
            tmp2.setRect(0, 0, 0, 0);
            c.getBounds(tmp, g);
            c.getInsets(tmp2);

            //define a posição (iterativa) do componente
            c.setTempLocation((int) (x + tmp2.x), (int) y);

            //se for um texo desenha e centraliza se necessario
            if (c instanceof TextLabel) {
                TextLabel tl = (TextLabel) c;
                g.setFont(tl.getFont());
                g.setColor(tl.getColor());

                double posX;
                double posY = y + tmp.height + tmp2.y;

                if (tl.center()) {
                    posX = (width - tmp.width) / 2;
                } else {
                    posX = x + tmp2.x;
                }

                g.translate(posX, posY);
                g.drawString(tl.getText(), 0, 0);
                g.translate(-posX, -posY);
            }

            //adiciona moldura ao tamanho do objeto
            tmp.x += x;
            tmp.width += tmp2.x + tmp2.width;
            tmp.y += y;
            tmp.height += tmp2.y + tmp2.height;

            //adiciona a area do objeto à area da sub-linha
            bounds.add(tmp);

//            g.setStroke(DEFAULT_STROKE);
//            g.setColor(Color.orange);
//            g.draw(tmp);
            //incrementa a posição em x
            x += (int) tmp.getWidth();

            //calcula o tamanho desta sub-linha
            subLineHeight = (int) ((tmp.getHeight() > subLineHeight) ? tmp.getHeight() : subLineHeight);

            //se for uma quebra de sub-linha
            if (c instanceof SubLineBreak) {
                //continua colocando componentes a partir da esquerda
                x = 0 + shapeStartX;
                //incrementa a posição em y
                y += subLineHeight;
                //se for uma linha do final do componente 
                //adiciona um espaçamento extra
                if (((SubLineBreak) c).isEndLine()) {
                    tmp2.setRect(0, 0, 0, 0);
                    c.getInsets(tmp2);
                    tmp2.x += x;
                    tmp2.height = tmp2.y;
                    tmp2.y = y;
                    bounds.add(tmp2);
                }
            }
        }
        //incrementa a posição em y
        y += subLineHeight;
        //retorna o novo valor para y
        return y;
    }

    protected void backDraw(Graphics2D g) {
    }

    //usados em drawWJC();
    Rectangle2D.Double lineBounds = new Rectangle2D.Double();
    Rectangle2D.Double tmp = new Rectangle2D.Double();
    Rectangle2D.Double tmp2 = new Rectangle2D.Double();

    protected void drawOpenBox(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        //escreve coisas quando os jcomponets estão visiveis
        if (updateStructure) {
            updateStructure();
            updateStructure = false;
        }

        double y = 0 + shapeStartY;

        //desenha sub-linhas
        for (int i = 0; i < rowTypes.size(); i++) {
            WidgetLine type = rowTypes.get(i);
            Collection<Component> components = rows.get(i);
            lineBounds.setRect(0, 0, 0, 0);

            //mantem os componentes sobrepostos
            if (type.isOnPageEnd()) {
                y -= getSubLineHeight(g, tmp, tmp2, components);
            }

            y = drawLine(g, y, shapeBounds.width, lineBounds, tmp, tmp2, components);
            shapeBounds.add(lineBounds);
        }

        //finaliza
        if (updateShape || updateHeight) {
            shape = updateShape(shapeBounds);
            updateShape = false;
        }
    }

    protected void drawClosedBox(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        //escreve coisas quando os jcomponets não estão visiveis

        if (!updateStructure) {
            boxLabel = getBoxLabel();
            splittedBoxLabel.clear();
            splitBoxLabel(boxLabel, splittedBoxLabel);
            stringWidth = 0;
            updateStructure = true;
        }

        g.setFont(stringFont);
        FontMetrics fm = g.getFontMetrics();

        double x = INSET_Xd + shapeStartX;
        double y = INSET_Yd + shapeStartY;

        double tmpWidth;

        g.setColor(boxLabelColor);

//        g.translate(x, 0);
//        for (String str : string.split(";")) {
//            str += ";";
//            str = str.trim();
//            tmpWidth = fm.stringWidth(str);
//            if (tmpWidth > width) {
//                width = tmpWidth;
//            }
//            y += fm.getAscent();
//
//            g.translate(0, y);
//            g.drawString(str, 0, 0);
//            g.translate(0, -y);
//
//        }
//        g.translate(-x, 0);
        g.translate(x, 0);
        for (String str : splittedBoxLabel) {
            tmpWidth = fm.stringWidth(str);
            if (tmpWidth > stringWidth) {
                stringWidth = tmpWidth;
            }
            y += fm.getAscent();

            if (center) {
                tmpWidth = (stringWidth - tmpWidth) / 2;
            } else {
                tmpWidth = 0;
            }

            g.translate(tmpWidth, y);
            g.drawString(str, 0, 0);
            g.translate(-tmpWidth, -y);
        }
        g.translate(-x, 0);

        x -= shapeStartX;
        y -= shapeStartY;

        shapeBounds.width = stringWidth + 2 * INSET_Xd;
        shapeBounds.height = y + 2 * INSET_Yd;

        if (!updateShape || firstShapeUpdate > 0) {
            shape = updateShape(shapeBounds);
            updateShape = true;
            //requer 2 updates iniciais para definir a forma corretamente
            firstShapeUpdate--;
        }

        g.translate(-bounds.x, -bounds.y);
    }

    @Override
    public int getDrawableLayer() {
        return GraphicObject.BACKGROUND_LAYER | GraphicObject.DEFAULT_LAYER;
    }

    public static void setAutoFillComboBox(final JComboBox cb, final Procedure p) {
        setAutoFillComboBox(cb, p, false);
    }

    public static void setAutoFillComboBox(final JComboBox cb, final Procedure p, final boolean allowEmpty) {
        MouseListener ml = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                Object o = cb.getSelectedItem();
                cb.removeAllItems();
                if (allowEmpty) {
                    cb.addItem(null);
                }
                for (String str : p.getDeclaredVariables()) {
                    cb.addItem(str);
                }
                if (o != null) {
                    cb.setSelectedItem(o);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };

        for (java.awt.Component c : cb.getComponents()) {
            c.addMouseListener(ml);
        }

        ml.mouseEntered(null);
    }

//    public static void setFocusableTextField(final JTextField jTextField, final MutableWidgetContainer container) {
//        
//                jTextField.getDocument().addDocumentListener(new DocumentListener() {
//                    @Override
//                    public void insertUpdate(DocumentEvent e) {
//                        container.setString(jTextField.getText());
//                        container.updateLines();
//                        jTextField.requestFocusInWindow();
//                    }
//
//                    @Override
//                    public void removeUpdate(DocumentEvent e) {
//                        container.setString(jTextField.getText());
//                        container.updateLines();
//                        jTextField.requestFocusInWindow();
//                    }
//
//                    @Override
//                    public void changedUpdate(DocumentEvent e) {
//                        container.setString(jTextField.getText());
//                        container.updateLines();
//                        jTextField.requestFocusInWindow();
//                    }
//                });
//    }
//    public static MutableWidgetContainer createDrawableProcedure() {
//
//        final int TEXTFIELD_WIDTH = 110;
//        final int TEXTFIELD_HEIGHT = 25;
//        final int BUTTON_WIDTH = 25;
//        final int INSET_X = 5;
//        final int INSET_Y = 5;
//
//        //HEADER LINE
//
//        final WidgetLine headerLine = new WidgetLine(20) {
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
//                labels.add(new TextLabel("Procedimento:", 20, true));
//            }
//        };
//
//        //TEXTFIELD LINES
//
//        int textFieldLineWidth = 4 * INSET_X + 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH;
//        int textFieldLineHeight = 2 * INSET_Y + TEXTFIELD_HEIGHT;
//        final WidgetLine textFieldLine = new WidgetLine(textFieldLineWidth, textFieldLineHeight) {
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
//                JTextField textField = new JTextField((String) data);
//
//                textField.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
////                            updateProcedure();
//                    }
//                });
//
//                int textFieldX = 2 * INSET_X + BUTTON_WIDTH;
//                widgets.add(new Widget(textField, textFieldX, INSET_Y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
//            }
//
//            @Override
//            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container) {
//                if (widgets.size() > 0) {
//                    Widget widget = widgets.iterator().next();
//                    JComponent jComponent = widget.getJComponent();
//                    if (jComponent instanceof JTextField) {
//                        return ((JTextField) jComponent).getText();
//                    }
//                }
//                return "";
//            }
//        };
//
//        //END LINE
//
//        final WidgetLine endLine = new WidgetLine(true) {
//            private Widget addButton;
//            private Widget remButton;
//
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
//                JButton bTmp = new JButton("+");
//
//                bTmp.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        container.addLine(textFieldLine, "");
//                        //desconta headerLine e endLine
//                        int size = container.getSize() - 2;
//                        if (size > 1) {
//                            JButton btn = (JButton) remButton.getJComponent();
//                            btn.setEnabled(true);
//                        }
//                    }
//                });
//
//                addButton = new Widget(bTmp, INSET_X, INSET_Y, BUTTON_WIDTH, TEXTFIELD_HEIGHT);
//
//                bTmp = new JButton("-");
//
//                bTmp.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        //desconta headerLine e endLine
//                        int size = container.getSize() - 2;
//                        if (size > 1) {
//                            container.removeLine(size);
//                        }
//                        if (size - 1 == 1) {
//                            JButton btn = (JButton) remButton.getJComponent();
//                            btn.setEnabled(false);
//                        }
//                    }
//                });
//
//                int remButtonX = 3 * INSET_X + BUTTON_WIDTH + TEXTFIELD_WIDTH;
//                remButton = new Widget(bTmp, remButtonX, INSET_Y, BUTTON_WIDTH, TEXTFIELD_HEIGHT);
//                widgets.add(addButton);
//                widgets.add(remButton);
//            }
//        };
//
//        MutableWidgetContainer mwc = new MutableWidgetContainer(Color.decode("#69CD87")) {
//            @Override
//            public void updateLines() {
//                clear();
//
//                addLine(headerLine, null);
//
//                boolean empty = true;
//                for (String str : string.split(";")) {
//                    addLine(textFieldLine, str);
//                    empty = false;
//                }
//
//                if (empty) {
//                    addLine(textFieldLine, "");
//                }
//
//                addLine(endLine, null);
//            }
//
//            @Override
//            public String getString() {
//                String str = super.getString();
//                System.out.println(str);
//                return str;
//            }
//        };
//
//        return mwc;
//    }
//
//    public static MutableWidgetContainer createDrawableIf() {
//
//        final String[] comparadores = {"==", "!=", "<", "<=", ">", ">="};
//        final String[] proximos = {" ", "&&", "||"};
//        final int TEXTFIELD_WIDTH = 80;
//        final int TEXTFIELD_HEIGHT = 25;
//        final int COMBOBOX_WIDTH = 55;
//        final int COMBOBOX_HEIGHT = 25;
//        final int BUTTON_WIDTH = 25;
//        //HEADER LINE
//        final WidgetLine headerLine = new WidgetLine(20) {
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
//                labels.add(new TextLabel("If:", 20, true));
//            }
//        };
//        //LINES
//        int textFieldLineWidth = 4 * INSET_X + 2 * TEXTFIELD_WIDTH + COMBOBOX_WIDTH;
//        int textFieldLineHeight = 3 * INSET_Y + TEXTFIELD_HEIGHT + COMBOBOX_HEIGHT;
//        final WidgetLine conditionalLine = new WidgetLine(textFieldLineWidth, textFieldLineHeight) {
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
//                JTextField primeiro = new JTextField();
//                JTextField segundo = new JTextField();
//                JComboBox comparacao = new JComboBox(comparadores);
//                JComboBox proximo = new JComboBox(proximos);
//
//                if (data instanceof Object[]) {
//                    if (((Object[]) data)[0] instanceof String[]) {
//                        String[] strArray = (String[]) ((Object[]) data)[0];
//                        if (strArray.length == 1) {
//                            primeiro.setText(strArray[0]);
//                        } else if (strArray.length == 2) {
//                            primeiro.setText(strArray[0]);
//                            segundo.setText(strArray[1]);
//                        }
//                    }
//                    if (((Object[]) data)[1] instanceof Integer) {
//                        int cmp = (Integer) ((Object[]) data)[1];
//                        if (cmp >= 0) {
//                            comparacao.setSelectedIndex(cmp);
//                        }
//                    }
//                    if (((Object[]) data)[2] instanceof Integer) {
//                        int op = (Integer) ((Object[]) data)[2];
//                        if (op >= 0) {
//                            proximo.setSelectedIndex(op);
//                        }
//                    }
//                }
//
//                final WidgetLine thisConditionalLine = this;
//
//                proximo.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        container.addLine(thisConditionalLine, "");
//                    }
//                });
//
//                int x = INSET_X;
//                int y = INSET_Y;
//
//                widgets.add(new Widget(primeiro, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
//                x += TEXTFIELD_WIDTH + INSET_X;
//                widgets.add(new Widget(comparacao, x, y, COMBOBOX_WIDTH, COMBOBOX_HEIGHT));
//                x += COMBOBOX_WIDTH + INSET_X;
//                widgets.add(new Widget(segundo, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
//                x = TEXTFIELD_WIDTH + 2 * INSET_X;
//                y += COMBOBOX_HEIGHT + INSET_Y;
//                widgets.add(new Widget(proximo, x, y, COMBOBOX_WIDTH, COMBOBOX_HEIGHT));
//            }
//
//            @Override
//            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container) {
//                System.out.println("PARSING");
//                if (widgets.size() >= 4) {
//                    try {
//                        StringBuilder sb = new StringBuilder();
//                        Iterator<Widget> iterator = widgets.iterator();
//                        String str;
//                        Widget tmpWidget;
//                        JComponent jComponent;
//                        //JTextField 1
//                        tmpWidget = iterator.next();
//                        jComponent = tmpWidget.getJComponent();
//                        if (jComponent instanceof JTextField) {
//                            str = ((JTextField) jComponent).getText();
//                            if (!str.isEmpty()) {
//                                sb.append(" ");
//                                sb.append(str);
//                                sb.append(" ");
//                            }
//                        }
//                        //JComboBox 1
//                        tmpWidget = iterator.next();
//                        jComponent = tmpWidget.getJComponent();
//                        if (jComponent instanceof JComboBox) {
//                            str = ((JComboBox) jComponent).getSelectedItem().toString();
//                            if (!str.isEmpty()) {
//                                sb.append(str);
//                                sb.append(" ");
//                            }
//                        }
//                        //JTextField 2
//                        tmpWidget = iterator.next();
//                        jComponent = tmpWidget.getJComponent();
//                        if (jComponent instanceof JTextField) {
//                            str = ((JTextField) jComponent).getText();
//                            if (!str.isEmpty()) {
//                                sb.append(str);
//                                sb.append(" ");
//                            }
//                        }
//                        //JComboBox 2
//                        tmpWidget = iterator.next();
//                        jComponent = tmpWidget.getJComponent();
//                        if (jComponent instanceof JComboBox) {
//                            str = ((JComboBox) jComponent).getSelectedItem().toString();
//                            if (!str.isEmpty()) {
//                                sb.append(str);
//                            }
//                        }
//                        System.out.println(sb.toString());
//                        return sb.toString();
//                    } catch (NoSuchElementException e) {
//                        System.out.println("ERROR!");
//                    }
//                }
//                return "";
//            }
//        };
//        MutableWidgetContainer mwc = new MutableWidgetContainer(Color.decode("#FFA500")) { //While: #1281BD
//            private Polygon myShape = new Polygon();
//            public static final int EXTENDED_HEIGHT = 15;
//            public static final int SIMPLE_HEIGHT = 20;
//            public static final int SIMPLE_WIDTH = 20;
//
//            {
//                center = true;
//            }
//
//            @Override
//            public void updateLines() {
//                //exclui todas as linhas
//                clear();
//
//                //adiciona cabeçalho
//                addLine(headerLine, null);
//
//                //tenta decodificar string
//                try {
//                    //aqui é onde vai ser armazendado os dados de cada linha
//                    Object[] data = null;
//                    int op = -1;
//                    int cmp = -1;
//                    String[] expressions;
//                    boolean and;
//                    boolean andEnd;
//                    boolean or;
//                    boolean orEnd;
//
//                    for (String str0 : string.split("&&")) {
//                        and = (str0.length() == string.length());
//                        andEnd = string.endsWith(str0);
//                        for (String str : str0.split("\\|\\|")) {
//                            or = (str.length() == str0.length());
//                            orEnd = str0.endsWith(str);
//
//                            System.out.println(str);
//
//                            if (!or && !orEnd) {
//                                // ||
//                                op = 2;
//                            } else if (!and && !andEnd) {
//                                // &&
//                                op = 1;
//                            } else if (andEnd && orEnd) {
//                                // ??
//                                op = 0;
//                            } else {
//                                System.err.println("IF parse Error!");
//                            }
//
//                            for (int i = 0; i < comparadores.length; i++) {
//                                if (str.contains(comparadores[i])) {
//                                    cmp = i;
//                                }
//                            }
//
//                            expressions = str.split(comparadores[cmp]);
//
//                            data = new Object[]{expressions, cmp, op};
//                            //adiciona uma nova linha com dados
//                            addLine(conditionalLine, data);
//                        }
//                    }
//                    return;
//                } catch (Exception e) {
//                }
//
//                //adiciona uma nova linha sem dados
//                addLine(conditionalLine, null);
//            }
//
//            @Override
//            public Shape updateShape(Rectangle2D bounds) {
//                myShape.reset();
//
//                if (widgetVisible) {
//                    shapeStartX = 0;
//                    shapeStartY = EXTENDED_HEIGHT;
//                    myShape.addPoint((int) bounds.getCenterX(), 0);
//                    myShape.addPoint((int) bounds.getMaxX(), EXTENDED_HEIGHT);
//                    myShape.addPoint((int) bounds.getMaxX(), (int) bounds.getMaxY() + EXTENDED_HEIGHT);
//                    myShape.addPoint((int) bounds.getCenterX(), (int) bounds.getMaxY() + 2 * EXTENDED_HEIGHT);
//                    myShape.addPoint(0, (int) bounds.getMaxY() + EXTENDED_HEIGHT);
//                    myShape.addPoint(0, EXTENDED_HEIGHT);
//                } else {
//                    shapeStartX = SIMPLE_WIDTH;
//                    shapeStartY = SIMPLE_HEIGHT;
//
//                    myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, 0);
//                    myShape.addPoint((int) bounds.getMaxX() + 2 * SIMPLE_WIDTH, (int) bounds.getCenterY() + SIMPLE_HEIGHT);
//                    myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, (int) bounds.getMaxY() + 2 * SIMPLE_HEIGHT);
//                    myShape.addPoint(0, (int) bounds.getCenterY() + SIMPLE_HEIGHT);
//                }
//                return myShape; //To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public String getString() {
//                String str = super.getString();
//                return str;
//            }
//
//            @Override
//            public void splitString(String original, Collection<String> splitted) {
//                //TODO:
////                String[] split = original.split("&&|\\|\\|");
////                splitted.add("if");
////                for (String str : split) {
////                    str = str.trim();
////                    splitted.add(str);
////                }
////                splitted.add(")");
//
//                boolean and;
//                boolean andEnd;
//                boolean or;
//                boolean orEnd;
//
//                for (String str0 : string.split("&&")) {
//                    and = (str0.length() == string.length());
//                    andEnd = string.endsWith(str0);
//                    for (String str : str0.split("\\|\\|")) {
//                        or = (str.length() == str0.length());
//                        orEnd = str0.endsWith(str);
//                        str = str.trim();
//                        splitted.add(str);
//
//                        if (!or && !orEnd) {
//                            splitted.add("||");
//                        } else if (!and && !andEnd) {
//                            splitted.add("&&");
//                        }
//                    }
//                }
//            }
//        };
//        return mwc;
//    }
//    public static MutableWidgetContainer createDrawableFunction() {
//
//        final int TEXTFIELD_WIDTH = 70;
//        final int TEXTFIELD_HEIGHT = 25;
//        final int COMBOBOX_WIDTH = 55;
//        final int COMBOBOX_HEIGHT = 25;
//        final int BUTTON_WIDTH = 25;
//        //HEADER LINE
//        int headerHeight = 2 * INSET_Y + TEXTFIELD_HEIGHT + 20;
//        int headerWidth = 4 * INSET_X + 2 * BUTTON_WIDTH + 18 + TEXTFIELD_WIDTH;
//        final WidgetLine headerLine = new WidgetLine(headerWidth, headerHeight) {
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
//                labels.add(new TextLabel("Função:", 20, true));
//                labels.add(new TextLabel("Nome:", INSET_X, 3 * INSET_Y + 28));
//                JTextField tfName = new JTextField();
//                widgets.add(new Widget(tfName, 2 * INSET_X + 50, INSET_Y + 20, TEXTFIELD_WIDTH + 20, TEXTFIELD_HEIGHT));
//            }
//        };
//
//        //LINES
//        int argumentLineHeight = 2 * INSET_Y + TEXTFIELD_HEIGHT;
//        final WidgetLine argumentLine = new WidgetLine(argumentLineHeight) {
//            private int argN = 0;
//
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
//                JTextField primeiro = new JTextField();
//
//                if (data instanceof String) {
//                    primeiro.setText((String) data);
//                }
//
//                int x = INSET_X;
//                int y = 0;
//
//                argN++;
//                labels.add(new TextLabel(argN + ":", INSET_X, y + 18));
//                x += 18;
//                widgets.add(new Widget(primeiro, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
//            }
//
//            @Override
//            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container) {
//                System.out.println("PARSING");
//                if (widgets.size() >= 4) {
//                    try {
//                        StringBuilder sb = new StringBuilder();
//                        Iterator<Widget> iterator = widgets.iterator();
//                        String str;
//                        Widget tmpWidget;
//                        JComponent jComponent;
//                        //JTextField 1
//                        tmpWidget = iterator.next();
//                        jComponent = tmpWidget.getJComponent();
//                        if (jComponent instanceof JTextField) {
//                            str = ((JTextField) jComponent).getText();
//                            if (!str.isEmpty()) {
//                                sb.append(" ");
//                                sb.append(str);
//                                sb.append(" ");
//                            }
//                        }
//                        //JComboBox 1
//                        tmpWidget = iterator.next();
//                        jComponent = tmpWidget.getJComponent();
//                        if (jComponent instanceof JComboBox) {
//                            str = ((JComboBox) jComponent).getSelectedItem().toString();
//                            if (!str.isEmpty()) {
//                                sb.append(str);
//                                sb.append(" ");
//                            }
//                        }
//                        //JTextField 2
//                        tmpWidget = iterator.next();
//                        jComponent = tmpWidget.getJComponent();
//                        if (jComponent instanceof JTextField) {
//                            str = ((JTextField) jComponent).getText();
//                            if (!str.isEmpty()) {
//                                sb.append(str);
//                                sb.append(" ");
//                            }
//                        }
//                        //JComboBox 2
//                        tmpWidget = iterator.next();
//                        jComponent = tmpWidget.getJComponent();
//                        if (jComponent instanceof JComboBox) {
//                            str = ((JComboBox) jComponent).getSelectedItem().toString();
//                            if (!str.isEmpty()) {
//                                sb.append(str);
//                            }
//                        }
//                        System.out.println(sb.toString());
//                        return sb.toString();
//                    } catch (NoSuchElementException e) {
//                        System.out.println("ERROR!");
//                    }
//                }
//                return "";
//            }
//        };
//
//        //END LINE
//
//        final WidgetLine endLine = new WidgetLine(true) {
//            private Widget addButton;
//            private Widget remButton;
//
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
//                JButton bTmp = new JButton("+");
//
//                bTmp.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        container.addLine(argumentLine, "");
//                        //desconta headerLine e endLine
//                        int size = container.getSize() - 2;
//                        if (size > 1) {
//                            JButton btn = (JButton) remButton.getJComponent();
//                            btn.setEnabled(true);
//                        }
//                    }
//                });
//
//                addButton = new Widget(bTmp, 2 * INSET_X + 18 + TEXTFIELD_WIDTH, 0, BUTTON_WIDTH, TEXTFIELD_HEIGHT);
//
//                bTmp = new JButton("-");
//
//                bTmp.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        //desconta headerLine e endLine
//                        int size = container.getSize() - 2;
//                        if (size > 1) {
//                            container.removeLine(size);
//                        }
//                        if (size - 1 == 1) {
//                            JButton btn = (JButton) remButton.getJComponent();
//                            btn.setEnabled(false);
//                        }
//                    }
//                });
//
//                int remButtonX = 3 * INSET_X + BUTTON_WIDTH + 18 + TEXTFIELD_WIDTH;
//                remButton = new Widget(bTmp, remButtonX, 0, BUTTON_WIDTH, TEXTFIELD_HEIGHT);
//                widgets.add(addButton);
//                widgets.add(remButton);
//            }
//        };
//
//        int nullLineHeight =  INSET_Y + TEXTFIELD_HEIGHT;
//        final WidgetLine nullLine = new WidgetLine(nullLineHeight) {
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
//                labels.add(new TextLabel("Argumentos:", INSET_X, 18));
//            }
//        };
//
//        MutableWidgetContainer mwc = new MutableWidgetContainer(Color.decode("#FFA500")) {
//            private Polygon myShape = new Polygon();
//            public static final int EXTENDED_HEIGHT = 15;
//            public static final int SIMPLE_HEIGHT = 20;
//            public static final int SIMPLE_WIDTH = 20;
//
//            {
//                center = true;
//            }
//
//            @Override
//            public void updateLines() {
//                //exclui todas as linhas
//                clear();
//
//                //adiciona cabeçalho
//                addLine(headerLine, null);
//
//                //adiciona uma nova linha sem dados
//                addLine(nullLine, null);
//
//                addLine(endLine, null);
//            }
//
//            @Override
//            public String getString() {
//                String str = super.getString();
//                return str;
//            }
//        };
//        return mwc;
//    }
//    public static MutableWidgetContainer createDrawablePrintString() {
//
//        final int TEXTFIELD_WIDTH = 110;
//        final int TEXTFIELD_HEIGHT = 25;
//        final int BUTTON_WIDTH = 25;
//        final int BEGIN_X = 20;
//        final int INSET_X = 5;
//        final int INSET_Y = 5;
//
//
//        //LINES
//
//        int varSelectiteonLineWidth = 4 * INSET_X + 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH;
//        int varSelectiteonLineHeight = 2 * INSET_Y + TEXTFIELD_HEIGHT;
//        final WidgetLine varSelectiteonLine = new WidgetLine(varSelectiteonLineWidth, varSelectiteonLineHeight) {
//            public static final String RELOAD_VARS_ITEM = "<atualizar>";
//            private String var;
//            private int varN = 0;
//
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
//                JComboBox combobVar = new JComboBox();
//                combobVar.addItem(RELOAD_VARS_ITEM);
//
//
//                combobVar.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        JComboBox cb = (JComboBox) e.getSource();
//                        var = (String) cb.getSelectedItem();
//                        if (var.equals(RELOAD_VARS_ITEM)) {
//                            cb.removeAllItems();
//                            cb.addItem(RELOAD_VARS_ITEM);
////                            for (String str : ReadDevice.super.getDeclaredVariables()) {
////                                cb.addItem(str);
////                            }
//                        }
//                    }
//                });
//
//                int x = BEGIN_X + INSET_X;
//                int y = INSET_Y;
//
//                varN++;
//                labels.add(new TextLabel("v" + varN + ":", x, y + 18));
//                x += 25;
//                //x += 2 * INSET_X + BUTTON_WIDTH;
//                widgets.add(new Widget(combobVar, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
//            }
//
//            @Override
//            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container) {
//                if (widgets.size() > 0) {
//                    Widget widget = widgets.iterator().next();
//                    JComponent jComponent = widget.getJComponent();
//                    if (jComponent instanceof JTextField) {
//                        return ((JTextField) jComponent).getText();
//                    }
//                }
//                return "";
//            }
//        };
//
//        //HEADER LINE
//
//        int headerHeight = 2 * INSET_Y + TEXTFIELD_HEIGHT + 20;
//        int headerWidth = BEGIN_X + 4 * INSET_X + 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH;
//        final WidgetLine headerLine = new WidgetLine(headerWidth, headerHeight) {
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
//                labels.add(new TextLabel("Exibir:", 20, true));
//                labels.add(new TextLabel("Formato:", BEGIN_X + INSET_X, 3 * INSET_Y + 28));
//                final JTextField tfName = new JTextField();
//
//                tfName.getDocument().addDocumentListener(new DocumentListener() {
//                    @Override
//                    public void insertUpdate(DocumentEvent e) {
//                        container.string = tfName.getText();
////                        container.updateLines();
//                    }
//
//                    @Override
//                    public void removeUpdate(DocumentEvent e) {
//                        container.string = tfName.getText();
////                        container.updateLines();
//                    }
//
//                    @Override
//                    public void changedUpdate(DocumentEvent e) {
//                        container.string = tfName.getText();
////                        container.updateLines();
//                    }
//                });
//
//                widgets.add(new Widget(tfName, BEGIN_X + 2 * INSET_X + 65, INSET_Y + 20, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
//                JButton bTmp = new JButton(">");
//
//                bTmp.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        container.string = tfName.getText();
//                        container.updateLines();
//                    }
//                });
//
//                widgets.add(new Widget(bTmp, BEGIN_X + 3 * INSET_X + 65 + TEXTFIELD_WIDTH, INSET_Y + 20, BUTTON_WIDTH, TEXTFIELD_HEIGHT));
//            }
//        };
//
//        MutableWidgetContainer mwc = new MutableWidgetContainer(Color.decode("#08B9AC")) {
//            private GeneralPath myShape = new GeneralPath();
//
//            @Override
//            public void updateLines() {
//                if (getSize() < 1){
//                    clear();
//
//                    addLine(headerLine, null);
//                }
//
//                String subStr = "%v";
//                int ocorrences = (string.length() - string.replace(subStr, "").length()) / subStr.length();;
//                ocorrences -= getSize() - 1;
//                for (int i = 0; i < ocorrences; i++){
//                    addLine(varSelectiteonLine, null);
//                }
//
//            }
//
//            @Override
//            public Shape updateShape(Rectangle2D bounds) {
//                double mx = bounds.getWidth();
//                double my = bounds.getHeight();
//                double a = 15;
//                double b = 20;
//
//                myShape.reset();
//                myShape.moveTo(a, 0);
//                myShape.lineTo(mx + a, 0);
//                myShape.curveTo(mx + b + a, 0, mx + b + a, my, mx + a, my);
//                myShape.lineTo(a, my);
//                myShape.lineTo(0, my / 2);
//                myShape.closePath();
//
//                return myShape;
//            }
//
//            @Override
//            public String getString() {
//                String str = super.getString();
//                System.out.println(str);
//                return str;
//            }
//        };
//
//        return mwc;
//    }
//    public static MutableWidgetContainer createDrawableMove() {
//
//        final int TEXTFIELD_WIDTH = 80;
//        final int TEXTFIELD_HEIGHT = 25;
//        final int BUTTON_WIDTH = 25;
//        final int INSET_X = 5;
//        final int INSET_Y = 5;
//
//        //HEADER LINE
//
//        int headerHeight = 4 * INSET_Y + 2 * TEXTFIELD_HEIGHT + 20;
//        int headerWidth = 4 * INSET_X + 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH;
//        final WidgetLine headerLine = new WidgetLine(headerWidth, headerHeight) {
//            @Override
//            public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
//                labels.add(new TextLabel("Mover:", 20, true));
//
//                final JSpinner spinner1 = new JSpinner();
//                final JSpinner spinner2 = new JSpinner();
//                spinner1.setModel(new SpinnerNumberModel(80, -128, 127, 2));
//                spinner2.setModel(new SpinnerNumberModel(80, -128, 127, 2));
//                JComboBox combobox1 = new JComboBox();
//                JComboBox combobox2 = new JComboBox();
//                
//                MutableWidgetContainer.setAutoFillComboBox(combobox1, null);
//                MutableWidgetContainer.setAutoFillComboBox(combobox2, null);
//                
//                final JButton changeButton1 = new JButton();
//                final JButton changeButton2 = new JButton();
//                ImageIcon icon = new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/system-search.png"));
//                changeButton1.setIcon(icon);
//                changeButton2.setIcon(icon);
//
//                changeButton1.setVisible(false);
//                changeButton2.setVisible(false);
//
//                int x = INSET_X;
//                int y = INSET_Y + 40;
//                labels.add(new TextLabel("V1:", x + 5, y));
//
//                x += 26;
//                y -= 18;
//
//                final Widget wspinner1 = new Widget(spinner1, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
//                final Widget wcombobox1 = new Widget(combobox1, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
//                widgets.add(wspinner1);
////                widgets.add(wcombobox1);
//
//                x += INSET_Y + TEXTFIELD_WIDTH;
//
//                widgets.add(new Widget(changeButton1, x, y, BUTTON_WIDTH, BUTTON_WIDTH));
//
//                x -= INSET_Y + TEXTFIELD_WIDTH;
//
//                x -= 26;
//                y += 50;
//
//                labels.add(new TextLabel("V2:", x + 5, y));
//
//                x += 26;
//                y -= 18;
//
//                final Widget wspinner2 = new Widget(spinner2, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
//                final Widget wcombobox2 = new Widget(combobox2, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
//                widgets.add(wspinner2);
////                widgets.add(wcombobox2);
//
//                x += INSET_Y + TEXTFIELD_WIDTH;
//
//                widgets.add(new Widget(changeButton2, x, y, BUTTON_WIDTH, BUTTON_WIDTH));
//
//                x -= INSET_Y + TEXTFIELD_WIDTH;
//
//
//                changeButton1.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        if (container.contains(wspinner1)) {
//                            container.removeWidget(wspinner1);
//                            container.addWidget(wcombobox1);
//                        } else {
//                            container.removeWidget(wcombobox1);
//                            container.addWidget(wspinner1);
//                        }
//                    }
//                });
//                
//                changeButton2.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        if (container.contains(wspinner2)) {
//                            container.removeWidget(wspinner2);
//                            container.addWidget(wcombobox2);
//                        } else {
//                            container.removeWidget(wcombobox2);
//                            container.addWidget(wspinner2);
//                        }
//                    }
//                });
//            }
//        };
//
//        MutableWidgetContainer mwc = new MutableWidgetContainer(Color.decode("#FF6200")) {
//            @Override
//            public void updateLines() {
//                clear();
//
//                addLine(headerLine, null);
//            }
//
//            @Override
//            public String getString() {
//                String str = super.getString();
//                System.out.println(str);
//                return str;
//            }
//        };
//
//        return mwc;
//    }
    public static void autoUpdateValue(final JSpinner jSpinner) {
        final JFormattedTextField tf = ((JSpinner.DefaultEditor) jSpinner.getEditor()).getTextField();
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                try {
                    int x = tf.getCaretPosition();
                    jSpinner.commitEdit();
                    tf.setCaretPosition(x);
                } catch (ParseException ex) {
                }
            }
        });
    }

    public static void main(String[] args) {
        QuickFrame.applyLookAndFeel();
//        QuickFrame.drawTest(createDrawableMove());
    }

    private void resetY() {

    }

    private void breakCurrentLine() {

    }
}
