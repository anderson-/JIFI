package robotinterface.plugin.cmdpack.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import robotinterface.algorithm.Command;
import static robotinterface.algorithm.procedure.DummyBlock.createSimpleBlock;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.GraphicObject;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.Interpreter;
import robotinterface.interpreter.ResourceManager;
import robotinterface.util.trafficsimulator.Clock;

/**
 *
 * @author antunes2
 */
public class StepMode extends Procedure {

   private static Color myColor = Color.decode("#631864");

    public StepMode() {

    }

    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createSimpleBlock(" (toggle step mode) ", Color.black, myColor);
        }
        return resource;
    }

    @Override
    public void begin(ResourceManager rm) throws ExecutionException {
        Interpreter interpreter = rm.getResource(Interpreter.class);
        interpreter.setTimestep((interpreter.getTimestep() == 0)? 200 : 0);
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
        return new Item("Modo passo-a-passo", myShape, myColor);
    }
    
    @Override
    public void drawLines(Graphics2D g) {
        
    }

    @Override
    public Object createInstance() {
        return new StepMode();
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        
    }
}
