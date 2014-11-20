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
package s3f.jifi.flowchart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import s3f.jifi.flowchart.blocks.Block;
import static s3f.jifi.flowchart.blocks.ScriptBlock.getBounds;
import s3f.magenta.DrawingPanel;
import s3f.magenta.FlowchartBlock;
import s3f.magenta.GraphicObject;
import s3f.magenta.graphicresource.GraphicResource;

/**
 * Comando genérico.
 */
public abstract class Command implements GraphicResource, GraphicFlowchart, FlowchartBlock {

    public static final String identChar = "\t";
    private Command prev;
    private Command next;
    private Command parent;
    private final String name;
    private final int id;
    private static int classCounter = 0;

    public Command() {
        id = classCounter++;
        name = this.getClass().getSimpleName() + "[" + id + "]";
    }

    public Command(Command c) {
        this();
    }

    public final int getID() {
        return id;
    }

    public final String getCommandName() {
        return name;
    }

    public Command getNext() {
        return next;
    }

    public void setNext(Command next) {
        this.next = next;
    }

    public final Command getPrevious() {
        return prev;
    }

    public void setPrevious(Command previous) {
        this.prev = previous;
    }

    public Command getParent() {
        return parent;
    }

    public void setParent(Command parent) {
        this.parent = parent;
    }

    public final int getLevel() {
        int level = 0;
        Command it = parent;
        while (it != null) {
            level++;
            it = it.parent;
        }

        return level;
    }

    public boolean addBefore(Command c) {
        if (prev != null) {
            prev.next = c;
        } else {
            if (parent != null && parent instanceof Block) {
                ((Block) parent).addBegin(c);
            }
        }
        if (prev != c) {
            c.prev = prev;
        }
        c.next = this;
        c.parent = parent;
        prev = c;
        return true;
    }

    public boolean addAfter(Command c) {
        c.prev = this;
        c.next = next;
        c.parent = parent;
        if (next != null) {
            next.prev = c;
        }
        next = c;
        return true;
    }

    public void remove() {
        if (parent instanceof Block) {
            Block block = (Block) parent;
            if (block.getStart() == this) {
                block.shiftStart();
            }
        }
        parent = null;

        if (prev != null) {
            prev.next = next;
        }
        if (next != null) {
            next.prev = prev;
        }
        prev = null;
        next = null;
    }

    public void print() {
        System.out.println("command{" + name + "}:\n"
                + "\t^  : " + ((parent != null) ? parent.name : "null") + "\n"
                + "\t<- : " + ((prev != null) ? prev.name : "null") + "\n"
                + "\t-> : " + ((next != null) ? next.name : "null") + "\n");
    }

    public void toString(String ident, StringBuilder sb) {
    }

    @Override
    public String toString() {
        return name;
    }
    private GraphicObject d = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (d == null) {
            d = new GraphicObject.SimpleDrawableObject(new Rectangle2D.Double(0, 0, 30, 30)) {
                @Override
                public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
                    g.setColor(Color.lightGray);

                    if (in.isMouseOver()) {
                        g.setColor(Color.CYAN);
                        if (in.isKeyPressed(KeyEvent.VK_D)) {
                            g.setColor(Color.RED);
                            if (in.mouseGeneralClick()) {
                                remove();
                            }
                        }
                    }

                    Rectangle r = shape.getBounds();

                    g.fillRect(0, 0, r.width, r.height);

                    g.setColor(Color.BLACK);
                    g.drawString(getCommandName(), (int) r.getCenterX(), (int) r.getCenterY());
                }
            };
        }
        return d;
    }

    @Override
    public void ident(double x, double y, double j, double k) {

        double cw = 0;
        double ch = 0;

        Rectangle2D.Double t = null;
        if (this instanceof GraphicResource) {
            GraphicObject d = ((GraphicResource) this).getDrawableResource();

            if (d != null) {
                t = (Rectangle2D.Double) d.getObjectBouds();
            }
        }

        if (t != null) {
            cw = t.width;
            ch = t.height;

            double px = x - (cw / 2);
            double py = y;

            if (this instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) this).getDrawableResource();

                if (d != null) {
                    d.setLocation(px, py);
                }
            }

            y += ch + j;
        }

        if (next != null) {
            next.ident(x, y, j, k);
        }
    }

    @Override
    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, double j, double k) {
        return getBounds(this, tmp, j, k);
    }

    protected static Rectangle2D.Double getBounds(Command c, Rectangle2D.Double tmp, double j, double k) {
        Rectangle2D.Double t = null;
        if (c instanceof GraphicResource) {
            GraphicObject d = ((GraphicResource) c).getDrawableResource();

            if (d != null) {
                t = (Rectangle2D.Double) d.getObjectBouds();
            }
        }

        if (tmp == null) {
            tmp = new Rectangle2D.Double();
        }

        tmp.setRect(Double.MAX_VALUE, Double.MAX_VALUE, 0, 0);

        if (t != null) {
            tmp.setRect(t);
//            tmp.x = 0;
//            tmp.y = 0;
//            tmp.x = (t.x < tmp.x) ? t.x : tmp.x;
//            tmp.y = (t.y < tmp.y) ? t.y : tmp.y;
//
//            tmp.width += t.width;
//            tmp.height += t.height;

            tmp.height += j;
        }
        return tmp;
    }

    @Override
    public void drawLines(Graphics2D g) {
        GraphicObject resource = getDrawableResource();
        if (resource != null) {
            Command c = getNext();
            if (c instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) c).getDrawableResource();
                if (d != null) {
                    Rectangle2D.Double bThis = resource.getObjectBouds();
                    Rectangle2D.Double bNext = d.getObjectBouds();
                    Line2D.Double l = new Line2D.Double(bThis.getCenterX(), bThis.getMaxY(), bNext.getCenterX(), bNext.getMinY());
                    g.draw(l);
                    drawArrow(g, bNext.getCenterX() + .2, bNext.getMinY(), ARROW_DOWN);
                }
            }
        }
    }

    public static final double ARROW_RIGHT = 0;
    public static final double ARROW_DOWN = Math.PI / 2;

    public static final int ARR_SIZE = 10;

    //90/0.8/.9
    public static Shape createArrow(float arrowLength, float arrowRatio, float waisting, float strokeWidth) {

        float veeX = -strokeWidth * 0.5f / arrowRatio;

        // vee
        Path2D.Float path = new Path2D.Float();

        float waistX = -arrowLength * 0.5f;
        float waistY = arrowRatio * arrowLength * 0.5f * waisting;
        float arrowWidth = arrowRatio * arrowLength;

        path.moveTo(veeX - arrowLength, -arrowWidth);
        path.quadTo(waistX, -waistY, 0, 0.0f);
        path.quadTo(waistX, waistY, veeX - arrowLength, arrowWidth);

        // end of arrow is pinched in
        path.lineTo(veeX - arrowLength * 0.75f, 0.0f);
        path.lineTo(veeX - arrowLength, -arrowWidth);

        return path;
    }

    public static void drawArrow(Graphics2D g, double x, double y, double angle) {
        if (angle == ARROW_DOWN) {
            x += .2;
            y -= 2;
        } else {
            x -= 3;
        }
        g.translate(x, y);
        g.rotate(angle);
        {//arrumar :/
            Color o = g.getColor();
            g.setColor(Color.WHITE);
            g.fillRect(-5, -5, 10, 10);
            g.setColor(o);
        }
        g.fill(createArrow(6, 1.3f, .5f, 10));
        g.rotate(-angle);
        g.translate(-x, -y);
    }

    public static void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
//        g.setStroke(new BasicStroke(2));

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        AffineTransform o = (AffineTransform) g.getTransform().clone();
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
//        g.backDraw(0, 0, len - ARR_SIZE, 0);
        g.fillPolygon(new int[]{len, len - ARR_SIZE, len - ARR_SIZE, len},
                new int[]{0, (int) (-ARR_SIZE * .5), (int) (ARR_SIZE * .5), 0}, 4);
        at.setToIdentity();
        g.setTransform(o);
    }

    public static void drawArrow(Graphics2D g, Line2D l, boolean drawLine) {
        double dx = l.getX2() - l.getX1();
        double dy = l.getY2() - l.getY1();
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        AffineTransform o = (AffineTransform) g.getTransform().clone();
        AffineTransform at = AffineTransform.getTranslateInstance(l.getX1(), l.getY1());
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);
        // Draw horizontal arrow starting in (0, 0)
        if (drawLine) {
            g.drawLine(0, 0, len - ARR_SIZE, 0);
        }
        g.fillPolygon(new int[]{len, len - ARR_SIZE, len - ARR_SIZE, len},
                new int[]{0, (int) (-ARR_SIZE * .5), (int) (ARR_SIZE * .5), 0}, 4);
        at.setToIdentity();
        g.setTransform(o);
    }
}
