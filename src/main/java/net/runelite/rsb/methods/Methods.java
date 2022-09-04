package net.runelite.rsb.methods;

import net.runelite.rsb.wrappers.*;
import net.runelite.rsb.wrappers.subwrap.ChooseOption;
import net.runelite.rsb.wrappers.subwrap.NPCChat;

import java.awt.*;

/**
 * Provides access to methods that can be used by RSBot scripts.
 */
public class Methods {
	public MethodContext ctx;

	/**
	 * The singleton of Skills
	 */
	protected Skills skills;
	/**
	 * The singleton of ClientLocalStorage
	 */
	protected ClientLocalStorage clientLocalStorage;
	/**
	 * The singleton of Magic
	 */
	protected Magic magic;
	/**
	 * The singleton of Bank
	 */
	protected Bank bank;
	/**
	 * The singleton of Players
	 */
	protected Players players;
	/**
	 * The singleton of Store
	 */
	protected Store store;
	/**
	 * The singleton of Camera
	 */
	protected Camera camera;
	/**
	 * The singleton of NPCs
	 */
	protected NPCs npcs;
	/**
	 * The singleton of GameScreen
	 */
	protected Game game;
	/**
	 * The singleton of Combat
	 */
	protected Combat combat;
	/**
	 * The singleton of Interfaces
	 */
	protected Interfaces interfaces;
	/**
	 * The singleton of Mouse
	 */
	protected Mouse mouse;
	/**
	 * The singleton of Keyboard
	 */
	protected Keyboard keyboard;
	/**
	 * The singleton of Menu
	 */
	protected Menu menu;
	/**
	 * The singleton of Tiles
	 */
	protected Tiles tiles;
	/**
	 * The singleton of Objects
	 */
	protected Objects objects;
	/**
	 * The singleton of Walking
	 */
	protected Walking walking;
	/**
	 * The singleton of Calculations
	 */
	protected Calculations calc;
	/**
	 * The singleton of Inventory
	 */
	protected Inventory inventory;
	/**
	 * The singleton of Equipment
	 */
	protected Equipment equipment;
	/**
	 * The singleton of GroundItems
	 */
	protected GroundItems groundItems;
	/**
	 * The singleton of Environment
	 */
	protected Environment env;
	/**
	 * The singleton of Prayer
	 */
	protected Prayer prayer;
	/**
	 * The singleton of Trade
	 */
	protected Trade trade;
	/**
	 * The singleton of Web
	 */
	protected Web web;
	/**
	 * The singleton of GrandExchange
	 */
	protected GrandExchange grandExchange;
	/**
	 * The singleton of WorldHopper
	 */
	protected WorldHopper worldHopper;
	/**
	 * The singleton of ChooseOption
	 */
	protected ChooseOption chooseOption;
	/**
	 *  The singleton of NPCChat
	 */
	protected NPCChat npcChat;

	/**
	 * For internal use only: initializes the method providers.
	 *
	 * @param ctx The MethodContext.
	 */
	public void init(MethodContext ctx) {
		this.ctx = ctx;
		this.skills = ctx.skills;
		this.clientLocalStorage = ctx.clientLocalStorage;
		this.magic = ctx.magic;
		this.bank = ctx.bank;
		this.players = ctx.players;
		this.store = ctx.store;
		this.camera = ctx.camera;
		this.npcs = ctx.npcs;
		this.game = ctx.game;
		this.grandExchange = ctx.grandExchange;
		this.combat = ctx.combat;
		this.interfaces = ctx.interfaces;
		this.mouse = ctx.mouse;
		this.keyboard = ctx.keyboard;
		this.menu = ctx.menu;
		this.tiles = ctx.tiles;
		this.objects = ctx.objects;
		this.walking = ctx.walking;
		this.calc = ctx.calc;
		this.inventory = ctx.inventory;
		this.equipment = ctx.equipment;
		this.groundItems = ctx.groundItems;
		this.env = ctx.env;
		this.prayer = ctx.prayer;
		this.web = ctx.web;
		this.trade = ctx.trade;
		this.worldHopper = ctx.worldHopper;
		this.chooseOption = ctx.chooseOption;
		this.npcChat = ctx.npcChat;
	}
}
