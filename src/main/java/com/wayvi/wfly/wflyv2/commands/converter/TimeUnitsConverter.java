package com.wayvi.wfly.wflyv2.commands.converter;

import com.wayvi.wfly.wflyv2.constants.commands.TimeUnits;
import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;


public class TimeUnitsConverter implements ArgumentConverter<TimeUnits>, TabCompleter<CommandSender> {
    @Override
    public TimeUnits apply(String input) {
        for (TimeUnits type : TimeUnits.values()) {
            if (type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }


    @Override
    public List<String> onCompletion(CommandSender commandSender, List<String> list) {
        return Arrays.asList(TimeUnits.DAYS.getTimeUnits(), TimeUnits.HOURS.getTimeUnits(),TimeUnits.MINUTES.getTimeUnits() ,TimeUnits.SECONDS.getTimeUnits());
    }
}