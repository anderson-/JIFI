/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.magenta.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import s3f.jifi.flowchart.Command;
import s3f.jifi.flowchart.blocks.Procedure;

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
