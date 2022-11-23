package net.runelite.rsb.wrappers;

import lombok.extern.slf4j.Slf4j;

import net.runelite.rsb.methods.MethodContext;

import java.util.function.BooleanSupplier;

/**
 * A class that provides methods that use data from the game client. For
 * internal use.
 *
 * @author GigiaJ
 */
@Slf4j
public abstract class MethodProvider {

	public static MethodContext methods = null;

	public MethodProvider(MethodContext ctx) {
		this.methods = ctx;
	}
}
