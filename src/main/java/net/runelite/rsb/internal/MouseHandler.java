package net.runelite.rsb.internal;

import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.api.MouseMotionObserver;
import com.github.joonasvali.naturalmouse.support.*;
import com.github.joonasvali.naturalmouse.util.FactoryTemplates;

import lombok.extern.slf4j.Slf4j;
import net.runelite.rsb.internal.naturalmouse.RSBSystemCalls;
import java.awt.Point;

@Slf4j
public class MouseHandler {

	private final InputManager inputManager;
	private MouseMotionNature nature;
	private MouseMotionFactory motionFactory;

	MouseHandler(final InputManager inputManager) {
		this.inputManager = inputManager;
                RSBSystemCalls calls = new RSBSystemCalls(inputManager);

                this.nature = new DefaultMouseMotionNature(calls, new DefaultMouseInfoAccessor());
                //this.motionFactory = FactoryTemplates.createAverageComputerUserMotionFactory(nature);
                this.motionFactory = FactoryTemplates.createFastGamerMotionFactory(nature);

                DefaultOvershootManager overshootManager = (DefaultOvershootManager) motionFactory.getOvershootManager();
                overshootManager.setOvershoots(1);

		motionFactory.setMouseInfo(() -> new Point(inputManager.getX(), inputManager.getY()));
	}

	public void moveMouse(final int x, final int y) {
		try {
			motionFactory.move(x, y);
		} catch (InterruptedException e) {
			log.info("Mouse move failed to execute properly.", e);
		}
	}

}
