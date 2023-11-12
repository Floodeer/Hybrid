package com.floodeer.hybrid;

import com.floodeer.hybrid.database.data.GamePlayer;
import com.floodeer.hybrid.game.Game;
import com.floodeer.hybrid.game.GameArena;
import com.floodeer.hybrid.game.GamePoint;
import com.floodeer.hybrid.game.payload.PayloadVehicle;
import com.floodeer.hybrid.game.payload.VehicleLoader;
import com.floodeer.hybrid.utils.MathUtils;
import com.floodeer.hybrid.utils.Runner;
import com.floodeer.hybrid.utils.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return true;
        }
        if(args[0].equalsIgnoreCase("next")) {
            Game game = GamePlayer.get((((Player)commandSender).getUniqueId())).getGame();
            game.unlockNextObjective();
        }
        if(args[0].equalsIgnoreCase("create")) {

            if(!commandSender.hasPermission("hybrid.admin"))
                return true;

            if(args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }
            String name = args[1];

            Game game = Hybrid.get().getGameManager().createGame(name, false);
            game.getArena().create();
            commandSender.sendMessage(Util.color("&aArena &e" + name + " &acriada!"));

        }else if(args[0].equalsIgnoreCase("delete")) {

            if(!commandSender.hasPermission("hybrid.admin"))
                return true;

            if(args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            if(Hybrid.get().getGameManager().doesMapExists(args[1])) {
                Hybrid.get().getGameManager().deleteGame(args[1]);
                commandSender.sendMessage(Util.color("&cArena &e" + args[1] + " &cdeletada!"));
            }else{
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
            }
        }else if(args[0].equalsIgnoreCase("finish")) {
            if (!commandSender.hasPermission("hybrid.admin"))
                return true;

            if (args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            if (!Hybrid.get().getGameManager().doesMapExists(args[1])) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }

            Game game = Hybrid.get().getGameManager().getGameFromName(args[1]);
            if (game.getState() == Game.GameState.IN_GAME) {
                game.shutdown(true);
            }
            Runner.make(Hybrid.get()).delay(20).run(() -> {
                game.setState(Game.GameState.RESTORING);
                Hybrid.get().getGameManager().getGames().remove(game);
                Hybrid.get().getGameManager().finish(args[1]);
                commandSender.sendMessage(Util.color("&aArena reiniciada com sucesso."));
            });
        }else if(args[0].equalsIgnoreCase("setBlueSpawn")) {
            if (!(commandSender instanceof Player))
                return false;

            if (args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            Player player = (Player) commandSender;
            if (!commandSender.hasPermission("hybrid.admin"))
                return true;

            String name = args[1];
            if (!Hybrid.get().getGameManager().doesMapExists(name)) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }
            Game game = Hybrid.get().getGameManager().getGameFromName(name);
            game.getArena().setLocation(GameArena.LocationType.BLUE_SPAWN, player.getLocation());
            player.sendMessage(Util.color("&aBlue spawn configurado com sucesso!"));
        }else if(args[0].equalsIgnoreCase("setRedSpawn")) {
            if(!(commandSender instanceof Player))
                return false;

            if(args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            Player player = (Player)commandSender;
            if(!commandSender.hasPermission("hybrid.admin"))
                return true;

            String name = args[1];
            if(!Hybrid.get().getGameManager().doesMapExists(name)) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }
            Game game = Hybrid.get().getGameManager().getGameFromName(name);
            game.getArena().setLocation(GameArena.LocationType.RED_SPAWN, player.getLocation());
            player.sendMessage(Util.color("&aRed spawn configurado com sucesso!"));
        }else if(args[0].equalsIgnoreCase("setLobby")) {
            if (!(commandSender instanceof Player))
                return false;

            if (args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            Player player = (Player) commandSender;
            if (!commandSender.hasPermission("hybrid.admin"))
                return true;

            String name = args[1];
            if (!Hybrid.get().getGameManager().doesMapExists(name)) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }
            Game game = Hybrid.get().getGameManager().getGameFromName(name);
            game.getArena().setLocation(GameArena.LocationType.LOBBY, player.getLocation());
            player.sendMessage(Util.color("&aLobby spawn configurado com sucesso!"));
        }else if(args[0].equalsIgnoreCase("setPoint")) {
            if (!(commandSender instanceof Player))
                return false;

            if (args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            Player player = (Player) commandSender;
            if (!commandSender.hasPermission("hybrid.admin"))
                return true;

            String name = args[1];
            if (!Hybrid.get().getGameManager().doesMapExists(name)) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }
            Game game = Hybrid.get().getGameManager().getGameFromName(name);
            game.getArena().setLocation(GameArena.LocationType.POINT, player.getLocation());
            player.sendMessage(Util.color("&aPoint  configurado com sucesso!"));
        }else if(args[0].equalsIgnoreCase("addPath")) {
            if (!(commandSender instanceof Player))
                return false;

            if (args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            Player p = (Player) commandSender;
            if (!commandSender.hasPermission("hybrid.admin"))
                return true;

            String name = args[1];
            if (!Hybrid.get().getGameManager().doesMapExists(name)) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }
            Game game = Hybrid.get().getGameManager().getGameFromName(name);
            game.getArena().addPath(p.getLocation());
            commandSender.sendMessage(Util.color("&aCaminho adicionado!"));
        }else if(args[0].equalsIgnoreCase("join")) {
            if (!(commandSender instanceof Player))
                return false;

            Player p = (Player)commandSender;
            if (args.length == 1) {
                if (GamePlayer.get(p.getUniqueId()) == null) {
                    p.sendMessage("&cTente novamente em alguns segundos!");
                    return true;
                }
                if (GamePlayer.get(p.getUniqueId()).isInGame()) {
                    p.sendMessage(Util.color("&cVocê já está em jogo!"));
                    return true;
                }
                if (Hybrid.get().getGameManager().findGameFor(GamePlayer.get(p.getUniqueId())) == null) {
                    p.sendMessage(Util.color("Nenhuma partida encontrada."));
                } else {
                    Hybrid.get().getGameManager().findGameFor(GamePlayer.get(p.getUniqueId())).addPlayer(GamePlayer.get(p.getUniqueId()));
                }
            }else {
                if (Hybrid.get().getGameManager().doesMapExists(args[1])) {
                    Game game = Hybrid.get().getGameManager().getGameFromName(args[1]);
                    if (game.getState() != Game.GameState.PRE_GAME && game.getState() != Game.GameState.STARTING) {
                        if (game.getState() == Game.GameState.ENDING || game.getState() == Game.GameState.RESTORING) {
                            p.sendMessage(Util.color("&cArena reiniciando!"));
                            return true;
                        }
                        p.sendMessage(Util.color("&cA partida já começou!"));
                        return true;
                    }
                    if (game.getPlayers().size() >= game.getArena().getMaxPlayers() && !p.hasPermission("hybrid.joinfull")) {
                        p.sendMessage(Util.color("&cPartida lotada"));
                        return true;
                    }

                    if (GamePlayer.get(p.getUniqueId()).isInGame()) {
                        p.sendMessage(Util.color("&cVocê já está em jogo!"));
                        return true;
                    }

                    game.addPlayer(GamePlayer.get(p.getUniqueId()));
                }
            }
        }else if(args[0].equalsIgnoreCase("leave")) {
            if (!(commandSender instanceof Player))
                return false;
            GamePlayer gp = GamePlayer.get(((Player) commandSender).getUniqueId());
            if (gp.isInGame()) {
                gp.getGame().removePlayer(gp, false, true);
            }
        }else if(args[0].equalsIgnoreCase("start")) {
            Game game = null;

            if (args.length == 1) {
                if(commandSender instanceof  Player && GamePlayer.get(((Player) commandSender).getUniqueId()).isInGame()) {
                    game = GamePlayer.get(((Player) commandSender).getUniqueId()).getGame();
                }else{
                    commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                    return true;
                }
            }

            if(game == null) {
                if (!Hybrid.get().getGameManager().doesMapExists(args[1])) {
                    commandSender.sendMessage(Util.color("&cErro: Arena inválida."));
                    return true;
                }
                game = Hybrid.get().getGameManager().getGameFromName(args[1]);
            }
            if(game.getState() == Game.GameState.PRE_GAME || game.getState() == Game.GameState.STARTING) {
                commandSender.sendMessage(Util.color("&aPartida iniciada."));
                game.start();
            }

        }else if(args[0].equalsIgnoreCase("stop")) {
            Game game = null;

            if (args.length == 1) {
                if (commandSender instanceof Player && GamePlayer.get(((Player) commandSender).getUniqueId()).isInGame()) {
                    game = GamePlayer.get(((Player) commandSender).getUniqueId()).getGame();
                } else {
                    commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                    return true;
                }
            }

            if (game == null) {
                if (!Hybrid.get().getGameManager().doesMapExists(args[1])) {
                    commandSender.sendMessage(Util.color("&cErro: Arena inválida."));
                    return true;
                }
                game = Hybrid.get().getGameManager().getGameFromName(args[1]);
            }
            if (game.getState() == Game.GameState.IN_GAME || game.getState() == Game.GameState.ENDING) {
                commandSender.sendMessage(Util.color("&cPartida encerrada."));
                game.shutdown(true);
            }
        }
        return false;
    }
}
