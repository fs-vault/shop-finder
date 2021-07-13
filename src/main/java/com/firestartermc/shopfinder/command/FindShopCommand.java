package com.firestartermc.shopfinder.command;

import com.firestartermc.kerosene.command.BrigadierCommand;
import com.firestartermc.shopfinder.ShopFinder;
import com.firestartermc.shopfinder.menu.ShopsMenu;
import com.firestartermc.shopfinder.data.ItemRepository;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FindShopCommand extends BrigadierCommand {

    private final ShopFinder plugin;
    private final ItemRepository itemRepository;

    public FindShopCommand(@NotNull ShopFinder plugin) {
        this.plugin = plugin;
        this.itemRepository = plugin.getItemRepository();

        registerCompletions();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2 || !(sender instanceof Player player)) {
            return false;
        }

        var type = args[0].equalsIgnoreCase("sell");
        var itemName = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
        var itemId = itemRepository.getItemByName(itemName);

        if (itemId.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You've entered an item type that doesn't exist.");
            return true;
        }

        var shops = plugin.getShopStorage().getShopsById(itemId.get(), type);

        if (shops.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No shops with this item were found.");
            return true;
        }

        new ShopsMenu(player, itemName, shops).open();
        return true;
    }

    @Override
    @NotNull
    public List<LiteralArgumentBuilder<?>> getCompletions() {
        var argument = RequiredArgumentBuilder.argument("item", StringArgumentType.greedyString()).suggests((context, suggestionsBuilder) -> {
            var remaining = suggestionsBuilder.getRemaining();

            plugin.getItemRepository().getItems().keySet().stream()
                    .filter(name -> name.toLowerCase().startsWith(remaining))
                    .forEach(suggestionsBuilder::suggest);

            return suggestionsBuilder.buildFuture();
        });

        var builder = LiteralArgumentBuilder.literal("findshop")
                .then(LiteralArgumentBuilder.literal("buy").then(argument))
                .then(LiteralArgumentBuilder.literal("sell").then(argument));

        return Collections.singletonList(builder);
    }
}
