package net.runelite.rsb.script;

import lombok.extern.slf4j.Slf4j;
import net.runelite.rsb.botLauncher.BotLite;
import net.runelite.rsb.methods.MethodContext;
import net.runelite.rsb.wrappers.RSPlayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class Script implements Runnable {

	protected MethodContext ctx;

	private volatile boolean running = false;
	private volatile boolean paused = false;

	private int id = -1;

	/**
	 * Finalized to cause errors intentionally to avoid confusion
	 * (yea I know how to deal with these script writers ;)).
	 *
	 * @param map The arguments passed in from the description.
	 * @return <code>true</code> if the script can start.
	 * @deprecated Use {@link #onStart()} instead.
	 */
	@Deprecated
	public final boolean onStart(Map<String, String> map) {
		return true;
	}

	/**
	 * Called before loop() is first called, after this script has
	 * been initialized with all method providers. Override to
	 * perform any initialization or prevent script start.
	 *
	 * @return <code>true</code> if the script can start.
	 */
	public boolean onStart() {
		return true;
	}

	/**
	 * The main loop. Called if you return true from onStart, then continuously until
	 * a negative integer is returned or the script stopped externally. When this script
	 * is paused this method will not be called until the script is resumed. Avoid causing
	 * execution to pause using sleep() within this method in favor of returning the number
	 * of milliseconds to sleep. This ensures that pausing and anti-randoms perform normally.
	 *
	 * @return The number of milliseconds that the manager should sleep before
	 *         calling it again. Returning a negative number will deactivate the script.
	 */
	public abstract int loop();

	/**
	 * Override to perform any clean up on script stopScript.
	 */
	public void onFinish() {

	}

	public final void init(MethodContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * For internal use only. Deactivates this script if
	 * the appropriate id is provided.
	 *
	 * @param id The id from ScriptHandler.
	 */
	public final void deactivate(int id) {
		if (id != this.id) {
			throw new IllegalStateException("Invalid id!");
		}
		this.running = false;
	}

	/**
	 * For internal use only. Sets the pool id of this script.
	 *
	 * @param id The id from ScriptHandler.
	 */
	public final void setID(int id) {
		if (this.id != -1) {
			throw new IllegalStateException("Already added to pool!");
		}
		this.id = id;
	}

	/**
	 * Pauses/resumes this script.
	 *
	 * @param paused <code>true</code> to pause; <code>false</code> to resume.
	 */
	public final void setPaused(boolean paused) {
		if (running) {
			this.paused = paused;
		}
	}

	/**
	 * Returns whether or not this script is paused.
	 *
	 * @return <code>true</code> if paused; otherwise <code>false</code>.
	 */
	public final boolean isPaused() {
		return paused;
	}

	/**
	 * Returns whether or not this script has started and not stopped.
	 *
	 * @return <code>true</code> if running; otherwise <code>false</code>.
	 */
	public final boolean isRunning() {
		return running;
	}

	/**
	 * Returns whether or not the loop of this script is able to
	 * receive control (i.e. not paused, stopped or in random).
	 *
	 * @return <code>true</code> if active; otherwise <code>false</code>.
	 */
	public final boolean isActive() {
		return running && !paused;
	}

	/**
	 * Stops the current script without logging out.
	 */
	public void stopScript() {
		stopScript(false);
	}

	/**
	 * Stops the current script; player can be logged out before
	 * the script is stopped.
	 *
	 * @param logout <code>true</code> if the player should be logged
	 *               out before the script is stopped.
	 */
	public void stopScript(boolean logout) {
		log.info("Script stopping...");
		if (logout) {
			if (ctx.bank.isOpen()) {
				ctx.bank.close();
			}
			if (ctx.game.isLoggedIn()) {
				ctx.game.logout();
			}
		}
		this.running = false;
	}

	public final void run() {
		boolean start = false;
		try {
			start = onStart();
		} catch (ThreadDeath ignored) {
			log.error("Thread died", ignored);
		} catch (Throwable ex) {
			log.error("Error starting script: ", ex);
		}
		if (start) {
			running = true;
			log.info("Script started.");
			try {
				while (running) {
					if (!paused) {
						int timeOut = -1;
						try {
							timeOut = loop();
						} catch (ThreadDeath td) {
							break;
						} catch (Exception ex) {
							log.warn("Uncaught exception from script: ", ex);
						}
						if (timeOut == -1) {
							break;
						}
						try {
							sleep(timeOut);
						} catch (ThreadDeath td) {
							break;
						}
					} else {
						try {
							sleep(1000);
						} catch (ThreadDeath td) {
							break;
						}
					}
				}
				try {
					onFinish();
				} catch (ThreadDeath ignored) {
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			} catch (Throwable t) {
				log.error("Throwable: ", t);
				onFinish();
			}
			running = false;
			log.info("Script stopped.");
		} else {
			log.error("Failed to start up.");
		}

		ctx.mouse.moveOffScreen();
		ctx.runeLite.getScriptHandler().stopScript(id);
		id = -1;
	}

	protected int random(int minValue, int maxValue) {
		return ctx.random(minValue, maxValue);
	}

	protected void sleep(int msecs) {
		ctx.sleep(msecs);
	}

	protected RSPlayer getMyPlayer() {
		return ctx.players.getMyPlayer();
	}
}
