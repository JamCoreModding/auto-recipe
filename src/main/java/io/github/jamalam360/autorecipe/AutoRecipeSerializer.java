/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Emi (original creator), Jamalam (current maintainer)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.github.jamalam360.autorecipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A 'generated' serializer for Auto Recipes.
 *
 * @param <T> The type of recipe
 *
 * @apiNote This class implements {@link QuiltRecipeSerializer} rather than {@link Recipe}, to support the Quilt recipe API (namely the debug dumping feature).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class AutoRecipeSerializer<T extends Recipe<?>> implements QuiltRecipeSerializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger("autorecipe");
    private final Function<Identifier, T> constructor;
    private final Map<RecipeVarData, Field> variables = new LinkedHashMap<>();
    private final String namespace;

    public AutoRecipeSerializer(Function<Identifier, T> constructor, Class<T> clazz, Identifier id) {
        this.constructor = constructor;
        namespace = id.getNamespace();
        Set<Field> fields = new ObjectArraySet<>();
        fields.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());
        fields.addAll(Arrays.stream(clazz.getFields()).toList());

        for (Field field : fields) {
            RecipeVar annot = field.getAnnotation(RecipeVar.class);

            if (annot != null) {
                String name = annot.value();

                if (name.equals("")) {
                    name = field.getName();
                }

                variables.put(new RecipeVarData(name, annot.required()), field);
                RecipeVarSerializer<?> serializer = AutoRecipeRegistry.getVariableSerializer(namespace, field.getType());

                if (field.getType() == Map.class) {
                    Class<?> stringType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                    if (stringType != String.class) {
                        try {
                            Constructor cs = stringType.getConstructor(String.class);

                            if (!cs.canAccess(this)) {
                                throw new RuntimeException("Map key for field " + field.getName() + " cannot be constructed from a string");
                            }
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException("Map key for field " + field.getName() + " cannot be constructed from a string", e);
                        }
                    }
                } else if (serializer == null && field.getType() != List.class && field.getType() != Set.class && field.getType() != DefaultedList.class) {
                    LOGGER.warn("No serializer found for type " + field.getType().getTypeName() + " at " + clazz.getTypeName() + "#" + field.getName() + ", has it not been registered yet?");
                }
            }
        }
    }

    @Override
    public T read(Identifier id, JsonObject json) {
        T t = constructor.apply(id);

        if (t instanceof AutoSerializedRecipe asr) {
            if (asr.id == null) {
                asr.id = id;
            }
        }

        for (Map.Entry<RecipeVarData, Field> entry : variables.entrySet()) {
            RecipeVarData var = entry.getKey();
            Field field = entry.getValue();

            try {
                field.setAccessible(true);
                JsonElement el;

                try {
                    String[] parts = var.name().split("/");
                    JsonObject obj = json;
                    int i = 0;

                    while (i + 1 < parts.length) {
                        obj = obj.getAsJsonObject(parts[i]);
                        i++;
                    }

                    el = obj.get(parts[i]);
                } catch (Exception e) {
                    if (var.required()) {
                        throw new RuntimeException(e);
                    } else {
                        continue;
                    }
                }

                if (el == null || el.isJsonNull()) {
                    if (var.required()) {
                        throw new RuntimeException("Required element not found");
                    } else {
                        continue;
                    }
                }

                Class<?> fieldType = field.getType();

                if (fieldType == List.class || fieldType == Set.class || fieldType == DefaultedList.class) {
                    Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    RecipeVarSerializer<?> varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    Collection collection;

                    if (fieldType == List.class) {
                        collection = Lists.newArrayList();
                    } else if (fieldType == Set.class) {
                        collection = Sets.newHashSet();
                    } else {
                        collection = DefaultedList.of();
                    }

                    if (el.isJsonArray()) {
                        for (JsonElement e : el.getAsJsonArray()) {
                            collection.add(varSerializer.readJson(e));
                        }
                    } else {
                        collection.add(varSerializer.readJson(el));
                    }

                    field.set(t, collection);
                } else if (fieldType == Map.class) {
                    Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                    Class<?> stringType = (Class<?>) types[0];
                    Class<?> genericType = (Class<?>) types[1];
                    RecipeVarSerializer<?> varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    Map map = Maps.newHashMap();
                    Function<String, Object> keySupplier;

                    if (stringType == String.class) {
                        keySupplier = s -> s;
                    } else {
                        Constructor constructor = stringType.getConstructor(String.class);
                        keySupplier = s -> {
                            try {
                                return constructor.newInstance(s);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        };
                    }

                    for (Map.Entry<String, JsonElement> me : el.getAsJsonObject().entrySet()) {
                        map.put(keySupplier.apply(me.getKey()), varSerializer.readJson(me.getValue()));
                    }

                    field.set(t, map);
                } else {
                    RecipeVarSerializer<?> varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, fieldType);
                    field.set(t, varSerializer.readJson(el));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error parsing recipe " + id + ", malformed field " + field.getName(), e);
            }
        }

        if (t instanceof AutoSerializedRecipe asr) {
            asr.compile();
        }

        return t;
    }

    @Override
    public JsonObject toJson(T recipe) {
        JsonObject root = new JsonObject();

        for (Map.Entry<RecipeVarData, Field> entry : variables.entrySet()) {
            RecipeVarData var = entry.getKey();
            Field field = entry.getValue();

            try {
                field.setAccessible(true);
                JsonObject obj = root;
                String[] parts = var.name().split("/");
                int i = 0;

                while (i + 1 < parts.length) {
                    JsonObject n = new JsonObject();
                    obj.add(parts[i], n);
                    obj = n;
                    i++;
                }

                JsonElement el;
                Class<?> fieldType = field.getType();

                if (fieldType == List.class || fieldType == Set.class || fieldType == DefaultedList.class) {
                    Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    RecipeVarSerializer<Object> varSerializer = (RecipeVarSerializer<Object>) AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    Object collection = field.get(recipe);
                    el = new JsonArray();

                    for (Object v : (Iterable<?>) collection) {
                        el.getAsJsonArray().add(varSerializer.toJson(v));
                    }
                } else if (fieldType == Map.class) {
                    Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                    Class<?> genericType = (Class<?>) types[1];
                    RecipeVarSerializer<Object> varSerializer = (RecipeVarSerializer<Object>) AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    Map<String, Object> map = (Map) field.get(recipe);
                    el = new JsonObject();

                    for (Map.Entry<String, Object> me : map.entrySet()) {
                        el.getAsJsonObject().add(me.getKey(), varSerializer.toJson(me.getValue()));
                    }
                } else {
                    RecipeVarSerializer<Object> varSerializer = (RecipeVarSerializer<Object>) AutoRecipeRegistry.getVariableSerializer(namespace, fieldType);
                    el = varSerializer.toJson(field.get(recipe));
                }

                obj.add(var.name(), el);
            } catch (Exception e) {
                throw new RuntimeException("Error serializing recipe " + recipe.getId() + ", malformed field " + field.getName(), e);
            }
        }

        return root;
    }

    @Override
    public T read(Identifier id, PacketByteBuf buf) {
        T t = constructor.apply(id);

        if (t instanceof AutoSerializedRecipe asr) {
            if (asr.id == null) {
                asr.id = id;
            }
        }

        for (Map.Entry<RecipeVarData, Field> entry : variables.entrySet()) {
            try {
                Field field = entry.getValue();
                Class<?> fieldType = field.getType();

                if (fieldType == List.class) {
                    Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    RecipeVarSerializer<?> varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    field.set(t, readCollection(buf, i -> new ArrayList(i), varSerializer::readPacket));
                } else if (fieldType == Set.class) {
                    Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    RecipeVarSerializer<?> varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    field.set(t, readCollection(buf, i -> new HashSet(), varSerializer::readPacket));
                } else if (fieldType == DefaultedList.class) {
                    Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    RecipeVarSerializer<?> varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    field.set(t, readCollection(buf, i -> DefaultedList.of(), varSerializer::readPacket));
                } else if (fieldType == Map.class) {
                    Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                    Class<?> stringType = (Class<?>) types[0];
                    Class<?> genericType = (Class<?>) types[1];
                    RecipeVarSerializer<?> varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    Function<String, Object> keySupplier;

                    if (stringType == String.class) {
                        keySupplier = s -> s;
                    } else {
                        Constructor constructor = stringType.getConstructor(String.class);
                        keySupplier = s -> {
                            try {
                                return constructor.newInstance(s);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        };
                    }

                    field.set(t, readMap(buf, Maps::newHashMapWithExpectedSize, b -> keySupplier.apply(b.readString()), varSerializer::readPacket));
                } else {
                    RecipeVarSerializer<?> varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, fieldType);
                    field.set(t, varSerializer.readPacket(buf));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error parsing packet", e);
            }
        }

        if (t instanceof AutoSerializedRecipe asr) {
            asr.compile();
        }

        return t;
    }

    @Override
    public void write(PacketByteBuf buf, T recipe) {
        for (Map.Entry<RecipeVarData, Field> entry : variables.entrySet()) {
            try {
                Field field = entry.getValue();
                Class<?> fieldType = field.getType();
                if (fieldType == List.class || fieldType == Set.class || fieldType == DefaultedList.class) {
                    Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    RecipeVarSerializer varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    Collection list = (Collection) field.get(recipe);
                    writeCollection(buf, list, varSerializer::writePacket);
                } else if (fieldType == Map.class) {
                    Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                    Class<?> genericType = (Class<?>) types[1];
                    RecipeVarSerializer<?> varSerializer = AutoRecipeRegistry.getVariableSerializer(namespace, genericType);
                    writeMap(buf, (Map) field.get(recipe), (b, k) -> b.writeString(k.toString()), varSerializer::writePacket);
                } else {
                    RecipeVarSerializer<Object> varSerializer = (RecipeVarSerializer<Object>) AutoRecipeRegistry.getVariableSerializer(namespace, fieldType);
                    varSerializer.writePacket(buf, field.get(recipe));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error writing packet", e);
            }
        }
    }

    private <G> void writeCollection(PacketByteBuf buf, Collection<G> collection, BiConsumer<PacketByteBuf, G> entrySerializer) {
        buf.writeVarInt(collection.size());

        for (G val : collection) {
            entrySerializer.accept(buf, val);
        }
    }

    private <G, C extends Collection<G>> C readCollection(PacketByteBuf buf, IntFunction<C> collectionFactory, Function<PacketByteBuf, G> entryParser) {
        int i = buf.readVarInt();
        C collection = collectionFactory.apply(i);

        for (int j = 0; j < i; j++) {
            collection.add(entryParser.apply(buf));
        }

        return collection;
    }

    private <K, V> void writeMap(PacketByteBuf buf, Map<K, V> map, BiConsumer<PacketByteBuf, K> keySerializer, BiConsumer<PacketByteBuf, V> valueSerializer) {
        buf.writeVarInt(map.size());
        map.forEach((key, value) -> {
            keySerializer.accept(buf, key);
            valueSerializer.accept(buf, value);
        });
    }

    public <K, V, M extends Map<K, V>> M readMap(PacketByteBuf buf, IntFunction<M> mapFactory, Function<PacketByteBuf, K> keyParser, Function<PacketByteBuf, V> valueParser) {
        int i = buf.readVarInt();
        M map = mapFactory.apply(i);

        for (int j = 0; j < i; j++) {
            K key = keyParser.apply(buf);
            V value = valueParser.apply(buf);
            map.put(key, value);
        }

        return map;
    }

    public record RecipeVarData(String name, boolean required) {}
}
