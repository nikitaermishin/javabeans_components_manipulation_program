/**
 * A BeanBoxFrame acts as the top-level "frame" that contains a BeanBox.
 * <p>
 * The BeanBoxFrame manages the frame's menubar and keeps track of which
 * bean currently has the focus.
 * <p>
 * Note that there is an assumption that there is only one BeanBoxFrame
 * per application, so various method/fields are static.
 */

package sun.beanbox;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Beans;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

public class BeanBoxFrame extends JFrame implements LayoutManager, Runnable,
        ActionListener, PropertyChangeListener {

    private static String tmpDir = "tmp";
    private static boolean doShowTimes = false;
    private static Thread focusThread;
    private static Component nextFocus;
    private static BeanBoxFrame instance;
    private static String clipDir = "tmp";
    private static String clipFile = "beanBoxClip.ser";
    private static String clipResource = "beanBoxClip";
    private static String versionID = "BDK1.0 - July 1998";
    private static String clipLabel;
    private static String clipName;
    private static boolean clipFromPrototypeInfo;
    private static boolean quickStart = false;
    private static boolean defineOnDemand = true;
    private static BeanBox topBox;
    private static Wrapper topWrapper;
    private static Wrapper currentFocus;
    private static PropertySheet propertySheet;
    private static ToolBox toolBox;
    private static boolean hideInvisibleBeans;

    /**
     * Create a new BeanBoxFrame at a default screen location.
     */

    public BeanBoxFrame() {

        super("BeanBox");

        // there should only be one instance of this class.
        if (instance != null) {
            throw new Error("Attempt to create multiple instances of BeanBoxFrame");
        }
        instance = this;

        setLayout(null);
        setBackground(Color.lightGray);
        setJMenuBar(new JMenuBar());
        //MenuBar(new MenuBar());

        // Timing note: the setFont causes AWT to initialiie itself,
        // so a large chunk of time happens here.
        setFont(new Font("Dialog", Font.PLAIN, 10));

        Timer tim = new Timer();
        toolBox = new ToolBox(20, 20);
        add(toolBox);
        toolBox.setVisible(true);
        toolBox.toFront();

        if (showTimes()) {
            System.err.println("new Toolbox => " + tim.elapsed());
            tim.reset();
        }

        topBox = new BeanBox();
        topBox.setBackground(Color.lightGray);
        topBox.getBeanContextProxy().addPropertyChangeListener("designMode", this);
        topWrapper = new Wrapper(topBox, "BeanBox", "sun.beanbox.BeanBox");
        topWrapper.setBackground(Color.lightGray);
        add(topWrapper);

        if (showTimes()) {
            System.err.println("new BeanBox => " + tim.elapsed());
            tim.reset();
        }

        doSetCurrentFocus(topWrapper);
        topBox.setSize(100, 100);
        setLayout(this);

        setBounds(170, 20, 1400, 850);
        new WindowCloser(this, true);
        setVisible(true);

        propertySheet = new PropertySheet(topWrapper, 575, 20);
        new WindowCloser(propertySheet, true);
        //add(propertySheet);
        //propertySheet.setVisible(true);
        //propertySheet.toFront();


        // Create a thread to handle focus changes.
        focusThread = new Thread(this);
        focusThread.start();
    }

    public static void main(String[] argv) {

        try {
            Class.forName("java.beans.beancontext.BeanContextSupport");
        } catch (Exception e) {
            System.err.println("This version of the beanbox requires Java 2 SDK or later.");
            System.err.println("You can download Java 2 SDK from http://java.sun.com/products/jdk/1.2");
            System.err.println("Exiting...");
            System.exit(-1);
        }

        Timer tim = new Timer();

        if (argv.length > 1) {
            throw new Error("Bad args");
        }
        for (String s : argv) {
            if (s.equals("-version")) {
                System.out.println("BeanBox version: " + versionID);
                System.exit(0);
            } else if (s.equals("-quick")) {
                System.err.println("quick starting...");
                quickStart = true;
            } else { // undocumented?
                tmpDir = argv[0];
            }
        }
        Beans.setDesignTime(true);

        // Popup a "we're starting" frame.
        Timer tim2 = new Timer();
        JFrame starter = new StartFrame();

        if (showTimes()) {
            System.err.println("awt startup => " + tim2.elapsed());
        }

        // Make sure the temporary directory exists and is writeable.
        String absTmpDir = (new java.io.File(tmpDir)).getAbsolutePath();
        java.io.File tmp = new java.io.File(absTmpDir);
        try {
            tmp.mkdirs();
        } catch (Exception ex) {
        }
        if (!tmp.isDirectory()) {
            new ErrorDialog(starter, "Couldn't create temporary directory\n\"" + tmp + "\"");
            System.exit(3);
        }
        if (!tmp.canWrite()) {
            new ErrorDialog(starter, "No write access to temporary directory\n\"" + tmp + "\"");
            System.exit(3);
        }

        // Create the main BeanBox frames.
        new BeanBoxFrame();

        if (showTimes()) {
            System.err.println("total startup time => " + tim.elapsed());
        }

        starter.dispose();
    }

    static Object getCurrentBean() {
        return currentFocus.getBean();
    }

    static Component getCurrentComponent() {
        return currentFocus.getChild();
    }

    static void setCurrentComponent(Component focus) {
        // Null means focus on the top-level beanbox.
        if (focus == null) {
            focus = topWrapper;
        }
        // Wakeup our internal thread to do the focus change.
        synchronized (instance) {
            nextFocus = focus;
            instance.notifyAll();
        }
    }

    static Wrapper getCurrentWrapper() {
        return currentFocus;
    }

    static BeanBox getCurrentBeanBox() {
        Component c = getCurrentComponent();
        for (; ; ) {
            if (c == null) {
                System.err.println("No BeanBox in focus???");
                return null;
            }
            if (c instanceof BeanBox) {
                return (BeanBox) c;
            }
            c = c.getParent();
        }
    }

    public static String getClipDir() {
        return clipDir;
    }

    public static String getClipFileName() {
        if (clipDir != null) {
            return clipDir + java.io.File.separator + clipFile;
        } else {
            return clipFile;
        }
    }

    // This method is currently unused
    public static String getClipResource() {
        return clipResource;
    }

    public static String getTmpDir() {
        return tmpDir;
    }

    public static String getClipName() {
        return clipName;
    }

    public static void setClipName(String name) {
        clipName = name;
    }

    public static boolean getClipFromPrototypeInfo() {
        return clipFromPrototypeInfo;
    }

    // Added the following getter/setters.
    // We need to propogate the fromPrototype info across copies and pastes
    // since beans originating from .ser files ("prototypes") must be
    // effectivley treated as having hidden-state at code generation time.
    // If we didn't propogate this info, we would have to generate
    // initialization statements for all properties, not just those modified
    // by the user at code generation time.

    public static void setClipFromPrototypeInfo(boolean prototypeInfo) {
        clipFromPrototypeInfo = prototypeInfo;
    }

    public static String getClipLabel() {
        return clipLabel;
    }

    public static void setClipLabel(String label) {
        clipLabel = label;
    }

    public static boolean getQuickStart() {
        return quickStart;
    }

    public static boolean getDefineOnDemand() {
        return defineOnDemand;
    }

    public static BeanBox getTopBox() {
        return topBox;
    }

    //----------------------------------------------------------------------
    // Layout related stuff.
    //----------------------------------------------------------------------

    public static Wrapper getTopWrapper() {
        return topWrapper;
    }

    public static ToolBox getToolBox() {
        return toolBox;
    }

    public static boolean showTimes() {
        return doShowTimes;
    }

    public static boolean getHideInvisibleBeans() {
        return hideInvisibleBeans;
    }

    public static void setHideInvisibleBeans(boolean hide) {
        hideInvisibleBeans = hide;
        Wrapper.showInvisibleBeans(!hide);
    }

    public static void setDesignMode(boolean designMode) {
        Beans.setDesignTime(designMode);
        if (designMode) {
            toolBox.setVisible(true);
            propertySheet.setVisible(true);
        } else {
            toolBox.setVisible(false);
            propertySheet.setVisible(false);
        }
    }

    public static String getVersionID() {
        return versionID;
    }

    private boolean inBeanBox(Component c) {
        while (c != null) {
            if (c instanceof BeanBox) {
                return true;
            }
            c = c.getParent();
        }
        return false;
    }

    public void actionPerformed(ActionEvent evt) {
        // Menu actions come here,
        // We forward them to the current BeanBox.
        getCurrentBeanBox().queueMenuItem(evt);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("designMode")) {
            setDesignMode((Boolean) evt.getNewValue());
        }
    }

    // Internal thread to handle focus changes.  We use a separate
    // thread for this to offload work from the event thread in order
    // to avoid deadlocks.
    public void run() {
        for (; ; ) {
            Component bean;
            synchronized (this) {
                while (nextFocus == null) {
                    try {
                        wait();
                    } catch (Exception ex) {
                        System.err.println("Unexpected interrupt in focus thread");
                    }
                }
                bean = nextFocus;
                nextFocus = null;
            }
            doSetCurrentFocus(bean);
        }
    }

    private void doSetCurrentFocus(Component focus) {
        Wrapper target = Wrapper.findWrapper(focus);

        if (target == currentFocus) {
            return;
        }

        // Deactivate the old Wrapper.
        if (currentFocus != null) {
            currentFocus.deactivate();
        }

        currentFocus = target;

        // Activate the new Wrapper.
        currentFocus.activate();

        // Find the nearest surrounding BeanBox and use its menubar.
        for (Component c = target.getChild(); c != null; c = c.getParent()) {
            if (c instanceof BeanBox) {
                BeanBox hdr = (BeanBox) c;
                hdr.updateMenuBar(getJMenuBar());
                break;
            }
        }

        // Point the property sheet at the new focus.
        if (propertySheet != null) {
            propertySheet.setTarget(getCurrentWrapper());
        }
    }

    public void setCustomizer(Customizer c) {
        if (propertySheet != null) {
            propertySheet.setCustomizer(c);
        }
    }

    //----------------------------------------------------------------------

    public void dispose() {
        System.exit(0);
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(400, 300);
    }

    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(100, 100);
    }

    public void layoutContainer(Container parent) {
        Dimension d = parent.getSize();
        Insets ins = parent.getInsets();
        int width = d.width - (ins.left + ins.right);
        int height = d.height - (ins.top + ins.bottom);
        topWrapper.setBounds(ins.left, ins.top, width, height);
        topWrapper.invalidate();
        topWrapper.doLayout();
    }
}
