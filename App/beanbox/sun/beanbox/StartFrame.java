
/**
 * Show a welcome message while BeanBox starts.
 */

package sun.beanbox;

import javax.swing.*;
import java.awt.*;

public class StartFrame extends JFrame {
    private static int width = 250;

    public StartFrame() {
        super("BeanBox");
        setBackground(Color.lightGray);

        JLabel l = new JLabel("Loading and analyzing jars...", JLabel.CENTER);
        add(l);
        l.setBounds(0, 35, width, 20);

        setBounds(200, 200, width, 70);
        setVisible(true);
    }
}
