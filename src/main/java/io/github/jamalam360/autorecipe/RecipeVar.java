package io.github.jamalam360.autorecipe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a field is annotated with this annotation, it is defined as a variable within the recipe (i.e. a JSON field).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecipeVar {

    /**
     * @return The serialized name of the variable. If this is omitted, the fields name is used.
     *
     * @apiNote This method can return a name in the form of `path1/path2/...` (i.e. including slashes) to define nested JSON.
     */
    String value() default "";

    /**
     * @return Whether this field is required in the recipe JSON. If this is false, then the field may be null when using the recipe.
     */
    boolean required() default true;
}
