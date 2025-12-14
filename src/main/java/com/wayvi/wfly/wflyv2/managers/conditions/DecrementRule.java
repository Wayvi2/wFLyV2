package com.wayvi.wfly.wflyv2.managers.conditions;

import com.wayvi.wfly.wflyv2.api.conditions.Condition;
import org.bukkit.entity.Player;
import java.util.List;

public class DecrementRule implements Comparable<DecrementRule> {

    private final String name;
    private final int priority;
    private final List<Condition> conditions;

    private final boolean stopTimer;

    public DecrementRule(String name, int priority, List<Condition> conditions, boolean stopTimer) {
        this.name = name;
        this.priority = priority;
        this.conditions = conditions;
        this.stopTimer = stopTimer;
    }

    public boolean canApply(Player player) {
        if (conditions.isEmpty()) return true;
        for (Condition c : conditions) {
            if (!c.check(player)) return false;
        }
        return true;
    }

    public boolean shouldStopTimer() {
        return stopTimer;
    }

    @Override
    public int compareTo(DecrementRule other) {
        return Integer.compare(this.priority, other.priority);
    }
}