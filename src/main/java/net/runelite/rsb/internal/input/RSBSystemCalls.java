package net.runelite.rsb.internal.input;

import com.github.joonasvali.naturalmouse.api.SystemCalls;
import net.runelite.rsb.internal.input.InputManager;

import java.awt.Toolkit;
import java.awt.Dimension;

public class RSBSystemCalls implements SystemCalls {
    InputManager inputManager;
    public RSBSystemCalls(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public void sleep(long time) throws InterruptedException {
        Thread.sleep(time);
    }

    @Override
    public Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    /**
     * Moves the mouse to specified pixel using the provided Robot.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    @Override
    public void setMousePosition(int x, int y) {
        inputManager.getVirtualMouse().moveMouse(x, y);
    }
}
