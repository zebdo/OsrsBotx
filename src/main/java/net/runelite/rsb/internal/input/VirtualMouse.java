package net.runelite.rsb.internal.input;

import lombok.extern.slf4j.Slf4j;

import net.runelite.rsb.internal.client_wrapper.RSClient;

import java.applet.Applet;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;


@Slf4j
@SuppressWarnings("removal")
public class VirtualMouse {

    private RSClient proxy;

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

    public final void mouseClicked(MouseEvent e) {
        clientX = e.getX();
        clientY = e.getY();
    }

    public final void mouseDragged(MouseEvent e) {
        clientX = e.getX();
        clientY = e.getY();
    }

    public final void mouseEntered(MouseEvent e) {
        clientPresent = true;
        clientX = e.getX();
        clientY = e.getY();
    }

    public final void mouseExited(MouseEvent e) {
        clientPresent = false;
        clientX = e.getX();
        clientY = e.getY();
    }

    public final void mouseMoved(MouseEvent e) {
        clientX = e.getX();
        clientY = e.getY();
    }

    public final void mousePressed(MouseEvent e) {
        clientPressed = true;
        clientX = e.getX();
        clientY = e.getY();
    }

    public final void mouseReleased(MouseEvent e) {
        clientX = e.getX();
        clientY = e.getY();
        clientPressX = e.getX();
        clientPressY = e.getY();
        clientPressTime = System.currentTimeMillis();
        clientPressed = false;
    }

    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent e) {
        try {
            // huh??? infinite loop!
            mouseWheelMoved(e);
        } catch (AbstractMethodError ame) {
            // it might not be implemented!
        }
        return e;
    }

    public final void sendEvent(MouseEvent e) {
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
