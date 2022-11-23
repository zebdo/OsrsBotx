package net.runelite.rsb.methods;

/**
 * Keyboard related operations.
 */
public class Keyboard {

	private MethodContext ctx;
	Keyboard(final MethodContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * Presses and releases a given key.
	 *
	 * @param c The character to press.
	 */
	public void sendKey(final char c) {
		ctx.inputManager.sendKey(c);
	}

	/**
	 * Types a given string.
	 *
	 * @param text       The text to press/send.
	 * @param pressEnter <code>true</code> to press enter after pressing the text.
	 */
	public void sendText(final String text, final boolean pressEnter) {
		ctx.inputManager.sendKeys(text, pressEnter);
	}

	/**
	 * Types a given string instantly.
	 *
	 * @param text       The text to press/send.
	 * @param pressEnter <code>true</code> to press enter after pressing the text.
	 */
	public void sendTextInstant(final String text, final boolean pressEnter) {
		ctx.inputManager.sendKeysInstant(text, pressEnter);
	}

	/**
	 * Presses and holds a given key.
	 *
	 * @param c The character to press.
	 * @see #releaseKey(char)
	 */
	public void pressKey(final char c) {
		ctx.inputManager.pressKey(c);
	}

	/**
	 * Releases a given held key.
	 *
	 * @param c The character to release.
	 * @see #pressKey(char)
	 */
	public void releaseKey(final char c) {
		ctx.inputManager.releaseKey(c);
	}
}
