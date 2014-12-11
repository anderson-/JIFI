/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi;

import s3f.core.code.CodeEditorTab;
import s3f.core.plugin.ConfigurableObject;
import s3f.core.plugin.PluginBuilder;
import s3f.core.project.EditableProperty;
import s3f.core.script.Script;
import s3f.core.ui.GUIBuilder;
import s3f.jifi.core.FlowScript;
import s3f.jifi.core.commands.Print;
import s3f.jifi.core.commands.Wait;
import s3f.jifi.flowchart.FlowchartEditorTab;
import s3f.jifi.flowchart.blocks.*;

/**
 *
 * @author antunes
 */
public class Builder extends PluginBuilder {

    static {
        GUIBuilder.setSplashScreen(new JIFISplashScreen("/resources/jifi_logo90.png"));
    }

    public Builder() {
        super("JIFI");
    }

    @Override
    public void init() {
        ConfigurableObject o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new If());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new While());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new DoWhile());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new KeyboardInput());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Wait());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new PrintString());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Procedure());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Comment());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Wait());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Print());
        pm.registerFactory(o);
        
        pm.registerFactory(FlowScript.JS_FLOWSCRIPTS);
        
        EditableProperty.put(FlowScript.JS_FLOWSCRIPTS.getData(), CodeEditorTab.class);
        EditableProperty.put(FlowScript.JS_FLOWSCRIPTS.getData(), FlowchartEditorTab.class);
    }

}
