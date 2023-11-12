package com.floodeer.hybrid.utils;;

import com.google.common.collect.Maps;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class IconCore implements Listener {

    private final Map<Player, IconMenu> menu = Maps.newHashMap();

    public void create(Player player, String name, int size, IconMenu.OptionClickEventHandler handler) {
        if (player != null) {
            destroy(player);
            menu.put(player, new IconMenu(name, size, handler));
        }
    }

    public IconMenu getMenu(Player player) {
        return menu.get(player);
    }

    public void show(Player player) {
        if (menu.containsKey(player)) {
            menu.get(player).open(player);
        }
    }

    public void update(final Player player) {
        if (menu.containsKey(player)) {
            menu.get(player).update(player);
        }
    }

    public void setOption(Player player, int position, ItemStack icon, String name, String... info) {
        if (menu.containsKey(player)) {
            menu.get(player).setOption(position, icon, name, info);
        }
    }

    public void setOption(Player player, int position, ItemStack icon) {
        if (menu.containsKey(player)) {
            menu.get(player).setOption(position, icon, icon.getItemMeta().getDisplayName(), icon.getItemMeta().getLore());
        }
    }

    public void setOption(Player player, int position, ItemStack icon, String name, List<String> info) {
        if (menu.containsKey(player)) {
            menu.get(player).setOption(position, icon, name, info);
        }
    }

    public void setOptionMetadata(Player player, int position, ItemStack item) {
        if (menu.containsKey(player)) {
            if(item.getItemMeta().getLore() == null) {
                menu.get(player).setOption(position, item, item.getItemMeta().getDisplayName(), Arrays.asList(" "));
            }else{
                menu.get(player).setOption(position, item, item.getItemMeta().getDisplayName(), item.getItemMeta().getLore());
            }
        }
    }

    public String[] getOptions(Player player) {
        if (menu.containsKey(player)) {
            return menu.get(player).getOptions();
        }
        return null;
    }

    public void destroy(Player player) {
        if (menu.containsKey(player)) {
            menu.remove(player).destroy();
            player.getOpenInventory().close();
        }
    }

    public void destroyAll() {
        for (Player player : new HashSet<Player>(menu.keySet())) {
            destroy(player);
        }
    }

    public boolean has(Player player) {
        return menu.containsKey(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (menu.containsKey(event.getWhoClicked()))
                menu.get(event.getWhoClicked()).onInventoryClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player && menu.containsKey(event.getPlayer())) {
            destroy((Player) event.getPlayer());
        }
    }
}