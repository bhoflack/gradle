package org.gradle.internal;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static org.gradle.util.Clock.prettyTime;

/**
 * By Szczepan Faber on 5/16/13
 */
public class Stats {

    Map<String, Long> stats = new HashMap<String, Long>();

    public void add(String stat, long started) {
        long duration = currentTimeMillis() - started;
        Long current = stats.get(stat);
        if (current == null) {
            stats.put(stat, duration);
        } else {
            stats.put(stat, current + duration);
        }
    }

    public String toString() {
        String out = "";
        for (String stat : stats.keySet()) {
            out += stat + ":" + prettyTime(stats.get(stat)) + ", ";
        }
        return out;
    }
}
