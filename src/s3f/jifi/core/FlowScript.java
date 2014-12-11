/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import s3f.core.code.CodeEditorTab;
import s3f.core.plugin.EntityManager;
import s3f.core.plugin.Plugabble;
import s3f.core.plugin.PluginManager;
import s3f.core.plugin.SimulableElement;
import s3f.core.project.ComplexElement;
import s3f.core.project.Editor;
import s3f.core.project.Element;
import s3f.core.project.Project;
import s3f.core.project.Resource;
import s3f.core.project.SimpleElement;
import s3f.core.project.editormanager.TextFile;
import s3f.core.script.Script;
import s3f.core.simulation.System;
import s3f.core.ui.GUIBuilder;
import s3f.core.ui.MainUI;
import s3f.jifi.core.commands.Command;
import s3f.jifi.core.interpreter.ResourceManager;
import s3f.jifi.core.js.JSDebugger;
import s3f.jifi.core.js.MyScriptable;
import s3f.jifi.flowchart.FlowchartEditorTab;
import s3f.util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class FlowScript extends ComplexElement implements TextFile {

    public static final Element.CategoryData JS_FLOWSCRIPTS = new Element.CategoryData("FlowScripts", "js", new ImageIcon(FlowScript.class.getResource("/resources/icons/fugue/categories.png")), new FlowScript());

    private String script;

    public FlowScript() {
        super("Empty FlowScript", "/resources/icons/fugue/block.png", JS_FLOWSCRIPTS, new Class[]{FlowchartEditorTab.class});
    }

    @Override
    public Plugabble createInstance() {
        return new FlowScript();
    }

    @Override
    public void setText(String text) {
        this.script = text;
    }

    @Override
    public String getText() {
        return script;
    }

    @Override
    public void setCurrentEditor(Editor editor) {
        super.setCurrentEditor(editor);
        GUIBuilder gui = new GUIBuilder("FlowScript Interpreter GUI Builder") {
            @Override
            public void init() {
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
                                    java.lang.System.out.println("ADD " + o.getName());
                                    myScriptable.register(o.getName(), o.getClass(), "perform", args);
                                }
                            }
                        }

                        final ResourceManager rm = new ResourceManager();
                        rm.addResource(new Clock());

                        final Script mainSource = new Script();

                        for (Object o : getExternalResources()) {
                            java.lang.System.out.println("ADD RESOURCE " + o.toString());
                            rm.addResource(o);
                        }

//                        Project project = (Project) em.getProperty("s3f.core.project.tmp", "project");
//                        for (s3f.core.project.Element e : project.getElements()) {
////                    if (e instanceof Robot) {
////                        Robot robot = (Robot) e;
////                        rm.addResource(robot);
//////                        robot.getMainConnection().establishConnection();
////                    } 
//                            if (e instanceof Script) {
//                                Script script = (Script) e;
//                                mainSource.setText(script.getText());
//                            }
//                        }
                        mainSource.setText(FlowScript.this.getText());

                        jsDebugger.init(mainSource.getText());
                        jsDebugger.setStepByStepExecution(false);
                        if (!jsDebugger.isRunning()) {
                            new Thread() {
                                @Override
                                public void run() {
                                    myScriptable.compileAndRun(mainSource.getText(), "My Script", rm, jsDebugger);
                                }
                            }.start();
                        } else {
                            jsDebugger.setStepByStepExecution(false);
                        }
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

        };

        //force GUI rebuild
        PluginManager pm = PluginManager.getInstance();
        pm.registerFactory(gui);
        pm.createFactoryManager(MainUI.getInstance());
    }

    @Override
    public void addResource(Resource resource) {
        super.addResource(resource);
        if (resource.getPrimary() instanceof FlowScript) {
            FlowScript flowchart = (FlowScript) resource.getPrimary();
            flowchart.addExternalResource(resource.getSecondary());
            Editor currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                currentEditor.update();
            }
        }
    }

    @Override
    public void removeResource(Resource resource) {
        super.removeResource(resource);
        if (resource.getPrimary() instanceof FlowScript) {
            FlowScript flowchart = (FlowScript) resource.getPrimary();
            flowchart.removeExternalResource(resource.getSecondary());
            Editor currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                currentEditor.update();
            }
        }
    }

}
