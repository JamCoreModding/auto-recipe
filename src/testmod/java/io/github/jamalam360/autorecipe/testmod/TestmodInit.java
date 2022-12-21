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
