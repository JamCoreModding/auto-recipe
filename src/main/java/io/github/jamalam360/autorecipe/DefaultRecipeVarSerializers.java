package io.github.jamalam360.autorecipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class DefaultRecipeVarSerializers {

    protected static void registerDefaultSerializers() {
        AutoRecipeRegistry.registerGlobalVariableSerializer(boolean.class, newSerializer(
              JsonElement::getAsBoolean,
              JsonPrimitive::new,
              PacketByteBuf::readBoolean,
              PacketByteBuf::writeBoolean
        ));
        AutoRecipeRegistry.VAR_SERIALIZERS.put(Boolean.class, AutoRecipeRegistry.VAR_SERIALIZERS.get(boolean.class));

        AutoRecipeRegistry.registerGlobalVariableSerializer(byte.class, newSerializer(
              JsonElement::getAsByte,
              JsonPrimitive::new,
              PacketByteBuf::readByte,
              (buf, value) -> buf.writeByte(value)
        ));
        AutoRecipeRegistry.VAR_SERIALIZERS.put(Byte.class, AutoRecipeRegistry.VAR_SERIALIZERS.get(byte.class));

        AutoRecipeRegistry.registerGlobalVariableSerializer(short.class, newSerializer(
              JsonElement::getAsShort,
              JsonPrimitive::new,
              PacketByteBuf::readShort,
              (buf, value) -> buf.writeShort(value)
        ));
        AutoRecipeRegistry.VAR_SERIALIZERS.put(Short.class, AutoRecipeRegistry.VAR_SERIALIZERS.get(short.class));

        AutoRecipeRegistry.registerGlobalVariableSerializer(int.class, newSerializer(
              JsonElement::getAsInt,
              JsonPrimitive::new,
              PacketByteBuf::readInt,
              PacketByteBuf::writeInt
        ));
        AutoRecipeRegistry.VAR_SERIALIZERS.put(Integer.class, AutoRecipeRegistry.VAR_SERIALIZERS.get(int.class));

        AutoRecipeRegistry.registerGlobalVariableSerializer(long.class, newSerializer(
              JsonElement::getAsLong,
              JsonPrimitive::new,
              PacketByteBuf::readLong,
              PacketByteBuf::writeLong
        ));
        AutoRecipeRegistry.VAR_SERIALIZERS.put(Long.class, AutoRecipeRegistry.VAR_SERIALIZERS.get(long.class));

        AutoRecipeRegistry.registerGlobalVariableSerializer(float.class, newSerializer(
              JsonElement::getAsFloat,
              JsonPrimitive::new,
              PacketByteBuf::readFloat,
              PacketByteBuf::writeFloat
        ));
        AutoRecipeRegistry.VAR_SERIALIZERS.put(Float.class, AutoRecipeRegistry.VAR_SERIALIZERS.get(float.class));

        AutoRecipeRegistry.registerGlobalVariableSerializer(double.class, newSerializer(
              JsonElement::getAsDouble,
              JsonPrimitive::new,
              PacketByteBuf::readDouble,
              PacketByteBuf::writeDouble
        ));
        AutoRecipeRegistry.VAR_SERIALIZERS.put(Double.class, AutoRecipeRegistry.VAR_SERIALIZERS.get(double.class));

        AutoRecipeRegistry.registerGlobalVariableSerializer(String.class, newSerializer(
              JsonElement::getAsString,
              JsonPrimitive::new,
              PacketByteBuf::readString,
              PacketByteBuf::writeString
        ));

        AutoRecipeRegistry.registerGlobalVariableSerializer(Identifier.class, newSerializer(
              element -> new Identifier(element.getAsString()),
              id -> new JsonPrimitive(id.toString()),
              PacketByteBuf::readIdentifier,
              PacketByteBuf::writeIdentifier
        ));

        AutoRecipeRegistry.registerGlobalVariableSerializer(ItemStack.class, newSerializer(
              element -> {
                  if (JsonHelper.isString(element)) {
                      Identifier id = new Identifier(element.getAsString());
                      return new ItemStack(Registry.ITEM.get(id));
                  } else {
                      JsonObject json = element.getAsJsonObject();
                      Identifier id = new Identifier(JsonHelper.getString(json, "item"));
                      int count = JsonHelper.getInt(json, "count", 1);
                      return new ItemStack(Registry.ITEM.get(id), count);
                  }
              },
              stack -> {
                  JsonObject obj = new JsonObject();
                  obj.add("item", new JsonPrimitive(Registry.ITEM.getId(stack.getItem()).toString()));
                  obj.add("count", new JsonPrimitive(stack.getCount()));
                  return obj;
              },
              PacketByteBuf::readItemStack,
              PacketByteBuf::writeItemStack
        ));

        AutoRecipeRegistry.registerGlobalVariableSerializer(Ingredient.class, newSerializer(
              Ingredient::fromJson,
              Ingredient::toJson,
              Ingredient::fromPacket,
              (buf, value) -> value.write(buf)
        ));

        AutoRecipeRegistry.registerGlobalVariableSerializer(Block.class, newSerializer(
              element -> Registry.BLOCK.get(new Identifier(element.getAsString())),
              block -> new JsonPrimitive(Registry.BLOCK.getId(block).toString()),
              buf -> Registry.BLOCK.get(buf.readIdentifier()),
              (buf, value) -> buf.writeIdentifier(Registry.BLOCK.getId(value))
        ));
    }

    private static <T> RecipeVarSerializer<T> newSerializer(Function<JsonElement, T> readJsonFunction,
          Function<T, JsonElement> toJsonFunction,
          Function<PacketByteBuf, T> readPacketFunction, BiConsumer<PacketByteBuf, T> writePacketConsumer) {
        return new RecipeVarSerializer<>() {
            public T readJson(JsonElement element) {
                return readJsonFunction.apply(element);
            }

            @Override
            public JsonElement toJson(T value) {
                return toJsonFunction.apply(value);
            }

            public T readPacket(PacketByteBuf buf) {
                return readPacketFunction.apply(buf);
            }

            public void writePacket(PacketByteBuf buf, T value) {
                writePacketConsumer.accept(buf, value);
            }

        };
    }
}