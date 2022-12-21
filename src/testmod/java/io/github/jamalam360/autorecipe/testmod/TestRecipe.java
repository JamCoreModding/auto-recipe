package io.github.jamalam360.autorecipe.testmod;

import io.github.jamalam360.autorecipe.AutoSerializedRecipe;
import io.github.jamalam360.autorecipe.RecipeVar;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TestRecipe extends AutoSerializedRecipe<Inventory> {
    @RecipeVar
    ItemStack input;
    @RecipeVar
    ItemStack output;
    @RecipeVar("time")
    int processingTime;

    @Override
    public String toString() {
        return input + " -> " + output + " (" + processingTime + " ticks)";
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return true;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getOutput() {
        return this.output;
    }
}
