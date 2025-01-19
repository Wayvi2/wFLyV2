package com.wayvi.wfly.wflyV2.migrations;

import fr.maxlego08.sarah.database.Migration;

public class CreateUserTableMigration extends Migration {

    @Override
    public void up() {
        create("fly",table ->{
            table.uuid("unique_id").primary();
            table.integer("isinFly");
        });

    }
}
