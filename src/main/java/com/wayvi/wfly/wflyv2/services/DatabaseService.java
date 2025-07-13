package com.wayvi.wfly.wflyv2.services;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.migrations.CreateUserTableMigration;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import fr.maxlego08.sarah.*;

import java.io.File;

import static fr.maxlego08.sarah.database.DatabaseType.MYSQL;

/**
 * Handles the initialization and management of the SQLite database for the WFly plugin.
 * This class ensures the database file is created, configured, and migrations are executed on initialization.
 */
public class DatabaseService {

    private ConfigUtil configUtil;
    private File databaseFile;
    private DatabaseConnection connection;
    private final WFlyV2 plugin;

    /**
     * Constructs a new DatabaseService instance.
     *
     * @param plugin the main WFlyV2 plugin instance, used for accessing plugin methods and configurations
     */
    public DatabaseService(WFlyV2 plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    /**
     * Initializes the SQLite database by creating the necessary database file and establishing a connection.
     * If the database file doesn't exist, it is created. The method also registers and executes migrations.
     * The migration "CreateUserTableMigration" is executed to ensure that the necessary tables are set up in the database.
     */
    public void initializeDatabase() {
        try {
            // Ensure the plugin's data folder exists
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            // Define the database file
            databaseFile = new File(plugin.getDataFolder(), "database.db");

            // Create the database file if it doesn't exist
            if (!databaseFile.exists()) {
                if (databaseFile.createNewFile()) {
                    plugin.getLogger().info("Database file created successfully!");
                }
            }
            // Configure and create a new SQLite database connection
            if (configUtil.getCustomConfig().getBoolean("mysql.enabled")){
                DatabaseConfiguration configuration = DatabaseConfiguration.create(configUtil.getCustomConfig().getString("mysql.username"),
                                                                                   configUtil.getCustomConfig().getString("mysql.password"),
                                                                                   configUtil.getCustomConfig().getInt("mysql.port"),
                                                                                   configUtil.getCustomConfig().getString("mysql.host"),
                                                                                   configUtil.getCustomConfig().getString("mysql.database"),
                                                                             false,
                                                                                   MYSQL);
                this.connection = new MySqlConnection(configuration);
            } else {
                DatabaseConfiguration configuration = DatabaseConfiguration.sqlite(false);
                this.connection = new SqliteConnection(configuration, plugin.getDataFolder());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Register and execute migrations
        MigrationManager.registerMigration(new CreateUserTableMigration());
        MigrationManager.execute(this.connection, plugin.getLogger()::info);
    }

    /**
     * Gets the current database connection.
     *
     * @return the active DatabaseConnection
     */
    public DatabaseConnection getConnection() {
        return this.connection;
    }
}
