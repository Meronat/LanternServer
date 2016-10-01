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

import org.lanternpowered.server.entity.LanternEntity;
import org.lanternpowered.server.entity.LanternEntityLiving;
import org.lanternpowered.server.entity.living.player.LanternPlayer;
import org.lanternpowered.server.game.registry.PluginCatalogRegistryModule;
import org.lanternpowered.server.network.entity.vanilla.HumanoidEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.HuskEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.LightningEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.MagmaCubeEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.PaintingEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.PlayerEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.RabbitEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.SlimeEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.VillagerEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.ZombieEntityProtocol;
import org.lanternpowered.server.network.entity.vanilla.ZombieVillagerEntityProtocol;

public class EntityProtocolTypeRegistryModule extends PluginCatalogRegistryModule<EntityProtocolType> {

    public EntityProtocolTypeRegistryModule() {
        super(EntityProtocolTypes.class);
    }

    @Override
    public void registerDefaults() {
        this.register(new LanternEntityProtocolType<>("minecraft", "human", LanternEntityLiving.class, HumanoidEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "husk", LanternEntityLiving.class, HuskEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "lightning", LanternEntity.class, LightningEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "magma_cube", LanternEntityLiving.class, MagmaCubeEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "painting", LanternEntity.class, PaintingEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "player", LanternPlayer.class, PlayerEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "rabbit", LanternEntityLiving.class, RabbitEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "slime", LanternEntityLiving.class, SlimeEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "villager", LanternEntityLiving.class, VillagerEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "zombie", LanternEntityLiving.class, ZombieEntityProtocol::new));
        this.register(new LanternEntityProtocolType<>("minecraft", "zombie_villager", LanternEntityLiving.class, ZombieVillagerEntityProtocol::new));
    }
}
