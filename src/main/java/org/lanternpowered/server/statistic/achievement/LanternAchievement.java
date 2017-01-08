/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.server.statistic.achievement;

import com.google.common.collect.ImmutableSet;
import org.lanternpowered.server.statistic.LanternStatistic;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.statistic.StatisticType;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.translation.Translation;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public final class LanternAchievement extends LanternStatistic implements IAchievement {

    private final Set<Achievement> children = new HashSet<>();
    @Nullable private volatile Set<Achievement> immutableChildren;
    private final long statisticTargetValue;

    @Nullable private final Achievement parent;
    private final Translation description;

    LanternAchievement(String pluginId, String id, String name, Translation translation, String internalId, NumberFormat format,
            @Nullable Criterion criterion, StatisticType type, long statisticTargetValue, @Nullable Achievement parent, Translation description) {
        super(pluginId, id, name, translation, internalId, format, criterion, type);
        this.statisticTargetValue = statisticTargetValue;
        this.parent = parent;
        this.description = description;
    }

    void addChild(LanternAchievement achievement) {
        this.children.add(achievement);
    }

    @Override
    public Translation getDescription() {
        return this.description;
    }

    @Override
    public Optional<Achievement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public Collection<Achievement> getChildren() {
        Set<Achievement> immutableChildren = this.immutableChildren;
        if (immutableChildren == null) {
            immutableChildren = ImmutableSet.copyOf(this.children);
            this.immutableChildren = immutableChildren;
        }
        return immutableChildren;
    }

    @Override
    public Optional<ItemStackSnapshot> getItemStackSnapshot() {
        return Optional.empty();
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public long getStatisticTargetValue() {
        return this.statisticTargetValue;
    }
}