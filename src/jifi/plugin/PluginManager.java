/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 *
 * @author antunes
 */
public class PluginManager {

//    public static class PluginPolicy extends Policy {
//
//        public PluginPolicy() {
//            Policy.setPolicy(new PluginPolicy());
//            System.setSecurityManager(new SecurityManager());
//        }
//
//        public PermissionCollection getPermissions(CodeSource codeSource) {
//            Permissions p = new Permissions();
//            
//            if (!codeSource.getLocation().toString().endsWith("/rogue.jar")) {
//                p.add(new AllPermission());
//            }
//            return p;
//        }
//
//        /**
//         * Does nothing.
//         */
//        public void refresh() {
//        }
//    }
    @Deprecated
    public static ArrayList<Class> getPluginsAlpha(String listFile, Class type) {
        ArrayList<Class> ret = new ArrayList<>();
//        PluginPolicy p = new PluginPolicy();
        ArrayList<String> pluginNames = new ArrayList<>();
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream(listFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                    pluginNames.add(line);
                }
            }
            reader.close();
            for (String pName : pluginNames) {
                Class c = loader.loadClass(pName);
                if (type.isAssignableFrom(c)) {
                    ret.add(c);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public static ArrayList<Class> getPlugins(String jarFile) {
        ArrayList<Class> ret = new ArrayList<>();
        File pluginJar = new File(jarFile);
        ArrayList<String> pluginNames = new ArrayList<>();
        try {
            ClassLoader loader = URLClassLoader.newInstance(new URL[]{pluginJar.toURL()});
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("list.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                    pluginNames.add(line);
                }
            }
            reader.close();
            for (String pName : pluginNames) {
                Class c = loader.loadClass(pName);
                ret.add(c);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }
}
