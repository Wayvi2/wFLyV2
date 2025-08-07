package com.wayvi.wfly.wflyv2.commands.converter;

import com.wayvi.wfly.wflyv2.constants.ToggleType;
import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ToggleTypeConverter implements ArgumentConverter<ToggleType>, TabCompleter  {
    @Override
    public ToggleType apply(String input) {
        for (ToggleType type : ToggleType.values()) {
            if (type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }


    @Override
    public List<String> onCompletion(Object o, List list) {
        return Arrays.stream(ToggleType.values())
                .map(ToggleType::getToggleType)
                .collect(Collectors.toList());
    }

}