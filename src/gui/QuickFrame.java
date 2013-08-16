/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author antunes
 */
public class QuickFrame {
    
    public static void schedule (final JComponent c, final String name){
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                QuickFrame.create(c, name);
            }
        });
    }
    
    public String verticalize(String text) {    
      String vertical= "<html>";    
      for (int i = 0; i < text.length(); i++) {    
         vertical += text.charAt(i);    
         vertical += "<br>";    
      }    
      vertical+= "</html>";    
      return vertical;
   }
    
    public static JFrame create (JComponent c, String name){
        //Create and set up the window.
        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(c);

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }
    
}
