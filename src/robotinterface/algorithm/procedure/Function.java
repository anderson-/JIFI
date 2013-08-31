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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.nfunk.jep.Variable;
import robotinterface.algorithm.Command;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;

/**
 * Função com *futuro* suporte a argumentos. <### EM DESENVOLVIMENTO ###>
 */
public class Function extends Block implements Drawable {

    private static HashMap<Command, Rectangle2D.Double> teste;
    private static HashMap<Class<? extends Command>, Point2D.Double[]> wiring;
    public static final Point2D.Double divider = new Point2D.Double(Double.NaN, Double.NaN);
    private static Random randNumGen = new Random(); //para testes

    public Function(String name, List<Variable> args) {
        teste = new HashMap<>();
        wiring = new HashMap<>();
        randNumGen.setSeed(System.currentTimeMillis());
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

    public static void ident(Block b, double x, double y, double j, double k, double Ix, double Iy, boolean a) {
        /*
         * j - espaçamento entre comandos (pixels)
         * k - espaçamento entre o if e seus fluxos  (pixels)
         * l - orientação (rad)
         * s - posição relativa ao comando anterior ([-1,1] sem unidade)
         * 
         * TODO: usar l, atualmente o fluxograma cresce na direção y+
         * 
         */

        double cw = 0;
        double ch = 0;

        Rectangle2D.Double t = getObjectBounds(b);
        if (t != null) {
            cw = t.width;
            ch = t.height;
            //set location
//            t.x = x - cw / 2;
//            t.y = y;
//            y += ch + j;

            t.x = x - cw / 2;
            t.y = y - ch / 2;

            x += Ix * (cw + j);
            y += Iy * (ch + j);
        }

        Command it = b.start;
        while (it != null) {

            t = getObjectBounds(it);
            if (t != null) {
                cw = t.width;
                ch = t.height;
                //set location
//                t.x = x - cw / 2;
//                t.y = y;
//                y += ch + j;

                t.x = x - cw / 2;
                t.y = y - ch / 2;

                x += Ix * (cw + j);
                y += Iy * (ch + j);
            }

            if (it instanceof Block) {
                //ident já cuida do espaçamento do incio do bloco
                x -= Ix * (cw + j);
                y -= Iy * (ch + j);
                ident((Block) it, x, y, j, k, Ix, Iy, a);
                Rectangle2D.Double bb = getBounds((Block) it, null, j, k, Ix, Iy, a);
                x += Ix * (bb.width);
                y += Iy * (bb.height);
            } else if (it instanceof If) {
                If i = (If) it;
                double pbtx;
                double pbty;
                double pbfx;
                double pbfy;

                Rectangle2D.Double bfb = getBounds(i.getBlockFalse(), null, j, k, Ix, Iy, a);
                Rectangle2D.Double btb = getBounds(i.getBlockTrue(), null, j, k, Ix, Iy, a);

                if (a) {
                    //true
                    pbtx = 0;
                    pbty = 0;
                    //false
                    pbfx = Iy * (bfb.width / 2 + btb.width / 2 + k);
                    pbfy = Ix * (bfb.height / 2 + btb.height / 2 + k);
                } else {
                    //true
                    pbtx = -Iy * (btb.width / 2 + k);
                    pbty = -Ix * (btb.height / 2 + k);
                    //false
                    pbfx = Iy * (bfb.width / 2 + k);
                    pbfy = Ix * (bfb.height / 2 + k);
                }

                ident(i.getBlockTrue(), x + pbtx, y + pbty, j, k, Ix, Iy, a);
                ident(i.getBlockFalse(), x + pbfx, y + pbfy, j, k, Ix, Iy, a);

                x += Ix * ((bfb.width > btb.width) ? bfb.width : btb.width);
                y += Iy * ((bfb.height > btb.height) ? bfb.height : btb.height);
            }

            it = it.getNext();
        }

    }

    @Deprecated //função de teste
    private static Rectangle2D.Double getObjectBounds(Command c) {
//        if (c instanceof GraphicResource) {
//            Drawable d = ((GraphicResource) c).getDrawableResource();
//            if (d != null){
//                if (teste.containsKey(c)){
//                    teste.remove(c);
//                }
//                return d.getObjectBouds();
//            }
//        }
        if (!teste.containsKey(c)) {
            double w = 30;
            double h = 30;
//            double w = randnum.nextDouble() * 80 + 20;
//            double h = randnum.nextDouble() * 80 + 20;
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
    public static Rectangle2D.Double getBounds(Command c, Rectangle2D.Double ret, double j, double k, double Ix, double Iy, boolean a) {

        /*
         * j - espaçamento entre comandos (pixels)
         * k - espaçamento entre o if e seus fluxos  (pixels)
         * l - orientação (rad)
         * 
         * Ix identação em x
         * Iy identação em y
         * 
         * a alinhamento bloco do if
         * 
         */

        if (ret == null) {
            ret = new Rectangle2D.Double();
        } else {
            ret.setRect(0, 0, 0, 0);
        }

        //pega o tamnho do comando atual
        Rectangle2D.Double t = getObjectBounds(c);
        if (t != null) {
            ret.width += t.width;
            ret.height += t.height;

            ret.width += Ix * j;
            ret.height += Iy * j;
        }

        if (c instanceof Block) {
            Block b = (Block) c;
            Rectangle2D.Double p = new Rectangle2D.Double();
            Command it = b.start;
            while (it != null) {
                p = getBounds(it, p, j, k, Ix, Iy, a);
                ret.width = (ret.width > p.width) ? ret.width : p.width;
                ret.height += p.height;
                it = it.getNext();
            }
        } else if (c instanceof If) {
            If i = (If) c;
            Rectangle2D.Double p = new Rectangle2D.Double();
            //false
            p = getBounds(i.getBlockFalse(), p, j, k, Ix, Iy, a);
            ret.width = (ret.width > p.width) ? ret.width : p.width;
            double ty = p.height;
            //true
            p = getBounds(i.getBlockTrue(), p, j, k, Ix, Iy, a);
            ret.width += p.width;

            ret.width += Ix * k;
            ret.height += Iy * k;

            ty = (ty > p.height) ? ty : p.height;
            ret.height += ty;
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

    public static void wire(Block b, ArrayList<Line2D.Double> lines, double j, double k, double Ix, double Iy, boolean a) {
        Command it = b.start;
        Line2D.Double line = null;
        while (it != null) {
            if (it instanceof Block.BlockEnd) {
                for (Class c : wiring.keySet()) {
                    if (c.isInstance(b)) {
                        Point2D.Double[] reference = wiring.get(c);
                        Point2D.Double pts[] = new Point2D.Double[]{new Point2D.Double(), new Point2D.Double()};
                        Rectangle2D.Double beginBounds = getObjectBounds(b);
                        double w = getBounds(b, null, j, k, Ix, Iy, a).x;
                        Rectangle2D.Double endBounds = getObjectBounds(it);
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
                wire((Block) it, lines, j, k, Ix, Iy, a);
            }

            it = it.getNext();
        }
    }
    Rectangle2D.Double shape = new Rectangle2D.Double();

    @Override
    public Shape getObjectShape() {
        shape = getBounds(this, null, 0, 0, 0, 1, true);
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
            Function.ident(this, 0, 0, 10, 10, 0, 1, false);
//            myLines.clear();
//            Function.wire(this, myLines, 50, 50, 0, 1, true);
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
