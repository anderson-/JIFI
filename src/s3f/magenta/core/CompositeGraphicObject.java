/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.magenta.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import s3f.magenta.Drawable;
import s3f.magenta.DrawingPanel;
import s3f.magenta.graphicresource.SimpleContainer;
import s3f.magenta.swing.WidgetContainer;
import s3f.magenta.swing.component.Widget;
import s3f.magenta.util.QuickFrame;

/**
 *
 * @author anderson
 */
public class CompositeGraphicObject extends WidgetContainer {

    private final JPanel container;
    private final ArrayList<Render> renders;
    private ShapeCreator shapeCreator = ShapeCreator.DEFAULT;
    private final Widget content;
    private Iterator<Render> iterator;
    private Render currentRender;

    public CompositeGraphicObject() {
        container = new JPanel();
        container.setBackground(new Color(0, 0, 0, 0));
//        container.setBackground(Color.red);
        container.setLayout(new GridBagLayout());
        renders = new ArrayList<>();
        content = new Widget(container);
        super.addWidget(content);
    }

    public JPanel getContainer() {
        return container;
    }

    public void setContent(JComponent component) {
        container.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.CENTER;
        container.add(component, gbc);
    }

    public ShapeCreator getShapeCreator() {
        return shapeCreator;
    }

    public void setShapeCreator(ShapeCreator shapeCreator) {
        if (shapeCreator != null) {
            this.shapeCreator = shapeCreator;
        } else {
            this.shapeCreator = ShapeCreator.DEFAULT;
        }
    }

    public void addRender(Render render) {
        if (render != null) {
            renders.add(render);
        }
    }

    public void removeRender(Render render) {
        renders.remove(render);
    }

    @Override
    public Shape getObjectShape() {
        shape = shapeCreator.create(getObjectBouds());
        return super.getObjectShape();
    }

    @Override
    public final Rectangle2D.Double getObjectBouds() {
        bounds.setRect(0, 0, container.getPreferredSize().getWidth(), container.getPreferredSize().getHeight());
        return bounds;
    }

    @Override
    public int getDrawableLayer() {
        return Drawable.BACKGROUND_LAYER | Drawable.DEFAULT_LAYER | Drawable.TOP_LAYER;
    }

    private void drawLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in, int layer) {
        while (iterator != null) {
            if (currentRender != null) {
                if ((currentRender.getDrawableLayer() & layer) != 0) {
                    if (layer == Drawable.BACKGROUND_LAYER) {
                        currentRender.drawBackground(g, ga, in);
                    } else if (layer == Drawable.DEFAULT_LAYER) {
                        currentRender.draw(g, ga, in);
                    } else {
                        currentRender.drawTopLayer(g, ga, in);
                    }
                } else {
                    return;
                }
            }
            if (iterator.hasNext()) {
                currentRender = iterator.next();
            } else {
                iterator = null;
                return;
            }
        }
    }

    @Override
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        iterator = renders.iterator();
        drawLayer(g, ga, in, Drawable.BACKGROUND_LAYER);
    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        drawLayer(g, ga, in, Drawable.DEFAULT_LAYER);
        if (in.getMouseClickCount() == 2) {
//            setContent(new JLabel("     ad" + Math.random() + "      "));
            in.consumeMouseClick();
        }
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        drawLayer(g, ga, in, Drawable.TOP_LAYER);
        currentRender = null;
    }

    public static void main(String[] args) {
        DrawingPanel p = new DrawingPanel();
        QuickFrame.create(p, "Teste do painel de desenho").addComponentListener(p);
        CompositeGraphicObject cgo = new CompositeGraphicObject();
        {
            JPanel container1 = cgo.getContainer();

            JPanel panel = container1;
//            JPanel panel = new JPanel();
            panel.setLayout(new MigLayout("insets 25 10 0 10"));
//            panel.setBackground(new Color(0, 0, 255, 20));
//            panel.add(new JLabel("teste: "));
//            panel.add(new JButton("a button"), "wrap");
//            panel.add(new JLabel("teste2: "));
//            panel.add(new JTextField("txt"), "grow, wrap");
            String arg = "";
            panel.add(new JLabel("VAR"), arg);
            panel.add(new JTextField(" x "), arg);
            panel.add(new JLabel("="), arg);
            panel.add(new JLabel("print("), arg);
            final RSyntaxTextArea rTextArea = new RSyntaxTextArea();
            rTextArea.setSyntaxEditingStyle("text/javascript");
            rTextArea.setText(" \"Hello World!\" ");
            rTextArea.addCaretListener(new CaretListener() {
                private int lastCarretPosition = 0;

                @Override
                public void caretUpdate(final CaretEvent ce) {
                    final int i = rTextArea.getText().indexOf('\n');
                    if (i != -1) {
                        new Thread() {
                            @Override
                            public void run() {
                                rTextArea.setText(rTextArea.getText().replaceAll("\n", ""));
                                rTextArea.setCaretPosition(lastCarretPosition);
                            }
                        }.start();
                    } else if (rTextArea.getText().isEmpty()) {
                        new Thread() {
                            @Override
                            public void run() {
                                rTextArea.setText("  ");
                            }
                        }.start();
                    } else {
                        lastCarretPosition = rTextArea.getCaretPosition();
                    }
                }
            });
            panel.add(rTextArea, arg);
            panel.add(new JLabel(","), arg);
            panel.add(new JComboBox(new Object[]{" x ", " y "}), arg);
            panel.add(new JLabel(");"), arg);
//            cgo.setContent(panel);
        }
        cgo.setWidgetVisible(true);
        cgo.setShapeCreator(ShapeCreator.DIAMOND);
        cgo.addRender(new ShapeRender(Color.GREEN));
        p.add(cgo);
    }

}
