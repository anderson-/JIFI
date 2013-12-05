/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import robotinterface.algorithm.Command;
import static robotinterface.algorithm.procedure.DummyBlock.createSimpleBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;

/**
 *
 * @author antunes2
 */
public class BreakLoop extends Procedure {

    private static Color myColor = Color.BLUE;

    public BreakLoop() {

    }

    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createSimpleBlock("Break;", Color.black, myColor);
        }
        return resource;
    }

    @Override
    public Command step() throws ExecutionException {
        Command loop = super.getParent();
        while (!(loop instanceof While || loop instanceof Function)) {
            loop = super.getParent();
        }
        
        if (loop instanceof Block){
            ((Block)loop).breakBlock(true);
            return loop.step();
        }
        System.out.println(loop);
        return loop.getNext();
    }

    @Override
    public Item getItem() {
        Area myShape = new Area();
        myShape.add(new Area(new Rectangle2D.Double(0, 0, 20, 12)));
        myShape.subtract(new Area(new Rectangle2D.Double(4, 4, 12, 4)));
        return new Item("Parar Repetição", myShape, myColor);
    }

    @Override
    public Object createInstance() {
        return new BreakLoop();
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append("break;\n");
    }
}
