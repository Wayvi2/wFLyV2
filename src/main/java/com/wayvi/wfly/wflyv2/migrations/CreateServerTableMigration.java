package com.wayvi.wfly.wflyv2.migrations;


import com.wayvi.wfly.wflyv2.storage.models.AccessServerDTO;

import fr.maxlego08.sarah.database.Migration;

public class CreateServerTableMigration extends Migration {


    @Override
    public void up() {
        create("server", AccessServerDTO.class);
    }
}
