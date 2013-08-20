/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import robotinterface.algorithm.Command;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nfunk.jep.ParseException;
import robotinterface.robot.Robot;
import robotinterface.interpreter.ExecutionException;

/**
 *
 * @author antunes
 */
public class If extends Procedure {

    //blocos para a divisão de fluxo
    private Block blockTrue, blockFalse;

    public If() {
        blockTrue = new Block();
        blockFalse = new Block();
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

    @Override
    public Command step() throws ExecutionException {
        //calcula o valor da expressão
        if (evaluate()) {
            return blockTrue.step();
        } else {
            return blockFalse.step();
        }
    }
}
