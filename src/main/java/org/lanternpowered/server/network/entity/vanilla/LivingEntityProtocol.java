/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://github.com/LanternPowered>
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) Contributors
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
package org.lanternpowered.server.network.entity.vanilla;

import org.lanternpowered.server.data.key.LanternKeys;
import org.lanternpowered.server.entity.LanternEntityLiving;
import org.lanternpowered.server.network.entity.parameter.ParameterList;
import org.spongepowered.api.data.key.Keys;

public abstract class LivingEntityProtocol<E extends LanternEntityLiving> extends EntityProtocol<E> {

    private float lastHealth;
    private int lastArrowsInEntity;

    protected LivingEntityProtocol(E entity) {
        super(entity);
    }

    @Override
    protected void spawn(ParameterList parameterList) {
        super.spawn(parameterList);
        parameterList.add(EntityParameters.Living.HAND_DATA, (byte) 0);
        parameterList.add(EntityParameters.Living.HEALTH, this.entity.get(Keys.HEALTH).orElse(1.0).floatValue());
        parameterList.add(EntityParameters.Living.ARROWS_IN_ENTITY, this.entity.get(LanternKeys.ARROWS_IN_ENTITY).orElse(0));
        parameterList.add(EntityParameters.Living.POTION_EFFECT_COLOR, 0);
        parameterList.add(EntityParameters.Living.POTION_EFFECT_AMBIENT, false);
    }

    @Override
    protected void update(ParameterList parameterList) {
        super.update(parameterList);
        final float health = this.entity.get(Keys.HEALTH).get().floatValue();
        if (health != this.lastHealth) {
            parameterList.add(EntityParameters.Living.HEALTH, health);
            this.lastHealth = health;
        }
        final int arrowsInEntity = this.entity.get(LanternKeys.ARROWS_IN_ENTITY).orElse(0);
        if (arrowsInEntity != this.lastArrowsInEntity) {
            parameterList.add(EntityParameters.Living.ARROWS_IN_ENTITY, arrowsInEntity);
            this.lastArrowsInEntity = arrowsInEntity;
        }
    }
}
