package android.framework.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FindJArray {

	String jpath() default "";

	String parent() default "";

	Class<? extends Entity> meta() default Entity.class;
}
