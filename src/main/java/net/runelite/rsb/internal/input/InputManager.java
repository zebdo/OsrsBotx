package net.runelite.rsb.internal.input;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

import java.applet.Applet;
import java.awt.event.*;
import java.awt.Canvas;

import net.runelite.rsb.internal.client_wrapper.RSClient;


@Slf4j
@SuppressWarnings("removal")
public class InputManager {
    private final java.util.Random random = new java.util.Random();
    private final MouseHandler mouseMovementManager = new MouseHandler(this);

    private VirtualMouse virtualMouse;
    private VirtualKeyboard virtualKeyboard;
    private RSClient proxy;

    // ZZZ shouldnt be here?
    private boolean LOG_MOUSE = false;

    public InputManager(RSClient proxy) {
        this.proxy = proxy;
        this.virtualMouse = new VirtualMouse(proxy);
        this.virtualKeyboard = new VirtualKeyboard(proxy);
    }

    public VirtualMouse getVirtualMouse() {
        return virtualMouse;
    }

    public VirtualKeyboard getVirtualKeyboard() {
        return virtualKeyboard;
    }

    public int getX() {
        return virtualMouse.getClientX();
    }

    public int getY() {
        return virtualMouse.getClientY();
    }

    public void pressMouse(final boolean left) {
        virtualMouse.pressMouse(left);
    }

    public void releaseMouse(final boolean left) {
        virtualMouse.releaseMouse(left);
    }

    public void clickMouse(final boolean left) {
        if (!virtualMouse.isClientPresent()) {
            log.warn("clickMouse not present x:{} y:{} - onCanvas: {}",
                     virtualMouse.getClientX(),
                     virtualMouse.getClientY(),
                     virtualMouse.isOnCanvas());
            return; // Can't click off the canvas
        }

        virtualMouse.pressMouse(left);
        // XXX this needs to be better
        sleepNoException(random(50, 100));

        virtualMouse.releaseMouse(left);
    }

    public void hopMouse(final int x, final int y) {
        // this should very much be discouraged
        virtualMouse.moveMouse(x, y);
    }

    public void windMouse(final int x, final int y) {
        int beforeX = getX();
        int beforeY = getY();

        long start = System.currentTimeMillis();
        mouseMovementManager.moveMouse(x, y);
        long end = System.currentTimeMillis();

        if (LOG_MOUSE) {
            log.info(String.format("from %d %d -> at %d %d in %d msecs",
                                   beforeX, beforeY, getX(), getY(), end - start));

            if (!virtualMouse.isOnCanvas()) {
                log.info(String.format("ZZZ off CANVAS: %d %d",
                                       proxy.getCanvasWidth(),
                                       proxy.getCanvasHeight()));
            }
        }
    }









    // key stuff:

    private java.awt.Canvas getTarget() {
        return proxy.getCanvas();
    }


    public int random(final int min, final int max) {
        final int n = Math.abs(max - min);
        return Math.min(min, max) + (n == 0 ? 0 : random.nextInt(n));
    }


    private char getKeyChar(final char c) {
        if ((c >= 36) && (c <= 40)) {
            return KeyEvent.VK_UNDEFINED;
        } else {
            return c;
        }
    }


    public void holdKey(final int keyCode, final int ms) {
        KeyEvent ke;
        ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, (char) keyCode);
        virtualKeyboard.sendEvent(ke);

        if (ms > 500) {
            ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + 500, 0, keyCode,
                    (char) keyCode);
            virtualKeyboard.sendEvent(ke);
            final int ms2 = ms - 500;
            for (int i = 37; i < ms2; i += random(20, 40)) {
                ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + i + 500, 0, keyCode,
                        (char) keyCode);
                virtualKeyboard.sendEvent(ke);
            }
        }
        final int delay2 = ms + random(-30, 30);
        ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + delay2, 0, keyCode,
                (char) keyCode);
        virtualKeyboard.sendEvent(ke);
    }

    public void pressKey(final char ch) {
        KeyEvent ke;
        ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, ch, getKeyChar(ch));
        virtualKeyboard.sendEvent(ke);
    }

    public void releaseKey(final char ch) {
        KeyEvent ke;
        ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), InputEvent.ALT_DOWN_MASK, ch,
                getKeyChar(ch));
        virtualKeyboard.sendEvent(ke);
    }

    public void sendKey(final char c) {
        sendKey(c, 0);
    }

    private void sendKey(final char ch, final int delay) {
        boolean shift = false;
        int code = ch;
        if ((ch >= 'a') && (ch <= 'z')) {
            code -= 32;
        } else if ((ch >= 'A') && (ch <= 'Z')) {
            shift = true;
        }
        KeyEvent ke;
        if ((code == KeyEvent.VK_LEFT) || (code == KeyEvent.VK_UP) || (code == KeyEvent.VK_DOWN)) {
            ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + delay, 0, code,
                    getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
            virtualKeyboard.sendEvent(ke);
            final int delay2 = random(50, 120) + random(0, 100);
            ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + delay2, 0, code,
                    getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
            virtualKeyboard.sendEvent(ke);
        } else {
            if (!shift) {
                ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + delay, 0, code,
                        getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
                virtualKeyboard.sendEvent(ke);
                // Event Typed
                ke = new KeyEvent(getTarget(), KeyEvent.KEY_TYPED, System.currentTimeMillis() + 0, 0, 0, ch, 0);
                virtualKeyboard.sendEvent(ke);
                // Event Released
                final int delay2 = random(50, 120) + random(0, 100);
                ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + delay2, 0, code,
                        getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
                virtualKeyboard.sendEvent(ke);
            } else {
                // Event Pressed for shift key
                final int s1 = random(25, 60) + random(0, 50);
                ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + s1,
                        InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SHIFT, (char) KeyEvent.VK_UNDEFINED,
                        KeyEvent.KEY_LOCATION_LEFT);
                virtualKeyboard.sendEvent(ke);

                // Event Pressed for char to send
                ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + delay,
                        InputEvent.SHIFT_DOWN_MASK, code, getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
                virtualKeyboard.sendEvent(ke);
                // Event Typed for char to send
                ke = new KeyEvent(getTarget(), KeyEvent.KEY_TYPED, System.currentTimeMillis() + 0,
                        InputEvent.SHIFT_DOWN_MASK, 0, ch, 0);
                virtualKeyboard.sendEvent(ke);
                // Event Released for char to send
                final int delay2 = random(50, 120) + random(0, 100);
                ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + delay2,
                        InputEvent.SHIFT_DOWN_MASK, code, getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
                virtualKeyboard.sendEvent(ke);

                // Event Released for shift key
                final int s2 = random(25, 60) + random(0, 50);
                ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + s2,
                        InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SHIFT, (char) KeyEvent.VK_UNDEFINED,
                        KeyEvent.KEY_LOCATION_LEFT);
                virtualKeyboard.sendEvent(ke);
            }
        }
    }

    public void sendKeys(final String text, final boolean pressEnter) {
        sendKeys(text, pressEnter, 50, 100);
    }

    public void sendKeys(final String text, final boolean pressEnter, final int delay) {
        sendKeys(text, pressEnter, delay, delay);
    }

    public void sendKeys(final String text, final boolean pressEnter, final int minDelay, final int maxDelay) {
        final char[] chs = text.toCharArray();
        for (final char element : chs) {
            sendKey(element, random(minDelay, maxDelay));
            sleepNoException(random(minDelay, maxDelay));
        }
        if (pressEnter) {
            sendKey((char) KeyEvent.VK_ENTER, random(minDelay, maxDelay));
        }
    }

    public void sendKeysInstant(final String text, final boolean pressEnter) {
        for (final char c : text.toCharArray()) {
            sendKey(c, 0);
        }
        if (pressEnter) {
            sendKey((char) KeyEvent.VK_ENTER, 0);
        }
    }

    public void sleepNoException(final long t) {
        try {
            Thread.sleep(t);
        } catch (final Exception ignored) {
            log.debug("Sleep exception in input manager", ignored);
        }
    }
}
