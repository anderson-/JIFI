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
package robotinterface.drawable.graphicresource;

import robotinterface.drawable.GraphicObject;

/**
 * Interface para possibilitar objetos serem desenhados por outra classe.
 *
 * Interface para possibilitar classes (ex: {@link Command}/{@link Procedure})
 * retornar uma implementação (possivelmente anônima) de {@link GraphicObject} ou
 * {@link DWidgetContainer}.
 *
 * @see ReadDevice
 */
public interface GraphicResource {

    public GraphicObject getDrawableResource();
}
