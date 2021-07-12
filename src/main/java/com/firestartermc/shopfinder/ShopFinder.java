package com.firestartermc.shopfinder;

import com.firestartermc.kerosene.Kerosene;
import com.firestartermc.shopfinder.command.FindShopCommand;
import com.firestartermc.shopfinder.data.ItemRepository;
import com.firestartermc.shopfinder.data.ShopStorage;
import com.firestartermc.shopfinder.data.SqlShopStorage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ShopFinder extends JavaPlugin {

    private ShopStorage shopStorage;
    private ItemRepository itemRepository;

    @Override
    public void onEnable() {
        shopStorage = new SqlShopStorage(this);
        itemRepository = new ItemRepository();

        // todo stock checker task
        Kerosene.getKerosene().getCommandManager().registerCommands(new FindShopCommand(this));
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
