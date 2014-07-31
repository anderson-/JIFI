package s3f.jifi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import s3f.util.RandomColor;
import s3f.util.splashscreen.SimpleSplashScreen;

/**
 * Present a simple graphic to the user upon launch of the application, to
 * provide a faster initial response than is possible with the main window.
 * 
* <P>
 * Adapted from an <a
 * href=http://developer.java.sun.com/developer/qow/archive/24/index.html>item</a>
 * on Sun's Java Developer Connection.
 * 
* <P>
 * This splash screen appears within about 2.5 seconds on a development machine.
 * The main screen takes about 6.0 seconds to load, so use of a splash screen
 * cuts down the initial display delay by about 55 percent.
 */
public class JIFISplashScreen extends SimpleSplashScreen {

    private boolean animated = false;
    private boolean asd = false;
    private int asd2 = 3;
    private int errorX = 0;
    private int b = 0;
    protected String errorMsg = "An error occurred while attempting to initialize the GUI. The stack trace is available in clipboard while this window is open. Click here to close.";
    private Thread repaintThread;

    public JIFISplashScreen(String aImageId) {
        this(aImageId, true);
    }

    public JIFISplashScreen(String aImageId, boolean animated) {
        super(aImageId);
        this.animated = animated;

        if (animated) {
            repaintThread = new Thread() {
                @Override
                public void run() {
                    while (splashWindow.isVisible()) {
                        splashWindow.repaint();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {

                        }
                    }
                }
            };
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (error) {
                        System.exit(1);
                    } else {
                        asd = !asd;
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    asd2 -= e.getWheelRotation();
                    asd2 = (asd2 <= 0) ? 1 : (asd2 > 4) ? 4 : asd2;
                }
            };
            splashWindow.addMouseListener(mouseAdapter);
            splashWindow.addMouseWheelListener(mouseAdapter);
        }
    }

    @Override
    public void splash() {
        super.splash();
        repaintThread.start();
    }

    @Override
    public boolean paintSplashScreen(Graphics g2) {
        if (fImage != null) {
            g2.drawImage(fImage, 0, 0, splashWindow);
            if (animated) {
                int w;
                switch (asd2) {
                    case 1:
                        w = 10;
                        break;
                    case 2:
                        w = 25;
                        break;
                    case 3:
                        w = 50;
                        break;
                    case 4:
                        w = 125;
                        break;
                    default:
                        w = 10;
                }

                int n = 250 / w - 1;
                if (error) {
                    g2.setColor(Color.red);
                    g2.fillRect(5, splashWindow.getHeight() - 21, 250, 12);
                    g2.setColor(Color.white);
                    g2.drawString(errorMsg, 20 + errorX, splashWindow.getHeight() - 11);
                    g2.setColor(Color.black);
                    g2.fillRect(0, 0, 5, splashWindow.getHeight());
                    g2.fillRect(splashWindow.getWidth() - 5, 0, 5, splashWindow.getHeight());
                    g2.fillRect(0, 0, splashWindow.getWidth(), 5);
                    g2.fillRect(0, splashWindow.getHeight() - 5, splashWindow.getWidth(), 5);
                    if (errorX < -g2.getFontMetrics().stringWidth(errorMsg)) {
                        errorX = splashWindow.getWidth();
                    }
                    errorX -= 5;
                } else {
                    if (!asd) {
                        for (int i = n; i >= 0; i--) {
                            g2.setColor(RandomColor.generate(.5f, .9f));
                            //g2.fillRect(i * w + 5, getHeight() - 15, w, 3);
                            g2.fillRect(i * w + 5, splashWindow.getHeight() - 17, w, 5);
                            //g2.fillRect(i * w + 5, getHeight() - 9, w, 4);
                            //g2.fillRect(i * w + 5, getHeight() - 8, w, 3);
                        }
                    } else {
                        b += 3;
                        for (int i = n; i >= 0; i--) {
                            g2.setColor(RandomColor.generate(.6f, .99f));
                            g2.fillRect(i * w + 5, (int) (50 * Math.sin((i - b) / Math.PI)) + 100, w, 110);
                        }
                        g2.setColor(Color.BLACK);
                        g2.fillRect(0, splashWindow.getHeight() - 5, splashWindow.getWidth(), 5);
                    }
                }
//                    for (int i = 15; i >= 0; i--) {
//                        for (int j = 10; j >= 0; j--) {
//                            g2.setColor(RandomColor.generate(.6f, .9f));
//                            g2.fillRect(i * 30, j * 30, 30, 30);
//                        }
//                    }
                Toolkit.getDefaultToolkit().sync();
            }
        }
        return true;
    }
}
