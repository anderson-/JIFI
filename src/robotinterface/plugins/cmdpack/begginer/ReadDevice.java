/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugins.cmdpack.begginer;

import robotinterface.algorithm.procedure.Declaration;
import robotinterface.algorithm.procedure.Function;
import robotinterface.algorithm.procedure.If;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.algorithm.procedure.While;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.DWidgetContainer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JComboBox;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.plugins.cmdpack.serial.Start;
import robotinterface.plugins.cmdpack.util.PrintString;
import robotinterface.robot.device.Device;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;
import robotinterface.interpreter.ExecutionException;
import robotinterface.util.trafficsimulator.Clock;
import robotinterface.util.trafficsimulator.Timer;

/**
 *
 * @author antunes
 */
public class ReadDevice extends Procedure implements GraphicResource {

    public static final String RELOAD_VARS_ITEM = "<atualizar>";
    private Timer timer;
    private Device device;
    private Class<? extends Device> type;
    private String var;
    private DWidgetContainer panel;

    public ReadDevice(ArrayList<Class<? extends Device>> devices) {
        final ArrayList<Class<? extends Device>> devs = devices;
        //criando uma classe anonima para cuidar do desenho do objeto
//        panel = new DWidgetContainer() {
//            private HashMap<String, Class<? extends Device>> deviceMap;
//
//            { //fake constructor
//                deviceMap = new HashMap<>();
//                JComboBox combobDevice = new JComboBox();
//                for (Class<? extends Device> c : devs) {
//                    deviceMap.put(c.getSimpleName(),c);
//                    combobDevice.addItem(c.getSimpleName());
//                }
//                
//                combobDevice.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        JComboBox cb = (JComboBox) e.getSource();
//                        String devName = (String) cb.getSelectedItem();
//                        type = deviceMap.get(devName);
//                        System.out.println(type);
//                    }
//                });
//                
//                JComboBox combobVar = new JComboBox();
//                combobVar.addItem(RELOAD_VARS_ITEM);
//                
//                combobVar.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        JComboBox cb = (JComboBox) e.getSource();
//                        var = (String) cb.getSelectedItem();
//                        if (var.equals(RELOAD_VARS_ITEM)){
//                            cb.removeAllItems();
//                            cb.addItem(RELOAD_VARS_ITEM);
//                            for (String str : ReadDevice.super.getDeclaredVariables()) {
//                                cb.addItem(str);
//                            }
//                        }
//                    }
//                });
//                
//                setObjectBounds(0, 0, 150, 60);
//                addJComponent(combobDevice, 15, 8, 110, 20);
//                addJComponent(combobVar, 15, 32, 110, 20);
//            }
//
//            @Override
//            public int getDrawableLayer() {
//                return Drawable.DEFAULT_LAYER;
//            }
//
//            private RoundRectangle2D.Double shape = new RoundRectangle2D.Double();
//            
//            @Override
//            public Shape getObjectShape() {
//                shape.setRoundRect(bounds.x,bounds.y,bounds.width,bounds.height,20,20);
//                return shape;
//            }
//
//            @Override
//            public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
//                if (in.isMouseOver()){
//                    widgetVisible = true;
////                    bounds.x = (bounds.x < 200)? bounds.x+.1 : 0;
////                    bounds.y = (bounds.y < 200)? bounds.y+.1 : 0;
//                } else {
//                    widgetVisible = false;
//                }
//                g.setColor(Color.getHSBColor(.5f, .3f, .7f));
//                
//                //nunca faça isso, a forma do objeto também contem as coordenadas do objeto
//                //g.fill(getObjectShape());
//                //tudo tem que ser desenhado a partir do ponto (0,0)
//                g.fillRoundRect(0,0,(int)bounds.width,(int)bounds.height,20,20);
//            }
//        };


        final HashMap<String, Class<? extends Device>> deviceMap = new HashMap<>();
        JComboBox combobDevice = new JComboBox();
        for (Class<? extends Device> c : devs) {
            deviceMap.put(c.getSimpleName(), c);
            combobDevice.addItem(c.getSimpleName());
        }

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
        
        Shape s = new RoundRectangle2D.Double(0, 0, 150, 60, 20, 20);
        Color c = Color.getHSBColor(.5f, .3f, .7f);
        
        s = SimpleContainer.createPoli(new Rectangle(0,0,150,100));
        
        panel = new SimpleContainer(s, c);
        panel.addJComponent(combobDevice, 15, 8, 110, 20);
        panel.addJComponent(combobVar, 15, 32, 110, 20);

        //ainda dentro do contrutor =)
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
        if (timer.isConsumed()) { //espera 200ms antes de ler o valor do dispositivo
            if (device != null) {
                String deviceState = device.stateToString();
                if (!deviceState.isEmpty()) {
                    execute(var + " = " + deviceState);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Drawable getDrawableResource() {
        return panel;
    }

    public static void main(String[] args) {
//        QuickFrame.applyLookAndFeel();

        ArrayList<Class<? extends Device>> a = new ArrayList<>();
        a.add(HBridge.class);
        a.add(Compass.class);

        ReadDevice rd = new ReadDevice(a);

        Function func = new Function("main", null);
        func.add(new Wait(1000));
        func.add(new PrintString("inicio"));
        func.add(new Start());
        func.add(new Declaration("i", 10));
        func.add(new PrintString("Girando %v vezes...", "i"));
        While loop = new While("i > 0");
        loop.add(new Move(70, 70)); //move
        loop.add(new Wait(500));
        loop.add(new Move(-70, 70)); //gira
        loop.add(new Wait(500));
        loop.add(new Move(0, 0)); //para
        loop.add(new Wait(500));
        loop.add(new PrintString("Falta mais %v passo(s)...", "i"));
        loop.add(new Procedure("i = i - 1"));
        func.add(loop);
        func.add(new PrintString("Procurando angulo 100"));
        func.add(new Wait(500));
        func.add(new Declaration("alpha", 10));
        While loopCompass = new While("alpha != 100");// vai até 100
        If ifCompass = new If("alpha > 100");
        ifCompass.addTrue(new Move(55, -55));
        ifCompass.addTrue(new PrintString("Girando para a esquerda"));
        ifCompass.addFalse(new Move(-55, 55));
        ifCompass.addFalse(new PrintString("Girando para a direita"));
        loopCompass.add(ifCompass);
        loopCompass.add(rd);
        loopCompass.add(new PrintString("Angulo atual: %v", "alpha"));
        func.add(loopCompass);
        func.add(new Move(0, 0));
        func.add(new ReadDevice(Compass.class, "alpha"));
        func.add(new PrintString("Angulo final: %v", "alpha"));
        func.add(new PrintString("fim"));

        QuickFrame.drawTest(rd.getDrawableResource());
    }
}
