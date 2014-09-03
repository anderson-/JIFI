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
package s3f.jifi.core.interpreter;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.nfunk.jep.JEP;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;
import s3f.core.plugin.EntityManager;
import s3f.core.plugin.PluginManager;
import s3f.core.project.Project;
import s3f.jifi.core.Command;
import s3f.jifi.core.Flowchart;
import s3f.jifi.core.FlowchartPanel;
import s3f.jifi.core.parser.parameterparser.Argument;
import s3f.jifi.flowchart.Function;
import s3f.jifi.flowchart.Return;
import s3f.util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class Interpreter implements s3f.core.simulation.System {

    private final Clock clock;
    private final JEP parser;
    private ResourceManager resourceManager;
    private Function mainFunction;
    private Command currentCmd = null;
    private Command errorCmd = null;
    private int timestep = 0;
    private int state = PAUSED;
    private boolean step = false;

    public Interpreter() {
        parser = new JEP();
        parser.addFunction("get", new Get());
        clock = new Clock();

        resourceManager = new ResourceManager();
        resourceManager.setResource(clock);
        resourceManager.setResource(parser);
        resourceManager.setResource(this);
    }

    public void addResource(Object o) {
        resourceManager.setResource(o);
    }

    private JEP reset(JEP parser) {
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();

        EntityManager em = PluginManager.getInstance().createFactoryManager(null);
        Project project = (Project) em.getProperty("s3f.core.project.tmp", "project");
        for (s3f.core.project.Element e : project.getElements()) {
            if (e instanceof Flowchart) {
                final Flowchart flowchart = (Flowchart) e;

                parser.addFunction(flowchart.getName(), new PostfixMathCommand() {
                    {
                        numberOfParameters = -1;
                    }

                    @Override
                    public void run(Stack inStack) throws ParseException {

                        checkStack(inStack);

                        Function function = flowchart.getFunction().copy();
                        String args = "";
                        for (int i = function.getArgSize() - 1; i >= 0; i--) {
                            Object o = inStack.pop();
                            String arg = function.getArg(i).getStringValue();
                            if (arg.contains("=")) {
                                arg = arg.substring(0, arg.indexOf('='));
                            }
                            arg += "  = " + o;
                            args = o + ", " + args;
                            function.addLineArg(i, Argument.EXPRESSION, arg);
                        }

//                        System.out.println("running: " + flowchart.getName() + "(" + args + ")");
                        try {
                            inStack.push(quickRun(function, clock, Interpreter.this));

                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new ParseException(e.getMessage());
                        }
                    }
                });
            }
        }

//        parser.setTraverse(true); //exibe debug
        parser.setImplicitMul(false);//multiplicação implicita: 2x+4
        parser.initSymTab(); // clear the contents of the symbol table
        try {
            parser.addStandardConstants();
        } catch (Throwable t) {

        }
        parser.setAllowAssignment(true);
        parser.addFunction("get", new Get());
        return parser;
    }

    @Override
    public void reset() {
        if (mainFunction != null) {
            currentCmd = mainFunction;
        } else {
            currentCmd = null;
        }

        reset(parser);
        state = PAUSED;
    }

    private Object quickRun(Command cmd, Clock clock, Interpreter i) throws ExecutionException {
        ResourceManager rm = new ResourceManager();
        rm.setResource(clock);
        rm.setResource(reset(new JEP()));
        rm.setResource(i);
        return quickRun(cmd, rm, clock);
    }

    @Deprecated
    private static Object quickRun(Command cmd, ResourceManager rm, Clock clock) throws ExecutionException {
        if (cmd == null) {
            return null;
        } else {

            cmd.begin(rm);
            while (!cmd.perform(rm)) {
                clock.increase();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                }
//                if (state != RUNNING) {
//                    return;
//                }
            }

            if (cmd instanceof Return) {
                return ((Return) cmd).getValue();
            } else {
                cmd = cmd.step(rm);
                return quickRun(cmd, rm, clock);
            }
        }
    }

    public Function getMainFunction() {
        return mainFunction;
    }

    public void setMainFunction(Function f) {
        mainFunction = f;
        reset();
    }

    public Command getCurrentCommand() {
        return currentCmd;
    }

    public Command getErrorCommand() {
        return errorCmd;
    }

    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }

    public int getTimestep() {
        return timestep;
    }

    @Override
    public void setSystemState(int state) {
        this.state = state;
    }

    @Override
    public int getSystemState() {
        return state;
    }

    @Override
    public void beginStep() {
        step = true;
    }

    @Override
    public boolean performStep() {
        if (step) {
            step = false;
            step();
        }
        return true;
    }

    private void step() {

        if (currentCmd == null) {
            state = DONE;
            return;
        }

        clock.setPaused(false);

//        System.out.println(currentCmd.getClass()); //exibe o comando atual
        try {
            currentCmd.begin(resourceManager);
            while (!currentCmd.perform(resourceManager)) {
                clock.increase();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                }
//                if (state != RUNNING) {
//                    return;
//                }
            }
            currentCmd = currentCmd.step(resourceManager);

        } catch (ForceInterruptionException e) {
            state = DONE;
        } catch (ExecutionException e) {
            state = DONE;
            //GUI.print("Erro: " + e.getMessage());
            errorCmd = currentCmd;
            String msg = "Houve um problema ao executar o código atual.\nO bloco que originou o erro foi destacado.\nFavor corrigir e tentar novamente.";
            String ObjButtons[] = {"Continuar", "Mais detalhes"};
            int PromptResult = JOptionPane.showOptionDialog(null, msg, "Erro", JOptionPane.NO_OPTION, JOptionPane.ERROR_MESSAGE, null, ObjButtons, ObjButtons[1]);
            if (PromptResult == JOptionPane.NO_OPTION) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            System.err.println(e.getMessage());
        }
    }
}
