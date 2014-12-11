/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.commands;

import java.io.PrintStream;
import java.util.Arrays;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import s3f.core.ui.MainUI;
import s3f.jifi.core.interpreter.ExecutionException;

/**
 *
 * @author gnome3
 */
public class Print implements Command {

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public Class[][] getArgs() {
        return new Class[][]{{Context.class, Scriptable.class, Object[].class, Function.class}};
    }

    public static void perform(Context cx, Scriptable thisObj, Object[] args, Function funOb) throws ExecutionException {
        PrintStream out = MainUI.getInstance().getConsole();
        if (args.length == 0) {
            out.println();
        } else if (args.length == 1) {
            out.println(args[0]);
        } else {
            out.printf(args[0] + "\n", Arrays.copyOfRange(args, 1, args.length));
        }
    }

}
