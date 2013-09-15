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
import java.util.ArrayList;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Variable;
import static robotinterface.algorithm.Command.endChar;
import static robotinterface.algorithm.Command.identChar;
import robotinterface.robot.Robot;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.Expression;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Comando genérico com suporte à variaveis.
 */
public class Procedure extends Command implements Expression {

    /**
     * Interface para a declaração de multiplas variaveis em algum comando.
     *
     * @see robotinterface.algorithm.procedure.Declaration
     */
    protected interface Declaration {
        
        public ArrayList<String> getVariableNames();
        
        public ArrayList<Object> getVariableValues();
    }
    private static JEP parser;
    private String procedure;
    
    public Procedure() {
        parser = null;
        procedure = "0";
    }
    
    public Procedure(String procedure) {
        this();
        this.procedure = procedure;
    }    
    
    protected final JEP getParser() {
        return parser;
    }
    
    @Override
    public final void setParser(JEP parser) {
        Procedure.parser = parser;
    }
    
    public final String getProcedure() {
        return procedure;
    }
    
    public final void setProcedure(String procedure) {
        this.procedure = procedure;
    }
    
    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        evaluate();
        return true;
    }

    //usado pelos descendentes dessa classe para executar expressoes simples
    protected final Object execute(String procedure) throws ExecutionException {
        if (parser == null) {
            throw new ExecutionException("Parser not found!");
        }
        parser.parseExpression(procedure);
        return parser.getValueAsObject();
    }
    
    protected final boolean evaluate(String procedure) throws ExecutionException {
        Object o = execute(procedure);
        if (o instanceof Number) {
            Double d = ((Number) o).doubleValue();
            return (d != 0 && !d.isNaN());
        }
        return false;
    }
    
    protected final boolean evaluate() throws ExecutionException {
        return evaluate(procedure);
    }
    
    protected final Variable newVariable(String name, Object value) {
        return parser.getSymbolTable().makeVarIfNeeded(name, value);
    }
    
    public ArrayList<String> getDeclaredVariables() {
        ArrayList<String> vars = new ArrayList<>();
        Command it = this;
        while (it != null) {
            Command up = it.getPrevious();
            while (up != null) {
                if (up instanceof Declaration) {
                    vars.addAll(((Declaration) up).getVariableNames());
                }
                up = up.getPrevious();
            }
            it = it.getParent();
        }
        return vars;
    }
    
    @Override
    public void toString(String ident, StringBuilder sb) {
        if (!procedure.equals("0")) {
            sb.append(ident).append(procedure);
        } else {
            sb.append(ident).append(toString());
        }
        sb.append(endChar).append("\n");
    }
}
