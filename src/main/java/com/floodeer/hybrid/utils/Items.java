package com.floodeer.hybrid.utils;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Items {

    @Getter private ItemStack towerMenuItem = null;

    public Items() {
        towerMenuItem = ItemFactory.create(Material.REDSTONE, Util.color("&e&lConfigurador de Torres"));
    }
}
