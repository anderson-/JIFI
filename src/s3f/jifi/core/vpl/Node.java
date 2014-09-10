/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package s3f.jifi.core.vpl;

/**
 *
 * @author anderson
 * @param <T>
 */
public class Node <T extends Node> {
    
    private T parent;
    private T prev;
    private T next;
    
}
