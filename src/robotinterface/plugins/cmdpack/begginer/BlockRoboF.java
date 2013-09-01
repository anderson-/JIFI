/**
 * @file .java
 * @author Fernando Padilha Ferreira <fpf.padilhaf@gmail.com>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Fernando Padilha Ferreira <fpf.padilhaf@gmail.com>
 *
 * RobotInterface is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * RobotInterface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RobotInterface. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package robotinterface.plugins.cmdpack.begginer;

import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DWidgetContainer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.robot.device.Device;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.device.IRProximitySensor;
import robotinterface.util.trafficsimulator.Clock;
import robotinterface.util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class BlockRoboF extends Procedure implements GraphicResource {

    public static final String ACAO = "<ação>";
    public static final String ACAO_MOVER = "mover";
    public static final String ACAO_GIRAR = "girar";
    private Timer timer;
    private Device device;
    private String var;
    private Class<?> type1;
    private Class<?> type2;
    private int valorParam1;
    private int valorParam2;
    private DWidgetContainer sContainer;
    private DrawableRobot drobot;

    public BlockRoboF(ArrayList<Class<? extends Device>> devices) {
        //Cria e inicializa os componentes Swing usados no componente
        final HashMap<String, Class<?>> deviceMap = new HashMap<>();
        final JComboBox comboParam1, comboParam2;
        
        comboParam1 = new JComboBox();
        comboParam2 = new JComboBox();
        comboParam1.setEditable(true);
        comboParam2.setEditable(true);
        for (Class<? extends Device> c : devices) {
            deviceMap.put(c.getSimpleName(), c);
            comboParam1.addItem(c.getSimpleName());
            comboParam2.addItem(c.getSimpleName());
        }
        
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String devName = (String) cb.getSelectedItem();
                if (cb.equals(comboParam1)) {
                  type1 = deviceMap.get(devName);
                  if (type1 == null) {
                    try {
                      valorParam1 = Integer.valueOf(devName);
                      type1 = Integer.class;
                    } catch (NumberFormatException ex) {
                      type1 = null;
                      valorParam1 = 0;
                    }
                  }
                } else {
                  type2 = deviceMap.get(devName);
                  if (type2 == null) {
                    try {
                      valorParam2 = Integer.valueOf(devName);
                      type2 = Integer.class;
                    } catch (NumberFormatException ex) {
                      type2 = null;
                      valorParam2 = 0;
                    }
                  }
                }
            }
        };
        
        comboParam1.addActionListener(al);
        comboParam2.addActionListener(al);
        
        drobot = new DrawableRobot();
        drobot.setPreferredSize(new Dimension(60, 60));

        JComboBox combobVar = new JComboBox();
        combobVar.addItem(ACAO);
        combobVar.addItem(ACAO_MOVER);
        combobVar.addItem(ACAO_GIRAR);

        combobVar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                var = (String) cb.getSelectedItem();
                drobot.mover = false;
                drobot.girar = false;
                comboParam1.removeItem("<veloc>");
                comboParam1.removeItem("<angulo>");
                comboParam2.removeItem("<tempo>");
                if (var.equals(ACAO_MOVER)) {
                    comboParam1.addItem("<veloc>");
                    comboParam1.setSelectedIndex(comboParam1.getItemCount()-1);
                    comboParam2.addItem("<tempo>");
                    comboParam2.setSelectedIndex(comboParam2.getItemCount()-1);
                    drobot.mover = true;
                } else if (var.equals(ACAO_GIRAR)) {
                    comboParam1.addItem("<angulo>");
                    comboParam1.setSelectedIndex(comboParam1.getItemCount()-1);
                    drobot.girar = true;
                }
            }
        });

        /*
         * se você estiver fazendo um comando simples, pode usar SimpleContainer para desenhar
         * você só precisa sobrescrever os metodos (pessimamente nomeados):
         *  - drawWJC (draw with jcomponents) - desenha quando os compoentes swing estão aparecendo
         *  - drawWoJC (draw without jcomponents) - desenha quando os compoentes swing não estão aparecendo
         * e passar uma forma geometrica (Shape) e uma cor.
         * 
         * para mostrar os componente deve-se selecionar com o mouse.
         */
        
        final Shape s = new RoundRectangle2D.Double(0, 0, 250, 120, 20, 20);
        //cria um Losango (usar em IF)
        //s = SimpleContainer.createDiamond(new Rectangle(0,0,150,100));
        
        Color c = Color.getHSBColor(.3f, .5f, .7f);

        sContainer = new SimpleContainer(s, c) {
            //re
            @Override
            protected void drawWJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
                //escreve coisas quando os jcomponets estão visiveis
                g.setColor(Color.BLACK);
                if (var == null || var.startsWith("<")) {
                  comboParam1.setVisible(false);
                  comboParam2.setVisible(false);
                } else if (var.equals(ACAO_MOVER)) {
                  comboParam1.setVisible(true);
                  comboParam2.setVisible(true);
                } else if (var.equals(ACAO_GIRAR)) {
                  comboParam1.setVisible(true);
                  comboParam2.setVisible(false);
                }
            }

            @Override
            protected void drawWoJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
                //escreve coisas quando os jcomponets não estão visiveis
                g.setColor(Color.BLACK);
                if (var != null && !var.startsWith("<")) {
                    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
                    g.drawString(var + "( ", 125, 44);
                    if (type1 != null) {
                        String text;
                        if (type1 == Integer.class)
                          text = valorParam1 + " ";
                        else
                          text = type1.getSimpleName() + " ";
                        text += (var.equals(ACAO_MOVER)) ? "," : ")";
                        //g.setFont(new Font(Font.MONOSPACED, Font.ITALIC,
                        //        Math.min(14, (int)(this.getObjectBouds().getWidth()-69)/text.length()+1)));
                        g.setFont(new Font(Font.MONOSPACED, Font.ITALIC,
                                Math.min(14, (int)(this.getObjectBouds().getWidth()-135)*2/text.length()-2))
                                );
                        g.drawString(text, 135, 69);
                    }
                    if (type2 != null && var.equals(ACAO_MOVER)) {
                        String text;
                        if (type2 == Integer.class)
                          text = valorParam2 + " )";
                        else
                          text = type2.getSimpleName() + " )";
                        g.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 
                                Math.min(14, (int)(this.getObjectBouds().getWidth()-135)*2/text.length()-2))
                                );
                        g.drawString(text, 135, 89);
                    } 
                } else {
                    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
                    g.drawString("EDITAR", 150, 68);
                }
            }
        };
        
        JPanel fpanel = new JPanel();
        fpanel.setLayout(new GridLayout(3,0));
        //fpanel.setBounds(0, 0, 115, 100);
        //combobVar.setBounds(0, 20, 100, 20);
        fpanel.add(combobVar);
        //comboParam1.setBounds(15, 45, 100, 20);
        fpanel.add(comboParam1);
        //comboParam2.setBounds(15, 65, 100, 20);
        fpanel.add(comboParam2);
        
        JLabel rlabel = new JLabel("ROBO F") {
            public void paint(Graphics g) {
              Graphics2D g2D = (Graphics2D)g;
              // Create a rotation transformation for the font.
              AffineTransform posAT = g2D.getTransform();
              AffineTransform fontAT = new AffineTransform();
              // get the current font
              Font theFont = g2D.getFont();
              
              posAT.translate(0, this.getHeight());
              g2D.setTransform(posAT);
              
              // Derive a new font using a rotatation transform
              fontAT.rotate(-Math.PI/2);
              Font theDerivedFont = theFont.deriveFont(fontAT);
              theDerivedFont = theDerivedFont.deriveFont((float)this.getHeight()/8);
              // set the derived font in the Graphics2D context
              g2D.setFont(theDerivedFont);
              // Render a string using the derived font
              g2D.drawString(this.getText(), this.getHeight()/8, -this.getHeight()/4);
              // put the original font back
              g2D.setFont(theFont);
            }
        };
        //rlabel.setBounds(0, 0, 30, 120);
        
        //adiciona os jcompoents no SimpleContainer
        sContainer.addJComponent(fpanel, 125, 10, 115, 100);
        sContainer.addJComponent(drobot, 25, 20, 80, 80);
        sContainer.addJComponent(rlabel, 0, 0, 22, 120);
        sContainer.setJComponentStatic(1, true);
        sContainer.setJComponentStatic(2, true);
        drobot.removeMouseMotionListener(null);

    }

    public BlockRoboF(Class<? extends Device> type, String var) {
        this.type1 = type;
        this.var = var;
        timer = new Timer(200);
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
    }

    @Override
    public boolean perform(Robot r, Clock clock) throws ExecutionException {
      return false;
    }

    @Override
    public Drawable getDrawableResource() {
        //retorna a classe responsável por desenhar esse comando.
        return sContainer;
    }

    public static void main(String[] args) {
//        QuickFrame.applyLookAndFeel();

        ArrayList<Class<? extends Device>> a = new ArrayList<>();
        a.add(HBridge.class);
        a.add(Compass.class);
        a.add(IRProximitySensor.class);

        BlockRoboF rd = new BlockRoboF(a);


        QuickFrame.drawTest(rd.getDrawableResource());
    }
}
