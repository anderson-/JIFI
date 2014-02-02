/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.parameterparser.Argument;
import static robotinterface.algorithm.procedure.DummyBlock.createSimpleBlock;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.swing.DrawableCommandBlock;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.component.Widget;
import robotinterface.drawable.swing.component.TextLabel;
import robotinterface.drawable.swing.WidgetContainer;
import robotinterface.drawable.swing.component.Component;
import robotinterface.drawable.swing.component.LineBreak;
import robotinterface.drawable.swing.component.WidgetLine;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.ResourceManager;

/**
 *
 * @author antunes2
 */
public class Comment extends Procedure {

    private static Color myColor = Color.decode("#FFB319");
    private String comment = "";

    public Comment() {

    }

    public Comment(String comment) {
        this.comment = comment;
        setProcedure(comment);
    }

    @Override
    public boolean perform(ResourceManager rm) throws ExecutionException {
        return true;
    }

    private GraphicObject resource = null;

    public static MutableWidgetContainer createDrawableComment(final Comment c) {

        final int TEXTFIELD_WIDTH = 200;
        final int TEXTFIELD_HEIGHT = 100;
        final int BUTTON_WIDTH = 25;
        final int INSET_X = 5;
        final int INSET_Y = 5;

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Comentário:", true));
                components.add(new LineBreak());
                JTextArea textField = new JTextArea();//(String) data);
                components.add(new Widget(textField, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
                components.add(new LineBreak(true));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                if (arguments.size() > 0) {
                    Argument arg = arguments.get(1);
                    sb.append(arg);
                }
            }
        };

        DrawableCommandBlock dcb = new DrawableCommandBlock(c, myColor) {
            private GeneralPath myShape = new GeneralPath();

            {
                Font font;
                try {
                    //                String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//
//                for (int i = 0; i < fonts.length; i++) {
//                    System.out.println(fonts[i]);
//                }
//                    font = Font.createFont(Font.TRUETYPE_FONT, new File("resources/Purisa-Bold.ttf"));
                    
                    InputStream myStream = new BufferedInputStream(this.getClass().getResourceAsStream("/resources/Purisa-Bold.ttf"));
                    Font fontRaw = Font.createFont(Font.TRUETYPE_FONT, myStream);
                    Font fontBase = fontRaw.deriveFont(12f);
                    font = fontBase;
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    ge.registerFont(font);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    font = defaultFont;
                }
                super.stringFont = font;
//                System.out.println(stringFont);
                super.stringColor = Color.BLACK;
                string = c.getProcedure();
                updateLines();
            }

            @Override
            public void updateLines() {
                clear();
                c.comment = string;
                c.setProcedure(string);
                addLine(headerLine);
            }

            @Override
            public void splitString(String original, Collection<String> splitted) {
                String[] split = original.split("\n");
                for (String str : split) {
                    str += "\n";
                    str = str.trim();
                    splitted.add(str);
                }
            }

            @Override
            public Shape updateShape(Rectangle2D bounds) {
                myShape.reset();
                double a = bounds.getWidth() / 10;
                myShape.moveTo(a, 0);
                myShape.lineTo(bounds.getWidth(), 0);
                myShape.lineTo(bounds.getWidth(), bounds.getHeight());
                myShape.lineTo(0, bounds.getHeight());
                myShape.lineTo(0, a);
                myShape.closePath();

                return myShape;
            }

            @Override
            public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {

                if (widgetsEnabled & in.mouseClicked() && in.getMouseClickCount() == 2) {
                    setWidgetVisible(!isWidgetVisible());
                    shapeBounds.setRect(0, 0, 0, 0);
                }

                //sombra
                AffineTransform t = ga.getT();
                t.translate(3, 2);
                g.setColor(color.darker());
                g.setStroke(new BasicStroke(5));
                g.draw(t.createTransformedShape(shape));
                ga.done(t);

                //fundo branco
                g.setColor(color);
                g.fill(shape);
                g.setStroke(new BasicStroke(5));
                g.setColor(color);
                g.draw(shape);

                AffineTransform o = g.getTransform();

                //componente
                if (isWidgetVisible()) {
                    drawWJC(g, ga, in);
                } else {
                    drawWoJC(g, ga, in);
                }

                g.setStroke(new BasicStroke(4));

                g.setTransform(o);

            }
        };

        return dcb;
    }

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawableComment(this);
        }

        return resource;
    }

    @Override
    public Item getItem() {
        Polygon tmpPoly = new Polygon();
        tmpPoly.addPoint(6, 0);
        tmpPoly.addPoint(20, 0);
        tmpPoly.addPoint(20, 13);
        tmpPoly.addPoint(0, 13);
        tmpPoly.addPoint(0, 6);
        return new Item("Comentário", tmpPoly, myColor);
    }

    @Override
    public Object createInstance() {
        return new Comment();
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append(comment);
    }
}
