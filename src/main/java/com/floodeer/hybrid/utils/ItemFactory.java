package com.floodeer.hybrid.utils;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemFactory {

    public static boolean isMat(ItemStack item, Material mat) {
        if (item == null) {
            return false;
        }
        return item.getType() == mat;
    }
    
    public static ItemStack create(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack create(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack create(Material material) {
        return new ItemStack(material, 1);
    }

    public static void addCustomModel(ItemStack item, Integer value) {
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(value);
        item.setItemMeta(meta);
    }

    public static ItemStack name(ItemStack itemStack, String name, List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!name.isEmpty()) {
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }
        itemMeta.setLore(Util.colorList(lore));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack lore(ItemStack itemStack, List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(Util.colorList(lore));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
    public static ItemStack name(ItemStack itemStack, String name, String... lores) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!name.isEmpty()) {
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }
        if (lores.length > 0) {
            List<String> loreList = new ArrayList<>(lores.length);

            for (String lore : lores) {
                loreList.add(ChatColor.translateAlternateColorCodes('&', lore));
            }
            itemMeta.setLore(loreList);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack hideAttributes(ItemStack item) {
        item.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return item;
    }

    public static void enchant(ItemStack item, int level, Enchantment enchant, boolean ignore) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchant, level, ignore);
        item.setItemMeta(meta);
    }

    public static ItemStack color(ItemStack stack, Color c) {
        if (!stack.getType().toString().startsWith("LEATHER"))
            return null;
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        meta.setColor(c);
        stack.setItemMeta(meta);
        return stack;
    }

    public static void addItemFlag(ItemStack item, ItemFlag flag) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(flag);
        item.setItemMeta(meta);
    }

    public static ItemStack parse(String config) {
        ItemStack stack = null;
        if (config == null || config.isEmpty() || config.equalsIgnoreCase("AIR")) {
            return create(Material.AIR);
        }

        List<String> lore = Lists.newArrayList();
        try {
            String[] parts = config.split(" : ");
            String name = parts[0].split(":")[0];
            int amount = Integer.parseInt(parts[1]);

            if (name.isEmpty()) {
                return ItemFactory.create(Material.AIR);
            }
            if (name.equalsIgnoreCase("AIR"))
                return ItemFactory.create(Material.AIR);

            XMaterial material = XMaterial.matchXMaterial(name).orElse(XMaterial.AIR);
            stack = create(material.parseMaterial());
            stack.setAmount(amount);

            if (parts[0].contains(":")) {
                if (name.equalsIgnoreCase("LINGERING_POTION")) {
                    setPotionEffect(stack, PotionType.valueOf(parts[0].split(":")[1]));
                } else if (((name.equalsIgnoreCase("POTION")) || (name.equalsIgnoreCase("SPLASH_POTION")) && (parts[0].split(":").length == 4))) {
                    setPotionEffect(stack, PotionType.valueOf(parts[0].split(":")[1]));
                } else {
                    ((Damageable)stack).setDamage(Integer.parseInt(parts[0].split(":")[1]));
                }
            }
            for (int i = 2; i < parts.length; i++) {
                String in = parts[i].split(":")[0].toLowerCase();
                if (in.equals("name")) {
                    name(stack, Util.color(parts[i].split(":")[1]));
                } else if (parts[i].startsWith("lore:")) {
                    int separatorIndex = parts[i].indexOf(":");
                    String value = parts[i].substring(separatorIndex + 1).trim();
                    lore.add(Util.color(value));
                } else if (in.equals("enchant")) {
                    enchant(stack, Integer.parseInt(parts[i].split(":")[2]), Enchantment.getByName(parts[i].split(":")[1].toUpperCase()), true);
                } else if (in.equals("dye")) {
                    if (!MathUtils.isInteger(parts[i].split(":")[1])) {
                        color(stack, getColor(parts[i].split(":")[1]));
                    } else {
                        color(stack, Color.fromRGB(Integer.parseInt(parts[i].split(":")[1])));
                    }

                } else if (in.equals("unbreakable")) {
                    stack.getItemMeta().setUnbreakable(true);
                } else if (in.equalsIgnoreCase("ModelData")) {
                    addCustomModel(stack, Integer.parseInt(parts[i].split(":")[1]));
                } else if (in.equalsIgnoreCase("ItemFlag")) {
                    addItemFlag(stack, ItemFlag.valueOf(parts[i].split(":")[1]));
                } else if (in.equalsIgnoreCase("HideItemInfo")) {
                    addItemFlag(stack, ItemFlag.HIDE_ATTRIBUTES);
                    addItemFlag(stack, ItemFlag.HIDE_UNBREAKABLE);
                    addItemFlag(stack, ItemFlag.HIDE_ENCHANTS);
                    addItemFlag(stack, ItemFlag.HIDE_DYE);
                    addItemFlag(stack, ItemFlag.HIDE_POTION_EFFECTS);
                }
            }

            lore(stack, lore);
        } catch (NullPointerException ex) {
            stack = create(Material.STONE);
            name(stack, "Error", "Something is wrong.");
            return stack;
        }
        return stack;
    }

    public static ItemStack setPotionEffect(ItemStack stack, PotionType paramPotionType) {
        PotionMeta localPotionMeta = (PotionMeta) stack.getItemMeta();
        if (stack.getType() == XMaterial.LINGERING_POTION.parseMaterial()) {
            localPotionMeta.setBasePotionData(new PotionData(paramPotionType));
        }
        stack.setItemMeta(localPotionMeta);
        return stack;
    }

    public static boolean isEnchanted(ItemStack itemStack) {
        return !itemStack.getEnchantments().isEmpty();
    }

    public static int getRGB(LeatherArmorMeta item) {
        return item.getColor().asRGB();
    }

    public static ItemStack getHead(UUID uuid) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return getHead(player);
    }

    public static ItemStack getHead(OfflinePlayer player) {
        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) head.getItemMeta();
        assert meta != null;
        meta.setOwningPlayer(player);
        head.setItemMeta(meta);
        return head;
    }

    public static String getItemColor(Color paramColor) {
        String str = "BLACK";
        if (paramColor.equals(Color.BLACK)) {
            return str;
        }
        if (paramColor.equals(Color.AQUA)) {
            str = "AQUA";
        } else if (paramColor.equals(Color.BLUE)) {
            str = "BLUE";
        } else if (paramColor.equals(Color.FUCHSIA)) {
            str = "FUCHSIA";
        } else if (paramColor.equals(Color.GRAY)) {
            str = "GRAY";
        } else if (paramColor.equals(Color.GREEN)) {
            str = "GREEN";
        } else if (paramColor.equals(Color.LIME)) {
            str = "LIME";
        } else if (paramColor.equals(Color.MAROON)) {
            str = "MAROON";
        } else if (paramColor.equals(Color.NAVY)) {
            str = "NAVY";
        } else if (paramColor.equals(Color.OLIVE)) {
            str = "OLIVE";
        } else if (paramColor.equals(Color.ORANGE)) {
            str = "ORANGE";
        } else if (paramColor.equals(Color.PURPLE)) {
            str = "PURPLE";
        } else if (paramColor.equals(Color.RED)) {
            str = "RED";
        } else if (paramColor.equals(Color.SILVER)) {
            str = "SILVER";
        } else if (paramColor.equals(Color.TEAL)) {
            str = "TEAL";
        } else if (paramColor.equals(Color.WHITE)) {
            str = "WHITE";
        } else if (paramColor.equals(Color.YELLOW)) {
            str = "YELLOW";
        }
        return str;
    }

    public static Color getColor(String c) {
        switch (c) {
            case "aqua":
                return Color.AQUA;
            case "black":
                return Color.BLACK;
            case "blue":
                return Color.BLUE;
            case "fuschia":
                return Color.FUCHSIA;
            case "gray":
                return Color.GRAY;
            case "green":
                return Color.GREEN;
            case "lime":
                return Color.LIME;
            case "maroon":
                return Color.MAROON;
            case "navy":
                return Color.NAVY;
            case "olvie":
                return Color.OLIVE;
            case "orange":
                return Color.ORANGE;
            case "purple":
                return Color.PURPLE;
            case "red":
                return Color.RED;
            case "silver":
                return Color.SILVER;
            case "teal":
                return Color.TEAL;
            case "white":
                return Color.WHITE;
            case "yellow":
                return Color.YELLOW;
            default:
                return Color.NAVY;
        }
    }
}
