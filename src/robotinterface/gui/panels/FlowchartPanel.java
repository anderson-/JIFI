/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import robotinterface.algorithm.procedure.Function;
import robotinterface.drawable.DWidgetContainer;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.interpreter.Interpreter;

/**
 *
 * @author antunes
 */
public class FlowchartPanel extends DrawingPanel {

    private static class SelectionPanel extends DWidgetContainer {

//        private static class ItemList implements Iterable<ItemList> {
//
//            private class ItemIterator implements Iterator<ItemList>{
//
//                ItemList it = ItemList.this;
//                boolean head = true;
//                
//                @Override
//                public boolean hasNext() {
//                    if (head){
//                        return (it != null);
//                    }
//                    return (it.next() != null);
//                }
//
//                @Override
//                public ItemList next() {
//                    if (head){
//                        head = false;
//                        return it;
//                    }
//                    it = it.next();
//                    return it;
//                }
//
//                @Override
//                public void remove() {
//                    
//                }
//                
//            }
//            
//            public ItemList next(){
//                
//            }
//            
//            @Override
//            public Iterator<ItemList> iterator() {
//                return new ItemIterator();
//            }
//            
//        }
        public static class Item implements Drawable {

            private static Font font = null;
            private static double maxHeight = 0;
            private static double maxWidth = 0;
            private static double maxIconHeight = 0;
            private static double maxIconWidth = 0;
            private static int insetX = 5;
            private static int insetY = 3;
            private RoundRectangle2D.Double shape = new RoundRectangle2D.Double(0, 0, 10, 10, 5, 5);
            private String name;
            private BufferedImage image;
            private Shape icon;
            private Color color;

            public Item(String name, BufferedImage image) {
                this.name = name;
                this.image = image;
                this.icon = null;
                this.color = null;
            }

            public Item(String name, Shape icon, Color color) {
                this.name = name;
                this.image = null;
                this.icon = icon;
                this.color = color;
            }

            private static void setBounds(int w1, int h1, String name, FontRenderContext context) {
                Rectangle2D stringBounds = font.getStringBounds(name, context);
                double h = (h1 > stringBounds.getHeight()) ? h1 : stringBounds.getHeight();

                maxIconWidth = (w1 > maxIconWidth) ? w1 : maxIconWidth;
                maxIconHeight = (h > maxIconHeight) ? h : maxIconHeight;

                maxWidth = ((w1 + stringBounds.getWidth() + 3 * insetX) > maxWidth) ? w1 + stringBounds.getWidth() + 3 * insetX : maxWidth;
                maxHeight = ((h + 2 * insetY) > maxHeight) ? h + 2 * insetY : maxHeight;
            }

            public static void setFont(Font font) {
                Item.font = font;
            }

            @Override
            public Shape getObjectShape() {
                return shape;
            }

            @Override
            public Rectangle2D.Double getObjectBouds() {
                return (Rectangle2D.Double) shape.getBounds2D();
            }

            @Override
            public void setObjectBounds(double x, double y, double width, double height) {
                shape.x = x;
                shape.y = y;
                shape.width = width;
                shape.height = height;
            }

            @Override
            public void setObjectLocation(double x, double y) {
                shape.x = x;
                shape.y = y;
            }

            @Override
            public int getDrawableLayer() {
                return Drawable.DEFAULT_LAYER;
            }

            @Override
            public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
            }

            @Override
            public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
                shape.width = maxWidth;
                shape.height = maxHeight;
                g.translate(shape.x, shape.y);

                g.setColor(color);

                if (shape.contains(in.getRelativeMouse())) {
                    if (in.isMouseOver()) {
                        g.setColor(color.darker());
                    }
                }

                g.drawRoundRect(0, 0, (int) maxWidth, (int) maxHeight, 10, 10);

                g.translate(insetX, insetY);

                double x, y;

                if (image != null) {
                    setBounds(image.getWidth(), image.getHeight(), name, g.getFontRenderContext());
                    x = maxIconWidth / 2. - image.getWidth() / 2.;
                    y = maxIconHeight / 2. - image.getHeight() / 2.;
                    g.translate(x, y);
                    g.drawImage(image, null, null);
                } else {
                    setBounds(icon.getBounds().width, icon.getBounds().height, name, g.getFontRenderContext());
                    x = maxIconWidth / 2. - icon.getBounds().width / 2.;
                    y = maxIconHeight / 2. - icon.getBounds().height / 2.;
//                    System.out.println(maxIconWidth);
                    g.translate(x, y);
                    g.setColor(color);
                    g.fill(icon);
                }
                g.translate(-x, -y);
                Rectangle2D stringBounds = font.getStringBounds(name, g.getFontRenderContext());

                x = maxIconWidth + insetX;
                y = maxIconHeight / 2 + stringBounds.getHeight() / 2 - insetY;

//                g.drawString(name, (int) x, (int) y);

                g.translate(x, y);
                g.drawString(name, 0, 0);
                g.translate(-x, -y);

                g.translate(-insetX, -insetY);
                g.translate(-shape.x, -shape.y);
            }

            @Override
            public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
            }
        }
        private static BufferedImage closeBtnImg = null;
        private int panelWidth = 200;
        private boolean open = true;
        private boolean animOpen = false;
        private boolean animClose = false;
        private RoundRectangle2D.Double closeBtn;
        private ArrayList<Item> itens;

        public SelectionPanel() {
            if (closeBtnImg == null) {
                try {
                    closeBtnImg = ImageIO.read(getClass().getResourceAsStream("/resources/arrow_state_blue_right.png"));
                } catch (IOException ex) {
                }
            }

            itens = new ArrayList<>();

            Item.setFont(new Font("Serif", Font.PLAIN, 12));

            itens.add(new Item("Anderson", new RoundRectangle2D.Double(0, 0, 10, 10, 5, 5), Color.CYAN));
            itens.add(new Item("Teste", SimpleContainer.createDiamond(new Rectangle2D.Double(0, 0, 10, 10)), Color.YELLOW));

            this.closeBtn = new RoundRectangle2D.Double(-15, 15, 20, 20, 10, 10);
            bounds.x = 0;
        }

        @Override
        public int getDrawableLayer() {
            return Drawable.TOP_LAYER;
        }

        @Override
        public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
            g.setStroke(new BasicStroke());

            g.setColor(Color.cyan.darker().darker());

            if (in.mouseGeneralClick() && closeBtn.contains(in.getRelativeMouse())) {
                if (open && !(animOpen || animClose)) {
                    animClose = true;
                } else {
                    animOpen = true;
                }
            }

            g.setFont(Item.font);

            g.draw(closeBtn);

            if (open) {
                g.drawImage(closeBtnImg, -14, 18, null);
            } else {
                g.translate(-14, 18);
                g.drawImage(closeBtnImg, closeBtnImg.getWidth(null), 0, 0, closeBtnImg.getHeight(null),
                        0, 0, closeBtnImg.getWidth(null), closeBtnImg.getHeight(null), null);
                g.translate(14, -18);
            }

            g.fillRect(0, 0, panelWidth, ga.getHeight());
            //super.setObjectLocation(0, x);
            if (in.isKeyPressed(KeyEvent.VK_1)) {
                animOpen = !animOpen;
            } else if (in.isKeyPressed(KeyEvent.VK_2)) {
                animClose = !animClose;
            }

            int x = 10, y = 10;
            for (Item i : itens) {
                i.setObjectLocation(x, y);
//                g.setColor(Color.GREEN);
//                g.draw(i.getObjectShape());
                i.draw(g, ga, in);
                y += i.getObjectBouds().height + 50;
            }

            //animação
            if (animOpen) {
                if (bounds.x > ga.getWidth() - panelWidth) {
                    bounds.x -= 2;
                } else {
                    bounds.x = ga.getWidth() - panelWidth;
                    animOpen = false;
                    open = true;
                }
            } else if (animClose) {
                open = false;
                if (bounds.x < ga.getWidth()) {
                    bounds.x += 2;
                } else {
                    bounds.x = ga.getWidth();
                    animClose = false;
                }
            } else if (open) {
                bounds.x = ga.getWidth() - panelWidth;
                bounds.y = 0;
                bounds.width = panelWidth;
                bounds.height = ga.getHeight();
            }
        }
    }
    
    private Function function;
    private int fx = 200;
    private int fy = 60;
    private int fj = 25;
    private int fk = 30;
    private int fIx = 0;
    private int fIy = 1;
    private boolean fsi = true;

    public FlowchartPanel() {
        add(new SelectionPanel());
        function = Interpreter.newTestFunction();
        add(function);
        function.ident(fx, fy, fj, fk, fIx, fIy, fsi);
        function.wire(fj, fk, fIx, fIy, fsi);
        function.appendDCommandsOn(this);
    }

    public static void main(String[] args) {
        FlowchartPanel p = new FlowchartPanel();
        QuickFrame.create(p, "Teste FlowcharPanel").addComponentListener(p);
    }
}