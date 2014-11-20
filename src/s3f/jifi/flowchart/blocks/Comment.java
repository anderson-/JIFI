/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.flowchart.blocks;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import s3f.jifi.flowchart.parameterparser.Argument;
import s3f.magenta.swing.DrawableProcedureBlock;
import s3f.magenta.DrawingPanel;
import s3f.magenta.GraphicObject;
import s3f.magenta.swing.MutableWidgetContainer;
import s3f.magenta.swing.component.Widget;
import s3f.magenta.swing.component.TextLabel;
import s3f.magenta.swing.component.Component;
import s3f.magenta.swing.component.SubLineBreak;
import s3f.magenta.swing.component.WidgetLine;
import s3f.magenta.sidepanel.Item;

/**
 *
 * @author antunes2
 */
public class Comment extends Procedure {

    private static Color myColor = Color.decode("#FFB319");
    private Argument arg0;

    public Comment() {
        this("//isso é um comentário :D");
    }

    public Comment(String comment) {
        if (comment.endsWith("\n")) {
            comment = comment.substring(0, comment.length() - 1);
        }
        comment = comment.replaceAll("[\t\r\f]+", "");

        arg0 = new Argument(comment, Argument.TEXT, true) {

            @Override
            public boolean setValueOfExtended(JComponent jc) {
                if (jc instanceof JTextArea) {
                    ((JTextArea) jc).setText(this.getStringValue());
                    return true;
                }
                return false;
            }

            @Override
            public boolean getValueOfExtended(JComponent jc) {
                if (jc instanceof JTextArea) {
                    this.set(((JTextArea) jc).getText(), TEXT);
                    return true;
                }
                return false;
            }

        };
    }

    private GraphicObject resource = null;

    public static MutableWidgetContainer createDrawableComment(final Comment c) {

        final int TEXTFIELD_WIDTH = 200;
        final int TEXTFIELD_HEIGHT = 100;

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Comentário:", true));
                components.add(new SubLineBreak());
                JTextArea textField = new JTextArea();
                Widget widget = new Widget(textField, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                components.add(widget);
                container.entangle(c.arg0, widget);
                components.add(new SubLineBreak(true));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                if (arguments.size() > 0) {
                    String str = arguments.get(0).toString();
                    if (!str.contains("\n") && !(str.startsWith("/*") || str.endsWith("*/"))) {
                        if (!str.startsWith("//")) {
                            sb.append("//").append(str);
                        } else {
                            sb.append(str);
                        }
                    } else {
                        if (!str.startsWith("/*")) {
                            sb.append("/*");
                        }
                        sb.append(str);
                        if (!str.endsWith("*/")) {
                            sb.append("*/");
                        }
                    }
                }
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(c, myColor) {
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
                super.boxLabelColor = Color.BLACK;
//                string = c.getProcedure();
//                updateLines();
            }

            @Override
            public void updateStructure() {
                clear();
//                c.comment = string;
//                c.setProcedure(string);
                addLine(headerLine);
            }

            @Override
            public void splitBoxLabel(String original, Collection<String> splitted) {
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
                    drawOpenBox(g, ga, in);
                } else {
                    drawClosedBox(g, ga, in);
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
        return new Item("Comentário", tmpPoly, myColor, "Usado para deixar notas e dicas sobre o funcionamento do seu programa, não interfere na execução do mesmo");
    }

    @Override
    public Object createInstance() {
        return new Comment();
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        String str = arg0.toString();

        if (!str.contains("\n")) {
            sb.append(ident);
            if (!str.startsWith("//") && !(str.startsWith("/*") || str.endsWith("*/"))) {
                sb.append("//").append(str);
            } else {
                sb.append(str);
            }
        } else {
            if (!str.startsWith("/*")) {
                sb.append("/*");
            }

            for (String s : str.split("\n")) {
                sb.append(ident).append(s).append("\n");
            }

            if (!str.endsWith("*/")) {
                sb.append("*/");
            }
        }
        sb.append("\n");
    }
}
