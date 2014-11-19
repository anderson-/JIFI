/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.commands;

/**
 *
 * @author gnome3
 */
public interface Command {
    
    public String getName();
    
    public Class [][] getArgs();
    
}
