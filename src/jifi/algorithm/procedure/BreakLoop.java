/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.algorithm.procedure;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import jifi.algorithm.Command;
import static jifi.algorithm.procedure.DummyBlock.createSimpleBlock;
import jifi.drawable.GraphicObject;
import jifi.gui.panels.sidepanel.Item;
import jifi.interpreter.ExecutionException;
import jifi.interpreter.ResourceManager;

/**
 *
 * @author antunes2
 */
public class BreakLoop extends Procedure {

    private static Color myColor = Color.decode("#01939A");

    public BreakLoop() {

    }

    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createSimpleBlock(this, " break; ", Color.black, myColor, 0);
        }
        return resource;
    }

    @Override
    public Command step(ResourceManager rm) throws ExecutionException {
        Command loop = getParent();
        while (!(loop instanceof While || loop instanceof Function)) {
            loop = loop.getParent();
        }

//        if (loop instanceof While){
//            ((Block)loop).breakLoop(true);
//            return ((Block)loop).getNext();
//        } else 
        if (loop instanceof Block) {
            ((Block) loop).breakLoop(true);
            return ((Block) loop).getEnd();
        }
        return loop.getNext();
    }

    @Override
    public Item getItem() {

        Area myShape = new Area();
        Polygon tmpPoly = new Polygon();
        tmpPoly.addPoint(10, 0);
        tmpPoly.addPoint(20, 10);
        tmpPoly.addPoint(10, 20);
        tmpPoly.addPoint(0, 10);
        myShape.add(new Area(tmpPoly));
        myShape.subtract(new Area(new Ellipse2D.Double(5, 5, 10, 10)));

        tmpPoly.reset();
        tmpPoly.addPoint(18, 0);
        tmpPoly.addPoint(20, 2);
        tmpPoly.addPoint(2, 20);
        tmpPoly.addPoint(0, 18);
        myShape.add(new Area(tmpPoly));

        myShape.add(new Area(new Ellipse2D.Double(7, 7, 6, 6)));
        return new Item("Parar Repetição", myShape, myColor, "Interrompe o laço de repetição quando é executado");
    }

    @Override
    public void drawLines(Graphics2D g) {

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
