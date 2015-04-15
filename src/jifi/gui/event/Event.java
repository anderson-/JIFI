/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jifi.gui.event;

import jifi.gui.shortcuts.Shortcut;

/**
 *
 * @author antunes
 */
public abstract class Event {
    
    private Shortcut shortchut;
    
    public Event(){
        
    }

    public Event(Shortcut shortchut) {
        this.shortchut = shortchut;
    }

    public Shortcut getShortchut() {
        return shortchut;
    }

    public void setShortchut(Shortcut shortchut) {
        this.shortchut = shortchut;
    }
    
    public abstract void perform();
    
}
