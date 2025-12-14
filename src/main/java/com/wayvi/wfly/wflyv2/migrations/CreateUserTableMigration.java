package com.wayvi.wfly.wflyv2.migrations;

import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import fr.maxlego08.sarah.database.Migration;

/**
 * A migration class responsible for creating the 'fly' table in the database.
 * Extends the Migration class to define the schema changes for the database.
 */
public class CreateUserTableMigration extends Migration {

    /**
     * Runs the migration to create the 'fly' table.
     * This method is executed when the migration is applied.
     */
    @Override
    public void up() {
        this.createOrAlter("fly", AccessPlayerDTO.class);
    }
}
