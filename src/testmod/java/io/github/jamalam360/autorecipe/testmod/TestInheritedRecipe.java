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
