package net.runelite.rsb.internal;

import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.api.MouseMotionObserver;
import com.github.joonasvali.naturalmouse.support.*;
import com.github.joonasvali.naturalmouse.util.FactoryTemplates;

import lombok.extern.slf4j.Slf4j;
import net.runelite.rsb.internal.naturalmouse.RSBSystemCalls;
import net.runelite.rsb.util.Timer;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

@Slf4j
public class MouseHandler {

	private final InputManager inputManager;
	private MouseMotionNature nature;
	private MouseMotionNature nature2;
	private MouseMotionFactory motionFactory;
	private MouseMotionFactory motionFactory2;

	private final Random random = new Random();

	MouseHandler(final InputManager inputManager) {
		this.inputManager = inputManager;
                RSBSystemCalls calls = new RSBSystemCalls(inputManager);
		this.nature = new DefaultMouseMotionNature(calls, new DefaultMouseInfoAccessor());
                this.nature2 = new DefaultMouseMotionNature(calls, new DefaultMouseInfoAccessor());


                this.motionFactory = FactoryTemplates.createAverageComputerUserMotionFactory(nature);
                this.motionFactory2 = FactoryTemplates.createFastGamerMotionFactory(nature);

                DefaultOvershootManager overshootManager = (DefaultOvershootManager) this.motionFactory2.getOvershootManager();
                overshootManager.setOvershoots(0);

		motionFactory.setMouseInfo(() -> new Point(inputManager.getX(), inputManager.getY()));
		motionFactory2.setMouseInfo(() -> new Point(inputManager.getX(), inputManager.getY()));
	}

	public void moveMouse(final int x, final int y) {
		try {
			motionFactory.move(x, y);
		} catch (InterruptedException e) {
			log.info("Mouse move failed to execute properly.", e);
		}
	}


	public void moveMouse2(final int x, final int y) {
		try {
			motionFactory.move(x, y);
		} catch (InterruptedException e) {
			log.info("Mouse move failed to execute properly.", e);
		}
	}

}
