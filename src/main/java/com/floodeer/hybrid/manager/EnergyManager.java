package com.floodeer.hybrid.manager;

import com.floodeer.hybrid.utils.ItemFactory;
import com.floodeer.hybrid.utils.TimeUtils;
import com.floodeer.hybrid.utils.Util;
import com.floodeer.hybrid.utils.update.UpdateEvent;
import com.floodeer.hybrid.utils.update.UpdateType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class EnergyManager implements Listener {

    public Set<String> informSet = Sets.newHashSet();
    public Map<String, HashMap<String, RechargeData>> recharge = Maps.newHashMap();

    @EventHandler
    public void PlayerDeath(PlayerDeathEvent event) {
        get(event.getEntity().getName()).clear();
    }

    @EventHandler
    public void update(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK)
            return;

        recharge();
    }

    public void recharge() {
        Bukkit.getOnlinePlayers().forEach((cur) -> {
            LinkedList<String> rechargeList = Lists.newLinkedList();
            for (String ability : get(cur).keySet()) {
                if (get(cur).get(ability).update())
                    rechargeList.add(ability);
            }
            for (String ability : rechargeList) {
                get(cur).remove(ability);

                if (informSet.contains(ability))
                    cur.sendMessage(Util.color("&aSua habilidade " + ability + " está pronta para ser usada!"));
            }
        });
    }

    public HashMap<String, RechargeData> get(String name) {
        if (!recharge.containsKey(name))
            recharge.put(name, new HashMap<>());

        return recharge.get(name);
    }

    public HashMap<String, RechargeData> get(Player player) {
        return get(player.getName());
    }

    public boolean use(Player player, String ability, long recharge, boolean inform, boolean attachItem) {
        return use(player, ability, ability, recharge, inform, attachItem);
    }

    public boolean use(Player player, String ability, String abilityFull, long recharge, boolean inform, boolean attachItem) {
        return use(player, ability, abilityFull, recharge, inform, attachItem, false);
    }

    public boolean use(Player player, String ability, long recharge, boolean inform, boolean attachItem, boolean attachDurability) {
        return use(player, ability, ability, recharge, inform, attachItem, attachDurability);
    }

    public boolean use(Player player, String ability, String abilityFull, long recharge, boolean inform, boolean attachItem, boolean attachDurability) {
        if (recharge == 0)
            return true;

        recharge();

        if (inform && recharge > 1000)
            informSet.add(ability);

        if (get(player).containsKey(ability)) {
            if (inform) {
                String cooldown = TimeUtils.convertString((get(player).get(ability).getRemaining()), 1, TimeUtils.TimeUnit.FIT);
                player.sendMessage(Util.color("&cAguarde &b" + cooldown + " &cpara usar &b" + abilityFull + " &cnovamente." ));
            }

            return false;
        }
        useRecharge(player, ability, recharge, attachItem, attachDurability);

        return true;
    }

    public void useForce(Player player, String ability, long recharge) {
        useForce(player, ability, recharge, false);
    }

    public void useForce(Player player, String ability, long recharge, boolean attachItem) {
        useRecharge(player, ability, recharge, attachItem, false);
    }

    public boolean usable(Player player, String ability) {
        return usable(player, ability, false);
    }

    public boolean usable(Player player, String ability, boolean inform) {
        if (!get(player).containsKey(ability))
            return true;

        if (get(player).get(ability).getRemaining() <= 0) {
            return true;
        } else {
            if (inform) {
                String cooldown = TimeUtils.convertString((get(player).get(ability).getRemaining()), 1, TimeUtils.TimeUnit.FIT);
                player.sendMessage(Util.color("&cAguarde &b" + cooldown + " &cpara usar &b" + ability + " &cnovamente." ));
            }

            return false;
        }
    }

    public void useRecharge(Player player, String ability, long recharge, boolean attachItem,
                            boolean attachDurability) {
        // Event
        get(player).put(ability, new RechargeData(this, player, ability, player.getItemInHand(),
                recharge, attachItem, attachDurability));
    }

    public void recharge(Player player, String ability) {
        get(player).remove(ability);
    }

    @EventHandler
    public void clearPlayer(PlayerQuitEvent event) {
        recharge.remove(event.getPlayer().getName());
    }

    public void setDisplayForce(Player player, String ability, boolean displayForce) {
        if (!recharge.containsKey(player.getName()))
            return;

        if (!recharge.get(player.getName()).containsKey(ability))
            return;

        recharge.get(player.getName()).get(ability).displayForce = displayForce;
    }

    public void setCountdown(Player player, String ability, boolean countdown) {
        if (!recharge.containsKey(player.getName()))
            return;

        if (!recharge.get(player.getName()).containsKey(ability))
            return;

        recharge.get(player.getName()).get(ability).Countdown = countdown;
    }

    public void reset(Player player) {
        recharge.put(player.getName(), new HashMap<String, RechargeData>());
    }

    public void reset(Player player, String stringContains) {
        HashMap<String, RechargeData> data = recharge.get(player.getName());

        if (data == null)
            return;

        data.keySet().removeIf(key -> key.toLowerCase().contains(stringContains.toLowerCase()));
    }

    public class RechargeData {

        public EnergyManager Host;

        public long time;
        public long recharge;

        public Player player;
        public String name;

        public ItemStack itemStack;

        public boolean displayForce = false;
        public boolean Countdown = false;
        public boolean item;
        public boolean durability;

        public RechargeData(EnergyManager host, Player player, String name, ItemStack stack, long rechargeTime, boolean attachitem, boolean attachDurability) {
            Host = host;

            this.player = player;
            this.name = name;
            this.itemStack = player.getItemInHand();
            this.time = System.currentTimeMillis();
            this.recharge = rechargeTime;

            item = attachitem;
            durability = attachDurability;
        }

        public boolean update() {
            if ((displayForce || itemStack != null) && name != null && player != null) {
                double percent = (double) (System.currentTimeMillis() - time) / (double) recharge;

                if (displayForce || item) {
                    if (displayForce || (itemStack != null && ItemFactory.isMat(player.getItemInHand(), itemStack.getType()))) {
                        if (!TimeUtils.elapsed(time, recharge)) {
                            Util.displayProgress(ChatColor.RED + name, percent, TimeUtils.toString(recharge - (System.currentTimeMillis() - time)), Countdown, player);
                        } else {
                            Util.sendActionBar(Util.color("&7Habilidade &9" + name + " &7pronta para ser usada!"), player);
                            if (recharge > 4000)
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.4f, 3f);
                        }
                    }
                }

                if (durability && itemStack != null) {
                    itemStack.setDurability((short) (itemStack.getType().getMaxDurability()  - (itemStack.getType().getMaxDurability() * percent)));
                }
            }

            return TimeUtils.elapsed(time, recharge);
        }

        public long getRemaining() {
            return recharge - (System.currentTimeMillis() - time);
        }
    }
}