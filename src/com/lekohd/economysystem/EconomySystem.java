package com.lekohd.economysystem;

/**
 * Created by Leon on 19.05.2015.
 * Project EconomySystem
 * <p/>
 * Copyright (C) 2014 Leon167 { LekoHD
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomySystem extends JavaPlugin
        implements Listener
{
    String DB_NAME = "jdbc:mysql://" + getConfig().getString("MySQL.Host") + ":" + getConfig().getInt("MySQL.Port") + "/" + getConfig().getString("MySQL.Database");
    String USER = getConfig().getString("MySQL.Username");
    String PASS = getConfig().getString("MySQL.Password");
    public static Connection conn;
    public static Statement s;

    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this, this);

        getConfig().options().copyDefaults(true);
        saveConfig();
        try
        {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(this.DB_NAME, this.USER, this.PASS);

            s = conn.createStatement();

            Bukkit.getLogger().warning("Successfuly connected to the mysql database!");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Bukkit.getLogger().warning("Cannot connect to MySQL database");
        }

        try
        {
            Statement check = conn.createStatement();

            check.executeUpdate("CREATE TABLE IF NOT EXISTS `NetworkCoins` (UUID text, coins int, gems int);");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static boolean hasConnection()
    {
        try {
            return conn != null || conn.isValid(1);
        } catch (SQLException sqle) {
            return false;
        }
    }

    public static void addCoins(Player p, Integer amount)
    {
        try
        {
            String uuid = p.getUniqueId().toString();
            Statement check = conn.createStatement();

            ResultSet res = check.executeQuery("SELECT * FROM NetworkCoins WHERE UUID ='" + uuid + "';");
            res.next();

            if (res.getString("UUID") != null) {
                int beforecoins = res.getInt("coins");
                Statement update = conn.createStatement();
                update.executeUpdate("UPDATE NetworkCoins SET coins = " + (beforecoins + amount.intValue()) + " WHERE UUID = '" + uuid + "';");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addGems(Player p, Integer amount)
    {
        try
        {
            String uuid = p.getUniqueId().toString();
            Statement check = conn.createStatement();

            ResultSet res = check.executeQuery("SELECT * FROM NetworkCoins WHERE UUID ='" + uuid + "';");
            res.next();

            if (res.getString("UUID") != null) {
                int beforegems = res.getInt("gems");
                Statement update = conn.createStatement();
                update.executeUpdate("UPDATE NetworkCoins SET gems = " + (beforegems + amount.intValue()) + " WHERE UUID = '" + uuid + "';");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeCoins(Player p, Integer amount)
    {
        try {
            String uuid = p.getUniqueId().toString();
            Statement check = conn.createStatement();

            ResultSet res = check.executeQuery("SELECT * FROM NetworkCoins WHERE UUID ='" + uuid + "';");
            res.next();

            if (res.getString("UUID") != null) {
                int beforecoins = res.getInt("coins");
                Statement update = conn.createStatement();
                update.executeUpdate("UPDATE NetworkCoins SET coins = " + (beforecoins - amount.intValue()) + " WHERE UUID = '" + uuid + "';");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeGems(Player p, Integer amount)
    {
        try {
            String uuid = p.getUniqueId().toString();
            Statement check = conn.createStatement();

            ResultSet res = check.executeQuery("SELECT * FROM NetworkCoins WHERE UUID ='" + uuid + "';");
            res.next();

            if (res.getString("UUID") != null) {
                int beforegems = res.getInt("gems");
                Statement update = conn.createStatement();
                update.executeUpdate("UPDATE NetworkCoins SET gems = " + (beforegems - amount.intValue()) + " WHERE UUID = '" + uuid + "';");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Integer getCoins(Player p) throws SQLException {
        String uuid = p.getUniqueId().toString();
        Statement check = conn.createStatement();

        ResultSet res = check.executeQuery("SELECT * FROM NetworkCoins WHERE UUID ='" + uuid + "';");
        res.next();
        int amount = res.getInt("coins");
        return Integer.valueOf(amount);
    }

    public static Integer getGems(Player p) throws SQLException {
        String uuid = p.getUniqueId().toString();
        Statement check = conn.createStatement();

        ResultSet res = check.executeQuery("SELECT * FROM NetworkCoins WHERE UUID ='" + uuid + "';");
        res.next();
        int amount = res.getInt("gems");
        return Integer.valueOf(amount);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        try {
            String uuid = p.getUniqueId().toString();
            Statement check = conn.createStatement();

            ResultSet res = check.executeQuery("SELECT UUID FROM NetworkCoins WHERE UUID = '" + uuid + "';");
            if (!res.next()) {
                Statement update = conn.createStatement();
                update.executeUpdate("INSERT INTO NetworkCoins VALUES ('" + uuid + "',0,0);");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLable, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("coins"))
        {
            if (args.length == 0) {
                try {
                    sender.sendMessage("§7Coins: §a" + getCoins((Player)sender));
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
                return true;
            }
            if ((args[0].equalsIgnoreCase("add")) &&
                    (sender.hasPermission("coins.add")))
            {
                if (args.length == 1) {
                    sender.sendMessage("§6Coins> /coins add {Player Name} {Amount}");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }

                if (args.length == 2) {
                    sender.sendMessage("§6Coins> /coins add {Player Name} {Amount}");
                    return true;
                }
                int toAdd = Integer.parseInt(args[2]);

                addCoins(target, Integer.valueOf(toAdd));

                sender.sendMessage("§6Coins> You have added §a" + toAdd + " §6to §7" + args[1] + "'s §6account");
            }

            if ((args[0].equalsIgnoreCase("remove")) &&
                    (sender.hasPermission("coins.remove"))) {
                if (args.length == 1) {
                    sender.sendMessage("§6Coins> /coins remove {Player Name} {Amount}");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);

                if (args.length == 2) {
                    sender.sendMessage("§6Coins> /coins remove {Player Name} {Amount}");
                    return true;
                }
                int toRemove = Integer.parseInt(args[2]);

                removeCoins(target, Integer.valueOf(toRemove));

                sender.sendMessage("§6Coins> You have removed §a" + toRemove + " §6from §7" + args[1] + "'s §6account");
            }

        }
        if (cmd.getName().equalsIgnoreCase("gems"))
        {
            if (args.length == 0) {
                try {
                    sender.sendMessage("§7Gems: §a" + getGems((Player)sender));
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
                return true;
            }
            if ((args[0].equalsIgnoreCase("add")) &&
                    (sender.hasPermission("gems.add")))
            {
                if (args.length == 1) {
                    sender.sendMessage("§6Gems> /gems add {Player Name} {Amount}");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }

                if (args.length == 2) {
                    sender.sendMessage("§6Gems> /gems add {Player Name} {Amount}");
                    return true;
                }
                int toAdd = Integer.parseInt(args[2]);

                addGems(target, Integer.valueOf(toAdd));

                sender.sendMessage("§6Gems> You have added §a" + toAdd + " §6to §7" + args[1] + "'s §6account");
            }

            if ((args[0].equalsIgnoreCase("remove")) &&
                    (sender.hasPermission("gems.remove"))) {
                if (args.length == 1) {
                    sender.sendMessage("§6Gems> /gems remove {Player Name} {Amount}");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);

                if (args.length == 2) {
                    sender.sendMessage("§6Gems> /gems remove {Player Name} {Amount}");
                    return true;
                }
                int toRemove = Integer.parseInt(args[2]);

                removeGems(target, Integer.valueOf(toRemove));

                sender.sendMessage("§6Gems> You have removed §a" + toRemove + " §6from §7" + args[1] + "'s §6account");
            }

        }

        return false;
    }

}
