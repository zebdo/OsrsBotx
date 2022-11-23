package net.runelite.rsb.methods;

import net.runelite.rsb.internal.input.VirtualKeyboard;
import net.runelite.rsb.internal.input.VirtualMouse;
import net.runelite.rsb.internal.input.InputManager;
import net.runelite.rsb.internal.client_wrapper.RSClient;

// XXX dont import this
import net.runelite.rsb.internal.launcher.BotLite;

import lombok.extern.slf4j.Slf4j;

/**
 * @author GigiaJ
 */
@Slf4j
public class MethodContext {
	/**
	 * The instance of {@link java.util.Random} for random number generation.
	 */
	public final java.util.Random randomGen = new java.util.Random();

	public final RSClient proxy;

	public final Bank bank;
	public final Game game;
	public final Calculations calc;
	public final Interfaces interfaces;
	public final GameGUI gui;
	public final Menu menu;
	public final NPCs npcs;
	public final Players players;
	public final Tiles tiles;
	public final Camera camera;
	public final Objects objects;
	public final Store store;
	public final Inventory inventory;
	public final Walking walking;
	public final ClientLocalStorage clientLocalStorage;
	public final Combat combat;
	public final Prayer prayer;
	public final RandomEvents randomEvents;
	public final Magic magic;
	public final GroundItems groundItems;
	public final Trade trade;
	public final Equipment equipment;
	public final GrandExchange grandExchange;
	public final WorldHopper worldHopper;

	// these need to refactored - possible only mouse/keyboard
	public final Mouse mouse;
	public final Keyboard keyboard;
	public final VirtualMouse virtualMouse;
	public final VirtualKeyboard virtualKeyboard;

	// also should be directly accessible (passed into mouse/keyboard virtual...)
	public final InputManager inputManager;

	// XXX this shouldn't be accessed via context (or ever to be honest)
	public final BotLite runeLite;

	/**
	 * Creates a method context for this client
	 * @param runeLite The client to provide method contexts for
	 */
	public MethodContext(BotLite runeLite, RSClient proxy) {
		this.runeLite = runeLite;
		this.proxy = proxy;

		this.inputManager = runeLite.getInputManager();

		this.bank = new Bank(this);
		this.game = new Game(this);
		this.calc = new Calculations(this);
		this.interfaces = new Interfaces(this);
		this.gui = new GameGUI(this);
		this.mouse = new Mouse(this);
		this.keyboard = new Keyboard(this);
		this.menu = new Menu(this);
		this.npcs = new NPCs(this);
		this.players = new Players(this);
		this.tiles = new Tiles(this);
		this.camera = new Camera(this);
		this.objects = new Objects(this);
		this.store = new Store(this);
		this.inventory = new Inventory(this);
		this.walking = new Walking(this);
		this.clientLocalStorage = new ClientLocalStorage(this);
		this.combat = new Combat(this);
		this.prayer = new Prayer(this);
		this.randomEvents = new RandomEvents(this);
		this.magic = new Magic(this);
		this.groundItems = new GroundItems(this);
		this.trade = new Trade(this);
		this.equipment = new Equipment(this);
		this.grandExchange = new GrandExchange(this);

		this.virtualMouse = new VirtualMouse(this);
		this.virtualKeyboard = new VirtualKeyboard(this);
		this.worldHopper = new WorldHopper(this);
	}

	///////////////////////////////////////////////////////////////////////////////

	public int random(int minValue, int maxValue) {
		// important maxValue is exclusive range: [minValue, maxValue)
		// XXX some places the API assumes inclusive, and sometimes exclusive... fix me
		if (minValue >= maxValue) {
			return minValue;
		}

		// randomGen.nextInt is exclusive 
		return minValue + randomGen.nextInt(maxValue - minValue);
	}

	/**
	 * Returns a normally distributed pseudorandom integer about a mean centered
	 * between min and max with a provided standard deviation.
	 *
	 * @param min The inclusive lower bound.
	 * @param max The exclusive upper bound.
	 * @param sd  The standard deviation. A higher value will increase the
	 *            probability of numbers further from the mean being returned.
	 * @return Random integer min (less than or equal to) n (less than) max from the normal distribution
	 *         described by the parameters.
	 */
	public int random(int min, int max, double sd) {
		int mean = min + (max - min) / 2;
		int x;
		do {
			x = (int) (randomGen.nextGaussian() * sd + mean);
		} while (x < min || x >= max);
		return x;
	}

	/**
	 * Returns a linearly distributed pseudorandom <code>double</code>.
	 *
	 * @param min The inclusive lower bound.
	 * @param max The exclusive upper bound.
	 * @return Random min (less than or equal) to n (less than) max.
	 */
	public double randomLinear(double min, double max) {
	 	return min + randomGen.nextDouble() * (max - min);
	}

	public void sleep(int toSleep) {
		try {
			long start = System.currentTimeMillis();
			Thread.sleep(toSleep);

			// Guarantee minimum sleep
			long now;
			while (start + toSleep > (now = System.currentTimeMillis())) {
				Thread.sleep(start + toSleep - now);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sleepRandom(int minValue, int maxValue) {
		this.sleep(random(minValue, maxValue));
	}
}
