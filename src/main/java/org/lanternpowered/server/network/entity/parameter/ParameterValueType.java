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
package org.lanternpowered.server.network.entity.parameter;

import org.lanternpowered.server.network.buffer.ByteBuffer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public final class ParameterValueType<T> {

    private final static AtomicInteger COUNTER = new AtomicInteger();

    private final byte internalId;
    private final BiConsumer<ByteBuffer, T> serializer;

    ParameterValueType(BiConsumer<ByteBuffer, T> serializer) {
        this.internalId = (byte) COUNTER.getAndIncrement();
        this.serializer = serializer;
    }

    /**
     * Gets the internal id of the value type.
     *
     * @return The internal id
     */
    byte getInternalId() {
        return internalId;
    }

    /**
     * Serializes the value to the {@link ByteBuffer}.
     *
     * @param buf The target byte buffer
     * @param value The value
     */
    void serialize(ByteBuffer buf, T value) {
        this.serializer.accept(buf, value);
    }

}
