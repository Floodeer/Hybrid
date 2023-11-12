package com.floodeer.hybrid.game;

import com.floodeer.hybrid.database.data.GamePlayer;
import com.floodeer.hybrid.game.Game;
import com.floodeer.hybrid.game.GameTeam;
import com.floodeer.hybrid.game.payload.GameObjective;
import com.floodeer.hybrid.utils.MathUtils;
import com.floodeer.hybrid.utils.TimeUtils;
import com.floodeer.hybrid.utils.Util;
import com.floodeer.hybrid.utils.XMaterial;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.util.UtilColor;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GamePoint implements GameObjective {

    private final Game game;

    private final String name;

    private final List<Block> floor = Lists.newArrayList();

    @Getter  private final Location loc;
    @Getter private GameTeam owner = null;

    private final double captureMax = 36;
    private final double captureRate = 1.0;
    private double captureAmount = 0;
    private boolean captured = false;
    private final List<Block> captureFloor = Lists.newArrayList();
    private final List<Block> indicators = Lists.newArrayList();
    private long decayDelay = 0;

    private ChatColor scoreboardColor = ChatColor.WHITE;
    private int scoreboardTick = 0;
    private int indicatorTick = 0;

    public GamePoint(Game host, String name, Location loc) {
        game = host;
        this.name = name;
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (Math.abs(x) == 3 && Math.abs(z) == 3) {
                    Block ind = loc.getBlock().getRelative(x, 3, z);
                    ind.setType(XMaterial.WHITE_WOOL.parseMaterial());
                    indicators.add(ind);
                }
                boolean b = Math.abs(x) <= 2 && Math.abs(z) <= 2;
                if (b) {
                    Block floor1 = loc.getBlock().getRelative(x, -2, z);
                    if (x != 0 || z != 0) {
                        floor1.setType(XMaterial.WHITE_WOOL.parseMaterial());
                        this.floor.add(floor1);
                    } else {
                        floor1.setType(Material.BEACON);
                    }
                }

                if (b) {
                    Block block = loc.getBlock().getRelative(x, -1, z);
                    block.setType(XMaterial.WHITE_STAINED_GLASS.parseMaterial());
                }

                if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    Block block = loc.getBlock().getRelative(x, -3, z);
                    block.setType(XMaterial.IRON_BLOCK.parseMaterial());
                }
            }
        }

        this.loc = loc;
    }

    @Override
    public void update() {
        capture();
        points();
    }

    private void points() {
        if (!captured)
            return;

        //TODO
    }

    private void capture() {

        GameTeam teamA = null;
        ArrayList<Player> playersA = new ArrayList<Player>();

        GameTeam teamB = null;
        ArrayList<Player> playersB = new ArrayList<Player>();

        for (GameTeam team : game.getTeams()) {
            for (Player player : team.getAlivePlayers()) {
                if (GamePlayer.get(player) == null)
                    continue;
                if (GamePlayer.get(player).isSpectator())
                    continue;

                if (Math.abs(loc.getX() - player.getLocation().getX()) > 2.5)
                    continue;

                if (Math.abs(loc.getY() - player.getLocation().getY()) > 2.5)
                    continue;

                if (Math.abs(loc.getZ() - player.getLocation().getZ()) > 2.5)
                    continue;

                if (teamA == null || teamA.equals(team)) {
                    teamA = team;
                    playersA.add(player);
                } else {
                    teamB = team;
                    playersB.add(player);
                }
            }
        }

        if (teamA == null) {
            if (captureAmount > 0)
                regen();

            return;
        }

        if (teamB == null)
            capture(teamA, playersA.size(), playersA);

        else if (playersA.size() > playersB.size())
            capture(teamA, playersA.size() - playersB.size(), playersA);

        else if (playersB.size() > playersA.size())
            capture(teamB, playersB.size() - playersA.size(), playersB);
    }

    private void regen() {
        if (!TimeUtils.elapsed(decayDelay, 2000))
            return;

        if (!captured) {
            captureAmount = Math.max(0, (captureAmount - (captureRate * 1)));
            while ((double) captureFloor.size() / ((double) captureFloor.size() + (double) floor.size()) > captureAmount / captureMax) {
                Block block = captureFloor.remove(MathUtils.r(captureFloor.size()));

                floor.add(block);

                setWoolColor(block, null, false);
            }
            if (captureAmount == 0) {
                for (Block block : indicators) {
                    block.setType(XMaterial.WHITE_WOOL.parseMaterial());
                }
                owner = null;
            }
            for (Block block : indicators)
                block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, XMaterial.WHITE_WOOL.parseMaterial());
        }
        else if (captureAmount < captureMax) {
            captureAmount = Math.min(captureMax, (captureAmount + (captureRate * 1)));
            while ((double) captureFloor.size() / ((double) captureFloor.size() + (double) floor.size()) < captureAmount / captureMax) {
                Block block = floor.remove(MathUtils.r(floor.size()));

                captureFloor.add(block);

                setWoolColor(block, owner.getColor(), false);
            }
            for (Block block : indicators) {
                if (owner.getColor() == ChatColor.RED)
                    block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, XMaterial.RED_WOOL.parseMaterial());
                else
                    block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, XMaterial.BLUE_WOOL.parseMaterial());
            }
        }
    }

    public void captured(Player p, int x) {
        GameTeam team = GamePlayer.get(p).getTeam();
        team.setCapturedPoints(1);

        team.getPlayers().forEach(cur -> {
            cur.sendMessage(Util.color("&e&lObjetivo capturado. Escolte a carga!"));
        });
    }

    public void capture(GameTeam team, int count, Collection<Player> objectivePlayers) {
        scoreboardColor = team.getColor();

        decayDelay = System.currentTimeMillis();

        Color color = Color.RED;
        if (team.getColor() == ChatColor.BLUE)
            color = Color.BLUE;

        int bonus = 0;
        if (owner != null && owner.equals(team)) {
            if (captured)
                bonus = 1;

            captureAmount = Math.min(captureMax, (captureAmount + ((captureRate * count) + bonus)));
            while ((double) captureFloor.size() / ((double) captureFloor.size() + (double) floor.size()) < captureAmount / captureMax) {
                Block block = floor.remove(MathUtils.r(floor.size()));

                captureFloor.add(block);

                setWoolColor(block, team.getColor(), false);

            }


            if (captureAmount == captureMax && !captured) {
                captured = true;
                firework(loc, color, true);

                GameTeam opposite = null;

                for (Block block : indicators) {
                    if (team.getColor() == ChatColor.RED) {
                        block.setType(XMaterial.RED_WOOL.parseMaterial());
                    } else {
                        block.setType(XMaterial.BLUE_WOOL.parseMaterial());
                    }
                }

                if(owner.getColor() == ChatColor.RED) {
                    opposite = game.getTeam(ChatColor.BLUE);
                }else{
                    opposite = game.getTeam(ChatColor.RED);
                }
                if(opposite != null) {
                    if (opposite.getCapturedPoints() >= 1)
                        opposite.setCapturedPoints(opposite.getCapturedPoints() - 1);
                    team.setCapturedPoints(team.getCapturedPoints() + 1);

                   // game.addScore(team, 0);

                   //  opposite.getPlayers().forEach(p -> Util.playSound(p, Wizards.get().getOptions().lostSound));
                }
                setWoolColor(loc.getBlock().getRelative(0, -2, 0), owner.getColor(), true);

                if (objectivePlayers != null) {
                    for (Player player : objectivePlayers) {
                        captured(player, 30);
                    }
                }


            }
        }
        else {
            if (!captured)
                bonus = 1;

            captureAmount = Math.max(0, (captureAmount - ((captureRate * count) + bonus)));
            if (owner != null && captureFloor.size() >= captureMax) {
                for (Player player : owner.getAlivePlayers()) {
                    //TODO
                }
            }
            while ((double) captureFloor.size() / ((double) captureFloor.size() + (double) floor.size()) > captureAmount / captureMax) {
                Block block = captureFloor.remove(MathUtils.r(captureFloor.size()));

                floor.add(block);

                setWoolColor(block, null, false);

                for (Player player : team.getAlivePlayers()) {
                    //TODO
                }
            }
            if (captureAmount == 0) {
                captured = false;
                owner = team;

                setWoolColor(loc.getBlock().getRelative(0, -2, 0), null, true);
                for (Block block : indicators) {
                    block.setType(XMaterial.WHITE_WOOL.parseMaterial());
                }
            }
        }

        if (captureAmount != captureMax) {
            if (objectivePlayers != null) {
                for (Player player : objectivePlayers) {
                    captured(player, 1);
                }
            }
            indicate(color);
        }
    }

    private void setWoolColor(Block block, ChatColor color, boolean glassOnly) {
        if(color == null) {
            if(!glassOnly) {
                block.setType(XMaterial.WHITE_WOOL.parseMaterial());
            }
            Block up = block.getRelative(BlockFace.UP);
            up.setType(XMaterial.WHITE_STAINED_GLASS.parseMaterial());
        } else if(color == ChatColor.RED) {
            if(!glassOnly) {
                block.setType(XMaterial.RED_WOOL.parseMaterial());
            }
            Block up = block.getRelative(BlockFace.UP);
            up.setType(XMaterial.RED_STAINED_GLASS.parseMaterial());
        } else {
            if(!glassOnly) {
                block.setType(XMaterial.BLUE_WOOL.parseMaterial());
            }
            Block up = block.getRelative(BlockFace.UP);
            up.setType(XMaterial.BLUE_STAINED_GLASS.parseMaterial());
        }
    }

    public void firework(Location loc, Color color, boolean major) {
        //TODO
    }

    public void indicate(Color color) {

        for (Block block : indicators)
            if (color == Color.RED)
                block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, XMaterial.RED_WOOL.parseMaterial());
            else
                block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, XMaterial.BLUE_WOOL.parseMaterial());

        indicatorTick = (indicatorTick + 1) % indicators.size();
    }


    public String getScoreboardName() {

        String out = "";

        if (scoreboardColor != null && scoreboardTick == 0) {

            out = Util.color(scoreboardColor + "■ " + "Objetivo A");

        } else {
            if (captured) {
                out = Util.color(owner.getColor() + "■ " + "Objetivo A");
            } else
                out = Util.color( "&f&l■ " + "Objetivo A");
        }

        out = clean(out);

        return out;
    }

    public void clearScoreboard() {
        scoreboardColor = null;
    }

    public String clean(String line) {
        if (ChatColor.stripColor(line).length() > 18) {
            line = line.substring(0, 18);
        }
        return line;
    }


    public void tickScoreboard() {
        scoreboardTick = (scoreboardTick + 1) % 2;
    }

}