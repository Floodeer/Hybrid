package com.floodeer.hybrid.manager;

import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.database.Callback;
import com.floodeer.hybrid.database.DatabaseProvider;
import com.floodeer.hybrid.database.SQLWriter;
import com.floodeer.hybrid.database.data.GamePlayer;
import com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final DatabaseProvider<?> database;

    public DataManager() {
        database = Hybrid.get().getDatabase();
    }

    public void loadPlayer(UUID player, GamePlayer gp) {
        if(database.getConnection() == null)
            return;

        new SQLWriter() {
            final Connection connection = database.getConnection();
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            @Override
            public void onWrite() throws SQLException {
                if(!doesPlayerExists(player))
                    insertPlayer(player.toString());

                String queryBuilder = "SELECT `games_played`, `wins`, `kills`, `damage_caused`, `exp`, `rank`, `balance`, `kit`, `kits`, `wave_record` " +
                        "FROM `wizards_player` " +
                        "WHERE `uuid` = ? " +
                        "LIMIT 1;";

                preparedStatement = connection.prepareStatement(queryBuilder);
                preparedStatement.setString(1, player.toString());
                resultSet = preparedStatement.executeQuery();
                try {
                    if (resultSet != null && resultSet.next()) {
                        gp.setExp(resultSet.getInt("exp"));
                        gp.setRank(resultSet.getString("rank"));
                        gp.setGamesPlayed(resultSet.getInt("games_played"));
                        gp.setWaveRecord(resultSet.getInt("wave_record"));
                        gp.setWins(resultSet.getInt("wins"));
                        gp.setKills(resultSet.getInt("kills"));
                        gp.setDamageCaused(resultSet.getInt("damage_caused"));
                        gp.setBalance(resultSet.getInt("balance"));
                        gp.setKits(new ResultSetDeserializer(resultSet.getString("kits")).execute().toList());
                        gp.setKit(resultSet.getString("kit"));

                        gp.setLoaded(true);
                    }
                }finally {
                    if(preparedStatement != null)
                        preparedStatement.close();
                    if(resultSet != null)
                        resultSet.close();
                }

            }
        }.writeOperation(database.getExecutor(), Hybrid.get().getLogger(), "Error while loading " + player.toString() + "'s data");
    }

    public void updatePlayerAsync(GamePlayer gp) {
        if(database.getConnection() == null)
            return;

        if(!gp.isLoaded())
            return;

        new SQLWriter() {
            final Connection connection = database.getConnection();
            PreparedStatement preparedStatement = null;
            @Override
            public void onWrite() throws SQLException {
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("UPDATE `defensor_player` SET ");
                queryBuilder.append("`playername` = ?, `games_played` = ?, `wins` = ?, `wave_record` = ?, ");
                queryBuilder.append("`kills` = ?, `damage_caused` = ?, balance` = ?, `kit` = ?, `kits` = ?, `exp` = ?, `rank` = ? ");
                queryBuilder.append("WHERE `uuid` = ?;");

                try {
                    preparedStatement = connection.prepareStatement(queryBuilder.toString());
                    preparedStatement.setString(1, gp.getName());
                    preparedStatement.setInt(2, gp.getGamesPlayed());
                    preparedStatement.setInt(3, gp.getWins());
                    preparedStatement.setInt(4, gp.getWaveRecord());
                    preparedStatement.setInt(5, gp.getKills());
                    preparedStatement.setDouble(6, gp.getDamageCaused());
                    preparedStatement.setInt(7, gp.getBalance());
                    preparedStatement.setString(8, gp.getKit());
                    preparedStatement.setString(9, new StatementSerializer(gp.getKits()).execute().get());
                    preparedStatement.setInt(10, gp.getExp());
                    preparedStatement.setString(11, gp.getRank());
                    preparedStatement.setString(12, gp.getUUID().toString());

                    preparedStatement.executeUpdate();

                }finally {
                    if(preparedStatement != null)
                        preparedStatement.close();
                }
            }
        }.writeOperation(database.getExecutor(), Hybrid.get().getLogger(), "Error while saving " + gp.getName() + "'s data");
    }

    public void updatePlayer(GamePlayer gp) {
        if(database.getConnection() == null)
            return;

        if(!gp.isLoaded())
            return;

        final Connection connection = database.getConnection();
        PreparedStatement preparedStatement = null;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE `defensor_player` SET ");
        queryBuilder.append("`playername` = ?, `games_played` = ?, `wins` = ?, `wave_record` = ?, ");
        queryBuilder.append("`kills` = ?, `damage_caused` = ?, balance` = ?, `kit` = ?, `kits` = ?, `exp` = ?, `rank` = ? ");
        queryBuilder.append("WHERE `uuid` = ?;");

        try {
            preparedStatement = connection.prepareStatement(queryBuilder.toString());
            preparedStatement.setString(1, gp.getName());
            preparedStatement.setInt(2, gp.getGamesPlayed());
            preparedStatement.setInt(3, gp.getWins());
            preparedStatement.setInt(4, gp.getWaveRecord());
            preparedStatement.setInt(5, gp.getKills());
            preparedStatement.setDouble(6, gp.getDamageCaused());
            preparedStatement.setInt(7, gp.getBalance());
            preparedStatement.setString(8, gp.getKit());
            preparedStatement.setString(9, new StatementSerializer(gp.getKits()).execute().get());
            preparedStatement.setInt(10, gp.getExp());
            preparedStatement.setString(11, gp.getRank());
            preparedStatement.setString(12, gp.getUUID().toString());

            preparedStatement.executeUpdate();
        }catch(SQLException ex) {
            ex.printStackTrace();
        }finally {
            if(preparedStatement != null)
                try {
                    preparedStatement.close();
                } catch (SQLException ignored) {}
        }
    }


    public void getData(Object identifier, String data, Callback<String> result) {
        if(database.getConnection() == null)
            return;

        database.getExecutor().execute(new Runnable() {
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            @Override
            public void run() {
                String query = "SELECT `balance` FROM `defensor_player` WHERE `" + (identifier instanceof UUID ? "uuid" : "playername") + "` = ? LIMIT 1;";
                try {
                    preparedStatement = database.getConnection().prepareStatement(query);
                    preparedStatement.setString(1, identifier instanceof UUID ? identifier.toString() : ((String)identifier));
                    resultSet = preparedStatement.executeQuery();
                    result.onCall(resultSet.getString(data));

                }catch(SQLException ex) {
                    ex.printStackTrace();
                }finally {
                    try {
                        if (preparedStatement != null)
                            preparedStatement.close();
                        if (resultSet != null)
                            resultSet.close();
                    } catch (final SQLException ignored) {
                    }
                }
            }
        });
    }

    public boolean doesPlayerExists(Object identifier) {
        int count = 0;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            String query = "SELECT Count(`player_id`) FROM `defensor_player` WHERE `" + (identifier instanceof UUID ? "uuid" : "playername") + "` = ? LIMIT 1;";

            preparedStatement = database.getConnection().prepareStatement(query);
            preparedStatement.setString(1, identifier instanceof UUID ? identifier.toString() : ((String)identifier));
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

        } catch (final SQLException sqlException) {
            sqlException.printStackTrace();

        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (final SQLException ignored) {
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (final SQLException ignored) {
                }
            }
        }

        return count > 0;
    }

    public void doesPlayerExists(Object identifier, Callback<Boolean> result) {
        if(database.getConnection() == null)
            return;

        database.getExecutor().execute(new Runnable() {
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;

            @Override
            public void run() {
                String query = "SELECT Count(`player_id`) FROM `defensor_player` WHERE `" + (identifier instanceof UUID ? "uuid" : "playername") + "` = ? LIMIT 1;";
                try {
                    preparedStatement = database.getConnection().prepareStatement(query);
                    preparedStatement.setString(1, identifier instanceof UUID ? identifier.toString() : ((String)identifier));
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) > 0)
                            result.onCall(true);
                        else
                            result.onCall(false);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        if (preparedStatement != null)
                            preparedStatement.close();
                        if (resultSet != null)
                            resultSet.close();
                    } catch (final SQLException ignored) {
                    }
                }
            }
        });
    }

    private void insertPlayer(String uid) {
        if(database.getConnection() == null)
            return;

        UUID uuid = UUID.fromString(uid);
        PreparedStatement preparedStatement = null;

        try {

            String queryBuilder = "INSERT INTO `defensor_player` " +
                    "(`player_id`, `uuid`, `playername`) " +
                    "VALUES " +
                    "(NULL, ?, ?);";
            preparedStatement = database.getConnection().prepareStatement(queryBuilder);
            preparedStatement.setString(1, uid);
            preparedStatement.setString(2, Hybrid.get().getServer().getPlayer(uuid).getName());
            preparedStatement.executeUpdate();

        } catch (final SQLException sqlException) {
            sqlException.printStackTrace();

        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (final SQLException ignored) {
                }
            }
        }
    }

    protected class StatementSerializer {

        private String result;
        private final List<String> list;

        public StatementSerializer(List<String> list) {
            this.list = list;
        }

        public StatementSerializer execute() {
            StringBuilder tList = new StringBuilder();
            String toSepare = "";
            for (String tL : list) {
                tList.append(toSepare);
                tList.append(tL);
                toSepare = ", ";
            }
            if(list.isEmpty())
                this.result = "";
            else
                this.result = tList.toString();
            return this;
        }

        public String get() {
            return result;
        }
    }

    protected class ResultSetDeserializer {

        private List<String> newList;
        private final String list;

        public ResultSetDeserializer(String list) {
            this.list = list;
        }

        public ResultSetDeserializer execute() {
            this.newList = Lists.newArrayList();
            String[] parts = list.split(", ");
            Collections.addAll(newList, parts);
            return this;
        }

        public List<String> toList() {
            return newList;
        }
    }
}
