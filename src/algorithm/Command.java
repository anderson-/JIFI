/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import algorithm.procedure.Block;
import robot.Robot;
import simulation.ExecutionException;
import util.Clock;

/**
 *
 * @author antunes
 */
public abstract class Command {

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

    public final Command getParent() {
        return parent;
    }

    public final void setParent(Command parent) {
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
        prev = c;
        return true;
    }

    public final boolean addAfter(Command c) {
        c.prev = this;
        c.next = next;
        if (next != null) {
            next.prev = c;
        }
        next = c;
        return true;
    }

    //inicio da execução do comando
    public void begin(Robot robot, Clock clock) throws ExecutionException{
        
    }
    
    //repete até retornar true ou lançar uma ExecutionException
    public boolean perform(Robot robot, Clock clock) throws ExecutionException{
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
    
    @Override
    public String toString() {
        return "command{" + name + "}:\n"
                + "\t^  : " + ((parent != null) ? parent.name : "null") + "\n"
                + "\t<- : " + ((prev != null) ? prev.name : "null") + "\n"
                + "\t-> : " + ((next != null) ? next.name : "null") + "\n";
    }
}
