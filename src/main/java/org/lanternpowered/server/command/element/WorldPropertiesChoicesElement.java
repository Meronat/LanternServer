/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://github.com/LanternPowered>
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
package org.lanternpowered.server.command.element;

import com.google.common.collect.ImmutableMap;
import org.lanternpowered.server.game.LanternGame;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Map;
import java.util.function.Supplier;

public final class WorldPropertiesChoicesElement {

    private static final Supplier<Map<String, Object>> supplier = () -> {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (WorldProperties world : LanternGame.get().getServer().getAllWorldProperties()) {
            builder.put(world.getWorldName(), world);
        }
        return builder.build();
    };

    public static CommandElement of(Text key) {
        return ChoicesElement.ofSupplier(key, supplier, false, false);
    }

    private WorldPropertiesChoicesElement() {
    }
}
