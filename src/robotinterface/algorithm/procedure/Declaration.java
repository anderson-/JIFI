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
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version.
 *
 * RobotInterface is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RobotInterface. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package robotinterface.algorithm.procedure;

import java.util.ArrayList;
import org.nfunk.jep.SymbolTable;
import robotinterface.robot.Robot;
import robotinterface.interpreter.ExecutionException;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Declaração de variaveis para identificação.
 * @see robotinterface.algorithm.procedure.Procedure.Declaration
 */
public class Declaration extends Procedure implements Procedure.Declaration {

    private ArrayList<String> names;
    private ArrayList<Object> values;

    public Declaration (){
        names = new ArrayList<>();
        values = new ArrayList<>();
    }
    
    public Declaration(String name, Object value) {
        this();
        addVariable(name, value);
    }
    
    public final void addVariable(String name, Object value){
        names.add(name);
        values.add(value);
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        SymbolTable st = getParser().getSymbolTable();
        for (int i = 0; i < names.size(); i++){
            String name = names.get(i);
            Object value = values.get(i);
            if (st.getVar(name) != null && st.getVar(name).hasValidValue()){
                throw new ExecutionException("Variable already exists!");
            } else {
                st.makeVarIfNeeded(name, value);
            }
        }
    }

    @Override
    public ArrayList<String> getVariableNames() {
        return names;
    }

    @Override
    public ArrayList<Object> getVariableValues() {
        return values;
    }
    
    @Override
    public void toString(String ident, StringBuilder sb) {
        for (int i = 0; i < names.size(); i++){
            sb.append(ident).append("var ").append(names.get(i));
            if (values.get(i) != null){
                sb.append(" = ").append(values.get(i));
            }
            sb.append(endChar).append("\n");
        }
    }
}
