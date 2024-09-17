package top.belovedyaoo.acs.core.sensitization;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解
 *
 * @author BelovedYaoo
 * @version 1.0
 */
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveInfoSerializer.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface Sensitization {

    SensitizationType type() default SensitizationType.DEFAULT;

    /**
     * 前置不需要打码的长度
     */
    int prefixLen() default 0;

    /**
     * 后置不需要打码的长度
     */
    int suffixLen() default 0;

    /**
     * 遮罩字符
     */
    String maskingChar() default "*";

}
