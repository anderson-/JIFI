/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugin.cmdpack.begginer;

import java.awt.Color;
import java.awt.geom.RoundRectangle2D;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.robot.connection.Connection;
import robotinterface.robot.Robot;
import robotinterface.robot.device.HBridge;
import robotinterface.interpreter.ExecutionException;
import robotinterface.util.trafficsimulator.Clock;
import robotinterface.util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class Wait extends Command implements Classifiable, FunctionToken<Wait>{
    
    private Timer timer;
    
    public Wait (){
        timer = new Timer(100);
    }
    
    public Wait (long ms){
        timer = new Timer(ms);
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        timer.reset();
        clock.addTimer(timer);
    }
    
    @Override
    public boolean perform(Robot robot, Clock clock) {
        return timer.isConsumed();
    }

    @Override
    public Item getItem() {
        return new Item("Wait", new RoundRectangle2D.Double(0, 0, 20, 20, 5, 5), Color.decode("#80DE71"));
    }

    @Override
    public Object createInstance() {
        return new Wait();
    }

    @Override
    public String getToken() {
        return "wait";
    }

    @Override
    public Wait createInstance(String args) {
        return new Wait();
    }
    
}
