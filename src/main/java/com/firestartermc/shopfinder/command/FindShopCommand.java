package com.firestartermc.shopfinder.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.firestartermc.kerosene.command.Command;
import com.firestartermc.shopfinder.ShopFinder;
import com.firestartermc.shopfinder.menu.ShopsMenu;
import com.firestartermc.shopfinder.data.ItemRepository;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@CommandAlias("findshop")
public class FindShopCommand extends Command {

    private final ShopFinder plugin;
    private final ItemRepository itemRepository;

    public FindShopCommand(@NotNull ShopFinder plugin) {
        this.plugin = plugin;
        this.itemRepository = plugin.getItemRepository();

        getCommandManager().registerCompletion("shop_type", context -> Arrays.asList("buy", "sell"));
        getCommandManager().registerCompletion("shop_items", context -> itemRepository.getItemNames());
    }

    @Default
    @CommandCompletion("@shop_type @shop_items")
    @Description("Locates a player shop that sells a specific item.")
    @Syntax("<buy/sell> <item>")
    public void onFind(Player player, String typeName, String itemName) {
        var type = typeName.equalsIgnoreCase("sell");
        var itemId = itemRepository.getItemByName(itemName);

        if (itemId.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You've entered an invalid item type.");
            return;
        }

        var shops = plugin.getShopStorage().getShopsById(itemId.get(), type);

        if (shops.isEmpty()) {
            player.sendMessage("No shops found.");
            return;
        }

        new ShopsMenu(player, itemName, shops).open();
    }
}
