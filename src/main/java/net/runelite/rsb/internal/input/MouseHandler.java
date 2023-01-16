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

        // this massively slows things down - need to figure out how to do % of the time
        setOvershoots(5);
    }

    public void setOvershoots(int num) {
        var overshootManager = (DefaultOvershootManager) this.motionFactory.getOvershootManager();
        overshootManager.setOvershoots(num);
    }

    private MouseMotionFactory create(MouseMotionNature nature) {
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        List<Flow> flows = new ArrayList<>(Arrays.asList(
                                                         new Flow(FlowTemplates.variatingFlow()),
                                                         new Flow(FlowTemplates.interruptedFlow()),
                                                         new Flow(FlowTemplates.interruptedFlow2()),
                                                         new Flow(FlowTemplates.slowStartupFlow()),
                                                         new Flow(FlowTemplates.slowStartup2Flow()),
                                                         new Flow(FlowTemplates.adjustingFlow()),
                                                         new Flow(FlowTemplates.jaggedFlow())
                                                         ));

        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(SinusoidalDeviationProvider.DEFAULT_SLOPE_DIVIDER));
        factory.setNoiseProvider(new DefaultNoiseProvider(DefaultNoiseProvider.DEFAULT_NOISINESS_DIVIDER));

        factory.getNature().setReactionTimeVariationMs(85);
        manager.setMouseMovementBaseTimeMs(150);

        factory.setSpeedManager(manager);
        factory.setMouseInfo(() -> new Point(inputManager.getX(), inputManager.getY()));

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
