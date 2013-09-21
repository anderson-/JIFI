/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.sidepanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.DrawingPanel;

public class Item implements Drawable {

    private static Font font = null;
    private static double maxHeight = 0;
    private static double maxWidth = 0;
    private static double maxIconHeight = 0;
    private static double maxIconWidth = 0;
    private static int insetX = 5;
    private static int insetY = 3;
    private SidePanel panel;
    private RoundRectangle2D.Double shape = new RoundRectangle2D.Double(0, 0, 10, 10, 5, 5);
    private String name;
    private BufferedImage image;
    private Shape icon;
    private Color color;
    private Object ref;

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

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
    
    public static Font getFont() {
        return font;
    }

    public String getName() {
        return name;
    }

    public Shape getIcon() {
        return icon;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Color getColor() {
        return color;
    }

    public void setPanel(SidePanel panel) {
        this.panel = panel;
    }

    private static void setBounds(int w1, int h1, String name, FontRenderContext context) {
        Rectangle2D stringBounds = font.getStringBounds(name, context);
        double h = (h1 > stringBounds.getHeight()) ? h1 : stringBounds.getHeight();

        maxIconWidth = (w1 > maxIconWidth) ? w1 : maxIconWidth;
        maxIconHeight = (h > maxIconHeight) ? h : maxIconHeight;

        maxWidth = ((maxIconWidth + stringBounds.getWidth() + 3 * insetX) > maxWidth) ? maxIconWidth + stringBounds.getWidth() + 3 * insetX : maxWidth;
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
    public void drawBackground(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }

    @Override
    public void draw(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        shape.width = maxWidth;
        shape.height = maxHeight;
        g.translate(shape.x, shape.y);

        g.setColor(Color.white);

        if (shape.contains(in.getRelativeMouse())) {
            if (in.isMouseOver()) {
                g.setColor(color.brighter());
                if (in.mouseClicked()) {
                    //notify
                    panel.ItemSelected(this, ref);
                }
            }
        }

        g.fillRoundRect(0, 0, (int) maxWidth, (int) maxHeight, 10, 10);

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
            g.translate(x, y);
            g.setColor(color);
            g.fill(icon);
        }
        g.translate(-x, -y);
        Rectangle2D stringBounds = font.getStringBounds(name, g.getFontRenderContext());

        x = maxIconWidth + insetX;
        y = maxIconHeight / 2 + stringBounds.getHeight() / 2 - insetY;

        g.translate(x, y);
        g.drawString(name, 0, 0);
        g.translate(-x, -y);

        g.translate(-insetX, -insetY);
        g.translate(-shape.x, -shape.y);
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
    }
}