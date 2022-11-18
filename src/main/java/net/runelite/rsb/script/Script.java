package net.runelite.rsb.script;

import lombok.extern.slf4j.Slf4j;
import net.runelite.rsb.methods.MethodContext;


// XXX detach script from runnable

@Slf4j
public abstract class Script implements Runnable {
	protected MethodContext ctx;

	private volatile boolean running = false;
	private volatile boolean paused = false;

	public abstract boolean onStart();
	public abstract int loop();
	public abstract void onFinish();

	public abstract void onInit();

	public final void init(MethodContext ctx) {
		this.ctx = ctx;
		this.onInit();
	}

	public final void cleanup() {
		ctx.mouse.moveOffScreen();
		// ZZZ pass the script handler in
		ctx.runeLite.getScriptHandler().stopScript();
	}

	/**
	 * For internal use only. Deactivates this script if
	 * the appropriate id is provided.
	 *
	 */
	public final void deactivate() {
		this.running = false;
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
		return this.paused;
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
		this.running = false;
		log.info("Script stopping from within Script...");
		if (logout) {
			if (ctx.game.isLoggedIn()) {
				ctx.game.logout();
			}
		}
	}

	public final void run() {
		boolean start = false;
		try {
			start = onStart();

		} catch (RuntimeException ex) {
			log.error("RuntimeException in Script.onStart() ", ex);
			ex.printStackTrace();
		} catch (Throwable ex) {
			log.warn("Uncaught exception from Script.onStart(): ", ex);
		}

		if (!start) {
			log.error("Failed Script.onStart().");
			cleanup();
			return;
		}

		log.info("Script started.");

		running = true;
		while (running) {
			if (!paused) {
				int timeOut = -1;
				try {
					timeOut = loop();

				} catch (RuntimeException e) {
					log.error("RuntimeException in Script.loop() ", e);
					e.printStackTrace();

				} catch (Throwable ex) {
					log.warn("Uncaught exception from Script.loop(): ", ex);
				}

				if (timeOut == -1) {
					break;
				}

				this.sleep(timeOut, true);

			} else {
				this.sleep(250, true);
			}
		}

		try {
			onFinish();

		} catch (RuntimeException ex) {
			log.error("RuntimeException in Script.onFinish() ", ex);
			ex.printStackTrace();
		} catch (Throwable ex) {
			log.warn("Uncaught exception from Script.onFinish(): ", ex);
		}

		running = false;
		log.info("Script stopped.");
		cleanup();
	}

	protected void sleep(int msecs) {
		sleep(msecs, false);
	}

	private void sleep(int msecs, boolean earlyBreakAllowed) {
		try {
			while (running && msecs > 0) {
				// if script is stopped, then we can break early
				if (earlyBreakAllowed) {
					if (!running) {
						break;
					}
				}

				ctx.sleep(25);
				msecs -= Math.min(25, msecs);
			}

		} catch (ThreadDeath td) {
			log.error("ThreadDeath in Script.sleep()");
		}
	}

}
