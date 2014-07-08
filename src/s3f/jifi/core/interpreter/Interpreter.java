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

import javax.swing.JOptionPane;
import org.nfunk.jep.JEP;
import s3f.jifi.core.Command;
import s3f.jifi.flowchart.Function;
import s3f.util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class Interpreter implements s3f.core.simulation.System {

    private final Clock clock;
    private final JEP parser;
    private final ResourceManager resourceManager;
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

    @Override
    public void reset() {
        if (mainFunction != null) {
            currentCmd = mainFunction;
        } else {
            currentCmd = null;
        }

        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
//        parser.setTraverse(true); //exibe debug
        parser.setImplicitMul(false);//multiplicação implicita: 2x+4
        parser.initSymTab(); // clear the contents of the symbol table
        try {
            parser.addStandardConstants();
        } catch (Throwable t) {

        }
        parser.setAllowAssignment(true);
        parser.addFunction("get", new Get());
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
    
    public void step() {
        if (currentCmd == null) {
            return;
        }

        clock.setPaused(false);

        //System.out.println(currentCmd); //exibe o comando atual
        try {
            currentCmd.begin(resourceManager);
            while (!currentCmd.perform(resourceManager)) {
                clock.increase();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                }
                if (state != RUNNING){
                    return;
                }
            }
            currentCmd = currentCmd.step(resourceManager);

        } catch (ForceInterruptionException e) {
            
        } catch (ExecutionException e) {
            if (e instanceof ForceInterruptionException) {
                return;
            }
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

    @Override
    public boolean performStep() {
        step = false;
        step();
        return true;
    }

}
