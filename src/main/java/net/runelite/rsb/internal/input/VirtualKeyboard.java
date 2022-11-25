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

		// why focus gained on each keystroke?
        EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();

		// XXX this is actually mental
        eventQueue.postEvent(new FocusEvent(proxy.getCanvas(), FocusEvent.FOCUS_GAINED));
        eventQueue.postEvent(e);
    }

}


