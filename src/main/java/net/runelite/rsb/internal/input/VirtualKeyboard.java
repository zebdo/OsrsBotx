package net.runelite.rsb.internal.input;

import lombok.extern.slf4j.Slf4j;

import net.runelite.rsb.internal.client_wrapper.RSClient;

import java.awt.Toolkit;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.FocusEvent;

@Slf4j
public class VirtualKeyboard {
    private RSClient proxy;

    public VirtualKeyboard(RSClient proxy) {
        this.proxy = proxy;
    }

    public void sendEvent(KeyEvent e) {
        if (!proxy.getCanvas().isFocusOwner()) {
            EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eventQueue.postEvent(new FocusEvent(proxy.getCanvas(),
                                                FocusEvent.FOCUS_GAINED));
        }

        proxy.getCanvas().dispatchEvent(e);
    }

}


