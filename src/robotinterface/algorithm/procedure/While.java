/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import robotinterface.algorithm.Command;
import robotinterface.interpreter.ExecutionException;

/**
 *
 * @author antunes
 */
public class While extends Block {

    public While() {
    }

    public While(String procedure) {
        setProcedure(procedure);
    }

    @Override
    public Command step() throws ExecutionException {
        if (evaluate()) {
            return start;
        }
        return super.step();
    }
}
