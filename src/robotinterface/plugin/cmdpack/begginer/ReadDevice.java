/**
 * @file .java
 * @author Anderson Antunes <anderson.utf@gmail.com>
 *         *seu nome* <*seu email*>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Anderson Antunes <anderson.utf@gmail.com>
 *                       *seu nome* <*seu email*>
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
package robotinterface.plugin.cmdpack.begginer;

import robotinterface.algorithm.procedure.Function;
import robotinterface.algorithm.procedure.If;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.algorithm.procedure.While;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.WidgetContainer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.plugin.cmdpack.serial.Start;
import robotinterface.plugin.cmdpack.util.PrintString;
import robotinterface.robot.device.Device;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.device.Device.TimeoutException;
import robotinterface.util.trafficsimulator.Clock;
import robotinterface.util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class ReadDevice extends Procedure implements GraphicResource, Classifiable {

    public static final String RELOAD_VARS_ITEM = "<atualizar>";
    private Timer timer;
    private Device device;
    private Class<? extends Device> type;
    private String var;
    private WidgetContainer sContainer;

    public ReadDevice() {
    }

    public ReadDevice(ArrayList<Class<? extends Device>> devices) {
        //Cria e inicializa os componentes Swing usados no componente
        final HashMap<String, Class<? extends Device>> deviceMap = new HashMap<>();
        JComboBox combobDevice = new JComboBox();
        for (Class<? extends Device> c : devices) {
            deviceMap.put(c.getSimpleName(), c);
            combobDevice.addItem(c.getSimpleName());
        }

        String devName = (String) combobDevice.getSelectedItem();
        type = deviceMap.get(devName);

        combobDevice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String devName = (String) cb.getSelectedItem();
                type = deviceMap.get(devName);
                System.out.println(type);
            }
        });

        JComboBox combobVar = new JComboBox();
        combobVar.addItem(RELOAD_VARS_ITEM);

        combobVar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                var = (String) cb.getSelectedItem();
                if (var.equals(RELOAD_VARS_ITEM)) {
                    cb.removeAllItems();
                    cb.addItem(RELOAD_VARS_ITEM);
                    for (String str : ReadDevice.super.getDeclaredVariables()) {
                        cb.addItem(str);
                    }
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

        Shape s = new RoundRectangle2D.Double(0, 0, 150, 60, 20, 20);
        //cria um Losango (usar em IF)
        //s = SimpleContainer.createDiamond(new Rectangle(0,0,150,100));
        Color c = Color.getHSBColor(.5f, .3f, .7f);

        sContainer = new SimpleContainer(s, c) {
            //re
            @Override
            protected void drawWJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
                //escreve coisas quando os jcomponets estão visiveis
                ((RoundRectangle2D.Double) shape).height = 100;
                this.bounds.height = 100;
                g.setColor(Color.BLACK);
                g.drawString("use o combobox:", 10, 10);
                double x = bounds.x + bounds.width / 2;
                double y = bounds.y;
                ReadDevice.this.ident(x, y, 30, 60, 0, 1, false);
            }

            @Override
            protected void drawWoJC(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
                //escreve coisas quando os jcomponets não estão visiveis
                ((RoundRectangle2D.Double) shape).height = 200;
                this.bounds.height = 200;
                g.setColor(Color.BLACK);
                if (var != null && type != null) {
                    g.drawString(var + " = " + type.getSimpleName(), 10, 30);
                } else {
                    g.drawString("Selecione a variável...", 10, 30);
                }
                double x = bounds.x + bounds.width / 2;
                double y = bounds.y;
                ReadDevice.this.ident(x, y, 30, 60, 0, 1, false);
            }
        };
        //adiciona os jcompoents no SimpleContainer
        sContainer.addWidget(combobDevice, 15, 8, 110, 20);
        sContainer.addWidget(combobVar, 15, 32, 110, 20);

        //esse timer é outra coisa...
        timer = new Timer(200);
    }

    public ReadDevice(Class<? extends Device> type, String var) {
        this.type = type;
        this.var = var;
        timer = new Timer(200);
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        device = robot.getDevice(type);
        if (device != null) {
            //mensagem get padrão 
            byte[] msg = device.defaultGetMessage();
            device.setWaiting();
            if (msg.length > 0) {
                //cria um buffer para a mensagem
                ByteBuffer GETmessage = ByteBuffer.allocate(64);
                //header do comando set
                GETmessage.put(Robot.CMD_GET);
                //id
                GETmessage.put(device.getID());
                //tamanho da mensagem
                GETmessage.put((byte) msg.length);
                //mensagem
                GETmessage.put(msg);
                //flip antes de enviar
                GETmessage.flip();
                robot.getMainConnection().send(GETmessage);
            } else {
                msg = new byte[]{Robot.CMD_GET, device.getID(), 0};
                robot.getMainConnection().send(msg);
            }
        }
        timer.reset();
        clock.addTimer(timer);
    }

    @Override
    public boolean perform(Robot r, Clock clock) throws ExecutionException {
        try {
            if (device != null && device.isValidRead()) {
                String deviceState = device.stateToString();
                if (!deviceState.isEmpty()) {
                    execute(var + " = " + deviceState);
                }
                return true;
            }
        } catch (TimeoutException ex) {
            System.err.println("RE-ENVIANDO");
            begin(r, clock);
        }
        return false;

//        if (timer.isConsumed()) { //espera 200ms antes de ler o valor do dispositivo
//            if (device != null) {
//                String deviceState = device.stateToString();
//                if (!deviceState.isEmpty()) {
//                    execute(var + " = " + deviceState);
//                }
//            }
//            return true;
//        }
//        return false;
    }

    @Override
    public GraphicObject getDrawableResource() {
        //retorna a classe responsável por desenhar esse comando.
        return sContainer;
    }

//    public static void main(String[] args) {
////        QuickFrame.applyLookAndFeel();
//
//        ArrayList<Class<? extends Device>> a = new ArrayList<>();
//        a.add(HBridge.class);
//        a.add(Compass.class);
//
//        ReadDevice rd = new ReadDevice(a);
//
//        Function func = new Function("main", null);
//        func.add(new Wait(1000));
//        func.add(new PrintString("inicio"));
//        func.add(new Start());
//        func.add(new robotinterface.algorithm.procedure.Declaration("i", 10));
//        func.add(new PrintString("Girando %v vezes...", "i"));
//        While loop = new While("i > 0");
//        loop.add(new Move(70, 70)); //move
//        loop.add(new Wait(500));
//        loop.add(new Move(-70, 70)); //gira
//        loop.add(new Wait(500));
//        loop.add(new Move(0, 0)); //para
//        loop.add(new Wait(500));
//        loop.add(new PrintString("Falta mais %v passo(s)...", "i"));
//        loop.add(new Procedure("i = i - 1"));
//        func.add(loop);
//        func.add(new PrintString("Procurando angulo 100"));
//        func.add(new Wait(500));
//        func.add(new robotinterface.algorithm.procedure.Declaration("alpha", 10));
//        While loopCompass = new While("alpha != 100");// vai até 100
//        If ifCompass = new If("alpha > 100");
//        ifCompass.addTrue(new Move(55, -55));
//        ifCompass.addTrue(new PrintString("Girando para a esquerda"));
//        ifCompass.addFalse(new Move(-55, 55));
//        ifCompass.addFalse(new PrintString("Girando para a direita"));
//        loopCompass.add(ifCompass);
//        loopCompass.add(rd);
//        loopCompass.add(new PrintString("Angulo atual: %v", "alpha"));
//        func.add(loopCompass);
//        func.add(new Move(0, 0));
//        func.add(new ReadDevice(Compass.class, "alpha"));
//        func.add(new PrintString("Angulo final: %v", "alpha"));
//        func.add(new PrintString("fim"));
//
//        QuickFrame.drawTest(rd.getDrawableResource());
//    }
    @Override
    public String toString() {
        if (var != null && type != null) {
            return var + " = Robot." + type.getSimpleName();
        } else {
            return getCommandName();
        }
    }

    @Override
    public Item getItem() {
        return new Item("Read Device", new RoundRectangle2D.Double(0, 0, 20, 20, 5, 5), Color.decode("#C05480"));
    }

    @Override
    public Object createInstance() {
        ArrayList<Class<? extends Device>> a = new ArrayList<>();
        a.add(HBridge.class);
        a.add(Compass.class);

        return new ReadDevice(a);
    }
}
