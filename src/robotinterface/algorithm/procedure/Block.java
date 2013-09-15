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

import robotinterface.algorithm.Command;
import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.Variable;
import robotinterface.interpreter.ExecutionException;

/**
 * Bloco de comandos com suporte a escopo de variável.
 */
public class Block extends Procedure {

    /**
     * Classe usada para definir o final de um loop.
     */
    public class BlockEnd extends Command {

        private Command begin;

        private BlockEnd() {
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
    }
    protected Command start;
    protected boolean returnNext = false;
    private BlockEnd end;

    public Block() {
        end = new BlockEnd();
        end.setBlockBegin(this);
        end.setParent(this);
        start = end;
    }

    @Deprecated
    public final Command getStart() {
        return start;
    }

    public final Command getEnd() {
        return end;
    }

    /**
     * Adiciona um comando ao bloco de comandos.
     *
     * @param c Comando a ser adicionado
     * @return true se o comando foi adicionado ao bloco
     */
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
                for (String varName : ((Declaration) it).getVariableNames()) {
                    Variable remove = st.getVar(varName);
                    if (remove != null) {
                        System.out.println("Removed var: " + remove.getName());
                        remove.setValidValue(false);
                    }
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

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append("").append("{\n");
        Command it = start;
        while (it != null){
            it.toString(ident + identChar, sb);
            it = it.getNext();
        }
        sb.append(ident).append("}\n");
    }
}
