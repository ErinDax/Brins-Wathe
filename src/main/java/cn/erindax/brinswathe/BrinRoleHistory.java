package cn.erindax.brinswathe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import org.jetbrains.annotations.Nullable;

public final class BrinRoleHistory {
    public static final double QUANTUM = 1024.0;
    private static final double FORGET_BELOW = 0.01;
    private static final double CAPPED_OFFSET = 1.0E6;

    private final Map<UUID, Map<String, Entry>> players = new HashMap<>();

    public double weight(UUID player, String category, Collection<UUID> pool, int slots, double strength, int maxStreak) {
        if (this.capped(player, category, maxStreak) && this.uncapped(pool, category, maxStreak) >= Math.max(1, slots)) {
            return 0.0;
        }
        return this.heatWeight(player, category, strength);
    }

    public double heatWeight(UUID player, String category, double strength) {
        Entry entry = this.find(player, category);
        double raw = Math.exp(-strength * (entry == null ? 0.0 : entry.heat));
        return Math.max(1.0, Math.rint(raw * QUANTUM)) / QUANTUM;
    }

    public boolean capped(UUID player, String category, int maxStreak) {
        if (maxStreak <= 0) return false;
        Entry entry = this.find(player, category);
        return entry != null && entry.streak >= maxStreak;
    }

    public List<Integer> order(List<UUID> candidates, String category, double strength, int maxStreak, DoubleSupplier uniform) {
        List<Ranked> ranked = new ArrayList<>(candidates.size());
        for (int index = 0; index < candidates.size(); index++) {
            UUID candidate = candidates.get(index);
            double key = Math.log(1.0 - uniform.getAsDouble());
            if (this.capped(candidate, category, maxStreak)) {
                key -= CAPPED_OFFSET;
            } else {
                key /= this.heatWeight(candidate, category, strength);
            }
            ranked.add(new Ranked(index, key));
        }
        ranked.sort(Comparator.comparingDouble(Ranked::key).reversed());
        List<Integer> order = new ArrayList<>(ranked.size());
        for (Ranked item : ranked) order.add(item.index());
        return order;
    }

    public void record(Map<UUID, Set<String>> held, double decay) {
        Iterator<Map.Entry<UUID, Map<String, Entry>>> playerIterator = this.players.entrySet().iterator();
        while (playerIterator.hasNext()) {
            Map.Entry<UUID, Map<String, Entry>> player = playerIterator.next();
            Set<String> current = held.getOrDefault(player.getKey(), Set.of());
            Iterator<Map.Entry<String, Entry>> entryIterator = player.getValue().entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, Entry> item = entryIterator.next();
                Entry entry = item.getValue();
                boolean kept = current.contains(item.getKey());
                entry.heat = entry.heat * decay + (kept ? 1.0 : 0.0);
                entry.streak = kept ? entry.streak + 1 : 0;
                if (!kept && entry.heat < FORGET_BELOW) entryIterator.remove();
            }
            if (player.getValue().isEmpty()) playerIterator.remove();
        }
        for (Map.Entry<UUID, Set<String>> player : held.entrySet()) {
            Map<String, Entry> entries = this.players.computeIfAbsent(player.getKey(), id -> new HashMap<>());
            for (String category : player.getValue()) {
                entries.computeIfAbsent(category, key -> new Entry(1.0, 1));
            }
        }
    }

    public void clear() {
        this.players.clear();
    }

    @Nullable
    private Entry find(UUID player, String category) {
        Map<String, Entry> entries = this.players.get(player);
        return entries == null ? null : entries.get(category);
    }

    private int uncapped(Collection<UUID> pool, String category, int maxStreak) {
        int count = 0;
        for (UUID candidate : pool) {
            if (!this.capped(candidate, category, maxStreak)) count++;
        }
        return count;
    }

    private static final class Entry {
        private double heat;
        private int streak;

        private Entry(double heat, int streak) {
            this.heat = heat;
            this.streak = streak;
        }
    }

    private record Ranked(int index, double key) {
    }
}
