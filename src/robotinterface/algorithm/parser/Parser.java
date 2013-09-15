/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.parser;

import robotinterface.algorithm.Command;
import robotinterface.algorithm.procedure.Block;

/**
 *
 * @author antunes
 */
public class Parser {
        
    public Command getCommand (String str){
        return null;
    }
    
    public static Block decode (String str){
        //usar JavaCC
        return null;
    }
    
    public static String encode (Block b){
        StringBuilder sb = new StringBuilder();
        b.toString("", sb);
        return sb.toString();
    }
    
}
