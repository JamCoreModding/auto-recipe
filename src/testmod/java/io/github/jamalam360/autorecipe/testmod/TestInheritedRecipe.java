package io.github.jamalam360.autorecipe.testmod;

import io.github.jamalam360.autorecipe.RecipeVar;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TestInheritedRecipe extends AbstractTestInheritedRecipe {
    @RecipeVar
    public ItemStack subClassStack;

    @Override
    public boolean matches(Inventory inventory, World world) {
        return true;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        return null;
    }

    @Override
    public ItemStack getOutput() {
        return null;
    }

    @Override
    public String toString() {
        return "Superclass Stack: " + this.baseClassStack + ", subclass stack: " + this.subClassStack;
    }
}
