/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import robotinterface.algorithm.procedure.Block;
import java.util.List;
import java.util.Random;
import org.nfunk.jep.Variable;
import robotinterface.algorithm.Command;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.robot.Robot;

/**
 *
 * @author antunes
 */
public class Function extends Block implements Drawable {

    private static HashMap<Command, Rectangle2D.Double> teste;
    private static HashMap<Class<? extends Command>, Point2D.Double[]> wiring;
    public static final Point2D.Double divider = new Point2D.Double(Double.NaN, Double.NaN);

    //ainda não tem funções, possuirá lista de argumentos, escopo de variaveis etc..
    public Function(String name, List<Variable> args) {
        teste = new HashMap<>();
        wiring = new HashMap<>();
        randnum.setSeed(System.currentTimeMillis());
//        randnum.setSeed(0);

        wiring.put(While.class, new Point2D.Double[]{
            new Point2D.Double(0, 2),
            new Point2D.Double(0, 3),
            new Point2D.Double(-1, 3),
            new Point2D.Double(-1, 0),
            new Point2D.Double(-0.5, 0),
            divider,
            new Point2D.Double(0.5, 0),
            new Point2D.Double(1, 0),
            new Point2D.Double(1, 4),
            new Point2D.Double(0, 4),
            new Point2D.Double(0, 5)});

    }

    public static void ident(Block b, double x, double y, double j, double k, double l) {
        /*
         * j - espaçamento entre comandos (pixels)
         * k - espaçamento entre o if e seus fluxos  (pixels)
         * l - orientação (rad)
         * s - posição relativa ao comando anterior ([-1,1] sem unidade)
         * 
         * TODO: usar l, atualmente o fluxograma cresce na direção y+
         * 
         */

        double cw;
        double ch = 0;

        Rectangle2D.Double t = getCBounds(b);
        if (t != null) {
            cw = t.width;
            ch = t.height;
            //set location
            t.x = x - cw / 2;
            t.y = y;
            y += ch + j;
        }

        Command it = b.start;
        while (it != null) {

            t = getCBounds(it);
            if (t != null) {
                cw = t.width;
                ch = t.height;
                //set location
                t.x = x - cw / 2;
                t.y = y;
                y += ch + j;
            }

//            if (it instanceof GraphicResource) {
//                Drawable d = ((GraphicResource) it).getDrawableResource();
//                cw = d.getObjectBouds().width;
//                ch = d.getObjectBouds().height;
//                d.setObjectLocation(x - cw / 2, y - ch / 2);
//                y += ch + j;
//            }

            if (it instanceof Block) {
                y -= (ch + j); //ident já cuida do espaçamento do incio do bloco
                ident((Block) it, x, y, j, k, l);
                Point2D.Double bb = getBounds((Block) it, null, j, k, l);
                y += bb.y;
            } else if (it instanceof If) {
                If i = (If) it;
                ident(i.getBlockFalse(), x, y, j, k, l);
                Point2D.Double bfb = getBounds(i.getBlockFalse(), null, j, k, l);
                Point2D.Double btb = getBounds(i.getBlockTrue(), null, j, k, l);
                ident(i.getBlockTrue(), x + bfb.x / 2 + k + btb.x / 2, y, j, k, l);

                y += (bfb.y > btb.y) ? bfb.y : btb.y;
//                y += getBounds(i, null, j, k, l).y;
            }

            it = it.getNext();
        }

    }
    private static Random randnum = new Random();

    private static Rectangle2D.Double getCBounds(Command c) {
        if (!teste.containsKey(c)) {
//            double w = 40;
//            double h = 70;
            double w = randnum.nextDouble() * 80 + 20;
            double h = randnum.nextDouble() * 80 + 20;
            teste.put(c, new Rectangle2D.Double(0, 0, w, h));
        }
        return teste.get(c);
    }

//    public static Rectangle2D.Double getBounds (Command c){
//        if (c instanceof GraphicResource) {
//            return ((GraphicResource) c).getDrawableResource().getObjectBouds();
//        }
//        
//        return null;
//    }
    private static Point2D.Double getBounds(Command c, Point2D.Double ret, double j, double k, double l) {

        /*
         * j - espaçamento entre comandos (pixels)
         * k - espaçamento entre o if e seus fluxos  (pixels)
         * l - orientação (rad)
         * s - posição relativa ao comando anterior ([-1,1] sem unidade)
         * 
         * TODO: usar l, atualmente o fluxograma cresce na direção y+
         * 
         */

        if (ret == null) {
            ret = new Point2D.Double();
        } else {
            ret.setLocation(0, 0);
        }

        //pega o tamnho do comando atual
        Rectangle2D.Double t = getCBounds(c);
        if (t != null) {
            ret.x += t.width;
            ret.y += t.height;
            ret.y += j;
        }

//        if (c instanceof GraphicResource) {
//            Rectangle2D.Double t = ((GraphicResource) c).getDrawableResource().getObjectBouds();
//            ret.x += t.width;
//            ret.y += t.height;
//        }

        if (c instanceof Block) {
            Block b = (Block) c;
            Point2D.Double p = new Point2D.Double();
            Command it = b.start;
            while (it != null) {
                p = getBounds(it, p, j, k, l);
                ret.x = (ret.x > p.x) ? ret.x : p.x;
                ret.y += p.y;
                it = it.getNext();
            }
        } else if (c instanceof If) {
            If i = (If) c;
            Point2D.Double p = new Point2D.Double();
            //false
            p = getBounds(i.getBlockFalse(), p, j, k, l);
            ret.x = (ret.x > p.x) ? ret.x : p.x;
            double ty = p.y;
            //true
            p = getBounds(i.getBlockTrue(), p, j, k, l);
            ret.x += j + p.x;
//            ret.x = (ret.x > p.x) ? ret.x : p.x;
            ty = (ty > p.y) ? ty : p.y;
            ret.y += ty;
        }

        return ret;
    }
    public static final int ARR_SIZE = 6;

    public static void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.setStroke(new BasicStroke(2));

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, len - ARR_SIZE - 1, 0);
        g.fillPolygon(new int[]{len, len - ARR_SIZE, len - ARR_SIZE, len},
                new int[]{0, -ARR_SIZE, ARR_SIZE, 0}, 4);
        at.setToIdentity();
        g.transform(at);
    }

    public static void wire(Block b, ArrayList<Line2D.Double> lines, double j, double k, double l) {
        Command it = b.start;
        Line2D.Double line = null;
        while (it != null) {
            if (it instanceof Block.BlockEnd) {
                for (Class c : wiring.keySet()) {
                    if (c.isInstance(b)) {
                        Point2D.Double[] reference = wiring.get(c);
                        Point2D.Double pts[] = new Point2D.Double[]{new Point2D.Double(), new Point2D.Double()};
                        Rectangle2D.Double beginBounds = getCBounds(b);
                        double w = getBounds(b, null, j, k, l).x;
                        Rectangle2D.Double endBounds = getCBounds(it);
                        for (int i = 1; i < reference.length; i++) {
                            if (reference[i] == divider || reference[i - 1] == divider) {
                                continue;
                            }
                            pts[0].setLocation(reference[i - 1]);
                            pts[1].setLocation(reference[i]);
                            for (Point2D.Double p : pts) {
                                if (p.x < 0) {
                                    if (p.x >= -0.5) {
                                        p.x = beginBounds.getCenterX() + p.x * beginBounds.width;
                                    } else {
                                        p.x = beginBounds.getCenterX() - w / 2 + p.x * (j / 2);
                                    }
                                } else if (p.x > 0) {
                                    if (p.x <= 0.5) {
                                        p.x = beginBounds.getCenterX() + p.x * beginBounds.width;
                                    } else {
                                        p.x = beginBounds.getCenterX() + w / 2 + p.x * (j / 2);
                                    }
                                } else {
                                    p.x = beginBounds.getCenterX();
                                }

                                switch ((int) p.y) {
                                    case 0:
                                        p.y = beginBounds.getCenterY();
                                        break;
                                    case 1:
                                        p.y = endBounds.getCenterY();
                                        break;
                                    case 2:
                                        p.y = endBounds.getMaxY();
                                        break;
                                    case 3:
                                        p.y = endBounds.getMaxY() + k * .40;
                                        break;
                                    case 4:
                                        p.y = endBounds.getMaxY() + k * (1 - .40);
                                        break;
                                    case 5:
                                        p.y = endBounds.getMaxY() + k;
                                        break;
                                    default:
                                        System.err.println("Parametro inválido, wire");
                                }
                            }

                            lines.add(new Line2D.Double(pts[0], pts[1]));
                        }
//                        lines.add(null);
                    }
                }
            } else if (it instanceof Block) {
                wire((Block) it, lines, j, k, l);
            }

            it = it.getNext();
        }
    }
    Rectangle2D.Double shape = new Rectangle2D.Double();

    @Override
    public Shape getObjectShape() {
        Point2D.Double p = getBounds(this, null, 0, 0, 0);
        shape.setRect(0, 0, p.x, p.y);
        return shape;
    }

    @Override
    public Rectangle2D.Double getObjectBouds() {
        return shape;
    }

    @Override
    public void setObjectBounds(double x, double y, double width, double height) {
    }

    @Override
    public void setObjectLocation(double x, double y) {
    }

    @Override
    public int getDrawableLayer() {
        return Drawable.DEFAULT_LAYER;
    }

    @Override
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }
    ArrayList<Line2D.Double> myLines = new ArrayList<>();

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        if (in.isKeyPressed(KeyEvent.VK_1)) {
            Function.ident(this, 100, 100, 50, 50, 10);
            myLines.clear();
            Function.wire(this, myLines, 50, 50, 10);
        }

        for (Command c : teste.keySet()) {
            g.setColor(Color.LIGHT_GRAY);
            Rectangle2D.Double r = teste.get(c);

            g.fill(r);
            g.setColor(Color.BLACK);
            g.drawString(c.getCommandName(), (int) r.getCenterX(), (int) r.getCenterY());
        }

        boolean b = false;;
        for (Line2D.Double l : myLines) {
            g.setStroke(new BasicStroke(2));
            g.setColor(Color.MAGENTA);
                    g.draw(l);
//            if (l == null) {
//                System.out.println("asd");
//                b = true;
//            } else {
//                if (b) {
//                    drawArrow(g, (int) l.x1, (int) l.y1, (int) l.x2, (int) l.y2);
//                    b = false;
//                } else {
//                    
//                }
//
//            }
        }
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }
}
