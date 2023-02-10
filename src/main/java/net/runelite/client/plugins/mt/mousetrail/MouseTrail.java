/*
 * Copyright (c) 2022, Ryan Bell <llaver@live.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.mousetrail;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;

import javax.inject.Inject;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;

@Slf4j
public class MouseTrail {
    private final Deque<Curve> curve = new ArrayDeque<>();
    private int curveSizeMultiplier = 5;
    private int curveMaxPoints = 50;
    private Point temp = null;
    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public MouseEvent mouseMoved(MouseEvent event) {
            updateMousePositions(new Point(event.getX(), event.getY()));
            return event;
        }

        @Override
        public MouseEvent mouseDragged(MouseEvent event) {
            updateMousePositions(new Point(event.getX(), event.getY()));
            return event;
        }
    };
    @Inject
    private MouseManager mouseManager;

    protected void startUp() {
        setMouseListenerEnabled(true);
    }

    protected void shutDown() {
        curve.clear();

        setMouseListenerEnabled(false);
    }

    public void setMouseListenerEnabled(boolean enabled) {
        if (enabled) {
            mouseManager.registerMouseListener(mouseAdapter);
        } else {
            mouseManager.unregisterMouseListener(mouseAdapter);
        }
    }

    public void setCurveSizeMultiplier(int size) {
        this.curveSizeMultiplier = size;
    }

    public void setCurvePointsSize(int size){
        this.curveMaxPoints = size;
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        popTrail();
        popTrail();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (!configChanged.getGroup().equals(MouseTrailPlugin.GROUP))
            return;

        if (configChanged.getKey().equals("whichMouseTrailStyle")) {
            setMouseListenerEnabled(!configChanged.getNewValue().equals("NONE"));
        }
        else if (configChanged.getKey().equals("curveSizeMultiplier")) {
            setCurveSizeMultiplier(Integer.parseInt(configChanged.getNewValue()));
            curve.clear();
        }
        else if (configChanged.getKey().equals("curvePoints")) {
            setCurvePointsSize(Integer.parseInt(configChanged.getNewValue()));
            curve.clear();
        }
    }

    public void updateMousePositions(Point point) {
        if (curve.size() < this.curveMaxPoints) {
            if (temp != null) {
                Curve current = new Curve(curveSizeMultiplier, temp, point);
                curve.add(current);
            }
            temp = point;
        }
    }

    public Deque<Curve> getTrail() {
        return curve;
    }

    public void popTrail() {
        if (curve.size() > 0) {
            curve.pop();
        }
    }
}