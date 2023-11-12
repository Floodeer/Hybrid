package com.floodeer.hybrid.utils;

import com.floodeer.hybrid.Hybrid;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IconMenu {

    private final String name;
    private final int size;
    private OptionClickEventHandler handler;
    private String[] optionNames;
    private ItemStack[] optionIcons;

    public IconMenu(String name, int size, OptionClickEventHandler handler) {
        this.name = name;
        this.size = size;
        this.handler = handler;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
    }

    public IconMenu setOption(int position, ItemStack icon, String name, String[] info) {
        this.optionNames[position] = name;
        this.optionIcons[position] = ItemFactory.name(icon, name, info);
        return this;
    }

    public IconMenu setOption(int position, ItemStack icon, String name, List<String> info) {
        this.optionNames[position] = name;
        this.optionIcons[position] = ItemFactory.name(icon, name, info);
        return this;
    }


    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, this.size, this.name);
        for (int iii = 0; iii < this.optionIcons.length; iii++) {
            if (this.optionIcons[iii] != null) {
                inventory.setItem(iii, this.optionIcons[iii]);
            }
        }
        player.openInventory(inventory);
    }

    public void update(Player player) {
        Inventory inventory = Bukkit.createInventory(player, this.size, this.name);
        if (this.getOptions() != null) {
            for (int iii = 0; iii < this.optionIcons.length; iii++) {
                if (this.optionIcons[iii] != null) {
                    inventory.setItem(iii, this.optionIcons[iii]);
                }
            }
        }
        player.openInventory(inventory);
    }

    public void destroy() {
        this.handler = null;
        this.optionNames = null;
        this.optionIcons = null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(name)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();

        try {
            if (!(slot >= 0 && slot < size && optionNames[slot] != null)) {
                return;
            }
        } catch (NullPointerException e) {
            return;
        }

        OptionClickEvent clickEvent = new OptionClickEvent((Player) event.getWhoClicked(), slot, optionNames[slot], event.getInventory().getItem(event.getRawSlot()));
        handler.onOptionClick(clickEvent);
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            if (clickEvent.willUpdate()) {
                update((Player) event.getWhoClicked());
            }
        }
        if (clickEvent.willClose()) {
            final Player player = (Player) event.getWhoClicked();

            Bukkit.getScheduler().runTaskLater(Hybrid.get(), (@NotNull Runnable) player::closeInventory, 1L);
        }

        if (clickEvent.willDestroy()) {
            destroy();
        }
    }

    public String getName() {
        return this.name;
    }

    public String[] getOptions() {
        return optionNames;
    }

    public static class OptionClickEvent {

        private final Player player;
        private final int position;
        private final String name;
        private boolean update;
        private boolean close;
        private boolean destroy;

        private final ItemStack item;

        public OptionClickEvent(Player player, int position, String name, ItemStack item) {
            this.player = player;
            this.position = position;
            this.name = name;
            this.close = false;
            this.destroy = false;
            this.update = false;
            this.item = item;
        }

        public Player getPlayer() {
            return this.player;
        }

        public int getPosition() {
            return this.position;
        }

        public String getName() {
            return this.name;
        }

        public boolean willClose() {
            return this.close;
        }

        public boolean willDestroy() {
            return this.destroy;
        }

        public void setWillClose(boolean close) {
            this.close = close;
        }

        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }

        public void setUpdate(boolean b) {
            this.update = b;
        }

        public boolean willUpdate() {
            return update;
        }

        public ItemStack getClickedItem() {
            return item;
        }

    }

    public interface OptionClickEventHandler {

        void onOptionClick(OptionClickEvent event);
    }
}