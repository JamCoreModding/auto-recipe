package io.github.jamalam360.autorecipe.testmod;

import io.github.jamalam360.autorecipe.AutoSerializedRecipe;
import io.github.jamalam360.autorecipe.RecipeVar;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public abstract class AbstractTestInheritedRecipe extends AutoSerializedRecipe<Inventory> {
    @RecipeVar
    public ItemStack baseClassStack;
}
