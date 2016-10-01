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

import org.lanternpowered.server.entity.LanternEntityLiving;
import org.lanternpowered.server.network.entity.parameter.ParameterList;
import org.spongepowered.api.data.key.Keys;

public class SlimeEntityProtocol<E extends LanternEntityLiving> extends MobEntityProtocol<E> {

    private int lastSize;

    public SlimeEntityProtocol(E entity) {
        super(entity);
    }

    @Override
    protected int getMobType() {
        return 55;
    }

    @Override
    protected void spawn(ParameterList parameterList) {
        parameterList.add(EntityParameters.Slime.SIZE, this.entity.get(Keys.SLIME_SIZE).orElse(1));
    }

    @Override
    protected void update(ParameterList parameterList) {
        final int size = this.entity.get(Keys.SLIME_SIZE).orElse(1);
        if (this.lastSize != size) {
            parameterList.add(EntityParameters.Slime.SIZE, size);
            this.lastSize = size;
        }
    }
}
