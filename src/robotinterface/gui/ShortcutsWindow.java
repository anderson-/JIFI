/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 *
 * @author anderson
 */
public final class ShortcutsWindow extends javax.swing.JFrame {

    public static final ImageIcon icon_mouse = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/mouse_click_left.png"));
    public static final ImageIcon icon_mouse_click_left = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/mouse_click_left.png"));
    public static final ImageIcon icon_mouse_click_right = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/mouse_click_right.png"));
    public static final ImageIcon icon_mouse_click_center = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/mouse_click_center2.png"));
    public static final ImageIcon icon_mouse_moving = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/mouse_moving.png"));
    public static final ImageIcon icon_mouse_moving_left = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/mouse_moving_left.png"));
    public static final ImageIcon icon_mouse_moving_right = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/mouse_moving_right.png"));
    public static final ImageIcon icon_mouse_scrolling = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/mouse_scrolling.png"));
    public static final ImageIcon icon_plus1 = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/plus2_math-26.png"));
    public static final ImageIcon icon_plus2 = new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/plus2_math-48.png"));
    public static final ImageIcon icon_key_alt = new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_alt.png"));
    public static final ImageIcon icon_key_ctrl = new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_ctrl.png"));
    public static final ImageIcon icon_key_shift = new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_shift.png"));

    /**
     * Creates new form Teste
     */
    public ShortcutsWindow() {

        initComponents();
        
        jScrollPane2.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane3.getVerticalScrollBar().setUnitIncrement(16);

        flowchartHotkeysPanel.setLayout(new BoxLayout(flowchartHotkeysPanel, BoxLayout.Y_AXIS));
        simluationHotkeysPanel.setLayout(new BoxLayout(simluationHotkeysPanel, BoxLayout.Y_AXIS));

        //Simulação
        addCategory(simluationHotkeysPanel, "Principais");
        addShortcut(simluationHotkeysPanel, "<html>Mover<br>ambiente", icon_mouse_moving_right);
        addShortcut(simluationHotkeysPanel, "Zoom", icon_mouse_scrolling);
        addShortcut(simluationHotkeysPanel, "<html>Trazer robô<br>para a origem", icon_mouse_click_center);
        addShortcut(simluationHotkeysPanel, "<html>Ir para<br>a origem", icon_key_ctrl, icon_plus1, icon_mouse_click_center);

        addCategory(simluationHotkeysPanel, "Linhas");
        addShortcut(simluationHotkeysPanel, "<html>Iniciar<br>linha", icon_mouse_click_left);
        addShortcut(simluationHotkeysPanel, "<html>Finalizar<br>linha", icon_mouse_click_right);

        addCategory(simluationHotkeysPanel, "Linhas Fechadas");
        addShortcut(simluationHotkeysPanel, "<html>Alterar<br>poligono", icon_key_ctrl, icon_plus1, icon_mouse_scrolling);

        addCategory(simluationHotkeysPanel, "Comandos do Teclado");
        addShortcut(simluationHotkeysPanel, "Zoom +", icon_key_ctrl, icon_plus1, new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_plus.png")));
        addShortcut(simluationHotkeysPanel, "Zoom -", icon_key_ctrl, icon_plus1, new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_minus.png")));
        addShortcut(simluationHotkeysPanel, "<html>Mover para<br>cima", new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_up.png")));
        addShortcut(simluationHotkeysPanel, "<html>Mover para<br>baixo", new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_down.png")));
        addShortcut(simluationHotkeysPanel, "<html>Mover para<br>esquerda", new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_left.png")));
        addShortcut(simluationHotkeysPanel, "<html>Mover para<br>direita", new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_right.png")));

        //Fluxograma
        addCategory(flowchartHotkeysPanel, "Principais");
        addShortcut(flowchartHotkeysPanel, "<html>Mover<br>ambiente", icon_mouse_moving_right);
        addShortcut(flowchartHotkeysPanel, "Zoom", icon_mouse_scrolling);
        addShortcut(flowchartHotkeysPanel, "<html>Selecionar tipo<br>de bloco", icon_mouse_click_left);
        addShortcut(flowchartHotkeysPanel, "<html>Cancelar<br>colocar bloco", icon_mouse_click_right);
        addShortcut(flowchartHotkeysPanel, "<html>Colocar<br>bloco", icon_mouse_click_left);
        addShortcut(flowchartHotkeysPanel, "<html>Editar<br>bloco", new ImageIcon(ShortcutsWindow.class.getResource("/resources/mouse/mouse_double_click.png")));

        addCategory(flowchartHotkeysPanel, "Comandos do Teclado");
        addShortcut(flowchartHotkeysPanel, "Copiar", icon_key_ctrl, icon_plus1, new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_C.png")));
        addShortcut(flowchartHotkeysPanel, "Colar", icon_key_ctrl, icon_plus1, new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_V.png")));
        addShortcut(flowchartHotkeysPanel, "Recortar", icon_key_ctrl, icon_plus1, new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_X.png")));
        addShortcut(flowchartHotkeysPanel, "Desfazer", icon_key_ctrl, icon_plus1, new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_Z.png")));
        addShortcut(flowchartHotkeysPanel, "Refazer", icon_key_ctrl, icon_plus1, new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_Y.png")));
        addShortcut(flowchartHotkeysPanel, "Zoom +", icon_key_ctrl, icon_plus1, new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_plus.png")));
        addShortcut(flowchartHotkeysPanel, "Zoom -", icon_key_ctrl, icon_plus1, new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_minus.png")));
        addShortcut(flowchartHotkeysPanel, "<html>Mover para<br>cima", new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_up.png")));
        addShortcut(flowchartHotkeysPanel, "<html>Mover para<br>baixo", new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_down.png")));
        addShortcut(flowchartHotkeysPanel, "<html>Mover para<br>esquerda", new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_left.png")));
        addShortcut(flowchartHotkeysPanel, "<html>Mover para<br>direita", new ImageIcon(ShortcutsWindow.class.getResource("/resources/keys/key_right.png")));
        
        
        flowchartHotkeysPanel.add(Box.createVerticalGlue());
        simluationHotkeysPanel.add(Box.createVerticalGlue());
        pack();
        super.setIconImage(new ImageIcon(getClass().getResource("/resources/jifi_icon.png")).getImage());
    }

    public void addCategory(JPanel p, String name) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("<html><b><u>" + name));
        p.add(panel);
//        p.add(new JSeparator());
    }

    public void addShortcut(JPanel p, String name, ImageIcon... imgs) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(name + ": "));
        panel.add(Box.createHorizontalGlue());
        for (ImageIcon img : imgs) {
            panel.add(new JLabel(img));
        }
//        panel.add(Box.createHorizontalGlue());
        p.add(panel);
        p.add(new JSeparator());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        simluationHotkeysPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        flowchartHotkeysPanel = new javax.swing.JPanel();

        setTitle("Atalhos do Programa");
        setAlwaysOnTop(true);

        jScrollPane2.setViewportView(simluationHotkeysPanel);

        jTabbedPane1.addTab("Simulação", jScrollPane2);

        flowchartHotkeysPanel.setLayout(new javax.swing.BoxLayout(flowchartHotkeysPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane3.setViewportView(flowchartHotkeysPanel);

        jTabbedPane1.addTab("Fluxograma", jScrollPane3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ShortcutsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ShortcutsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ShortcutsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ShortcutsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ShortcutsWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel flowchartHotkeysPanel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel simluationHotkeysPanel;
    // End of variables declaration//GEN-END:variables
}
