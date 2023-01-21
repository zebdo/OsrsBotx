package net.runelite.rsb.internal.input;

import lombok.extern.slf4j.Slf4j;

import com.github.joonasvali.naturalmouse.util.FlowTemplates;
import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.support.*;

import net.runelite.rsb.internal.input.RSBSystemCalls;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MouseHandler {

    private final InputManager inputManager;
    private final MouseMotionFactory motionFactory;

    MouseHandler(final InputManager inputManager) {
        this.inputManager = inputManager;

        var calls = new RSBSystemCalls(inputManager);
        var nature = new DefaultMouseMotionNature(calls, new DefaultMouseInfoAccessor());
        this.motionFactory = create(nature);
    }

    private MouseMotionFactory create(MouseMotionNature nature) {
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        List<Flow> flows = new ArrayList<>(Arrays.asList(
                                                         new Flow(FlowTemplates.variatingFlow()),
                                                         new Flow(FlowTemplates.random()),
                                                         new Flow(FlowTemplates.interruptedFlow()),
                                                         new Flow(FlowTemplates.interruptedFlow2()),
                                                         new Flow(FlowTemplates.adjustingFlow()),
                                                         new Flow(FlowTemplates.jaggedFlow())
                                                         ));

        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(9));
        factory.setNoiseProvider(new DefaultNoiseProvider(1.6));

        factory.getNature().setReactionTimeVariationMs(100);
        factory.getNature().setTimeToStepsDivider(DefaultMouseMotionNature.TIME_TO_STEPS_DIVIDER - 2);

        manager.setMouseMovementBaseTimeMs(450);

        factory.setSpeedManager(manager);
        factory.setMouseInfo(() -> new Point(inputManager.getX(), inputManager.getY()));

        // Overshoot manager:
        var man = (DefaultOvershootManager) factory.getOvershootManager();
        man.setMinDistanceForOvershoots(3);
        man.setOvershootRandomModifierDivider(DefaultOvershootManager.OVERSHOOT_RANDOM_MODIFIER_DIVIDER / 2f);
        man.setOvershootSpeedupDivider(DefaultOvershootManager.OVERSHOOT_SPEEDUP_DIVIDER * 2);
        man.setOvershoots(3);

        // XXX overshooting massively slows things down
        // so currently hacked in DefaultOvershootManager to do only a percentage of the time
        man.setOvershootPct(25);

        return factory;
    }


    public void moveMouse(final int x, final int y) {
        try {
            motionFactory.move(x, y);
        } catch (InterruptedException e) {
            log.info("Mouse move failed to execute properly.", e);
        }
    }

}
