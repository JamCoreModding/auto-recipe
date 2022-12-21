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
