/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.robot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JPanel;
import robotinterface.gui.GUI;

/**
 *
 * @author antunes
 */
public class RobotManager extends JPanel implements Iterable<RobotControlPanel> {

    private ArrayList<RobotControlPanel> panels = new ArrayList<>();
    private JButton btnAddRobot;
    private GUI gui = null;
    private final GridBagConstraints cons;

    public RobotManager(final GUI gui) {
        this.gui = gui;
        
        super.setLayout(new GridBagLayout());
        cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.gridx = 0;

        btnAddRobot = new JButton("Adicionar Robô");

        btnAddRobot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createRobot();
            }
        });

        super.add(btnAddRobot, cons);
    }

    public void createRobot() {
        RobotControlPanel p = new RobotControlPanel(RobotManager.this);
        panels.add(p);
        RobotManager.this.add(p, cons);
        RobotManager.this.remove(btnAddRobot);
        RobotManager.this.add(btnAddRobot, cons);
        gui.updateRobotList();
    }

    public void remove(RobotControlPanel robotControlPanel) {
        panels.remove(robotControlPanel);
        super.remove(robotControlPanel);
        gui.getSimulationPanel().removeRobot(robotControlPanel.getRobot());
    }

    @Override
    public Iterator<RobotControlPanel> iterator() {
        return panels.iterator();
    }
}
