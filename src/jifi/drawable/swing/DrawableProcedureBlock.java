/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.drawable.swing;

import jifi.drawable.swing.MutableWidgetContainer;
import java.awt.Color;
import java.awt.Graphics2D;
import jifi.algorithm.Command;
import jifi.algorithm.procedure.Procedure;
import jifi.drawable.FlowchartBlock;

/**
 *
 * @author antunes
 */
public class DrawableProcedureBlock extends MutableWidgetContainer {

    private Command command = null;

    public DrawableProcedureBlock(Command c, Color color) {
        super(color);
        this.command = c;
        if (c instanceof Procedure) {
            boxLabel = ((Procedure) c).getProcedure();
        }
        updateStructure();
    }

    @Override
    protected final void backDraw(Graphics2D g) {
        command.drawLines(g);
    }
}
