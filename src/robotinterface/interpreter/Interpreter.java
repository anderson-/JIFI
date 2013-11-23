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
package robotinterface.interpreter;

import java.util.ArrayList;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.procedure.Function;
import robotinterface.algorithm.procedure.If;
import robotinterface.algorithm.procedure.While;
import robotinterface.algorithm.procedure.Procedure;
import org.nfunk.jep.JEP;
import robotinterface.algorithm.parser.Parser;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.SimulationPanel;
import robotinterface.plugin.cmdpack.begginer.Move;
import robotinterface.plugin.cmdpack.begginer.ReadDevice;
import robotinterface.plugin.cmdpack.begginer.Wait;
import robotinterface.plugin.cmdpack.util.PrintString;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;
import robotinterface.robot.connection.Serial;
import robotinterface.robot.device.Device;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Classe responsável por interpretar os algoritmos e executar os comandos.
 */
public class Interpreter extends Thread {

    public static final int STOP = 0;
    public static final int PLAY = 1;
    public static final int WAITING = 2;
    private JEP parser;
    private Function mainFunction;
    private Command currentCmd = null;
    private int state;
    private Robot robot;
    private Clock clock = new Clock();
    private int timestep = 0;

    public Interpreter() {
        super("Interpreter");
        parser = new JEP();
        state = WAITING;
    }

    public Robot getRobot() {
        return robot;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    public void reset() {
        if (mainFunction != null) {
            currentCmd = mainFunction;
        } else {
            currentCmd = null;
        }

        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
//        parser.setTraverse(true); //exibe debug
        parser.setImplicitMul(true);//multiplicação implicita: 2x+4
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.setAllowAssignment(true);
        if (robot != null && robot.getMainConnection() != null) {
            robot.stopAll();
        }
    }

    public Function getMainFunction() {
        return mainFunction;
    }

    public void setMainFunction(Function f) {
        mainFunction = f;
        reset();
    }

    public void setInterpreterState(int state) {
        this.state = state;
        if (state == STOP) {
            reset();
        }

        if (state != PLAY && robot != null) {
            robot.setRightWheelSpeed(0);
            robot.setLeftWheelSpeed(0);
        }
    }

    public int getInterpreterState() {
        return state;
    }

    public Command getCurrentCommand() {
        return currentCmd;
    }

    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }

    public int getTimestep() {
        return timestep;
    }

    public boolean step() {

        if (currentCmd == null) {
            return false;
        }

        clock.setPaused(false);

        //System.out.println(currentCmd); //exibe o comando atual
        try {
            if (currentCmd instanceof Procedure) {
                ((Procedure) currentCmd).setParser(parser);
            }
            currentCmd.begin(robot, clock);
            while (!currentCmd.perform(robot, clock)) {
                clock.increase();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                }
                if (state != PLAY) {
                    return true;
                }
            }
            currentCmd = currentCmd.step();
        } catch (ExecutionException e) {
            System.out.println("Erro");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (state == PLAY) {
                    if (!step()) {
                        state = WAITING;
                    }

                    if (currentCmd != null && currentCmd.getDrawableResource() != null) {
                        if (timestep > 50 && Robot.UPDATE_ALL_DEVICES.getTimeout() <= 50) {
                            for (int i = 0; i < timestep; i += 50) {
                                robot.updatePerception();
                                Thread.sleep(50 - Robot.UPDATE_ALL_DEVICES.getTimeout());
                            }
                        } else {
                            Thread.sleep(timestep);
                        }
                    }
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException ex) {
        }
    }

    public static void main(String[] args) {

        Robot r = new Robot();
        r.add(new HBridge());
        r.add(new Compass());
        r.add(new Serial(57600));
        Interpreter i = new Interpreter();
        i.setRobot(r);

//        f.add(new Cmd("a"));
//        f.add(new Cmd("b"));
//        If fi = new If();
//        fi.addTrue(new Cmd("c"));
//        fi.addFalse(new Cmd("d"));
//        f.add(fi);
//        
//        Loop l1 = new Loop("l1");
//        
//        l1.add(new Cmd("e"));
//        Loop l2 = new Loop("l2");
//        l2.add(new Cmd("f"));
//        l2.add(new Cmd("g"));
//        l2.add(new Cmd("h"));
//        l1.add(l2);
//        l1.add(new Cmd("i"));
//        
//        f.add(l1);
//        f.add(new Cmd("j"));
//        i.setMainFunction(f);
//        
//        
//        l1.addAfter(new Cmd("j0"));
//        l1.addBefore(new Cmd("e0"));
//        f.add(new Start());
//
//        Loop l = new Loop();
//        l.add(new Move(80, 80));
//        l.add(new Move(0, 0));
//        l.add(new Move(50, -50));
//        l.add(new Move(0, 0));
//        l.add(new Move(-80, -80));
//        l.add(new Move(0, 0));
//        f.add(l);
//        f.add(new Stop());
//        i.setMainFunction(f);
//        Function func = new Function("main", null);
//        func.add(new PrintString("inicio"));
//        func.add(new Declaration("i", 3));
//        While l = new While("i > 0");
//        l.add(new Declaration("j", 512));
//        If ii = new If("i == 2");
//        ii.addTrue(new PrintString("true"));
//        ii.addFalse(new PrintString("false"));
//        l.add(ii);
//        l.add(new PrintString("valor de i = %v","i"));
//        l.add(new Wait(501));
//        l.add(new Procedure("i=i-1"));
//        l.add(new PrintString("%v","j"));
//        func.add(l);
//        func.add(new PrintString("v = %v","j"));
//        func.add(new PrintString("fim"));
//        i.setMainFunction(func);
        ArrayList<Class<? extends Device>> aw = new ArrayList<>();
        aw.add(HBridge.class);
        aw.add(Compass.class);

        Function func = new Function("main", null);
//        func.add(new Wait(1000));
//        func.add(new PrintString("inicio"));
//        func.add(new Start());
//        func.add(new Declaration("i", 10));
//        func.add(new PrintString("Girando %v vezes...", "i"));
//        While loop = new While("i > 0");
//        loop.add(new Move(70, 70)); //move
//        loop.add(new Wait(500));
//        loop.add(new Move(-70, 70)); //gira
//        loop.add(new Wait(500));
//        loop.add(new Move(0, 0)); //para
//        loop.add(new Wait(500));
//        loop.add(new PrintString("Falta mais %v passo(s)...", "i"));
//        loop.add(new Procedure("i = i - 1"));
//        func.add(loop);
//        func.add(new PrintString("Procurando angulo 100"));
//        func.add(new Wait(500));
//        func.add(new Declaration("alpha", 10));
//        While loopCompass = new While("alpha != 100");// vai até 100
//        If ifCompass = new If("alpha > 100");
//        ifCompass.addTrue(new Move(55, -55));
//        ifCompass.addTrue(new PrintString("Girando para a esquerda"));
//        ifCompass.addFalse(new Move(-55, 55));
//        ifCompass.addFalse(new PrintString("Girando para a direita"));
//        loopCompass.add(ifCompass);
//        loopCompass.add(new ReadDevice(Compass.class, "alpha"));
//        loopCompass.add(new PrintString("Angulo atual: %v", "alpha"));
//        func.add(loopCompass);
//        func.add(new Move(0, 0));
//        func.add(new ReadDevice(aw));
//        func.add(new BlockRoboF(aw));
//        //func.add(new ReadDevice(Compass.class, "alpha"));
//        func.add(new PrintString("Angulo final: %v", "alpha"));
//        func.add(new PrintString("fim"));
//        func.add(new Declaration("i", new Vector<Object>(Arrays.asList(new Double[]{1.2,3.4}))));

//        func.add(new PrintString("inicio"));
//        func.add(new FunctionBlock(bubbleSort(10, false)));
//        func.add(new PrintString("fim"));
        func = newTestFunction();
        i.setMainFunction(func);

//        DrawingPanel p = new DrawingPanel();
//        p.add((Drawable) func);
//        boolean singleIdent = true;
//        func.ident(0, 0, 40, 100, 0, 1, singleIdent);
//        func.wire(40, 100, 0, 1, singleIdent);
//        QuickFrame.create(p, "Teste do painel de desenho").addComponentListener(p);
//        func.appendDCommandsOn(p);
//        System.out.println(Function.getBounds(ifCompass, null, 10, 100, 1, 1, true));
//        System.out.println(Function.getBounds(new Wait(1), null, 10,10,0));
        SimulationPanel p = new SimulationPanel();
        p.addRobot(r);
        r.setEnvironment(p.getEnv());
        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);

        //executa
        while (i.step()) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ex) {
//            }
        }

//        i.reset();
//        
//        while (i.step()) {
////            try {
////                Thread.sleep(100);
////            } catch (InterruptedException ex) {
////            }
//        }
        System.out.println(Parser.encode(func));

//        System.exit(0);
    }

    public static Function bubbleSort(int size, boolean rand) {
        rand = false;
        Function func = new Function("bbSort", null);

        StringBuilder sb = new StringBuilder();
        sb.append("V = [");
        if (rand) {
            for (int i = size - 1; i > 1; i--) {
                sb.append((int) (Math.random() * 100)).append(",");
            }
            sb.append((int) (Math.random() * 100)).append("]");
        } else {
            for (int i = size - 1; i > 1; i--) {
                sb.append(i).append(",");
            }
            sb.append("1]");
        }

        func.add(new Procedure("var V"));
        func.add(new Procedure(sb.toString()));
        func.add(new Procedure("var size = " + size));
        func.add(new Procedure("var i =1,j = 0;var k = 0, aux ,v = 1"));
//        func.add(new Declaration("i", 1));
//        func.add(new Declaration("j", 0));
//        func.add(new Declaration("k", 0));
//        func.add(new Declaration("aux", 0));
//        func.add(new Declaration("v", 1));

//        func.add(new DummyBlock());
        func.add(new PrintString("Antes:"));
        While loopP0 = new While("i < size");
        loopP0.add(new Procedure("v = V[i]"));
        loopP0.add(new PrintString("V[%v]: %v", "i", "v"));
        loopP0.add(new Procedure("i = i + 1"));
        func.add(loopP0);

        func.add(new Procedure("k = size-1"));

        func.add(new Procedure("i = 1"));
        While loopI = new While("i < size");
        loopI.add(new Procedure("j = 1"));

        While loopJ = new While("j <= k");

        If cond = new If("V[j] > V[j+1]");

//        cond.addTrue(new Procedure("aux = V[j]"));
//        cond.addTrue(new Procedure("V[j] = V[j+1]"));
//        cond.addTrue(new Procedure("V[j+1] = aux"));
        cond.addTrue(new Procedure("aux = V[j];V[j] = V[j+1];V[j+1] = aux;"));

        loopJ.add(cond);
        loopJ.add(new Procedure("j = j + 1"));

        loopI.add(loopJ);
        loopI.add(new Procedure("k = k - 1"));
        loopI.add(new Procedure("i = i + 1"));
        func.add(loopI);

        func.add(new PrintString("\nDepois:"));
        func.add(new Procedure("i = 1"));
        While loopP1 = new While("i < size");
        loopP1.add(new Procedure("v = V[i]"));
        loopP1.add(new PrintString("V[%v]: %v", "i", "v"));
        loopP1.add(new Procedure("i = i + 1"));
        func.add(loopP1);

        return func;
    }

    public static Function newTestFunction() {
        ArrayList<Class<? extends Device>> aw = new ArrayList<>();
        aw.add(HBridge.class);
        aw.add(Compass.class);

        Function func = new Function("main", null);
        func.add(new Wait(1000));
        func.add(new PrintString("inicio"));
        func.add(new Procedure("var i = 10"));
        func.add(new PrintString("Girando %v vezes...", "i"));
        While loop = new While("i > 10");
        loop.add(new Move(70, 70)); //move
        loop.add(new Wait(500));
        loop.add(new Move(-70, 70)); //gira
        loop.add(new Wait(500));
        loop.add(new Move(0, 0)); //para
        loop.add(new Wait(500));
        loop.add(new PrintString("Falta mais %v passo(s)...", "i"));
        loop.add(new Procedure("i = i - 1"));
        func.add(loop);
        func.add(new PrintString("Procurando angulo 100"));
        func.add(new Wait(500));
        func.add(new Procedure("var alpha = 10"));
        While loopCompass = new While("alpha != 100");// vai até 100
        If ifCompass = new If("alpha > 100");
        ifCompass.addTrue(new Move(55, -55));
        ifCompass.addTrue(new PrintString("Girando para a esquerda"));
        ifCompass.addFalse(new Move(-55, 55));
        ifCompass.addFalse(new PrintString("Girando para a direita"));

//
//        If ifCompass2 = new If("alpha > 100");
//        ifCompass2.addTrue(new Move(55, -55));
//        ifCompass2.addTrue(new PrintString("Girando para a esquerda"));
//        ifCompass2.addFalse(new Move(-55, 55));
//        ifCompass2.addFalse(new PrintString("Girando para a direita"));
//        ifCompass.addFalse(ifCompass2);
//
//        If ifCompass3 = new If("alpha > 100");
//        ifCompass3.addTrue(new Move(55, -55));
//        ifCompass3.addTrue(new PrintString("Girando para a esquerda"));
//        ifCompass3.addFalse(new Move(-55, 55));
//        ifCompass3.addFalse(new PrintString("Girando para a direita"));
//        ifCompass2.addTrue(ifCompass3);
        loopCompass.add(ifCompass);
        loopCompass.add(new ReadDevice(Compass.class, "alpha"));
        loopCompass.add(new PrintString("Angulo atual: %v", "alpha"));
        func.add(loopCompass);
        func.add(new Move(0, 0));
//        func.add(new ReadDevice(aw));
//        func.add(new BlockRoboF(aw));
        func.add(new Wait(500));
        func.add(new ReadDevice(Compass.class, "alpha"));
        func.add(new PrintString("Angulo final: %v", "alpha"));
        func.add(new PrintString("fim"));
        return func;
    }
}
