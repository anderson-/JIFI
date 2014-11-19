/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import s3f.core.plugin.EntityManager;
import s3f.core.plugin.PluginManager;
import s3f.core.project.Project;
import s3f.core.script.Script;
import s3f.core.ui.GUIBuilder;
import s3f.jifi.core.commands.Command;
import s3f.jifi.core.interpreter.ResourceManager;
import s3f.jifi.core.js.JSDebugger;
import s3f.jifi.core.js.MyScriptable;
import s3f.util.trafficsimulator.Clock;

public class UI extends GUIBuilder {

    public UI() {
        super("JIFI");
    }

    @Override
    public void init() {

//        System.out.println("addTab");
//        this.addTab(new TesteTab(), 2);
//        this.addToolbarComponent(new ToolBarButton().getJComponent(), 0);
//        this.addToolbarComponent(new ToolBarButton().getJComponent(), 0);
//        this.addToolbarComponent(new ToolBarButton().getJComponent(), 600);
//        this.addToolbarComponent(new ToolBarButton().getJComponent(), 600);
//
//        Project project = new Project(getString("project.name"));
//
//        for (int i = 0; i < 10; i++) {
//            project.addElement(new TMPElement("asdl", UIManager.getIcon("FileView.fileIcon")));
//        }
//
//        this.addTab(new ProjectTreeTab(project), 2);
//
//        ProjectTreeTab projectTreeTab = new ProjectTreeTab(project);
//        this.addTab(projectTreeTab, 2);
//        this.addTab(new CodeEditorTab("javascript"), 2);
//
//        JMenu jMenu = new JMenu();
//
//        jMenu.setText("JIFI");
//        jMenu.add(new JMenuItem("asd"));
//        this.addMenubar(jMenu, 0);
        
                final JSDebugger jsDebugger = new JSDebugger();

        final AbstractAction runScript = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                final MyScriptable myScriptable = new MyScriptable();

                EntityManager em = PluginManager.getInstance().createFactoryManager(null);
                List<Command> list = em.getAllProperties("s3f.jifi.cmd.*", "procedure", Command.class);
                if (list != null) {
                    for (Command o : list) {
                        for (Class[] args : o.getArgs()) {
                            myScriptable.register(o.getName(), o.getClass(), "perform", args);
                        }
                    }
                }

                final ResourceManager rm = new ResourceManager();
                rm.addResource(new Clock());

                final Script mainSource = new Script();

                Project project = (Project) em.getProperty("s3f.core.project.tmp", "project");
                for (s3f.core.project.Element e : project.getElements()) {
//                    if (e instanceof Robot) {
//                        Robot robot = (Robot) e;
//                        rm.addResource(robot);
////                        robot.getMainConnection().establishConnection();
//                    } 
                    if (e instanceof Script) {
                        Script script = (Script) e;
                        mainSource.setText(script.getText());
                    }
                }

                jsDebugger.init(mainSource.getText());
                jsDebugger.setStepByStepExecution(false);
                
                new Thread() {
                    @Override
                    public void run() {
                        myScriptable.compileAndRun(mainSource.getText(), "My Script", rm, jsDebugger);
                    }
                }.start();
            }
        };

        addMenuItem("Run>", "S", null, null, null, 4, null);
        addMenuItem("Run>Run script", "R", "F6", null, null, 0, runScript);

        addMenuItem("Run>Step by step", "E", "shift released S", null, null, 0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!jsDebugger.isRunning()) {
                            runScript.actionPerformed(null);
                        }
                        jsDebugger.setStepByStepExecution(enabled);
                    }
                });
            }
        });

        addMenuItem("Run>Stop script", "S", "ESCAPE", null, null, 0, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                jsDebugger.killProgram();
            }
        });

    }

}
