package net.runelite.rsb.methods;

import net.runelite.rsb.script.ScriptManifest;
import net.runelite.rsb.util.ScreenshotUtil;

import java.awt.image.BufferedImage;

/**
 * Bot environment related operations.
 *
 * @author GigiaJ
 */
public class Environment extends MethodProvider {
	public static final int INPUT_MOUSE = 1;
	public static final int INPUT_KEYBOARD = 2;

	public Environment(MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Takes and saves a screenshot.
	 *
	 * @param hideUsername <code>true</code> to cover the player's username; otherwise
	 *                     <code>false</code>
	 */
	public void saveScreenshot(boolean hideUsername) {
		ScreenshotUtil.saveScreenshot(methods.runeLite, hideUsername);
	}

	/**
	 * Takes a screenshot.
	 *
	 * @param hideUsername <code>true</code> to cover the player's username; otherwise
	 *                     <code>false</code>
	 * @return The screen capture image.
	 */
	public BufferedImage takeScreenshot(boolean hideUsername) {
		return ScreenshotUtil.takeScreenshot(methods.runeLite, hideUsername);
	}

}
