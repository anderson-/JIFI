/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi;

import java.util.Locale;
import s3f.core.plugin.PluginManager;
import s3f.core.ui.MainUI;

/**
 *
 * @author antunes
 */
public class Main {

    public static void main(String[] args) {
        Thread t = Thread.currentThread();
        PluginManager pm = PluginManager.getInstance(args, Main.class);
        pm.loadSoftPlugin("s3f/magenta/plugin.cfg", null);
        pm.loadSoftPlugin("s3f/jifi/plugin.cfg", null);
        
        pm.printTree(System.out);
        MainUI.buildAndRun();
    }
}
