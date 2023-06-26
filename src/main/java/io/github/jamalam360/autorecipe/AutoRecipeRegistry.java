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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main interface to Auto Recipe
 */
@SuppressWarnings({"unchecked", "unused"})
public class AutoRecipeRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("autorecipe");
    private static final Identifier AUTO_CLASS = new Identifier("auto", "class");
    private static final Map<Class<? extends Recipe<?>>, AutoRecipeSerializer<?>> RECIPE_SERIALIZERS = new HashMap<>();
    private static final Map<Class<? extends Recipe<?>>, RecipeType<?>> RECIPE_TYPES = new HashMap<>();
    protected static final Map<Class<?>, RecipeVarSerializer<?>> VAR_SERIALIZERS = new HashMap<>();
    private static final Map<String, Map<Class<?>, RecipeVarSerializer<?>>> SCOPED_VAR_SERIALIZERS = new HashMap<>();

    static {
        DefaultRecipeVarSerializers.registerDefaultSerializers();
    }

    /**
     * Creates and registers a recipe type and serializer based on the provided supplier of a child of {@link AutoSerializedRecipe}, such as a blank constructor
     *
     * @return The recipe type
     */
    public static <V extends Inventory, T extends AutoSerializedRecipe<V>> RecipeType<T> registerRecipeSerializer(Identifier id, Supplier<T> supplier) {
        return registerRecipeSerializer(id, i -> supplier.get());
    }

    /**
     * Creates and registers a recipe type and serializer based on the provided function of an {@link Identifier} to a {@link Recipe}, such as a constructor
     *
     * @return The recipe type
     */
    public static <V extends Inventory, T extends Recipe<V>> RecipeType<T> registerRecipeSerializer(Identifier id, Function<Identifier, T> function) {
        Class<T> clazz = (Class<T>) function.apply(AUTO_CLASS).getClass();
        AutoRecipeSerializer<T> serializer = new AutoRecipeSerializer<>(function, clazz, id);
        RecipeType<T> type = new RecipeType<>() {
            public String toString() {
                return id.toString();
            }
        };

        RECIPE_SERIALIZERS.put(clazz, serializer);
        RECIPE_TYPES.put(clazz, type);
        Registry.register(Registries.RECIPE_TYPE, id, type);
        Registry.register(Registries.RECIPE_SERIALIZER, id, serializer);

        return type;
    }

    /**
     * Registers a recipe variable serializer for a given class in a given namespace
     */
    public static <T> void registerVariableSerializer(String namespace, Class<T> clazz, RecipeVarSerializer<T> serializer) {
        Map<Class<?>, RecipeVarSerializer<?>> map = SCOPED_VAR_SERIALIZERS.computeIfAbsent(namespace, a -> new HashMap<>());

        if (map.containsKey(clazz)) {
            LOGGER.warn("Variable serializer registered over existing serializer for class " + clazz.getTypeName());
        }

        map.put(clazz, serializer);
    }

    /**
     * Registers a recipe variable serializer in the global scope for a given class. Use with caution.
     */
    public static <T> void registerGlobalVariableSerializer(Class<T> clazz, RecipeVarSerializer<T> serializer) {
        if (VAR_SERIALIZERS.containsKey(clazz)) {
            LOGGER.warn("Global variable serializer registered over existing serializer for class " + clazz.getTypeName());
        }

        VAR_SERIALIZERS.put(clazz, serializer);
    }

    public static AutoRecipeSerializer<? extends Recipe<?>> getRecipeSerializer(Class<?> clazz) {
        return RECIPE_SERIALIZERS.get(clazz);
    }

    public static RecipeVarSerializer<?> getVariableSerializer(String namespace, Class<?> clazz) {
        if (SCOPED_VAR_SERIALIZERS.containsKey(namespace)) {
            Map<Class<?>, RecipeVarSerializer<?>> map = SCOPED_VAR_SERIALIZERS.get(namespace);

            if (map.containsKey(clazz)) {
                return map.get(clazz);
            }
        }

        return VAR_SERIALIZERS.get(clazz);
    }

    public static RecipeType<?> getRecipeType(Class<?> clazz) {
        return RECIPE_TYPES.get(clazz);
    }
}
