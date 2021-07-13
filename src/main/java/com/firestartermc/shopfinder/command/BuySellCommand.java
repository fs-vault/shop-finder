package com.firestartermc.shopfinder.command;

import com.firestartermc.kerosene.command.BrigadierCommand;
import com.firestartermc.shopfinder.ShopFinder;
import com.firestartermc.shopfinder.menu.ShopsMenu;
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
import java.util.List;

public class BuySellCommand extends BrigadierCommand {

    private final ShopFinder plugin;

    public BuySellCommand(@NotNull ShopFinder plugin) {
        this.plugin = plugin;
        registerCompletions();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length < 1 || !(sender instanceof Player player)) {
            return false;
        }

        var itemName = StringUtils.join(args, " ");

        plugin.getItemRepository().getItemByName(itemName).ifPresentOrElse(id -> {
            var shops = plugin.getShopStorage().getShopsById(id, alias.equals("sell"));

            if (shops.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No shops with this item were found.");
                return;
            }

            new ShopsMenu(player, itemName, shops).open();
        }, () -> {
            player.sendMessage(ChatColor.RED + "You've entered an item type that doesn't exist.");
        });

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

        return Arrays.asList(
                LiteralArgumentBuilder.literal("buy").then(argument),
                LiteralArgumentBuilder.literal("sell").then(argument)
        );
    }
}
