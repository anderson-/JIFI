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

import java.awt.Color;
import robotinterface.algorithm.Command;
import static robotinterface.algorithm.Command.identChar;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;

/**
 * Laço de repetição simples.
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
    
    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append("while (").append(getProcedure()).append(")").append("{\n");
        Command it = start;
        while (it != null){
            it.toString(ident + identChar, sb);
            it = it.getNext();
        }
        sb.append(ident).append("}\n");
    }
    
     @Override
    public Item getItem() {
        return super.getItem(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object createInstance() {
        return new While();
    }

    @Override
    public Drawable getDrawableResource() {
        SimpleContainer sc = (SimpleContainer) super.getDrawableResource();
        sc.setColor(Color.blue);
        return sc;
    }
}
