package net.runelite.rsb.methods;

import net.runelite.client.callback.ClientThread;
import net.runelite.rsb.botLauncher.BotLite;
import net.runelite.api.Client;
import net.runelite.rsb.internal.input.VirtualKeyboard;
import net.runelite.rsb.internal.input.VirtualMouse;
import net.runelite.rsb.internal.InputManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.rsb.wrappers.client_wrapper.RSClient;
import net.runelite.rsb.wrappers.subwrap.ChooseOption;
import net.runelite.rsb.wrappers.subwrap.NPCChat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * For internal use to link MethodProviders.
 *
 * @author GigiaJ
 */
@Slf4j
public class MethodContext {
	/**
	 * The instance of {@link java.util.Random} for random number generation.
	 */
	public final java.util.Random random = new java.util.Random();

	// singletons:
	public final Game game = new Game(this);
	public final Calculations calc = new Calculations(this);
	public final Interfaces interfaces = new Interfaces(this);
	public final GameGUI gui = new GameGUI(this);
	public final Mouse mouse = new Mouse(this);
	public final Keyboard keyboard = new Keyboard(this);
	public final Menu menu = new Menu(this);
	public final InputManager inputManager;
	public final NPCs npcs = new NPCs(this);
	public final Players players = new Players(this);
	public final Tiles tiles = new Tiles(this);
	public final Camera camera = new Camera(this);
	public final Objects objects = new Objects(this);
	public final Store store = new Store(this);
	public final Inventory inventory = new Inventory(this);
	public final Bank bank = new Bank(this);
	public final Environment env = new Environment(this);
	public final Walking walking = new Walking(this);
	public final ClientLocalStorage clientLocalStorage = new ClientLocalStorage(this);
	public final Combat combat = new Combat(this);
	public final Skills skills = new Skills(this);
	public final Prayer prayer = new Prayer(this);
	public final RandomEvents randomEvents = new RandomEvents(this);
	public final Magic magic = new Magic(this);
	public final GroundItems groundItems = new GroundItems(this);
	public final Web web = new Web(this);
	public final Trade trade = new Trade(this);
	public final Equipment equipment = new Equipment(this);
	public final GrandExchange grandExchange = new GrandExchange(this);

	public final VirtualMouse virtualMouse = new VirtualMouse(this);
	public final VirtualKeyboard virtualKeyboard = new VirtualKeyboard(this);
	public final WorldHopper worldHopper = new WorldHopper(this);
	public final RSClient client;
	public final BotLite runeLite;
	public final ExecutorService executorService = Executors.newSingleThreadExecutor();

	public final ChooseOption chooseOption = new ChooseOption(this);
	public final NPCChat npcChat = new NPCChat(this);

	/**
	 * Creates a method context for this client
	 * @param runeLite The client to provide method contexts for
	 */
	public MethodContext(BotLite runeLite) {
		this.runeLite = runeLite;
		this.client = new RSClient(runeLite.getInjector().getInstance(Client.class),
								   runeLite.getInjector().getInstance(ClientThread.class));
		this.inputManager = runeLite.getInputManager();
	}

	/**
	 * Pauses execution for a random amount of time between two values.
	 *
	 * @param minSleep The minimum time to sleep.
	 * @param maxSleep The maximum time to sleep.
	 * @see #sleep(int)
	 * @see #random(int, int)
	 */
	public void sleep(int minSleep, int maxSleep) {
		if (minSleep >= maxSleep) {
			return;
		}

		sleep(minSleep + random.nextInt(maxSleep - minSleep));
	}

	/**
	 * Pauses execution for a given number of milliseconds.
	 *
	 * @param toSleep The time to sleep in milliseconds.
	 */
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

}
