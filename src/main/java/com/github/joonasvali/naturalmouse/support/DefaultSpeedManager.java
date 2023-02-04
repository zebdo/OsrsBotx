package com.github.joonasvali.naturalmouse.support;

import com.github.joonasvali.naturalmouse.api.SpeedManager;
import com.github.joonasvali.naturalmouse.util.FlowTemplates;
import com.github.joonasvali.naturalmouse.util.Pair;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Slf4j
public class DefaultSpeedManager implements SpeedManager {
  private static final double SMALL_DELTA = 10e-6;
  private final List<Flow> flows = new ArrayList<>();
  private long mouseMovementTimeMs = 500;

  public DefaultSpeedManager(Collection<Flow> flows) {
    this.flows.addAll(flows);
  }

  public DefaultSpeedManager() {
    this(Arrays.asList(
        new Flow(FlowTemplates.constantSpeed()),
        new Flow(FlowTemplates.variatingFlow()),
        new Flow(FlowTemplates.interruptedFlow()),
        new Flow(FlowTemplates.interruptedFlow2()),
        new Flow(FlowTemplates.slowStartupFlow()),
        new Flow(FlowTemplates.slowStartup2Flow()),
        new Flow(FlowTemplates.adjustingFlow()),
        new Flow(FlowTemplates.jaggedFlow()),
        new Flow(FlowTemplates.stoppingFlow())
    ));
  }

  @Override
  public Pair<Flow, Long> getFlowWithTime(double distance) {
    //log.info("getFlowWithTime distance {}", distance);
    double scale = Math.min(3.0, Math.max(0.25, distance / 250.0));

    // randomize short distances
    if (scale < 0.7) {
        scale = Math.max(Math.random(), scale);
    }

    double time = mouseMovementTimeMs + (Math.random() / 4.0 * mouseMovementTimeMs);
    Flow flow = flows.get((int) (Math.random() * flows.size()));

    // Let's ignore waiting time, e.g 0's in flow, by increasing the total time
    // by the amount of 0's there are in the flow multiplied by the time each bucket represents.
    double timePerBucket = time / (double)flow.getFlowCharacteristics().length;
    for (double bucket : flow.getFlowCharacteristics()) {
      if (Math.abs(bucket - 0) < SMALL_DELTA) {
        time += timePerBucket;
      }
    }

    time *= scale;
    return new Pair<>(flow, (long)time);
  }

  public void setMouseMovementBaseTimeMs(long mouseMovementSpeedMs) {
    this.mouseMovementTimeMs = mouseMovementSpeedMs;
  }
}
