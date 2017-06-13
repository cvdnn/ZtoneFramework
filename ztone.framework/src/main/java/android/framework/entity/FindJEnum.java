package android.framework.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于JSON Entity中Enum数据查找，
 * 在JSON转pojo时会被调用注解的方法，并且该方法只能带1个int类型的参数
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FindJEnum {

}
