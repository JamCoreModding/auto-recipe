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

import io.github.jamalam360.autorecipe.AutoRecipeRegistry;
import java.util.List;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class TestmodInit implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {
        RecipeType<TestRecipe> type = AutoRecipeRegistry.registerRecipeSerializer(new Identifier("autorecipe_testmod", "test_recipe"), TestRecipe::new);

        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
            if (!world.isClient && world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.IRON_BLOCK)) {
                List<TestRecipe> recipes = world.getRecipeManager().getAllMatches(type, new SimpleInventory(1), world);

                for (TestRecipe recipe : recipes) {
                    player.sendMessage(Text.literal(recipe.toString()), false);
                }

                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        }));
    }
}
