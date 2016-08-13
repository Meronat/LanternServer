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
package org.lanternpowered.server.data.persistence;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import org.lanternpowered.server.data.LanternDataManager;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class DataTypeSerializers {

    @SuppressWarnings("unchecked")
    public static void registerSerializers(LanternDataManager dataManager) {
        dataManager.registerTypeSerializer(new TypeToken<Multimap<?,?>>() {}, new MultimapSerializer());
        dataManager.registerTypeSerializer(new TypeToken<Map<?,?>>() {}, new MapSerializer());
        dataManager.registerTypeSerializer(new TypeToken<List<?>>() {}, new ListSerializer());
        dataManager.registerTypeSerializer(new TypeToken<Set<?>>() {}, new SetSerializer());
        dataManager.registerTypeSerializer((TypeToken) new TypeToken<Enum<?>>() {}, new EnumSerializer());
        dataManager.registerTypeSerializer(TypeToken.of(CatalogType.class), new CatalogTypeSerializer());
        dataManager.registerTypeSerializer(TypeToken.of(Number.class), new NumberSerializer());
        dataManager.registerTypeSerializer(TypeToken.of(String.class), new StringSerializer());
        dataManager.registerTypeSerializer(TypeToken.of(Boolean.class), new BooleanSerializer());
        dataManager.registerTypeSerializer(TypeToken.of(DataSerializable.class), new DataSerializableSerializer());
    }

    private static class DataSerializableSerializer implements DataViewTypeSerializer<DataSerializable> {

        @SuppressWarnings("unchecked")
        @Override
        public Optional<DataSerializable> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, DataView data) throws InvalidDataException {
            DataBuilder<DataSerializable> dataBuilder = (DataBuilder<DataSerializable>) LanternDataManager.getInstance()
                    .getBuilder((Class<? extends DataSerializable>) type.getRawType())
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a DataBuilder for the DataSerializable: " + type.toString()));
            return dataBuilder.build(data);
        }

        @Override
        public DataView serialize(TypeToken<?> type, DataTypeSerializerContext ctx, DataSerializable obj) throws InvalidDataException {
            return obj.toContainer();
        }
    }

    private static class CatalogTypeSerializer implements DataTypeSerializer<CatalogType, String> {

        @SuppressWarnings("unchecked")
        @Override
        public Optional<CatalogType> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, String data) throws InvalidDataException {
            Optional<CatalogType> catalogType = Sponge.getRegistry().getType((Class<CatalogType>) type.getRawType(), data);
            if (!catalogType.isPresent()) {
                throw new InvalidDataException("The catalog type " + data + " of type " + type.toString() + " is missing.");
            }
            return catalogType;
        }

        @Override
        public String serialize(TypeToken<?> type, DataTypeSerializerContext ctx, CatalogType obj) throws InvalidDataException {
            return obj.getId();
        }
    }

    private static class MultimapSerializer implements DataTypeSerializer<Multimap<?, ?>, List<DataView>> {

        private static final DataQuery KEY = DataQuery.of("K");
        private static final DataQuery VALUE = DataQuery.of("V");
        private static final DataQuery ENTRIES = DataQuery.of("E");

        private final TypeVariable<Class<Multimap>> keyTypeVariable = Multimap.class.getTypeParameters()[0];
        private final TypeVariable<Class<Multimap>> valueTypeVariable = Multimap.class.getTypeParameters()[1];

        @SuppressWarnings("unchecked")
        @Override
        public Optional<Multimap<?, ?>> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, List<DataView> entries) throws InvalidDataException {
            TypeToken<?> keyType = type.resolveType(this.keyTypeVariable);
            TypeToken<?> valueType = type.resolveType(this.valueTypeVariable);
            DataTypeSerializer keySerial = ctx.getSerializers().getTypeSerializer(keyType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + keyType.toString()));
            DataTypeSerializer valueSerial = ctx.getSerializers().getTypeSerializer(valueType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + valueType.toString()));
            Multimap map = HashMultimap.create();
            for (DataView entry : entries) {
                Object key = keySerial.deserialize(type, ctx, entry.get(KEY)
                        .orElseThrow(() -> new InvalidDataException("Entry is missing a key.")));
                if (!entry.contains(VALUE) && entry.contains(ENTRIES)) {
                    throw new InvalidDataException("Entry is missing values.");
                }
                List<?> dataViews = entry.getList(ENTRIES).orElse(null);
                if (dataViews != null) {
                    dataViews.forEach(o -> {
                        Object value = valueSerial.deserialize(type, ctx, o);
                        map.put(key, value);
                    });
                } else {
                    Object value = valueSerial.deserialize(type, ctx, entry.get(VALUE).get());
                    map.put(key, value);
                }
            }
            return Optional.of(map);
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<DataView> serialize(TypeToken<?> type, DataTypeSerializerContext ctx, Multimap<?, ?> obj) throws InvalidDataException {
            TypeToken<?> keyType = type.resolveType(this.keyTypeVariable);
            TypeToken<?> valueType = type.resolveType(this.valueTypeVariable);
            DataTypeSerializer keySerial = ctx.getSerializers().getTypeSerializer(keyType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + keyType.toString()));
            DataTypeSerializer valueSerial = ctx.getSerializers().getTypeSerializer(valueType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + valueType.toString()));
            List<DataView> dataViews = new ArrayList<>();
            for (Object key : obj.keySet()) {
                DataContainer dataContainer = new MemoryDataContainer();
                dataContainer.set(KEY, keySerial.serialize(keyType, ctx, key));
                Collection<Object> values = ((Multimap) obj).get(key);
                if (values.size() == 1) {
                    dataContainer.set(VALUE, valueSerial.serialize(valueType, ctx, values.iterator().next()));
                } else {
                    dataContainer.set(ENTRIES, values.stream().map(v -> valueSerial.serialize(valueType, ctx, v)).collect(Collectors.toList()));
                }
                dataViews.add(dataContainer);
            }
            return dataViews;
        }
    }

    private static class MapSerializer implements DataTypeSerializer<Map<?, ?>, List<DataView>> {

        private static final DataQuery KEY = DataQuery.of("K");
        private static final DataQuery VALUE = DataQuery.of("V");

        private final TypeVariable<Class<Map>> keyTypeVariable = Map.class.getTypeParameters()[0];
        private final TypeVariable<Class<Map>> valueTypeVariable = Map.class.getTypeParameters()[1];

        @SuppressWarnings("unchecked")
        @Override
        public Optional<Map<?, ?>> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, List<DataView> entries) throws InvalidDataException {
            TypeToken<?> keyType = type.resolveType(this.keyTypeVariable);
            TypeToken<?> valueType = type.resolveType(this.valueTypeVariable);
            DataTypeSerializer keySerial = ctx.getSerializers().getTypeSerializer(keyType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + keyType.toString()));
            DataTypeSerializer valueSerial = ctx.getSerializers().getTypeSerializer(valueType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + valueType.toString()));
            Map map = new HashMap<>();
            for (DataView entry : entries) {
                Object key = keySerial.deserialize(type, ctx, entry.get(KEY)
                        .orElseThrow(() -> new InvalidDataException("Entry is missing a key.")));
                Object value = valueSerial.deserialize(type, ctx, entry.get(VALUE)
                        .orElseThrow(() -> new InvalidDataException("Entry is missing a value.")));
                map.put(key, value);
            }
            return Optional.of(map);
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<DataView> serialize(TypeToken<?> type, DataTypeSerializerContext ctx, Map<?, ?> obj) throws InvalidDataException {
            TypeToken<?> keyType = type.resolveType(this.keyTypeVariable);
            TypeToken<?> valueType = type.resolveType(this.valueTypeVariable);
            DataTypeSerializer keySerial = ctx.getSerializers().getTypeSerializer(keyType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + keyType.toString()));
            DataTypeSerializer valueSerial = ctx.getSerializers().getTypeSerializer(valueType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + valueType.toString()));
            return obj.entrySet().stream().map(entry -> new MemoryDataContainer()
                    .set(KEY, keySerial.serialize(keyType, ctx, entry.getKey()))
                    .set(VALUE, valueSerial.serialize(valueType, ctx, entry.getValue()))).collect(Collectors.toList());
        }
    }

    private static class ListSerializer implements DataTypeSerializer<List<?>, List<Object>> {

        private final TypeVariable<Class<List>> typeVariable = List.class.getTypeParameters()[0];

        @SuppressWarnings("unchecked")
        @Override
        public Optional<List<?>> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, List<Object> data) throws InvalidDataException {
            TypeToken<?> elementType = type.resolveType(this.typeVariable);
            DataTypeSerializer elementSerial = ctx.getSerializers().getTypeSerializer(elementType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + elementType.toString()));
            return Optional.of((List) data.stream()
                    .map(object -> elementSerial.deserialize(elementType, ctx, object).get())
                    .collect((Collector) Collectors.toList()));
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Object> serialize(TypeToken<?> type, DataTypeSerializerContext ctx, List<?> obj) throws InvalidDataException {
            TypeToken<?> elementType = type.resolveType(this.typeVariable);
            DataTypeSerializer elementSerial = ctx.getSerializers().getTypeSerializer(elementType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + elementType.toString()));
            return obj.stream().map(object -> elementSerial.serialize(elementType, ctx, object)).collect(Collectors.toList());
        }
    }

    private static class SetSerializer implements DataTypeSerializer<Set<?>, List<Object>> {

        private final TypeVariable<Class<Set>> typeVariable = Set.class.getTypeParameters()[0];

        @SuppressWarnings("unchecked")
        @Override
        public Optional<Set<?>> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, List<Object> data) throws InvalidDataException {
            TypeToken<?> elementType = type.resolveType(this.typeVariable);
            DataTypeSerializer elementSerial = ctx.getSerializers().getTypeSerializer(elementType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + elementType.toString()));
            return Optional.of((Set) data.stream()
                    .map(object -> elementSerial.deserialize(elementType, ctx, object).get())
                    .collect((Collector) Collectors.toSet()));
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Object> serialize(TypeToken<?> type, DataTypeSerializerContext ctx, Set<?> obj) throws InvalidDataException {
            TypeToken<?> elementType = type.resolveType(this.typeVariable);
            DataTypeSerializer elementSerial = ctx.getSerializers().getTypeSerializer(elementType)
                    .orElseThrow(() -> new IllegalStateException("Wasn't able to find a type serializer for: " + elementType.toString()));
            return obj.stream().map(object -> elementSerial.serialize(elementType, ctx, object)).collect(Collectors.toList());
        }
    }

    private static class EnumSerializer implements DataTypeSerializer<Enum, String> {

        @SuppressWarnings("unchecked")
        @Override
        public Optional<Enum> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, String data) throws InvalidDataException {
            // Enum values should be uppercase
            data = data.toUpperCase();

            Enum ret;
            try {
                ret = Enum.valueOf(((TypeToken) type).getRawType().asSubclass(Enum.class), data);
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException("Invalid enum constant, expected a value of enum " + type + ", got " + data);
            }

            return Optional.of(ret);
        }

        @Override
        public String serialize(TypeToken<?> type, DataTypeSerializerContext ctx, Enum obj) throws InvalidDataException {
            return obj.name();
        }
    }

    private static class NumberSerializer implements DataTypeSerializer<Number, Object> {

        private static final DataQuery TYPE = DataQuery.of("Type");
        private static final DataQuery VALUE = DataQuery.of("Value");

        @Override
        public Optional<Number> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, Object data) throws InvalidDataException {
            if (data instanceof DataView) {
                DataView view = (DataView) data;
                if (view.contains(TYPE) && view.contains(VALUE)) {
                    String numberType = view.getString(TYPE).get();
                    String value = view.getString(VALUE).get();
                    if (numberType.equals(BigDecimal.class.getSimpleName())) {
                        return Optional.of(new BigDecimal(value));
                    } else if (numberType.equals(BigInteger.class.getSimpleName())) {
                        return Optional.of(new BigInteger(value));
                    } else {
                        throw new InvalidDataException("Unsupported number type: " + numberType);
                    }
                }
                return Optional.empty();
            } else if (data instanceof Number) {
                return Optional.of((Number) data);
            }
            throw new InvalidDataException("Unsupported data type: " + data.getClass().getName());
        }

        @Override
        public Object serialize(TypeToken<?> type, DataTypeSerializerContext ctx, Number obj) throws InvalidDataException {
            if (obj instanceof BigDecimal || obj instanceof BigInteger) {
                return new MemoryDataContainer()
                        .set(TYPE, obj.getClass().getSimpleName())
                        .set(VALUE, obj.toString());
            }
            return obj;
        }
    }

    private static class BooleanSerializer implements DataTypeSerializer<Boolean, Boolean> {

        @Override
        public Optional<Boolean> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, Boolean data) throws InvalidDataException {
            return Optional.of(data);
        }

        @Override
        public Boolean serialize(TypeToken<?> type, DataTypeSerializerContext ctx, Boolean obj) throws InvalidDataException {
            return obj;
        }
    }

    private static class StringSerializer implements DataTypeSerializer<String, String> {

        @Override
        public Optional<String> deserialize(TypeToken<?> type, DataTypeSerializerContext ctx, String data) throws InvalidDataException {
            return Optional.of(data);
        }

        @Override
        public String serialize(TypeToken<?> type, DataTypeSerializerContext ctx, String obj) throws InvalidDataException {
            return obj;
        }
    }
}
