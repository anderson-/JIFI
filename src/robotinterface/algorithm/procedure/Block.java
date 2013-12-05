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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import robotinterface.algorithm.Command;
import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.Variable;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Bloco de comandos com suporte a escopo de variável.
 */
public class Block extends Procedure {

    /**
     * Classe usada para definir o final de um loop.
     */
    public class BlockEnd extends Command {

        protected Command begin;

        protected BlockEnd() {
        }

        /**
         * Define o inicio do bloco de coamndos.
         *
         * @param b Bloco de comando.
         */
        public void setBlockBegin(Command b) {
            begin = b;
        }

        /**
         * Reseta o escopo de variável e retorna o bloco de comandos que
         * pertence.
         */
        @Override
        public Command step() {
            returnNext = true;
            resetVariableScope();
            return begin;
        }

        @Override
        public GraphicObject getDrawableResource() {
            return null;
        }
    }
    protected Command start;
    protected boolean returnNext = false;
    private BlockEnd end;

    public Block() {
        setEnd(new BlockEnd());
        start = end;
    }

    public void clear() {
        start = end;
        end.setNext(null);
        end.setPrevious(null);
    }

    public final Command getStart() {
        return start;
    }

    public Command shiftStart() {
        if (start != null && start != end) {
            start = start.getNext();
        }
        return start;
    }

    public final BlockEnd getEnd() {
        return end;
    }

    public final void setEnd(BlockEnd end) {
        if (this.end != null) {
            end.setNext(this.end.getNext());
            end.setPrevious(this.end.getPrevious());
        }
        end.setBlockBegin(this);
        end.setParent(this);
        this.end = end;
    }

    public final boolean add(Command c) {
        if (c == null) {
            return false;
        }
        c.setParent(this);
        //pega o elemento antes do ultimo
        Command begin = end.getPrevious();
        Command it = c;
        while (it.getNext() != null) {
            it.setParent(this);
            it = it.getNext();
        }
        it.setNext(end);
        it.setParent(this);
        end.setPrevious(c);
        end.setNext(null);
        //adiciona end ao final da lista
        if (begin != null) {
            //...<-it<->c<->end->null
            begin.setNext(c);
            c.setPrevious(begin);
        } else {
            start = c;
        }

        return true;
    }

//    public final boolean addAfter(Command x, Command c) {
//        if (contains(x) && c != null) {
//            c.setPrevious(x);
//            c.setNext(x.getNext());
//            x.getNext().setPrevious(c);
//            x.setNext(c);
//            c.setParent(this);
//        }
//        return false;
//    }
    public final boolean addAfter(Command x, Command c) {
        if (c == null || x == null) {
            return false;
        }
        c.setParent(this);
        //pega o elemento antes do ultimo
        Command begin = x;

        Command it = c;
        while (it != null && it.getNext() != x) {
            it = it.getNext();
        }

        //begin<->c<->...<->end<->begin.next
        if (it != null && begin.getNext() != null) {
            it.setNext(begin.getNext());
            begin.getNext().setPrevious(it);

            begin.setNext(c);
            c.setPrevious(begin);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Obtem o numero de comandos dentro do bloco.
     *
     * @return O numero de comandos dentro desse bloco
     */
    public final int size() {
        int size = 0;
        Command it = start;
        while (it != null) {
            size++;
            it = it.getNext();
        }
        return size;
    }

    //não protege o final!
    public final int indexOf(Command c) {
        int index = 0;
        Command it = start;
        while (it != null) {
            if (it == c) {
                return index;
            }
            it = it.getNext();
            index++;
        }
        return -1;
    }

    //não protege o final!
    public final Command get(int index) {
        int i = 0;
        Command it = start;
        while (it != null) {
            if (i == index) {
                return it;
            }
            it = it.getNext();
            i++;
        }
        return null;
    }

    //protege o final
    public final boolean remove(int index) {
        int i = 0;
        Command it = start;
        while (it != null) {
            if (i == index) {
                if (it == end) {
                    return false; //protege o final
                }
                Command prev = it.getPrevious();
                Command next = it.getNext();
                if (prev != null) {
                    prev.setNext(it.getNext());
                } else {
                    start = it;
                }

                if (next != null) {
                    next.setPrevious(it.getPrevious());
                }
                return true;
            }
            it = it.getNext();
            i++;
        }
        return false;
    }

    //protege o final
    public final boolean remove(Command c) {
        int i = indexOf(c);
        if (i >= 0) {
            remove(i);
            return true;
        }
        return false;
    }

    public final boolean contains(Command c) {
        return (indexOf(c) >= 0);
    }

    public final boolean addBegin(Command c) {
        return addBefore(start, c);
    }

    public final boolean addBefore(Command x, Command c) {
        if (contains(x)) {
            Command prev = x.getPrevious();
            if (prev != null) {
                addAfter(prev, c);
            } else {
                x.setPrevious(c);
                c.setNext(x);
                start = c;
            }
            c.setParent(this);
        }
        return false;
    }

    //função executada ao final do bloco
    protected void reset() {
    }

    //remove todas as variaveis definidas dentro do bloco
    private void resetVariableScope() {
        SymbolTable st = getParser().getSymbolTable();
        Command it = start;
        while (it != null) {
            if (it instanceof Procedure) {
                for (String varName : ((Procedure) it).getVariableNames()) {
                    Variable remove = st.getVar(varName);
                    if (remove != null) {
//                        System.out.println("Removed var: " + remove.getName());
                        remove.setValidValue(false);
                    }
                }
            }
            it = it.getNext();
        }
    }

    public boolean isDone() {
        return returnNext;
    }

    void setDone(boolean b) {
        returnNext = b;
    }

    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        return true;
    }

    @Override
    public Command step() throws ExecutionException {
        if (returnNext) {
            returnNext = false;
            reset();
            return super.step();
        } else {
            return start;
        }
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        //sb.append(ident).append("").append("{\n");
        Command it = start;
        while (it != null) {
            it.toString(ident + identChar, sb);
            it = it.getNext();
        }
        //sb.append(ident).append("}\n");
    }

    @Override
    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, double j, double k) {

        tmp = super.getBounds(tmp, j, k);

        Rectangle2D.Double p = new Rectangle2D.Double();
        Command it = this.start;
//        boolean ident = true;
        while (it != null) {
            p = it.getBounds(p, j, k);

            tmp.x = (p.x < tmp.x) ? p.x : tmp.x;
            tmp.y = (p.y < tmp.y) ? p.y : tmp.y;

            tmp.width = (p.width > tmp.width) ? p.width : tmp.width;
            
            tmp.height += p.height;

//            if (it instanceof If) {
//                ident = false;
//            }

            it = it.getNext();
        }

//            tmp.x -= j;
//            tmp.width += 2 * j;

        return tmp;
    }

    @Override
    public final void ident(double x, double y, double j, double k) {
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

            double px = x - cw / 2;
            double py = y;

            if (this instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) this).getDrawableResource();

                if (d != null) {
                    d.setLocation(px, py);
                }
            }

            y += ch + j;
        }

        start.ident(x, y, j, k);

        if (getNext() != null) {
            getNext().ident(x, y + this.getBounds(null, j, k).height - (ch + j), j, k);
        }

    }

    @Override
    public Item getItem() {
        return super.getItem();
    }

    @Override
    public Object createInstance() {
        return new Block();
    }

    @Override
    public Procedure copy(Procedure copy) {
        Procedure p = super.copy(copy);

        if (copy instanceof Block) {
            if (start instanceof Procedure) {
                ((Block) copy).add(Procedure.copyAll((Procedure) start));
            }
        } else {
            System.out.println("Erro ao copiar: ");
            start.print();
        }

        return p;
    }

    @Override
    public GraphicObject getDrawableResource() {
        return null;
    }
}
