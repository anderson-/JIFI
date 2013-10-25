/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.robot;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author antunes
 */
public class RobotManager extends JPanel {

    private JButton btnAddRobot;

    public RobotManager() {
//        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        super.setLayout(new GridBagLayout());
        final GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.gridx = 0;
//        super.add(new RobotControlPanel());
//        super.add(new RobotControlPanel());

        btnAddRobot = new JButton("Adicionar Robô");

        btnAddRobot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RobotManager.this.add(new RobotControlPanel(RobotManager.this), cons);
                RobotManager.this.remove(btnAddRobot);
                RobotManager.this.add(btnAddRobot, cons);
            }
        });

        super.add(btnAddRobot, cons);
    }
}
