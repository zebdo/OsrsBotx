package net.runelite.rsb.script;

import lombok.extern.slf4j.Slf4j;

import net.runelite.rsb.internal.launcher.BotLite;


@Slf4j
public abstract class Script implements Runnable {
	private volatile boolean running = false;
	private volatile boolean paused = false;

	protected BotLite bot;

	public abstract boolean onStart();
	public abstract int loop();
	public abstract void onFinish();

	public abstract void onInit();

	public final void init(BotLite bot) {
		this.bot = bot;
		this.onInit();
	}

	public final void cleanup() {
		// moves it offscreen
		bot.getInputManager().windMouse(-10, -10);
		bot.getScriptHandler().stopScript();
	}

	public final void deactivate() {
		this.running = false;
	}

	/**
	 * Pauses/resumes this script.
	 */
	public final void setPaused(boolean paused) {
		if (running) {
			this.paused = paused;
		}
	}

	/**
	 * Returns whether or not this script is paused.
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
	 */
	public void stopScript(boolean logout) {
		this.running = false;
		log.info("Script stopping from within Script...");
		// XXX oh dear
		// if (logout) {
		// 	if (ctx.game.isLoggedIn()) {
		// 		ctx.game.logout();
		// 	}
		// }
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

				Thread.sleep(25);
				msecs -= Math.min(25, msecs);
			}

		} catch (InterruptedException e) {
		} catch (ThreadDeath td) {
			log.error("ThreadDeath in Script.sleep()");
		}
	}

}
