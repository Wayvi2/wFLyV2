package com.wayvi.wfly.wflyv2.migrations.updates;

import fr.maxlego08.sarah.SchemaBuilder;
import fr.maxlego08.sarah.database.Migration;

public class CreateLastUpdateMigration extends Migration {

    @Override
    public void up() {
        SchemaBuilder.alter(this, "fly", schema -> schema.bigInt("lastUpdate").defaultValue(0));
    }

}
