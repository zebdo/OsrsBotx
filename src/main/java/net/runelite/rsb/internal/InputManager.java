package net.runelite.rsb.internal;

import lombok.extern.slf4j.Slf4j;
import net.runelite.rsb.botLauncher.BotLite;
import net.runelite.api.Client;
import net.runelite.rsb.internal.input.Canvas;

import java.applet.Applet;
import java.awt.event.*;

@Slf4j
@SuppressWarnings("removal")
public class InputManager {

	private final java.util.Random random = new java.util.Random();
	private final MouseHandler mouseHandler = new MouseHandler(this);
	private final BotLite bot;

	private byte dragLength = 0;

	public InputManager(BotLite bot) {
		this.bot = bot;
	}

	/**
	 * Determines if a coordinate is valid on the game canvas.
	 * @param x		the x coordinate
	 * @param y		the y coordinate
	 * @return		<code>true</code> if the coordinate is valid on the game canvas otherwise <code>false</code>
	 */
	private boolean isOnCanvas(final int x, final int y) {
		return x >= 0 && x < bot.getCanvas().getWidth() && y >= 0 && y < bot.getCanvas().getHeight();
	}

	/**
	 * Click the mouse at the current position.
	 * @param left	whether to click the left mouse button or not
	 */
	public void clickMouse(final boolean left) {
		if (!bot.getMethodContext().mouse.isPresent()) {
			return; // Can't click off the canvas
		}
		pressMouse(getX(), getY(), left);
		sleepNoException(random(50, 100));
		releaseMouse(getX(), getY(), left);
	}

	/**
	 * Drag the mouse from the current position to a certain other position.
	 *
	 * @param x the x coordinate to drag to
	 * @param y the y coordinate to drag to
	 */
	public void dragMouse(final int x, final int y) {
		pressMouse(getX(), getY(), true);
		sleepNoException(random(300, 500));

		// XXX this wasn't using naturalmouse, but now is
		windMouse(x, y);
		sleepNoException(random(300, 500));
		releaseMouse(x, y, true);
	}

	@SuppressWarnings("unused")
	private void gainFocus() {
		final Canvas cw = getCanvasWrapper();
		if (!cw.hasFocus()) {
			cw.setFocused(true);
		}
	}

	private Canvas getCanvasWrapper() {
		return (Canvas) getTarget().getComponent(0);
	}

	private Client getClient() {
		return bot.getClient();
	}

	private char getKeyChar(final char c) {
		if ((c >= 36) && (c <= 40)) {
			return KeyEvent.VK_UNDEFINED;
		} else {
			return c;
		}
	}

	private Applet getTarget() {
		return (Applet) getClient();
	}

	public int getX() {
		return bot.getMethodContext().virtualMouse.getClientX();//getClient().getMouseCanvasPosition().getX();
	}

	public int getY() {
		return bot.getMethodContext().virtualMouse.getClientY();//getClient().getMouseCanvasPosition().getY();
	}

	public void holdKey(final int keyCode, final int ms) {
		KeyEvent ke;
		ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, (char) keyCode);
		bot.getMethodContext().virtualKeyboard.sendEvent(ke);

		if (ms > 500) {
			ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + 500, 0, keyCode,
					(char) keyCode);
			bot.getMethodContext().virtualKeyboard.sendEvent(ke);
			final int ms2 = ms - 500;
			for (int i = 37; i < ms2; i += random(20, 40)) {
				ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + i + 500, 0, keyCode,
						(char) keyCode);
				bot.getMethodContext().virtualKeyboard.sendEvent(ke);
			}
		}
		final int delay2 = ms + random(-30, 30);
		ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + delay2, 0, keyCode,
				(char) keyCode);
		bot.getMethodContext().virtualKeyboard.sendEvent(ke);
	}

	public void hopMouse(final int x, final int y) {
		moveMouse(x, y);
	}

	@SuppressWarnings("unused")
	private void loseFocus() {
		final Canvas cw = getCanvasWrapper();
		if (cw.hasFocus()) {
			cw.setFocused(false);
		}
	}


	private void moveMouse(final int x, final int y) {
		long curTime = System.currentTimeMillis();

		// Firstly invoke drag events
		if (bot.getMethodContext().mouse.isPressed()) {
			final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_DRAGGED, curTime, 0, x, y, 0, false);

			bot.getMethodContext().virtualMouse.sendEvent(me);
			if ((dragLength & 0xFF) != 0xFF) {
				dragLength++;
			}
		}

		if (!bot.getMethodContext().mouse.isPresent()) {
			if (isOnCanvas(x, y)) { // Entered
				final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_ENTERED, curTime, 0, x, y, 0, false);
				bot.getMethodContext().virtualMouse.sendEvent(me);
			} else {
				final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_MOVED, curTime, 0, x, y, 0, false);
				bot.getMethodContext().virtualMouse.sendEvent(me);
			}
		} else if (!isOnCanvas(x, y)) {
			final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_EXITED, curTime, 0, x, y, 0, false);
			bot.getMethodContext().virtualMouse.sendEvent(me);

		} else if (!bot.getMethodContext().mouse.isPressed()) {
			final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_MOVED, curTime, 0, x, y, 0, false);
			bot.getMethodContext().virtualMouse.sendEvent(me);
		}
	}

	public void pressKey(final char ch) {
		KeyEvent ke;
		ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, ch, getKeyChar(ch));
		bot.getMethodContext().virtualKeyboard.sendEvent(ke);
	}

	private void pressMouse(final int x, final int y, final boolean left) {
		if (bot.getMethodContext().mouse.isPressed() || !bot.getMethodContext().mouse.isPresent()) {
			return;
		}
		final MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, x, y,
				1, false, left ? MouseEvent.BUTTON1 : MouseEvent.BUTTON3);
		bot.getMethodContext().virtualMouse.sendEvent(me);
	}

	public int random(final int min, final int max) {
		final int n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random.nextInt(n));
	}

	public void releaseKey(final char ch) {
		KeyEvent ke;
		ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), InputEvent.ALT_DOWN_MASK, ch,
				getKeyChar(ch));
		bot.getMethodContext().virtualKeyboard.sendEvent(ke);
	}

	private void releaseMouse(final int x, final int y, final boolean leftClick) {
		if (!bot.getMethodContext().mouse.isPressed()) {
			return;
		}
		MouseEvent me = new MouseEvent(getTarget(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, x, y, 1,
				false, leftClick ? MouseEvent.BUTTON1 : MouseEvent.BUTTON3);
		bot.getMethodContext().virtualMouse.sendEvent(me);

		if ((dragLength & 0xFF) <= 3) {
			me = new MouseEvent(getTarget(), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, x, y, 1, false,
					leftClick ? MouseEvent.BUTTON1 : MouseEvent.BUTTON3);
			bot.getMethodContext().virtualMouse.sendEvent(me);
		}
		// reset
		dragLength = 0;
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
			bot.getMethodContext().virtualKeyboard.sendEvent(ke);
			final int delay2 = random(50, 120) + random(0, 100);
			ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + delay2, 0, code,
					getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
			bot.getMethodContext().virtualKeyboard.sendEvent(ke);
		} else {
			if (!shift) {
				ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + delay, 0, code,
						getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
				bot.getMethodContext().virtualKeyboard.sendEvent(ke);
				// Event Typed
				ke = new KeyEvent(getTarget(), KeyEvent.KEY_TYPED, System.currentTimeMillis() + 0, 0, 0, ch, 0);
				bot.getMethodContext().virtualKeyboard.sendEvent(ke);
				// Event Released
				final int delay2 = random(50, 120) + random(0, 100);
				ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + delay2, 0, code,
						getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
				bot.getMethodContext().virtualKeyboard.sendEvent(ke);
			} else {
				// Event Pressed for shift key
				final int s1 = random(25, 60) + random(0, 50);
				ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + s1,
						InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SHIFT, (char) KeyEvent.VK_UNDEFINED,
						KeyEvent.KEY_LOCATION_LEFT);
				bot.getMethodContext().virtualKeyboard.sendEvent(ke);

				// Event Pressed for char to send
				ke = new KeyEvent(getTarget(), KeyEvent.KEY_PRESSED, System.currentTimeMillis() + delay,
						InputEvent.SHIFT_DOWN_MASK, code, getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
				bot.getMethodContext().virtualKeyboard.sendEvent(ke);
				// Event Typed for char to send
				ke = new KeyEvent(getTarget(), KeyEvent.KEY_TYPED, System.currentTimeMillis() + 0,
						InputEvent.SHIFT_DOWN_MASK, 0, ch, 0);
				bot.getMethodContext().virtualKeyboard.sendEvent(ke);
				// Event Released for char to send
				final int delay2 = random(50, 120) + random(0, 100);
				ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + delay2,
						InputEvent.SHIFT_DOWN_MASK, code, getKeyChar(ch), KeyEvent.KEY_LOCATION_STANDARD);
				bot.getMethodContext().virtualKeyboard.sendEvent(ke);

				// Event Released for shift key
				final int s2 = random(25, 60) + random(0, 50);
				ke = new KeyEvent(getTarget(), KeyEvent.KEY_RELEASED, System.currentTimeMillis() + s2,
						InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SHIFT, (char) KeyEvent.VK_UNDEFINED,
						KeyEvent.KEY_LOCATION_LEFT);
				bot.getMethodContext().virtualKeyboard.sendEvent(ke);
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

	private boolean LOG_MOUSE = false;
	public void windMouse(final int x, final int y) {
		int beforeX = getX();
		int beforeY = getY();
		long start = System.currentTimeMillis();

		mouseHandler.moveMouse(x, y);
		long end = System.currentTimeMillis();

		if (LOG_MOUSE) {
			log.info(String.format("from %d %d - req %d, %d -> at %d %d (}, in %d msecs",
								   beforeX, beforeY, x, y, getX(), getY(), end-start));
		}
	}

}
