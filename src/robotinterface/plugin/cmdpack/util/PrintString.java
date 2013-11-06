/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugin.cmdpack.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.procedure.Procedure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.nfunk.jep.Variable;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.drawable.DWidgetContainer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class PrintString extends Procedure implements FunctionToken<PrintString> {

    private String str;
    private ArrayList<String> varNames;

    public PrintString() {
        varNames = new ArrayList<>();
        str = "";
    }

    public PrintString(String str, boolean b) {
        int l = str.lastIndexOf("\"");

        String w = str.substring(l + 1, str.length());

        varNames = new ArrayList<>();

        for (String var : w.split(",")) {
            String vart = var.trim();
            if (!vart.isEmpty()) {
                varNames.add(vart);
            }
        }

        this.str = str.substring(1, l);
        setProcedure(toString());
    }

    public PrintString(String str, String... vars) {
        if (vars != null) {
            varNames = new ArrayList<>();
            varNames.addAll(Arrays.asList(vars));
        }
        this.str = str;
        setProcedure(toString());
    }

    @Override
    public boolean perform(Robot r, Clock clock) {
        String out = new String(str);
        for (String varName : varNames) {
            Variable v = getParser().getSymbolTable().getVar(varName);
            if (v != null && v.hasValidValue()) {
                out = out.replaceFirst("%v", v.getValue().toString());
            } else {
                out = out.replaceFirst("%v", "¿" + varName + "?");
            }
        }

        System.out.println(out);
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("print(\"").append(str.replaceAll("\n", "/n")).append("\"");

        for (String s : varNames) {
            sb.append(", ").append(s);
        }

        sb.append(")");
        return sb.toString();
    }

    @Override
    public Item getItem() {
        return new Item("Exibir", new Rectangle2D.Double(0, 0, 20, 15), color);
    }

    @Override
    public Object createInstance() {
        return new PrintString("Hello Worlld!");
    }
    private Color color = Color.decode("#6693BC");
    private Drawable d = null;
    private static Font font = new Font("Dialog", Font.BOLD, 12);

    public static Shape createPrintShape(Rectangle2D r) {
        GeneralPath gp = new GeneralPath();
        double mx = r.getWidth();
        double my = r.getHeight();
        double a = 15;
        double b = 20;

        gp.moveTo(a, 0);
        gp.lineTo(mx + a, 0);
        gp.curveTo(mx + b + a, 0, mx + b + a, my, mx + a, my);
        gp.lineTo(a, my);
        gp.lineTo(0, my / 2);
        gp.closePath();

        return gp;
    }

    @Override
    public Drawable getDrawableResource() {
        if (d == null) {
            Rectangle2D s = new Rectangle2D.Double(0, 0, 150, 60);
            //cria um Losango (usar em IF)
            //s = SimpleContainer.createDiamond(new Rectangle(0,0,150,100));

            SimpleContainer sContainer = new SimpleContainer(createPrintShape(s), color) {
                private ArrayList<DWidgetContainer.Widget> wFields = new ArrayList<>();
                private DWidgetContainer.Widget addButton;
                private DWidgetContainer.Widget remButton;
                private boolean updateFields = false;
                private boolean updateShape = false;

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
                            DWidgetContainer.Widget w = wFields.get(wFields.size() - 1);
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

                private void updateProcedure() {
//                    StringBuilder sb = new StringBuilder();
//
//                    for (DWidgetContainer.Widget w : wFields) {
//                        JTextField tf = (JTextField) w.getJComponent();
//                        String str = tf.getText();
//                        sb.append(str);
//                        if (!str.isEmpty() && !str.endsWith(";")) {
//                            sb.append(";");
//                        }
//                    }
//
//                    procedure = sb.toString();
                }

                private void createTextFields() {
                    for (Iterator<DWidgetContainer.Widget> it = wFields.iterator(); it.hasNext();) {
                        DWidgetContainer.Widget w = it.next();
                        removeJComponent(w);
                        it.remove();
                    }

                    addTextField(PrintString.this.toString());
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

                    for (DWidgetContainer.Widget w : wFields) {
                        y += INSET_Y;
                        w.setLocation((int) x, (int) y);
                        y += TEXTFIELD_HEIGHT;
                    }

//                    ((Rectangle2D.Double) shape).width = totalWidth;
//                    ((Rectangle2D.Double) shape).height = y + INSET_Y;
                    ((Rectangle2D.Double) bounds).width = totalWidth;
                    ((Rectangle2D.Double) bounds).height = y + INSET_Y;
//                    shape = createPrintShape(((Rectangle2D.Double) bounds));

                    if (updateShape) {
                        shape = createPrintShape(((Rectangle2D.Double) bounds));
                        updateShape = false;
                    }

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

                    double x = INSET_X + 15;
                    double y = INSET_Y;

                    double width = 0;
                    double tmpWidth;

                    g.setColor(Color.black);

                    g.translate(x, 0);
                    String www = PrintString.this.toString();
                    for (String str : www.split(";")) {
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

//                    ((Rectangle2D.Double) shape).width = width + 2 * INSET_X;
//                    ((Rectangle2D.Double) shape).height = y + 2 * INSET_Y;
                    ((Rectangle2D.Double) bounds).width = width + 2 * INSET_X;
                    ((Rectangle2D.Double) bounds).height = y + 2 * INSET_Y;
//                    shape = createPrintShape(((Rectangle2D.Double) bounds));

                    if (!updateShape) {
                        shape = createPrintShape(((Rectangle2D.Double) bounds));
                        updateShape = true;
                    }

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
    
    @Override
    public Procedure copy(Procedure copy) {
        Procedure p = super.copy(copy);
        
        if (copy instanceof PrintString){
            ((PrintString)copy).str = str;
            ((PrintString)copy).varNames.addAll(varNames);
        } else {
            
        }

        return p;
    }

    public static void main(String[] args) {
        Procedure p = new PrintString("ANDERSON");
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }

    @Override
    public String getToken() {
        return "print";
    }

    @Override
    public PrintString createInstance(String args) {
        return new PrintString(args, true);
    }
}
