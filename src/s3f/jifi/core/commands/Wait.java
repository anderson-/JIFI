/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.commands;

import s3f.jifi.core.interpreter.ExecutionException;

/**
 *
 * @author gnome3
 */
public class Wait implements Command {

    public static void perform(Integer time, Object rm) throws ExecutionException {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
        }
    }

    @Override
    public String getName() {
        return "wait";
    }

    @Override
    public Class[][] getArgs() {
        return new Class[][]{{Integer.class, Object.class}};
    }

}
