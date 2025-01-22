package com.wayvi.wfly.wflyV2.migrations;

import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import fr.maxlego08.sarah.database.Migration;

public class CreateUserTableMigration extends Migration {

    @Override
    public void up() {
        create("fly", AccessPlayerDTO.class);
    }
}
