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
package org.lanternpowered.server.data.io.store;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.lanternpowered.server.data.io.store.entity.EntityStore;
import org.lanternpowered.server.data.io.store.entity.LivingStore;
import org.lanternpowered.server.data.io.store.entity.PlayerStore;
import org.lanternpowered.server.entity.LanternEntity;
import org.lanternpowered.server.entity.LanternEntityLiving;
import org.lanternpowered.server.entity.living.player.LanternPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public final class ObjectStoreRegistry {

    private static final ObjectStoreRegistry registry = new ObjectStoreRegistry();

    public static ObjectStoreRegistry get() {
        return registry;
    }

    private final Map<Class<?>, ObjectStore> objectsStores = new HashMap<>();
    private final LoadingCache<Class<?>, Optional<ObjectStore>> objectStoreCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<?>, Optional<ObjectStore>>() {
                @Override
                public Optional<ObjectStore> load(Class<?> key) throws Exception {
                    ObjectStore store;
                    while (key != Object.class) {
                        store = objectsStores.get(key);
                        if (store != null) {
                            return Optional.of(store);
                        }
                        for (Class<?> interf : key.getInterfaces()) {
                            store = objectsStores.get(interf);
                            if (store != null) {
                                return Optional.of(store);
                            }
                        }
                        key = key.getSuperclass();
                    }
                    return Optional.empty();
                }
            });

    public ObjectStoreRegistry() {
        this.register(LanternEntity.class, new EntityStore<>());
        this.register(LanternEntityLiving.class, new LivingStore<>());
        this.register(LanternPlayer.class, new PlayerStore());
    }

    /**
     * Register a {@link ObjectStore} for the specified object type.
     *
     * @param objectType The object type
     * @param objectStore The object store
     * @param <T> The type of the object
     */
    public <T> void register(Class<? extends T> objectType, ObjectStore<T> objectStore) {
        this.objectsStores.put(checkNotNull(objectType, "objectType"), checkNotNull(objectStore, "objectStore"));
        this.objectStoreCache.invalidateAll();
    }

    /**
     * Gets the most suitable {@link ObjectStore} for the specified object type,
     * may return {@link Optional#empty()} if no suitable store could be found.
     *
     * @param objectType The object type
     * @param <T> The type of the object
     * @return The object store
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<ObjectStore<T>> get(Class<? extends T> objectType) {
        try {
            return (Optional) this.objectStoreCache.get(objectType);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
