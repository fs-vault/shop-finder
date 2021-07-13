package com.firestartermc.shopfinder.data;

import com.Acrobot.ChestShop.Utils.ItemUtil;
import com.firestartermc.kerosene.item.ItemBuilder;
import com.firestartermc.kerosene.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.enchantments.CraftEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class ItemRepository {

    private final Map<String, String> items;

    public ItemRepository() {
        this.items = new HashMap<>();
        populateItems();
    }

    @NotNull
    public Map<String, String> getItems() {
        return items;
    }

    @NotNull
    public Set<String> getItemNames() {
        return items.keySet();
    }

    @NotNull
    public Optional<String> getItemByName(@NotNull String name) {
        return Optional.ofNullable(items.get(name));
    }

    private void populateItems() {
        for (var material : Material.values()) {
            if (material.name().contains("LEGACY_")) {
                continue;
            }

            var stack = new ItemStack(material, 1);
            var name = ItemUtils.getFriendlyName(material);
            var id = ItemUtil.getName(stack);
            items.put(name, id);
        }

        for (var enchantment : Enchantment.values()) {
            IntStream.range(1, enchantment.getMaxLevel() + 1).forEach(level -> {
                var stack = ItemBuilder.of(Material.ENCHANTED_BOOK)
                        .storeEnchantment(enchantment, level)
                        .build();

                var id = ItemUtil.getName(stack);
                items.put(getEnchantmentName(enchantment, level), id);
            });
        }
    }

    @NotNull
    private String getEnchantmentName(@NotNull Enchantment enchantment, int level) {
        var nmsEnchantment = ((CraftEnchantment) enchantment).getHandle();
        return nmsEnchantment.d(level).getString();
    }
}
