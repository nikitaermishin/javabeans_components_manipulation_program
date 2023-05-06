
/**
 * Pop up a (modal) dialog that talks about the BeanBox.
 */

package sun.beanbox;

import javax.swing.*;
import java.awt.*;
import java.beans.*;

public class AboutDialog extends JDialog {
    private final static String message = "<html><center>Beans Development Kit 1.0 ["+BeanBoxFrame.getVersionID()+"]<br>"
	 + "<br>"
	 + "BeanBox<br>"
	 + "Reginald Adkins, Graham Hamilton, Eduardo Pelegri-Llopart<br>"
	 + "<br>"
	 + "Example Beans<br>"
	 + "Larry Cable, Jerome Dochez, Graham Hamilton, Hans Muller<br>"
	 + "<br>"
	 + "Quality Assurance<br>"
	 + "Stuart Moore<br>"
	 + "<br>"
	 + "Tutorial & Documentation<br>"
	 + "Alden Desoto, Andrew Quinn<br>"
	 + "<br>"
	 + "Fearless Product Marketing<br>"
	 + "Gina Centoni, Onno Kluyt, Frank Rimalovski<br>"
	 + "<center><html>";


    public AboutDialog(JFrame frame) {
		super(frame, "About");
		setMinimumSize(new Dimension(400, 300));
		add(new JLabel(message, JLabel.CENTER), BorderLayout.CENTER);
		setVisible(true);
    }
}
