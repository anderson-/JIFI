/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm.procedure;

import algorithm.Command;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.Variable;
import robot.Robot;
import simulation.ExecutionException;

/**
 *
 * @author antunes
 */
public class Block extends Procedure {

    // classe usada para definir o final de um loop
    private class BlockEnd extends Command {

        private Command begin;

        private BlockEnd() {
        }

        public void setBegin(Command b) {
            begin = b;
        }

        @Override
        public Command step() {
            returnNext = true;
            resetVariableScope();
            return begin;
        }
    }
    protected Command start;
    protected boolean returnNext = false;
    private BlockEnd end;

    public Block() {
        end = new BlockEnd();
        end.setBegin(this);
        start = end;
    }

    @Deprecated
    public final Command getStart() {
        return start;
    }

    public final boolean add(Command c) {
        if (c == null) {
            return false;
        }
        c.setParent(this);
        //pega o elemento antes do ultimo
        Command it = end.getPrevious();
        //define a relação entre o novo elemento e o final ...<-c<->end
        c.setNext(end);
        end.setPrevious(c);
        end.setNext(null);
        //adiciona end ao final da lista
        if (it != null) {
            //...<-it<->c<->end->null
            it.setNext(c);
            c.setPrevious(it);
        } else {
            start = c;
        }
        return true;
    }

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
        }
        return false;
    }

    public final boolean addAfter(Command x, Command c) {
        if (contains(x) && c != null) {
            c.setPrevious(x);
            c.setNext(x.getNext());
            x.getNext().setPrevious(c);
            x.setNext(c);
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
            if (it instanceof Declaration) {
                Variable remove = st.getVar(((Declaration)it).getName());
                if (remove != null){
                    System.out.println("Removed var: " + remove.getName());
                    remove.setValidValue(false);
                }
            }
            it = it.getNext();
        }
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
}
