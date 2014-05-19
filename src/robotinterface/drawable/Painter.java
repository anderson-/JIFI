/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package robotinterface.drawable;

import java.awt.Graphics2D;

/**
 *
 * @author anderson
 */
public interface Painter {
    
    public int getLayer();
    
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in);
    
}
