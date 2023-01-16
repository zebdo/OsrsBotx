package net.runelite.rsb.internal.input;

import lombok.extern.slf4j.Slf4j;

import net.runelite.client.input.KeyListener;
import net.runelite.rsb.internal.client_wrapper.RSClient;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;


@Slf4j
@SuppressWarnings("removal")
public class VirtualKeyboard implements KeyListener {

    private RSClient proxy;

    public VirtualKeyboard(RSClient proxy) {
        this.proxy = proxy;
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void sendEvent(KeyEvent e) {

        if (e.getID() == KeyEvent.KEY_TYPED) {
            keyTyped(e);
        }

        if (e.getID() == KeyEvent.KEY_PRESSED) {
            keyPressed(e);
        }

        if (e.getID() == KeyEvent.KEY_RELEASED) {
            keyReleased(e);
        }

        if (!proxy.getCanvas().isFocusOwner()) {
            EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eventQueue.postEvent(new FocusEvent(proxy.getCanvas(), FocusEvent.FOCUS_GAINED));
        }

        proxy.getCanvas().dispatchEvent(e);
    }

}


