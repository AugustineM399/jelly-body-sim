import javax.swing.JFrame;
import java.awt.event.*;
import java.util.ArrayList;
/**
 * 06/08/2024
 * The JFrame containing the JPanel that makes all of the graphics for the game.
 * @author moormonkey
 */
public class GraphicsFrame extends JFrame {
    GraphicsPanel panel;

    /**
     * Creates the window inside which the graphics are contained.
     */
    GraphicsFrame(ArrayList<Softbody> s) {
        panel = new GraphicsPanel(s, this);
        this.add(panel);
        this.setTitle("JellySim");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                panel.repaint();
            }
        });
    }

    /**
     * @return the GraphicsPanel contained within this GraphicsFrame.
     */
    public GraphicsPanel getPanel() {
        return panel;
    }
}