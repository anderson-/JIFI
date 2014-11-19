/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi;

import s3f.core.plugin.ConfigurableObject;
import s3f.core.plugin.PluginBuilder;
import s3f.core.ui.GUIBuilder;
import s3f.jifi.core.commands.Print;
import s3f.jifi.core.commands.Wait;

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
//        ConfigurableObject o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new If());
//        pm.registerFactory(o);
//        o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new While());
//        pm.registerFactory(o);
//        o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new DoWhile());
//        pm.registerFactory(o);
//        o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new BreakLoop());
//        pm.registerFactory(o);
//        o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new KeyboardInput());
//        pm.registerFactory(o);
//        o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new Wait());
//        pm.registerFactory(o);
//        o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new PrintString());
//        pm.registerFactory(o);
//        o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new Procedure());
//        pm.registerFactory(o);
//        o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new Comment());
//        pm.registerFactory(o);
////        o = new ConfigurableObject("s3f.jifi.cmd");
////        o.getData().setProperty("procedure", new FunctionBlock());
////        pm.registerFactory(o);
//        o = new ConfigurableObject("s3f.jifi.cmd");
//        o.getData().setProperty("procedure", new Return());
//        pm.registerFactory(o);
//        
//        pm.registerFactory(Flowchart.FLOWCHART_FILES);
        
        ConfigurableObject o;
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Wait());
        pm.registerFactory(o);
        o = new ConfigurableObject("s3f.jifi.cmd");
        o.getData().setProperty("procedure", new Print());
        pm.registerFactory(o);
    }

}
