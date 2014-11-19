/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.magenta.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import s3f.jifi.flowchart.parser.parameterparser.Argument;
import s3f.jifi.flowchart.blocks.Procedure;
import s3f.magenta.DrawingPanel;
import s3f.magenta.GraphicObject;
import s3f.magenta.sidepanel.Item;
import s3f.magenta.swing.component.Component;
import s3f.magenta.swing.component.SubLineBreak;
import s3f.magenta.swing.component.TextLabel;
import s3f.magenta.swing.component.Widget;
import s3f.magenta.swing.component.WidgetLine;

/**
 *
 * @author antunes
 */
public class MutableWidgetContainer extends WidgetContainer {

    protected static Font defaultFont;
    public static final int INSET_Xd = 5;
    public static final int INSET_Yd = 5;
    public static Stroke BOUNDS_STROKE = new BasicStroke(5);
    public static Stroke SHADOW_STROKE = new BasicStroke(5);
    public static Stroke FLOWCHART_LINE_STROKE = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    protected String boxLabel = "";
    protected Color color;
    protected int shapeStartX = 0;
    protected int shapeStartY = 0;
    protected boolean center = false;
    protected boolean widgetsEnabled = true;
    protected Color boxLabelColor = Color.BLACK;
    protected Font stringFont = defaultFont;
    protected Rectangle2D.Double shapeBounds;
    private Rectangle2D.Double tmpShape = null;
    private String name = "";
    private double stringWidth = 0;
    private int firstShapeUpdate = 2;
    private boolean updateStructure = false;
    private boolean updateShape = false;
    private boolean updateHeight = false;
    private final ArrayList<String> splittedBoxLabel;
    protected final ArrayList<WidgetLine> rowTypes;
    protected final ArrayList<ArrayList<Component>> rows;
    private final ArrayList<Argument> tmpArguments;
    private final HashMap<Widget, Argument> eMap;
    private boolean absolute = false;
    private boolean mouseBlocked = false;
    private boolean simpleDraw = false;

    public MutableWidgetContainer(Boolean simpleDraw) {
        this(Color.RED);
        this.simpleDraw = simpleDraw;
        this.absolute = true;
        this.mouseBlocked = true;
        setWidgetVisible(true);
        defaultFont = UIManager.getDefaults().getFont("TabbedPane.font");
    }

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

    public void setMouseBlocked(boolean mouseBlocked) {
        this.mouseBlocked = mouseBlocked;
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
        if (tmpShape == null) {
            tmpShape = new Rectangle2D.Double();
        }
        tmpShape.setRect(bounds);
        return tmpShape;
    }

    @Override
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        //linhas
        g.setColor(color.darker());
        g.setStroke(FLOWCHART_LINE_STROKE);
        g.translate(-bounds.x, -bounds.y);
        backDraw(g);
        g.translate(bounds.x, bounds.y);
    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {

        //usado para bloquear a edição do fluxograma
        if (!mouseBlocked && widgetsEnabled && in.mouseClicked() && in.getMouseClickCount() == 2) {
//            Interpreter interpreter = GUI.getInstance().getInterpreter();
//            Command currentCommand = interpreter.getCurrentCommand();
//            Function function = interpreter.getMainFunction();
//            if (interpreter.getInterpreterState() == Interpreter.PLAY || currentCommand != function) {
//                SwingUtilities.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        JOptionPane.showMessageDialog(null, "Atenção: A edição do fluxograma está suspensa\naté que o código termine ou seja parado.", "Atenção", JOptionPane.WARNING_MESSAGE);
//
//                    }
//                });
//            } else {
            setWidgetVisible(!isWidgetVisible());
            shapeBounds.setRect(0, 0, 0, 0);
//            }
            in.consumeMouseClick();
        }

        if (simpleDraw) {
            //utilizando como item :/ não pensei em nada melhor...
            g.setColor(Color.white);
            if (bounds.width > Item.maxWidth) {
                Item.maxWidth = bounds.width;
            }

            g.fillRoundRect(0, 0, (int) Item.maxWidth, (int) bounds.height, 10, 10);
        } else {
            //sombra
            AffineTransform t = ga.getT();

            t.translate(3, 2);
            g.setColor(color.darker());
            g.setStroke(SHADOW_STROKE);
            g.draw(t.createTransformedShape(shape));
            ga.done(t);

            //fundo branco
            g.setColor(Color.white);

            g.fill(shape);

            g.setStroke(BOUNDS_STROKE);
            g.setColor(color);

            g.draw(shape);
        }

        AffineTransform o = g.getTransform();

        //componente
        if (isWidgetVisible()) {
            drawOpenBox(g, ga, in);
        } else {
            drawClosedBox(g, ga, in);
        }

//        g.setStroke(                new BasicStroke(4));
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

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    @Override
    public int getDrawableLayer() {
        return GraphicObject.BACKGROUND_LAYER | GraphicObject.DEFAULT_LAYER | ((absolute) ? GraphicObject.ABSOLUTE : 0);
    }

    private void resetY() {

    }

    public void softEntangle(Argument arg, Widget w) {
        w.setDynamic(true);
        eMap.put(w, arg);
        arg.setValueOf(w);
        addWidget(w);

        if (w.getJComponent() instanceof JSpinner) {
            JSpinner c = (JSpinner) w.getJComponent();
            c.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    getBoxLabel();
                }
            });
        }

        if (w.getJComponent() instanceof JComboBox) {
            JComboBox c = (JComboBox) w.getJComponent();
            c.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getBoxLabel();
                }
            });
        }

        if (w.getJComponent() instanceof JTextField) {
            JTextField c = (JTextField) w.getJComponent();
            c.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getBoxLabel();
                }
            });
        }
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
}
