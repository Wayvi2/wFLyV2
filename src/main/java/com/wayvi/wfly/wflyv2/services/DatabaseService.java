package com.wayvi.wfly.wflyv2.services;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.migrations.CreateUserTableMigration;
import fr.maxlego08.sarah.*;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.SQLException;

import static fr.maxlego08.sarah.database.DatabaseType.MYSQL;

/**
 * Handles the initialization and management of the SQLite database for the WFly plugin.
 * This class ensures the database file is created, configured, and migrations are executed on initialization.
 */
public class DatabaseService {

    private File databaseFile;
    private DatabaseConnection connection;
    private final WFlyV2 plugin;

    /**
     * Constructs a new DatabaseService instance.
     *
     * @param plugin the main WFlyV2 plugin instance, used for accessing plugin methods and configurations
     */
    public DatabaseService(WFlyV2 plugin) {
        this.plugin = plugin;
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

            if (plugin.getConfigFile().get(ConfigEnum.MYSQL_ENABLED)) {
                DatabaseConfiguration configuration = DatabaseConfiguration.create(
                        plugin.getConfigFile().get(ConfigEnum.MYSQL_USERNAME),
                        plugin.getConfigFile().get(ConfigEnum.MYSQL_PASSWORD),
                        plugin.getConfigFile().get(ConfigEnum.MYSQL_PORT),
                        plugin.getConfigFile().get(ConfigEnum.MYSQL_HOST),
                        plugin.getConfigFile().get(ConfigEnum.MYSQL_DATABASE),
                        false,
                        MYSQL
                );


                this.connection = new MySqlConnection(configuration);
            } else {
                DatabaseConfiguration configuration = DatabaseConfiguration.sqlite(false);
                this.connection = new SqliteConnection(configuration, plugin.getDataFolder());

            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Cause: " + e.getMessage());

        }

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
