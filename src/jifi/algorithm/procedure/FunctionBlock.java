package jifi.algorithm.procedure;

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package jifi.algorithm.procedure;
//
//import java.awt.BasicStroke;
//import java.awt.Color;
//import java.awt.Font;
//import java.awt.FontMetrics;
//import java.awt.Graphics2D;
//import java.awt.Shape;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.geom.Line2D;
//import java.awt.geom.Rectangle2D;
//import javax.swing.JComboBox;
//import jifi.algorithm.Command;
//import jifi.drawable.GraphicObject;
//import jifi.drawable.DrawingPanel;
//import jifi.drawable.graphicresource.GraphicResource;
//import jifi.drawable.graphicresource.SimpleContainer;
//import jifi.drawable.swing.component.Widget;
//import jifi.gui.GUI;
//import jifi.gui.panels.sidepanel.Item;
//import jifi.interpreter.ExecutionException;
//import jifi.interpreter.ResourceManager;
//
///**
// *
// * @author antunes
// */
//public class FunctionBlock extends Procedure {
//    //talvez tenha que ser criado um clone completo da função (para recursividade)
//    //o parser suporta isso? Acho que não...
//
//    private Function function;
//
//    public FunctionBlock() {
//    }
//
//    public FunctionBlock(Function function) {
//        setFunction(function);
//    }
//
//    public final void setFunction(Function function) {
//        if (function == null) {
//            return;
//        }
//        this.function = function;
//        System.out.println("selecionando: " + function.getCommandName());
//        function.getEnd().setBlockBegin(this);
//    }
//
//    public Function getFunction() {
//        return function;
//    }
//
//    @Override
//    public Command step(ResourceManager rm) throws ExecutionException {
//        if (function == null){
//            return super.step(rm);
//        }
//        
//        if (function.isDone()) {
//            function.setDone(false);
//            function.reset();
//            return super.step(rm);
//        } else {
//            return function.step(rm);
//        }
//    }
//
//    @Override
//    public void toString(String ident, StringBuilder sb) {
//        sb.append(ident).append(function).append("(....);");
//    }
//
//    @Override
//    public Item getItem() {
//        return new Item("Função Externa", new Rectangle2D.Double(0, 0, 20, 15), Color.decode("#69CD87"));
//    }
//
//    @Override
//    public Object createInstance() {
//        return new FunctionBlock();
//    }
//    private GraphicObject d = null;
//    private static Font font = new Font("Dialog", Font.BOLD, 12);
//
//    @Override
//    public GraphicObject getDrawableResource() {
//        if (d == null) {
//            Shape s = new Rectangle2D.Double(0, 0, 150, 60);
//            //cria um Losango (usar em IF)
//            //s = SimpleContainer.createDiamond(new Rectangle(0,0,150,100));
//            Color c = Color.decode("#69CD87");
//
//            SimpleContainer sContainer = new SimpleContainer(s, c) {
//                private Widget wcb;
//                private boolean updateFields = false;
//                private JComboBox cb;
//
//                {
//
//                    cb = new JComboBox();
//
//                    wcb = addWidget(cb, 0, 0, 150, 25);
//
//                    cb.addActionListener(new ActionListener() {
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
////                            JComboBox cb = (JComboBox) e.getSource();
//                            setFunction((Function) cb.getSelectedItem());
////                            update();
//                        }
//                    });
//
//                }
//
//                private void drawLine(Graphics2D g) {
//                    Command c = getNext();
//                    if (c instanceof GraphicResource) {
//                        GraphicObject d = ((GraphicResource) c).getDrawableResource();
//                        if (d != null) {
//                            Rectangle2D.Double bThis = getObjectBouds();
//                            Rectangle2D.Double bNext = d.getObjectBouds();
//                            Line2D.Double l = new Line2D.Double(bThis.getCenterX(), bThis.getMaxY(), bNext.getCenterX(), bNext.getMinY());
//                            g.setStroke(new BasicStroke(2));
//                            g.setColor(Color.red);
//                            g.draw(l);
//                        }
//                    }
//                }
//
//                public void update() {
//                    cb.removeAllItems();
//                    for (Function f : GUI.getInstance().getFunctions()) {
//                        cb.addItem(f);
//                    }
//                }
//
//                @Override
//                protected void drawWJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
//
//                    if (updateFields) {
//                        update();
//                        updateFields = false;
//                    }
//
//                    String str = "Função Externa:";
//
//                    g.setFont(font);
//                    FontMetrics fm = g.getFontMetrics();
//
//                    double x;
//                    double y;
//
//                    double totalWidth = 2 * BUTTON_WIDTH + TEXTFIELD_WIDTH + 4 * INSET_X;
//
//                    double width = fm.stringWidth(str);
//                    double height = fm.getHeight();
//
//                    x = (totalWidth - width) / 2;
//                    y = INSET_Y + fm.getAscent();
//
//                    g.setColor(Color.black);
//                    g.translate(x, y);
//                    g.drawString(str, 0, 0);
//                    g.translate(-x, -y);
//
//                    x = BUTTON_WIDTH + 2 * INSET_X;
//
//                    y += INSET_Y;
//                    wcb.setLocation((int) x, (int) y);
//                    y += TEXTFIELD_HEIGHT;
//
//                    ((Rectangle2D.Double) shape).width = totalWidth;
//                    ((Rectangle2D.Double) shape).height = y + INSET_Y;
//                    ((Rectangle2D.Double) bounds).width = totalWidth;
//                    ((Rectangle2D.Double) bounds).height = y + INSET_Y;
//
//                    y -= TEXTFIELD_HEIGHT;
//                    x = INSET_X;
//
//
//
//
////                    AffineTransform o = g.getTransform();
////                    System.out.println(o);
////                    ga.removeRelativePosition(o);
////                    ga.applyGlobalPosition(o);
////                    //ga.removeZoom(o);
////                    g.setTransform(o);
//
//
//                    g.translate(-bounds.x, -bounds.y);
//                    drawLine(g);
//                }
//
//                @Override
//                protected void drawWoJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
//                    //escreve coisas quando os jcomponets não estão visiveis
//
//                    if (!updateFields) {
//                        updateFields = true;
//                    }
//
//                    g.setFont(font);
//                    FontMetrics fm = g.getFontMetrics();
//
//                    double x = INSET_X;
//                    double y = INSET_Y;
//
//                    double width = 0;
//                    double tmpWidth;
//
//                    g.setColor(Color.black);
//
//                    g.translate(x, 0);
//
//                    String str;
//                    if (getFunction() != null) {
//                        str = getFunction().getName() + "(...)";
//                    } else {
//                        str = "selecione a função";
//                    }
//                    
//                    str += ";";
//                    str = str.trim();
//                    tmpWidth = fm.stringWidth(str);
//                    if (tmpWidth > width) {
//                        width = tmpWidth;
//                    }
//                    y += fm.getAscent();
//
//                    g.translate(0, y);
//                    g.drawString(str, 0, 0);
//                    g.translate(0, -y);
//
//                    g.translate(-x, 0);
//
//                    ((Rectangle2D.Double) shape).width = width + 2 * INSET_X;
//                    ((Rectangle2D.Double) shape).height = y + 2 * INSET_Y;
//                    ((Rectangle2D.Double) bounds).width = width + 2 * INSET_X;
//                    ((Rectangle2D.Double) bounds).height = y + 2 * INSET_Y;
//
//                    g.translate(-bounds.x, -bounds.y);
//                    drawLine(g);
//
////                    double width = fm.stringWidth(procedure);
////                    double height = fm.getHeight();
////
////                    ((Rectangle2D.Double) shape).width = width + 2 * INSET_X;
////                    ((Rectangle2D.Double) shape).height = height + 2 * INSET_Y;
////
////                    double x;
////                    double y;
////
////                    x = INSET_X;
////                    y = (((Rectangle2D.Double) shape).height - height) / 2 + fm.getAscent();
////
////                    g.setColor(Color.black);
////                    g.translate(x, y);
////                    g.drawString(procedure, 0, 0);
////                    g.translate(-x, -y);
//
//                }
//            };
//
//            d = sContainer;
//        }
//        return d;
//    }
//}
