package sunw.demo.transitional;

/**
 * This interface describes the method that gets called when
 * an OurButton gets pushed.
 */

public interface ButtonPushListener extends java.util.EventListener {

    public void push(ButtonPushEvent e);

}