/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.grounditems.GroundItemsPlugin;
import net.runelite.client.plugins.groundmarkers.GroundMarkerPlugin;
import net.runelite.client.plugins.inventorytags.InventoryTagsPlugin;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsPlugin;
import net.runelite.client.plugins.objectindicators.ObjectIndicatorsPlugin;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(name = "Mouse Trail", tags = {"mouse"})
public class MouseTrailPlugin extends Plugin {
    public static final String GROUP = "rainbow_rave";
    @Inject
    private MouseTrailConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MouseTrail mouseTrail;
    private MouseTrailOverlay mouseTrailOverlay;
    @Inject
    private EventBus eventBus;

    @Override
    protected void startUp() {

        if (mouseTrailOverlay == null) {
            mouseTrailOverlay = new MouseTrailOverlay(this, mouseTrail, config);
        }
        overlayManager.add(mouseTrailOverlay);
        mouseTrail.startUp();
        eventBus.register(mouseTrail);

    }

    @Override
    protected void shutDown() {
        overlayManager.remove(mouseTrailOverlay);
        mouseTrail.shutDown();
        eventBus.unregister(mouseTrail);
    }

    @Provides
    MouseTrailConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MouseTrailConfig.class);
    }


}
