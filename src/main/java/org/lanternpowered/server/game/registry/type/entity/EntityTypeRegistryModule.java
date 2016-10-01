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
package org.lanternpowered.server.game.registry.type.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.lanternpowered.server.entity.LanternEntityType;
import org.lanternpowered.server.entity.LanternItem;
import org.lanternpowered.server.entity.living.player.LanternPlayer;
import org.lanternpowered.server.game.registry.AdditionalPluginCatalogRegistryModule;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class EntityTypeRegistryModule extends AdditionalPluginCatalogRegistryModule<EntityType> {

    private static final EntityTypeRegistryModule INSTANCE = new EntityTypeRegistryModule();

    public static EntityTypeRegistryModule get() {
        return INSTANCE;
    }

    private final Map<Class<?>, EntityType> entityTypeByClass = new HashMap<>();

    private EntityTypeRegistryModule() {
        super(EntityTypes.class);
    }

    @Override
    protected void register(EntityType catalogType, boolean disallowInbuiltPluginIds) {
        checkNotNull(catalogType, "catalogType");
        checkArgument(!this.entityTypeByClass.containsKey(catalogType.getClass()), "There is already a EntityType registered for the class: %s",
                catalogType.getEntityClass().getName());
        super.register(catalogType, disallowInbuiltPluginIds);
        this.entityTypeByClass.put(catalogType.getEntityClass(), catalogType);
    }

    public Optional<EntityType> getByClass(Class<? extends Entity> entityClass) {
        checkNotNull(entityClass, "entityClass");
        return Optional.ofNullable(this.entityTypeByClass.get(entityClass));
    }

    @Override
    public void registerDefaults() {
        this.register(LanternEntityType.of("minecraft", "player", "entity.player.name", LanternPlayer.class,
                uuid -> { throw new UnsupportedOperationException("You cannot construct a Player."); }));
        this.register(LanternEntityType.of("minecraft", "item", "entity.Item.name", LanternItem::new));
    }
}
