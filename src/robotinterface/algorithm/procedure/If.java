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

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import robotinterface.algorithm.Command;
import static robotinterface.algorithm.Command.identChar;
import static robotinterface.algorithm.procedure.Function.getBounds;
import static robotinterface.algorithm.procedure.Function.ident;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Divisor de fluxo.
 */
public class If extends Procedure {

    public class BlockTrue extends Block {
    }

    public class BlockFalse extends Block {
    }
    //blocos para a divisão de fluxo
    private BlockTrue blockTrue;
    private BlockFalse blockFalse;

    public If() {
        blockTrue = new BlockTrue();
        blockFalse = new BlockFalse();
        blockTrue.setParent(this);
        blockFalse.setParent(this);
    }

    public If(String procedure) {
        this();
        setProcedure(procedure);
    }

    public boolean addTrue(Command c) {
        return blockTrue.add(c);
    }

    public boolean addFalse(Command c) {
        return blockFalse.add(c);
    }

    public Block getBlockTrue() {
        return blockTrue;
    }

    public Block getBlockFalse() {
        return blockFalse;
    }
    
    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        return true;
    }

    @Override
    public Command step() throws ExecutionException {
        //calcula o valor da expressão
        if (evaluate()) {
            return blockTrue.step();
        } else {
            return blockFalse.step();
        }
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append("if (").append(getProcedure()).append(")").append("{\n");
        blockTrue.toString(ident + identChar, sb);
        sb.append(ident).append("}");

        if (blockFalse.size() > 1) {
            sb.append(" else {\n");
            blockFalse.toString(ident + identChar, sb);
        } else {
            sb.append("\n");
        }
    }

    @Override
    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, double j, double k, double Ix, double Iy, boolean a) {
        tmp = super.getBounds(tmp, j, k, Ix, Iy, a);

        Rectangle2D.Double p = new Rectangle2D.Double();
        //false
        p = Function.getBounds(getBlockFalse(), p, j, k, Ix, Iy, a);
        tmp.add(p);
        //true
        p = Function.getBounds(getBlockTrue(), p, j, k, Ix, Iy, a);
        tmp.add(p);

        return tmp;
    }

    @Override
    public void ident(double x, double y, double j, double k, double Ix, double Iy, boolean a) {
        double xj = Ix * j;
        double yj = Iy * j;
        double xk = Iy * k;
        double yk = Ix * k;

        double pbtx;
        double pbty;
        double pbfx;
        double pbfy;
        
        Rectangle2D.Double t = null;
        if (this instanceof GraphicResource) {
            Drawable d = ((GraphicResource) this).getDrawableResource();

            if (d != null) {
                t = (Rectangle2D.Double) d.getObjectBouds();
            }
        }

        if (t != null) {
            double cw = t.width;
            double ch = t.height;

            double px = x - Iy * (cw / 2);
            double py = y - Ix * (ch / 2);

            if (this instanceof GraphicResource) {
                Drawable d = ((GraphicResource) this).getDrawableResource();

                if (d != null) {
                    d.setObjectLocation(px, py);
//                    System.out.println(this + " [" + px + "," + py + "]");
                }
            }

            x += Ix * (cw + xj);
            y += Iy * (ch + yj);
        }

        Rectangle2D.Double btb = blockTrue.getBounds(null, j, k, Ix, Iy, a);
        Rectangle2D.Double bfb = blockFalse.getBounds(null, j, k, Ix, Iy, a);
//        System.out.println(bfb);
//        System.out.println(btb);

        if (a) {
            //true
            pbtx = 0;
            pbty = 0;
            //false
            pbfx = Iy * (bfb.width / 2 + btb.width / 2 + xk);
            pbfy = Ix * (bfb.height / 2 + btb.height / 2 + yk);
        } else {
            //true
            pbtx = -Iy * (btb.width / 2 + xk);
            pbty = -Ix * (btb.height / 2 + yk);
            //false
            pbfx = Iy * (bfb.width / 2 + xk);
            pbfy = Ix * (bfb.height / 2 + yk);
        }

        blockTrue.ident(x + pbtx, y + pbty, j, k, Ix, Iy, a);
        blockFalse.ident(x + pbfx, y + pbfy, j, k, Ix, Iy, a);

        x += Ix * ((bfb.width > btb.width) ? bfb.width : btb.width);
        y += Iy * ((bfb.height > btb.height) ? bfb.height : btb.height);

        if (getNext() != null) {
//            System.out.println("*" + this + " => " + getNext());
            getNext().ident(x, y, j, k, Ix, Iy, a);
        }
    }
    
     @Override
    public Item getItem() {
        return super.getItem(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object createInstance() {
        return new If();
    }
    
    
    @Override
    public Procedure copy(Procedure copy) {
        Procedure p = super.copy(copy);
        
        if (copy instanceof If) {
            blockTrue.copy(((If) copy).blockTrue);
            blockFalse.copy(((If) copy).blockFalse);
        } else {
            System.out.println("Erro ao copiar: ");
            print();
        }
        
        return p;
    }

    @Override
    public Drawable getDrawableResource() {
        SimpleContainer sc = (SimpleContainer) super.getDrawableResource();
        sc.setColor(Color.yellow);
        return sc;
    }
    
    
}