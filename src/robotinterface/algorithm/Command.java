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
package robotinterface.algorithm;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import robotinterface.algorithm.procedure.Block;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.robot.Robot;
import robotinterface.interpreter.ExecutionException;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Comando genérico.
 */
public abstract class Command implements GraphicResource {

    public static final String identChar = "    ";
    public static final String endChar = ";";
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

    public final int getID() {
        return id;
    }

    public final String getCommandName() {
        return name;
    }

    public final Command getNext() {
        return next;
    }

    public final void setNext(Command next) {
        this.next = next;
    }

    public final Command getPrevious() {
        return prev;
    }

    public final void setPrevious(Command previous) {
        this.prev = previous;
    }

    public Command getParent() {
        return parent;
    }

    public void setParent(Command parent) {
        this.parent = parent;
    }

    public final boolean addBefore(Command c) {
        if (prev != null) {
            prev.next = c;
        } else {
            if (parent != null && parent instanceof Block) {
                ((Block) parent).addBegin(c);
            }
        }
        c.prev = prev;
        c.next = this;
        c.parent = parent;
        prev = c;
        return true;
    }

    public final boolean addAfter(Command c) {
        c.prev = this;
        c.next = next;
        c.parent = parent;
        if (next != null) {
            next.prev = c;
        }
        next = c;
        return true;
    }

    public final void remove() {
        next = prev;
        prev = next;
    }

    //inicio da execução do comando
    public void begin(Robot robot, Clock clock) throws ExecutionException {
    }

    //repete até retornar true ou lançar uma ExecutionException
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        return true;
    }

    //executada ao final do comando a fim de saber qual é o proximo comando a ser executado
    public Command step() throws ExecutionException {
        if (next == null) {
            Command i = getParent();
            Command j;
            while (i != null) {
                j = i.getNext();
                if (j != null) {
                    return j;
                }
                i = i.getParent();
            }
            return i;
        }
        return next;
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
    private Drawable d = null;

    @Override
    public Drawable getDrawableResource() {
        if (d == null) {
            d = new Drawable.SimpleDrawableObject(new Rectangle2D.Double(0, 0, 30, 30)) {
                @Override
                public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
                    g.setColor(Color.lightGray);

                    if (in.isMouseOver()) {
                        g.setColor(Color.CYAN);
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
}
