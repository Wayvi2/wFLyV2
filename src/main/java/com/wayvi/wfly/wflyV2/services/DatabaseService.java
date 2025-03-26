package com.wayvi.wfly.wflyV2.services;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.migrations.CreateUserTableMigration;
import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.DatabaseConnection;
import fr.maxlego08.sarah.MigrationManager;
import fr.maxlego08.sarah.SqliteConnection;

import java.io.File;

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

            // Configure and create a new SQLite database connection
            DatabaseConfiguration configuration = DatabaseConfiguration.sqlite(false);
            this.connection = new SqliteConnection(configuration, plugin.getDataFolder());

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
