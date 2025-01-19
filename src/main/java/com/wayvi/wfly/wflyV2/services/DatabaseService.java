package com.wayvi.wfly.wflyV2.services;

import com.wayvi.wfly.wflyV2.migrations.CreateUserTableMigration;
import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.DatabaseConnection;
import fr.maxlego08.sarah.MigrationManager;
import fr.maxlego08.sarah.SqliteConnection;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class DatabaseService {

    private File databaseFile;
    private DatabaseConnection connection;
    private Plugin plugin;


    public DatabaseService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void initializeDatabase() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            databaseFile = new File(plugin.getDataFolder(), "database.db");

            if (!databaseFile.exists()) {
                if (databaseFile.createNewFile()) {
                    plugin.getLogger().info("Database file created successfully!");
                }
            }

            DatabaseConfiguration configuration = DatabaseConfiguration.sqlite(true);
            this.connection = new SqliteConnection(configuration, plugin.getDataFolder());


        } catch (Exception e) {
            e.printStackTrace();
        }

        MigrationManager.registerMigration(new CreateUserTableMigration());
        MigrationManager.execute(this.connection, plugin.getLogger()::info);
    }

    public DatabaseConnection getConnection() {
        return connection;
    }
}


