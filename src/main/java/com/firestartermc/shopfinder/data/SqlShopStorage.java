package com.firestartermc.shopfinder.data;

import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Database.Account;
import com.Acrobot.ChestShop.Events.AccountQueryEvent;
import com.Acrobot.ChestShop.Events.ItemParseEvent;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Utils.ItemUtil;
import com.firestartermc.kerosene.Kerosene;
import com.firestartermc.kerosene.data.db.RemoteDatabase;
import com.firestartermc.kerosene.util.ConcurrentUtils;
import com.firestartermc.shopfinder.ShopFinder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.firestartermc.shopfinder.data.SqlStatements.*;

public class SqlShopStorage implements ShopStorage, Listener {

    private final RemoteDatabase database;
    private final ListMultimap<String, ShopSign> buySigns;
    private final ListMultimap<String, ShopSign> sellSigns;
    private final Map<Location, ShopSign> signLocations;

    public SqlShopStorage(@NotNull ShopFinder plugin) {
        this.database = Kerosene.getKerosene().getDatabase();
        this.buySigns = ArrayListMultimap.create();
        this.sellSigns = ArrayListMultimap.create();
        this.signLocations = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        ConcurrentUtils.callAsync(this::preload).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    @NotNull
    public List<ShopSign> getShopsById(@NotNull String id, boolean type) {
        return (type ? sellSigns : buySigns).get(id);
    }

    @Override
    public void addShop(@NotNull ShopCreatedEvent event) {
        if (signLocations.containsKey(event.getSign().getLocation())) {
            return;
        }

        var itemId = getFullId(event.getSignLine((short) 3));
        var location = event.getSign().getLocation();
        var seller = event.getPlayer().getUniqueId();
        var prices = event.getSignLine((short) 2);

        var buyPrice = PriceUtil.getExactBuyPrice(prices);
        var sellPrice = PriceUtil.getExactSellPrice(prices);

        if (buyPrice.doubleValue() > 0.0D) {
            var shopSign = new ShopSign(itemId, false, seller, location);
            buySigns.put(itemId, shopSign);
            recordShopSign(shopSign);
        }

        if (sellPrice.doubleValue() > 0.0D) {
            var shopSign = new ShopSign(itemId, true, seller, location);
            sellSigns.put(itemId, shopSign);
            recordShopSign(shopSign);
        }
    }

    @Override
    public void addShop(@NotNull Sign sign) {
        if (signLocations.containsKey(sign.getLocation())) {
            return;
        }

        var itemId = getFullId(sign.getLine((short) 3));
        var location = sign.getLocation();

        // Seller query
        AccountQueryEvent accountQueryEvent = new AccountQueryEvent(sign.getLine((short) 0));
        Bukkit.getPluginManager().callEvent(accountQueryEvent);

        var seller = accountQueryEvent.getAccount().getUuid();
        var prices = sign.getLine((short) 2);

        var buyPrice = PriceUtil.getExactBuyPrice(prices);
        var sellPrice = PriceUtil.getExactSellPrice(prices);

        if (buyPrice.doubleValue() > 0.0D) {
            var shopSign = new ShopSign(itemId, false, seller, location);
            buySigns.put(itemId, shopSign);
            recordShopSign(shopSign);
        }

        if (sellPrice.doubleValue() > 0.0D) {
            var shopSign = new ShopSign(itemId, true, seller, location);
            sellSigns.put(itemId, shopSign);
            recordShopSign(shopSign);
        }

        System.out.println("Recorded new shop sign: " + sign.getLocation());
    }

    @Override
    public void removeShop(@NotNull Sign sign) {
        var locationHash = sign.getLocation().hashCode();

        buySigns.entries().removeIf(entry -> entry.getValue().location().hashCode() == locationHash);
        sellSigns.entries().removeIf(entry -> entry.getValue().location().hashCode() == locationHash);
        signLocations.remove(sign.getLocation());
        deleteShopSign(sign.getLocation());
    }

    @NotNull
    private String getFullId(@NotNull String line) {
        var event = new ItemParseEvent(line);
        Bukkit.getPluginManager().callEvent(event);

        return ItemUtil.getName(event.getItem());
    }

    private void recordShopSign(@NotNull ShopSign sign) {
        signLocations.put(sign.location(), sign);

        ConcurrentUtils.callAsync(() -> {
            try (var connection = database.getConnection()) {
                var statement = connection.prepareStatement(RECORD_SIGN);
                var location = sign.location();

                statement.setString(1, sign.item());
                statement.setBoolean(2, sign.type());
                statement.setString(3, sign.seller().toString());
                statement.setString(4, location.getWorld().getUID().toString());
                statement.setInt(5, location.getBlockX());
                statement.setInt(6, location.getBlockY());
                statement.setInt(7, location.getBlockZ());
                statement.execute();
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    private void deleteShopSign(@NotNull Location location) {
        ConcurrentUtils.callAsync(() -> {
            try (var connection = database.getConnection()) {
                var statement = connection.prepareStatement(DELETE_SIGN);

                statement.setString(1, location.getWorld().getUID().toString());
                statement.setInt(2, location.getBlockX());
                statement.setInt(3, location.getBlockY());
                statement.setInt(4, location.getBlockZ());
                statement.execute();
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    private void preload() {
        try (var connection = database.getConnection()) {
            var statement = connection.prepareStatement(SELECT_ALL);
            var result = statement.executeQuery();

            while (result.next()) {
                var item = result.getString(1);
                var type = result.getBoolean(2);
                var seller = UUID.fromString(result.getString(3));
                var world = Bukkit.getWorld(UUID.fromString(result.getString(4)));
                var x = result.getInt(5);
                var y = result.getInt(6);
                var z = result.getInt(7);

                var sign = new ShopSign(item, type, seller, new Location(world, x, y, z));

                if (type) { // true = sell, false = buy
                    sellSigns.put(item, sign);
                } else {
                    buySigns.put(item, sign);
                }

                signLocations.put(sign.location(), sign);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopCreated(ShopCreatedEvent event) {
        addShop(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopDestroyed(ShopDestroyedEvent event) {
        removeShop(event.getSign());
    }
}
