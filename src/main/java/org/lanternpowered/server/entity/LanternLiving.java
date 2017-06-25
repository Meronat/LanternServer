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
package org.lanternpowered.server.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import org.lanternpowered.server.data.key.LanternKeys;
import org.lanternpowered.server.effect.potion.LanternPotionEffectType;
import org.lanternpowered.server.world.rules.RuleTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LanternLiving extends LanternEntity implements Living {

    private Vector3d headRotation = Vector3d.ZERO;
    private long lastFoodTime = System.currentTimeMillis();
    private long lastPeacefulFoodTime = System.currentTimeMillis();

    public LanternLiving(UUID uniqueId) {
        super(uniqueId);
    }

    @Override
    public void registerKeys() {
        super.registerKeys();
        registerKey(Keys.MAX_AIR, 300, 0, Integer.MAX_VALUE);
        registerKey(Keys.REMAINING_AIR, 300, 0, Keys.MAX_AIR);
        registerKey(Keys.MAX_HEALTH, 20.0, 0.0, 1024.0);
        registerKey(Keys.HEALTH, 20.0, 0.0, Keys.MAX_HEALTH);
        registerKey(Keys.POTION_EFFECTS, new ArrayList<>());
    }

    protected void setRawHeadRotation(Vector3d rotation) {
        this.headRotation = checkNotNull(rotation, "rotation");
    }

    @Override
    public Vector3d getHeadRotation() {
        return this.headRotation;
    }

    @Override
    public void setHeadRotation(Vector3d rotation) {
        setRawHeadRotation(rotation);
    }

    @Override
    public Text getTeamRepresentation() {
        return Text.of(getUniqueId().toString());
    }

    @Override
    public void pulse() {
        super.pulse();

        pulsePotions();
        pulseFood();
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass) {
        return Optional.empty();
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass, Vector3d velocity) {
        return Optional.empty();
    }

    private void pulsePotions() {
        // TODO: Move potion effects to a component? + The key registration
        final List<PotionEffect> potionEffects = get(Keys.POTION_EFFECTS).get();
        if (!potionEffects.isEmpty()) {
            final PotionEffect.Builder builder = PotionEffect.builder();
            final ImmutableList.Builder<PotionEffect> newPotionEffects = ImmutableList.builder();
            for (PotionEffect potionEffect : potionEffects) {
                final boolean instant = potionEffect.getType().isInstant();
                final int duration = instant ? 1 : potionEffect.getDuration() - 1;
                if (duration > 0) {
                    final PotionEffect newPotionEffect = builder.from(potionEffect).duration(duration).build();
                    ((LanternPotionEffectType) newPotionEffect.getType()).getEffectConsumer().accept(this, newPotionEffect);
                    if (!instant) {
                        newPotionEffects.add(newPotionEffect);
                    }
                }
                if (potionEffect.getType() == PotionEffectTypes.GLOWING) {
                    offer(Keys.GLOWING, duration > 0);
                }
                if (potionEffect.getType() == PotionEffectTypes.INVISIBILITY) {
                    offer(Keys.INVISIBLE, duration > 0);
                }
                if (potionEffect.getType() == PotionEffectTypes.HUNGER && supports(Keys.EXHAUSTION)) {
                    offer(Keys.EXHAUSTION, Math.min(get(Keys.EXHAUSTION).orElse(0.0) + (0.005 * (potionEffect.getAmplifier() + 1)),
                            get(LanternKeys.MAX_EXHAUSTION).orElse(Double.MAX_VALUE)));
                }
            }
            offer(Keys.POTION_EFFECTS, newPotionEffects.build());
        }
    }

    private void pulseFood() {
        if (!get(FoodData.class).isPresent()) {
            return;
        }
        final Difficulty difficulty = getWorld().getDifficulty();

        if (get(Keys.EXHAUSTION).get() > 4.0) {
            final MutableBoundedValue<Double> saturation = getValue(Keys.SATURATION).get();

            if (saturation.get() > saturation.getMinValue()) {
                offer(Keys.SATURATION, Math.max(saturation.get() - 1.0, saturation.getMinValue()));
            } else if (!difficulty.equals(Difficulties.PEACEFUL)) {
                offer(Keys.FOOD_LEVEL, Math.max(get(Keys.FOOD_LEVEL).get() - 1, getValue(Keys.FOOD_LEVEL).get().getMinValue()));
            }

            offer(Keys.EXHAUSTION, get(Keys.EXHAUSTION).get() - 4.0);
        }

        final boolean naturalRegeneration = getWorld().getOrCreateRule(RuleTypes.NATURAL_REGENERATION).getValue();

        final MutableBoundedValue<Double> saturation = getValue(Keys.SATURATION).get();
        final MutableBoundedValue<Integer> foodLevel = getValue(Keys.FOOD_LEVEL).get();
        final MutableBoundedValue<Double> exhaustion = getValue(Keys.EXHAUSTION).get();

        final long currentTime = System.currentTimeMillis();

        if (naturalRegeneration && canBeHealed() && saturation.get() > saturation.getMinValue() && foodLevel.get() >= foodLevel.getMaxValue()) {
            if ((currentTime - this.lastFoodTime) >= 500) {
                final double amount = Math.min(saturation.get(), 6.0);
                heal(amount / 6.0);
                offer(Keys.EXHAUSTION, Math.min(amount + exhaustion.get(), exhaustion.getMaxValue()));
                this.lastFoodTime = currentTime;
            }
        } else if (naturalRegeneration && canBeHealed() && foodLevel.get() >= 18) {
            if ((currentTime - this.lastFoodTime) >= 4000) {
                heal(1.0);
                offer(Keys.EXHAUSTION, Math.min(6.0 + exhaustion.get(), exhaustion.getMaxValue()));
                this.lastFoodTime = currentTime;
            }
        } else if (foodLevel.get() <= foodLevel.getMinValue()) {
            if ((currentTime - this.lastFoodTime) >= 4000) {
                if (get(Keys.HEALTH).get() > 10.0 || getWorld().getDifficulty().equals(Difficulties.HARD)
                        || get(Keys.HEALTH).get() > 1.0 && difficulty.equals(Difficulties.NORMAL)) {
                    damage(1.0, DamageSources.STARVATION);
                }
                this.lastFoodTime = currentTime;
            }
        } else {
            this.lastFoodTime = currentTime;
        }

        if (difficulty.equals(Difficulties.PEACEFUL) && naturalRegeneration) {
            if (((currentTime - this.lastPeacefulFoodTime) >= 1000) && get(Keys.HEALTH).orElse(0.0) < get(Keys.MAX_HEALTH).orElse(Double.MAX_VALUE)) {
                heal(1);
                this.lastPeacefulFoodTime = currentTime;
            }

            final int oldFoodLevel = get(Keys.FOOD_LEVEL).orElse(0);
            if ((((currentTime - this.lastPeacefulFoodTime) / 2) >= 500) && oldFoodLevel < 20) {
                offer(Keys.FOOD_LEVEL, oldFoodLevel + 1);
            }
        }
    }

    /**
     * Whether or not this entity can be healed properly.
     *
     * <p>If they aren't dead and have less than max health
     * they can be healed.</p>
     *
     * @return If this entity can be healed
     */
    public boolean canBeHealed() {
        final MutableBoundedValue<Double> health = health();
        return health.get() > health.getMinValue() && health.get() < health.getMaxValue();
    }

    /**
     * Heals the entity for the specified amount.
     *
     * <p>Will not heal them if they are dead and will not set
     * them above their maximum health.</p>
     *
     * @param amount The amount to heal for
     */
    public void heal(double amount) {
        final MutableBoundedValue<Double> health = health();
        if (health.get() > health.getMinValue()) {
            offer(Keys.HEALTH, Math.min(health.get() + amount, health.getMaxValue()));
        }
    }

}
