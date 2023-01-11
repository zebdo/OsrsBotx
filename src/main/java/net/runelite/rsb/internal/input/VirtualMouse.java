package net.runelite.rsb.internal.input;

import lombok.extern.slf4j.Slf4j;

import net.runelite.rsb.internal.client_wrapper.RSClient;

import java.awt.Toolkit;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

@Slf4j
public class VirtualMouse {

    private RSClient proxy;

    private byte dragLength = 0;

    // XXX rename this clientXXX
    private int clientX;
    private int clientY;
    private int clientPressX = -1;
    private int clientPressY = -1;
    private long clientPressTime = -1;
    private boolean clientPresent;
    private boolean clientPressed;
    private boolean clientInFocus;

    public VirtualMouse(RSClient proxy) {
        this.proxy = proxy;
    }

    private java.awt.Canvas getTarget() {
        return proxy.getCanvas();
    }

    public boolean isOnCanvas() {
        return isOnCanvas(clientX, clientY);
    }

    private boolean isOnCanvas(int x, int y) {
        return (clientX >= 0 && clientX < proxy.getCanvasWidth() &&
                clientY >= 0 && clientY < proxy.getCanvasHeight());
    }

    private void checkFocused() {
        if (clientPresent && !proxy.getCanvas().isFocusOwner()) {
            EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eventQueue.postEvent(new FocusEvent(proxy.getCanvas(), FocusEvent.FOCUS_GAINED));
        }
    }

    public int getClientX() {
        return clientX;
    }

    public int getClientY() {
        return clientY;
    }

    public int getClientPressX() {
        return clientPressX;
    }

    public int getClientPressY() {
        return clientPressY;
    }

    public long getClientPressTime() {
        return clientPressTime;
    }

    public boolean isClientPresent() {
        return clientPresent;
    }

    public boolean isClientPressed() {
        return clientPressed;
    }

    public boolean isClientInFocus() {
        return clientInFocus;
    }

    public void pressMouse(final boolean isLeft) {
        if (isClientPressed() || !isClientPresent()) {
            log.info("isPressed(): {}, isPresent(): {}", isClientPressed(), isClientPresent());
            return;
        }

        final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                                             0, clientX, clientY,
                                             1, false, isLeft ? MouseEvent.BUTTON1 : MouseEvent.BUTTON3);
        sendEvent(me);
    }

    public void releaseMouse(final boolean isLeft) {
        if (!isClientPressed()) {
            return;
        }

        MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
                                       0, clientX, clientY,
                                       1, false, isLeft ? MouseEvent.BUTTON1 : MouseEvent.BUTTON3);
        sendEvent(me);

        if ((dragLength & 0xFF) <= 3) {
            me = new MouseEvent(getTarget(), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                                0, clientX, clientY,
                                1, false, isLeft ? MouseEvent.BUTTON1 : MouseEvent.BUTTON3);
            sendEvent(me);
        }

        // reset
        dragLength = 0;
    }

    public void moveMouse(final int x, final int y) {
        // Firstly invoke drag events
        if (isClientPressed()) {
            final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, x, y, 0, false);

            sendEvent(me);
            if ((dragLength & 0xFF) != 0xFF) {
                dragLength++;
            }
        } else {
            long curTime = System.currentTimeMillis();

            // moving on to screen
            if (!isClientPresent() && isOnCanvas(x, y)) {
                final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_ENTERED,
                                                     curTime, 0, x, y, 0, false);
                sendEvent(me);
                return;
            }

            // moving off of screen
            if (isClientPresent() && !isOnCanvas(x, y)) {
                final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_EXITED,
                                                     curTime, 0, x, y, 0, false);
                sendEvent(me);
                return;
            }

            final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_MOVED,
                                                 curTime, 0, x, y, 0, false);
            sendEvent(me);
        }
    }

    private final void mouseClicked(MouseEvent e) {
        clientX = e.getX();
        clientY = e.getY();

        checkFocused();
    }

    private final void mouseDragged(MouseEvent e) {
        clientX = e.getX();
        clientY = e.getY();

        checkFocused();
    }

    private final void mouseEntered(MouseEvent e) {
        clientPresent = true;
        clientX = e.getX();
        clientY = e.getY();

        checkFocused();
    }

    private final void mouseExited(MouseEvent e) {
        clientPresent = false;
        clientX = e.getX();
        clientY = e.getY();

        EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        eventQueue.postEvent(new FocusEvent(proxy.getCanvas(), FocusEvent.FOCUS_LOST));
    }

    private final void mouseMoved(MouseEvent e) {
        clientX = e.getX();
        clientY = e.getY();

        checkFocused();
    }

    private final void mousePressed(MouseEvent e) {
        clientPressed = true;
        clientX = e.getX();
        clientY = e.getY();

        checkFocused();
    }

    private final void mouseReleased(MouseEvent e) {
        clientX = e.getX();
        clientY = e.getY();
        clientPressX = e.getX();
        clientPressY = e.getY();
        clientPressTime = System.currentTimeMillis();
        clientPressed = false;

        checkFocused();
    }

    private MouseWheelEvent mouseWheelMoved(MouseWheelEvent e) {
        try {
            // XXX huh??? infinite loop!
            mouseWheelMoved(e);
        } catch (AbstractMethodError ame) {
            // it might not be implemented!
        }
        return e;
    }

    private final void sendEvent(MouseEvent e) {
        this.clientX = e.getX();
        this.clientY = e.getY();
        try {
            if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                mouseClicked(e);
            } else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                mouseDragged(e);
            } else if (e.getID() == MouseEvent.MOUSE_ENTERED) {
                mouseEntered(e);
            } else if (e.getID() == MouseEvent.MOUSE_EXITED) {
                mouseExited(e);
            } else if (e.getID() == MouseEvent.MOUSE_MOVED) {
                mouseMoved(e);
            } else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                mousePressed(e);
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                mouseReleased(e);
            } else if (e.getID() == MouseEvent.MOUSE_WHEEL) {
                log.debug("mouseWheelMoved Mouse event might not be implemented");
                // try {
                //     mouseWheelMoved((MouseWheelEvent) e);
                // } catch (AbstractMethodError ignored) {
                //     log.debug("Mouse event might not be implemented", ignored);
                //     // !
                // }
            } else {
                throw new InternalError(e.toString());
            }

            proxy.getCanvas().dispatchEvent(e);

        } catch (NullPointerException ignored) {
            log.debug("Listener is being re-instantiated on the client", ignored);
        }
    }
}
