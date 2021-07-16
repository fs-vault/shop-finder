package com.firestartermc.shopfinder;

import com.firestartermc.kerosene.plugin.BukkitPlugin;
import com.firestartermc.shopfinder.command.BuySellCommand;
import com.firestartermc.shopfinder.command.FindShopCommand;
import com.firestartermc.shopfinder.data.ItemRepository;
import com.firestartermc.shopfinder.data.ShopStorage;
import com.firestartermc.shopfinder.data.SqlShopStorage;
import com.firestartermc.shopfinder.listener.ChunkLoadListener;
import org.jetbrains.annotations.NotNull;

public class ShopFinder extends BukkitPlugin {

    private ShopStorage shopStorage;
    private ItemRepository itemRepository;

    @Override
    public void onEnable() {
        shopStorage = new SqlShopStorage(this);
        itemRepository = new ItemRepository();

        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ChunkLoadListener(this), this);

        registerCommand("buy", new BuySellCommand(this));
        registerCommand("findshop", new FindShopCommand(this));

        // todo stock checker task (or on chunk load)
    }

    @NotNull
    public ShopStorage getShopStorage() {
        return shopStorage;
    }

    @NotNull
    public ItemRepository getItemRepository() {
        return itemRepository;
    }
}
