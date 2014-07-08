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
package s3f.jifi.core;

import s3f.magenta.graphicresource.GraphicResource;
import s3f.jifi.core.interpreter.ExecutionException;
import s3f.util.trafficsimulator.Clock;

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

//    public void begin(Robot robot, Clock clock) throws ExecutionException;

    //repete até retornar true ou lançar uma ExecutionException
//    public boolean perform(Robot robot, Clock clock) throws ExecutionException;

    //executada ao final do comando a fim de saber qual é o proximo comando a ser executado
    public ICommand step() throws ExecutionException;

    public void print();

    public void toString(String ident, StringBuilder sb);
}
