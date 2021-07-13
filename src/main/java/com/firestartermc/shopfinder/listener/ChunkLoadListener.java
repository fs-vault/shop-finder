package com.firestartermc.shopfinder.listener;

import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.firestartermc.shopfinder.ShopFinder;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.NotNull;

public class ChunkLoadListener implements Listener {

    private final ShopFinder plugin;

    public ChunkLoadListener(@NotNull ShopFinder plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (var blockEntity : event.getChunk().getTileEntities(false)) {
            if (!(blockEntity instanceof Sign sign)) {
                continue;
            }

            if (!ChestShopSign.isValid(sign)) {
                continue;
            }

            plugin.getShopStorage().addShop(sign);
        }
    }
}
