package com.firestartermc.shopfinder.menu;

import com.firestartermc.kerosene.gui.GuiPosition;
import com.firestartermc.kerosene.gui.PlayerGui;
import com.firestartermc.kerosene.gui.components.buttons.ButtonComponent;
import com.firestartermc.kerosene.item.ItemBuilder;
import com.firestartermc.kerosene.util.MessageUtils;
import com.firestartermc.shopfinder.data.ShopSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.firestartermc.kerosene.util.ConcurrentUtils.ensureMain;
import static com.firestartermc.kerosene.util.TeleportUtils.teleport;

public class ShopsMenu extends PlayerGui {

    private final String itemName;

    public ShopsMenu(@NotNull Player player, @NotNull String itemName, @NotNull List<ShopSign> shops) {
        super(player, MessageUtils.formatColors("&lSHOPS &rSearch..."), (int) Math.ceil(shops.size() / 9F));
        this.itemName = itemName;

        var slot = new AtomicInteger();
        shops.stream()
                .map(shop -> createShopButton(shop, slot.getAndIncrement()))
                .forEach(this::addElement);
    }

    @NotNull
    private ButtonComponent createShopButton(@NotNull ShopSign sign, int slot) {
        var seller = Bukkit.getOfflinePlayer(sign.seller()).getName();
        var item = ItemBuilder.of(Material.CHEST)
                .name(ChatColor.WHITE + itemName)
                .lore(
                        "&#ffe799Seller: " + seller,
                        "&r ",
                        "&#59ffa1➥ Click to visit!"
                )
                .build();

        return new ButtonComponent(GuiPosition.fromSlot(slot), item, interaction -> {
            teleportToSign(interaction.getPlayer(), sign, () -> {
                // TODO
            });
        });
    }

    private void teleportToSign(@NotNull Player player, @NotNull ShopSign sign, @NotNull Runnable callback) {
        var location = sign.location();

        location.getWorld().getChunkAtAsync(location).thenAccept(chunk -> ensureMain(() -> {
            var facingLocation = sign.facingLocation();
            teleport(player, facingLocation);
            callback.run();
        }));
    }
}
