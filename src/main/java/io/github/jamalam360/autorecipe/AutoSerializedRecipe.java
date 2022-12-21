package io.github.jamalam360.autorecipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

/**
 * A basic implementation of {@link net.minecraft.recipe.Recipe} that uses a recipe type registered with and created by the {@link AutoRecipeRegistry}.
 *
 * @param <T> The type of inventory this recipe operates on.
 */
public abstract class AutoSerializedRecipe<T extends Inventory> implements Recipe<T> {

    public Identifier id;

    /**
     * Called after the recipe is parsed from JSON, to finalize values.
     */
    public void compile() {
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AutoRecipeRegistry.getRecipeSerializer(this.getClass());
    }

    @Override
    public RecipeType<?> getType() {
        return AutoRecipeRegistry.getRecipeType(this.getClass());
    }
}
