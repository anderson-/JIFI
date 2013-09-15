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
import static robotinterface.algorithm.Command.identChar;
import robotinterface.interpreter.ExecutionException;

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
}
