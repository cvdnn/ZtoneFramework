package android.framework.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FindJNode {

	String jpath() default "";

	String parent() default "";

	String s() default "";

	int i() default 0;

	long l() default 0l;

	boolean b() default false;

	float f() default 0f;

	double d() default 0d;
}
