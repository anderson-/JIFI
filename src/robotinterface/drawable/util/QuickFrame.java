/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.util;

import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.swing.WidgetContainer;
import java.awt.Dimension;
import java.awt.event.ComponentListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author antunes
 */
public class QuickFrame {

    public static final int MIN_WIDTH = 350;

    public static void applyLookAndFeel() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        }
    }

    public static JFrame create(JComponent c, String name) {

        //Create and set up the window.
        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(c);

        if (c instanceof ComponentListener) {
            frame.addComponentListener((ComponentListener) c);
        }

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }

    public static void drawTest(GraphicObject d) {
        DrawingPanel p = new DrawingPanel();
//        int width = (int) d.getObjectBouds().width;
//        if (width < MIN_WIDTH) {
//            if (width < 100) {
//                System.err.println("Atenção! Seu objeto tem tamanho: " + d.getObjectBouds() + " isso está certo?");
//            }
//            width = MIN_WIDTH;
//        }
//        p.setPreferredSize(new Dimension(width, (int) d.getObjectBouds().height));
        p.setPreferredSize(new Dimension(400,200));
        p.add(d);
        create(p, "Teste de desenho: " + d.getClass().getSimpleName());
    }
}
