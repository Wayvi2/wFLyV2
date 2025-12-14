package com.wayvi.wfly.wflyv2.managers.conditions;

import com.wayvi.wfly.wflyv2.api.conditions.Condition;
import com.wayvi.wfly.wflyv2.constants.conditions.FlyResult;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FlyRule implements Comparable<FlyRule> {

    private final String id;

    private final int priority;

    private final List<Condition> conditions;

    private final FlyResult result;

    private final List<String> actions;


    public FlyRule(String id, int priority, List<Condition> conditions, FlyResult result, List<String> actions) {
        this.id = id;
        this.priority = priority;
        this.result = result;

        this.conditions = (conditions != null) ? conditions : new ArrayList<>();
        this.actions = (actions != null) ? actions : new ArrayList<>();
    }


    public boolean canApply(Player player) {
        if (conditions.isEmpty()) {
            return true;
        }

        for (Condition cond : conditions) {
            if (!cond.check(player)) {
                return false;
            }
        }

        return true;
    }


    @Override
    public int compareTo(FlyRule other) {
        return Integer.compare(this.priority, other.priority);
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public FlyResult getResult() {
        return result;
    }

    public List<String> getActions() {
        return actions;
    }


    public List<Condition> getConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        return "FlyRule{id='" + id + "', priority=" + priority + ", result=" + result + "}";
    }
}