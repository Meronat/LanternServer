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
package org.lanternpowered.server.data;

import org.lanternpowered.server.data.persistence.DataSerializers;
import org.lanternpowered.server.data.persistence.DataTypeSerializers;
import org.lanternpowered.server.data.property.LanternPropertyRegistry;
import org.lanternpowered.server.data.property.block.GroundLuminancePropertyStore;
import org.lanternpowered.server.data.property.block.SkyLuminancePropertyStore;
import org.lanternpowered.server.effect.potion.LanternPotionEffectBuilder;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.property.PropertyRegistry;
import org.spongepowered.api.data.property.block.GroundLuminanceProperty;
import org.spongepowered.api.data.property.block.SkyLuminanceProperty;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.util.RespawnLocation;

public class DataRegistrar {

    public static void setupRegistrations(Game game) {
        final PropertyRegistry propertyRegistry = LanternPropertyRegistry.getInstance();
        propertyRegistry.register(SkyLuminanceProperty.class, new SkyLuminancePropertyStore());
        propertyRegistry.register(GroundLuminanceProperty.class, new GroundLuminancePropertyStore());

        final LanternDataManager dataManager = LanternDataManager.getInstance();
        // Register the data type serializers
        DataTypeSerializers.registerSerializers(dataManager);
        // Register the data serializers
        DataSerializers.registerSerializers(dataManager);

        // Register the data builders
        dataManager.registerBuilder(PotionEffect.class, new LanternPotionEffectBuilder());
        dataManager.registerBuilder(RespawnLocation.class, new RespawnLocation.Builder());
    }

    public static void finalizeRegistrations(Game game) {
        LanternPropertyRegistry.getInstance().completeRegistration();
    }
}
