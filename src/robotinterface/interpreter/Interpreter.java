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

import robotinterface.algorithm.Command;
import robotinterface.algorithm.procedure.Declaration;
import robotinterface.algorithm.procedure.Function;
import robotinterface.algorithm.procedure.If;
import robotinterface.algorithm.procedure.While;
import robotinterface.algorithm.procedure.Procedure;
import org.nfunk.jep.JEP;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.plugins.cmdpack.begginer.Move;
import robotinterface.plugins.cmdpack.begginer.ReadDevice;
import robotinterface.plugins.cmdpack.begginer.Wait;
import robotinterface.plugins.cmdpack.serial.Start;
import robotinterface.plugins.cmdpack.util.PrintString;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;
import robotinterface.robot.connection.Serial;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Classe responsável por interpretar os algoritmos e executar os comandos.
 */
public class Interpreter extends Thread {

    private JEP parser;
    private Function mainFunction;
    private Command currentCmd = null;
    private Robot robot;
    private Clock clock = new Clock();

    public Interpreter(Robot r) {
        super("Interpreter::" + r.toString());
        parser = new JEP();
        robot = r;
    }

    public void reset() {
        if (mainFunction != null) {
            currentCmd = mainFunction.get(0);
        }
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
//        parser.setTraverse(true); //exibe debug
        parser.setImplicitMul(true);//multiplicação implicita: 2x+4
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.setAllowAssignment(true);
//        parser.setAllowUndeclared(true);
    }

    public void setMainFunction(Function f) {
        mainFunction = f;
        reset();
    }

    public void setCommand(Command c) {
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
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
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
    }

    public static void main(String[] args) {

        Robot r = new Robot();
        r.add(new HBridge(1));
        r.add(new Compass());
        r.add(new Serial(9600));
        Interpreter i = new Interpreter(r);


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


        Function func = new Function("main", null);
        func.add(new Wait(1000));
        func.add(new PrintString("inicio"));
        func.add(new Start());
        func.add(new Declaration("i", 10));
        func.add(new PrintString("Girando %v vezes...", "i"));
        While loop = new While("i > 0");
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
        func.add(new Declaration("alpha", 10));
        While loopCompass = new While("alpha != 100");// vai até 100
        If ifCompass = new If("alpha > 100");
        ifCompass.addTrue(new Move(55, -55));
        ifCompass.addTrue(new PrintString("Girando para a esquerda"));
        ifCompass.addFalse(new Move(-55, 55));
        ifCompass.addFalse(new PrintString("Girando para a direita"));
        loopCompass.add(ifCompass);
        loopCompass.add(new ReadDevice(Compass.class, "alpha"));
        loopCompass.add(new PrintString("Angulo atual: %v", "alpha"));
        func.add(loopCompass);
        func.add(new Move(0, 0));
        func.add(new ReadDevice(Compass.class, "alpha"));
        func.add(new PrintString("Angulo final: %v", "alpha"));
        func.add(new PrintString("fim"));
        i.setMainFunction(func);

        DrawingPanel p = new DrawingPanel();
        p.add((Drawable) func);
        Function.ident(func, 0, 0, 10, 10, 0, 1, true);
        QuickFrame.create(p, "Teste do painel de desenho").addComponentListener(p);


//        System.out.println(Function.getBounds(new Wait(1), null, 10,10,0));

        //executa
//        while (i.step()) {
////            try {
////                    Thread.sleep(100);
////                } catch (InterruptedException ex) {
////                }
//        }

//        System.exit(0);

    }
}
