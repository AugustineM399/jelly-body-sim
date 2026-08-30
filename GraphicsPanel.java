import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.time.LocalDateTime;
import java.util.ArrayList;
/**
 * 06/08/2024
 * The JPanel that makes all of the graphics for the game.
 * @author moormonkey
 */
public class GraphicsPanel extends JPanel {
    private int windowHeight;
    private int windowWidth;
    private int cameraX;
    private int cameraY;
    private double zoom;
    private ArrayList<Softbody> softbodies;
    private boolean renderIdealShapes;
    private GraphicsFrame frame;

    /**
     * Creates a new GraphicsPanel, with a reference to a Board object for rendering.
     */
    GraphicsPanel(ArrayList<Softbody> s, GraphicsFrame frame) {
        this.setPreferredSize(new Dimension(1920,1080));
        this.frame = frame;
        windowWidth = this.getSize().width;
        windowHeight = this.getSize().height;
        cameraX = 0;
        cameraY = 0;
        zoom = 3.0;
        softbodies = s;
        renderIdealShapes = false;
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        MyKeyAdapter m = new MyKeyAdapter();
        this.addKeyListener(m);
        m.setPanel(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws the Softbody ArrayList.
     * @param g the Graphics object, onto which the Softbody ArrayList is drawn.
     */
    public void draw(Graphics g) {
        double[] stats = Jelly.stats();
        frame.setTitle("JellySim - Simulating " + softbodies.size() + " softbodies at " + stats[1] + " FPS (" + (stats[1] / 120.0) + "x speed, trying for " + stats[0] + "x). " + (int)stats[2] + " seconds elapsed. Camera at " + cameraX + ", " + cameraY + " and zoomed " + zoom + "x." + (renderIdealShapes ? " Rendering shape-match shapes." : ""));
        windowWidth = this.getSize().width;
        windowHeight = this.getSize().height;
        g.setColor(Color.WHITE);
        for (Softbody s : softbodies) {
            double[][] xy = new double[][] {s.getPointsX(), s.getPointsY()};
            int n = s.getNumPoints();
            int[][] xyDraw = new int[2][n];
            for (int i = 0; i < n; i++) {
                xyDraw[0][i] = (int)((xy[0][i] - cameraX) * zoom + windowWidth / 2);
                xyDraw[1][i] = (int)((-xy[1][i] + cameraY) * zoom + windowHeight / 2);
            }
            g.drawPolygon(xyDraw[0], xyDraw[1], n);
            g.drawOval((int) ((s.getCOM().x - cameraX) * zoom - 1 + windowWidth / 2), (int) ((-s.getCOM().y + cameraY) * zoom - 1 + windowHeight / 2), 2, 2);
            if (renderIdealShapes) {
                xy = s.getShapeMatchXY();
                for (int i = 0; i < n; i++) {
                    xyDraw[0][i] = (int)((xy[0][i] - cameraX) * zoom + windowWidth / 2);
                    xyDraw[1][i] = (int)((xy[1][i] * -1 + cameraY) * zoom + windowHeight / 2);
                }
                g.setColor(Color.GREEN);
                g.drawPolygon(xyDraw[0], xyDraw[1], n);
                g.setColor(Color.WHITE);
            }
        }
    }

    /**
     * Handles keyboard input. Accepted keys:
     * <ul>
     * <li>WASD keys move the camera around.
     * <li>+/- (or really =/-) zooms in/out.
     * <li>I toggles idealShape visualization.
     * <li>J/K/L slows/pauses/fast-forwards the simulation.
     * <li>BACK_SPACE removes the last character from moveSequence.
     * <li>ENTER causes the player to move according to the moveSequence (or prints an error if it doesn't work).
     * <li>ESCAPE ends the game early, without (necessarily) winning, and opens the load file dialog.
     * <li>F12 takes a screenshot of this panel.
     * </ul>
     */
    public class MyKeyAdapter extends KeyAdapter {
        private GraphicsPanel panel;

        public void setPanel(GraphicsPanel p) {
            panel = p;
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                // move camera
                case KeyEvent.VK_W:
                    cameraY += 100 / zoom;
                    break;
                case KeyEvent.VK_A:
                    cameraX -= 100 / zoom;
                    break;
                case KeyEvent.VK_S:
                    cameraY -= 100 / zoom;
                    break;
                case KeyEvent.VK_D:
                    cameraX += 100 / zoom;
                    break;
                // zoom
                case KeyEvent.VK_MINUS:
                    zoom = Math.max(0.01, zoom / 1.2);
                    break;
                case KeyEvent.VK_EQUALS:
                    zoom = Math.min(100, zoom * 1.2);
                    break;
                // idealShape visualization
                case KeyEvent.VK_I:
                    panel.renderIdealShapes = !panel.renderIdealShapes;
                    break;
                // playback speed
                case KeyEvent.VK_J:
                    Jelly.timescale(0.5);
                    break;
                case KeyEvent.VK_K:
                    Jelly.timescale(0.0);
                    break;
                case KeyEvent.VK_L:
                    Jelly.timescale(2.0);
                    break;
                // screenshot
                case KeyEvent.VK_F12:
                    try {
                        BufferedImage im = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        panel.paint(im.getGraphics());
                        String path = LocalDateTime.now().toString();
                        path = path.substring(0,13) + "-" + path.substring(14,16) + "-" + path.substring(17) + ".png";
                        ImageIO.write(im, "PNG", new File(path)); 
                    } catch (Exception error) {
                        error.printStackTrace();
                    }
            }
        }
    }
}
