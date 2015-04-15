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
 * JIFI is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * JIFI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JIFI. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jifi.algorithm;

import jifi.drawable.graphicresource.GraphicResource;
import jifi.robot.Robot;
import jifi.interpreter.ExecutionException;
import jifi.util.trafficsimulator.Clock;

/**
 * Comando genérico.
 */
public interface ICommand extends GraphicResource {

    public int getID();

    public String getCommandName();

    public ICommand getNext();

    public void setNext(ICommand next);

    public ICommand getPrevious();

    public void setPrevious(ICommand previous);

    public ICommand getParent();

    public void setParent(ICommand parent);

    public boolean addBefore(ICommand c);

    public boolean addAfter(ICommand c);

    public void remove();

    public void begin(Robot robot, Clock clock) throws ExecutionException;

    //repete até retornar true ou lançar uma ExecutionException
    public boolean perform(Robot robot, Clock clock) throws ExecutionException;

    //executada ao final do comando a fim de saber qual é o proximo comando a ser executado
    public ICommand step() throws ExecutionException;

    public void print();

    public void toString(String ident, StringBuilder sb);
}
