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
package org.lanternpowered.server.network.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

public final class EntityProtocolType {

    private final List<ParameterType<?>> parameterTypes;

    public EntityProtocolType() {
        this(new ArrayList<>());
    }

    private EntityProtocolType(List<ParameterType<?>> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     * Copies this {@link EntityProtocolType}.
     */
    public EntityProtocolType copy() {
        return new EntityProtocolType(new ArrayList<>(this.parameterTypes));
    }

    /**
     * Creates a new {@link ParameterType}.
     *
     * @param valueType The parameter value type
     * @param <T> The value type
     * @return The parameter type
     */
    public <T> ParameterType<T> newParameterType(ParameterValueType<T> valueType) {
        final ParameterType<T> parameterType = new ParameterType<>(
                this.parameterTypes.size(), checkNotNull(valueType, "valueType"));
        this.parameterTypes.add(parameterType);
        return parameterType;
    }
}