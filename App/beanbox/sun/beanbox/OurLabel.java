package sun.beanbox;

/**
 * OurLabel draws a string to reprensent an invisible bean.
 * <p>
 * It differs from java.awt.Label in that it forwards mice events
 * up to its container, which makes it friendlier in the BeanBox.
 */

import javax.swing.*;
import java.awt.*;

public class OurLabel extends JComponent {

    public OurLabel(String label) {
        this.label = label;
        Font f = new Font("Helvetica", Font.PLAIN, 10);
        setFont(f);
    }

    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        baseline = fm.getMaxAscent() + 2;
        int height = baseline + fm.getMaxDescent() + 2;
        int width = fm.stringWidth(label) + 17;
        return new Dimension(width, height);
    }

    public void paint(Graphics g) {
        g.drawString(label, 3, baseline);
    }

    String label;
    int baseline;
}
