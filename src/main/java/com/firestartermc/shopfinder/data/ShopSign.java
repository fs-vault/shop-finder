package com.firestartermc.shopfinder.data;

import org.bukkit.Location;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ShopSign(@NotNull String item, boolean type, @NotNull UUID seller, @NotNull Location location) {

    @NotNull
    public Location facingLocation() {
        var face = ((Directional) location().getBlock().getBlockData()).getFacing();
        var location = location().clone();

        location.add(0.5, 0.5, 0.5);
        location.setDirection(face.getOppositeFace().getDirection());
        return location;
    }
}
