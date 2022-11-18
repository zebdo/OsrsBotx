package net.runelite.rsb.methods;

import net.runelite.api.WorldType;

public class WorldHopper extends MethodProvider {
    WorldHopper(MethodContext ctx) {
        super(ctx);
    }

    public int getWorld() {
        return methods.proxy.getWorld();
    }

    public boolean isCurrentWorldMembers() {
        return methods.proxy.getWorldType().stream().anyMatch((worldType) -> worldType == WorldType.MEMBERS);
    }
}
