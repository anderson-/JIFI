/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.swing;

import robotinterface.drawable.swing.MutableWidgetContainer;
import java.awt.Color;
import java.awt.Graphics2D;
import robotinterface.algorithm.Command;
import robotinterface.drawable.FlowchartBlock;

/**
 *
 * @author antunes
 */
public class DrawableCommandBlock extends MutableWidgetContainer {

    private Command command = null;

    public DrawableCommandBlock(Command command, Color color) {
        super(color);
        this.command = command;
    }

    @Override
    protected final void backDraw(Graphics2D g) {
        if (command instanceof FlowchartBlock) {
            ((FlowchartBlock) command).drawLines(g);
        }
    }
}
