package com.firestartermc.shopfinder.data;

import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ShopStorage {

    @NotNull
    List<ShopSign> getShopsById(@NotNull String id, boolean type);

    void addShop(@NotNull ShopCreatedEvent event);

    void removeShop(@NotNull Sign sign);
}
