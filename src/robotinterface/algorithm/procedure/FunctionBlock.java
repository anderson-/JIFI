/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import robotinterface.algorithm.Command;
import static robotinterface.algorithm.Command.identChar;
import robotinterface.interpreter.ExecutionException;

/**
 *
 * @author antunes
 */
public class FunctionBlock extends Procedure {
    //talvez tenha que ser criado um clone completo da função (para recursividade)
    //o parser suporta isso? Acho que não...

    private Function function;

    public FunctionBlock() {
    }

    public FunctionBlock(Function function) {
        setFunction(function);
    }

    public final void setFunction(Function function) {
        this.function = function;
        function.getEnd().setBlockBegin(this);
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public Command step() throws ExecutionException {
        if (function.isDone()) {
            function.setDone(false);
            function.reset();
            return super.step();
        } else {
            return function.step();
        }
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append(function).append("(....);");
    }
}
