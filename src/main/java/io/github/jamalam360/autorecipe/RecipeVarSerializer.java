package io.github.jamalam360.autorecipe;

import com.google.gson.JsonElement;
import net.minecraft.network.PacketByteBuf;

/**
 * Used to define new serializers for fields annotated with {@link io.github.jamalam360.autorecipe.RecipeVar}.
 *
 * @param <T> The target type for fields.
 */
public interface RecipeVarSerializer<T> {


    /**
     * @param element The JSON element to be converted
     *
     * @return An instance of the target type, parsed from the JSON element.
     */
    T readJson(JsonElement element);

    /**
     * Used for the {@link org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer} implementation. This method is used to dump recipes for debugging if the
     * `quilt.recipe.dump` system property is true.
     *
     * @param value The value
     *
     * @return A JSON representation of the value
     */
    JsonElement toJson(T value);

    /**
     * @param buf The packet
     *
     * @return An instance of the target type, parsed from the packet.
     */
    T readPacket(PacketByteBuf buf);

    /**
     * @param buf The packet to write the data to
     * @param value The instance of the target type to write
     */
    void writePacket(PacketByteBuf buf, T value);
}
