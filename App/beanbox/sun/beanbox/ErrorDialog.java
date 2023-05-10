
/**
 * Pop up a (modal) error dialog and wait for a user to press "continue".
 */

package sun.beanbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

public class ErrorDialog extends MessageDialog {

    public ErrorDialog(JFrame frame, String message) {
	super(frame, "Error", message);
    }

}
