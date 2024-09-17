package top.belovedyaoo.acs.core.eo;

import com.tangzc.mybatisflex.annotation.FieldFill;
import com.tangzc.mybatisflex.annotation.DefaultValue;
import com.tangzc.mybatisflex.annotation.FillData;
import com.tangzc.mybatisflex.annotation.FillTime;
import com.tangzc.mybatisflex.annotation.handler.AutoFillHandler;
import com.tangzc.mybatisflex.core.FieldTypeHandler;
import com.tangzc.mybatisflex.util.SpringContextUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static top.belovedyaoo.acs.common.toolkit.LogUtil.LOGGER;

/**
 * 实体操作基类
 *
 * @author BelovedYaoo
 * @version 1.0
 */
public class EntityOperate {

    /**
     * get、set方法前缀
     */
    static final String GET_PREFIX = "get";
    static final String SET_PREFIX = "set";

    /**
     * 需要处理的字段的缓存列表
     */
    private static final Map<Class<?>, List<Field>> FIELD_LIST_CATCH_MAP = new HashMap<>();

    /**
     * 基本类型与包装类型对应关系
     */
    public Map<Class<?>, Class<?>> baseTypeClassMap = new HashMap<>();

    {
        baseTypeClassMap.put(Byte.class, byte.class);
        baseTypeClassMap.put(Short.class, short.class);
        baseTypeClassMap.put(Integer.class, int.class);
        baseTypeClassMap.put(Long.class, long.class);
        baseTypeClassMap.put(Double.class, double.class);
        baseTypeClassMap.put(Float.class, float.class);
        baseTypeClassMap.put(Character.class, char.class);
        baseTypeClassMap.put(Boolean.class, boolean.class);
    }

    /**
     * 获取给定类实例中的字段列表
     *
     * @param clazz 类实例
     * @return 字段列表
     */
    public static List<Field> getFieldList(Class<?> clazz) {
        return FIELD_LIST_CATCH_MAP.computeIfAbsent(clazz, ct -> {
            Field[] declaredFields = clazz.getDeclaredFields();
            List<Field> fields = new ArrayList<>(Arrays.asList(declaredFields));
            for (Class<?> superClass = clazz.getSuperclass(); superClass != null; superClass = superClass.getSuperclass()) {
                declaredFields = superClass.getDeclaredFields();
                fields.addAll(Arrays.asList(declaredFields));
            }
            return fields.stream()
                    .filter(field -> AnnotatedElementUtils.hasMetaAnnotationTypes(field, FillTime.class) ||
                            AnnotatedElementUtils.hasMetaAnnotationTypes(field, FillData.class) ||
                            AnnotatedElementUtils.hasMetaAnnotationTypes(field, DefaultValue.class))
                    .collect(Collectors.toList());
        });
    }

    /**
     * 通过类和字段获取属性描述符
     * @param clazz 类实例
     * @param field 字段实例
     * @return 属性描述符
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, Field field) {

        List<PropertyDescriptor> beanPropertiesList = Arrays.stream(ReflectUtils.getBeanProperties(clazz))
                .filter(bp -> bp.getName().equals(field.getName())).toList();

        if (!beanPropertiesList.isEmpty()) {
            return beanPropertiesList.stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(clazz.getName() + "下没有找到字段" + field.getName() + "的getter、setter"));
        }

        try {
            Method getter = getGetterOrSetterByField(clazz,field,true);
            Method setter = getGetterOrSetterByField(clazz,field,false);
            return new PropertyDescriptor(field.getName(), getter, setter);
        } catch (IntrospectionException e) {
            throw new RuntimeException("获取" + clazz.getName() + "的Bean属性信息异常");
        }

    }

    /**
     * 从给定的类实例中查找指定字段实例的getter、setter。
     *
     * @param clazz      要搜索的类
     * @param field      要搜索的字段
     * @param findGetter 是否查找getter方法
     *
     * @return 返回找到的方法对象，如果没有找到则返回null
     */
    public static Method getGetterOrSetterByField(Class<?> clazz, Field field, boolean findGetter) {
        try {
            // 此处必须显式声明方法的参数类型，若直接缺省可能会导致getter为空
            Class<?>[] parameterTypes = findGetter ? new Class<?>[0] : new Class<?>[]{field.getType()};
            return clazz.getMethod(field.getName(), parameterTypes);
        } catch (NoSuchMethodException e) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals((findGetter ? GET_PREFIX : SET_PREFIX) + field.getName())) {
                    return method;
                }
            }
        }
        return null;
    }

    public void fill(FieldFill fill, Object object, Class<?> clazz, List<Field> fieldList) {
        Now now = new Now();
        fieldList.forEach(field -> {
            // 默认值
            setDefaultVale(fill, object, clazz, field);
            // 操作时间
            setOptionDate(object, clazz, field, now);
            // 操作数据
            setOptionData(object, clazz, field);
        });
    }

    private void setDefaultVale(FieldFill fill, Object object, Class<?> clazz, Field field) {
        DefaultValue defaultValue = AnnotatedElementUtils.getMergedAnnotation(field, DefaultValue.class);
        if (defaultValue != null && (defaultValue.fill() == FieldFill.INSERT_UPDATE || defaultValue.fill() == fill)) {
            PropertyDescriptor propertyDescriptor = getPropertyDescriptor(clazz, field);
            Method readMethod = propertyDescriptor.getReadMethod();
            boolean canSet = ReflectionUtils.invokeMethod(readMethod, object) == null;
            if (canSet) {
                Object newVal = convert(field, defaultValue);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.setAccessible(true);
                ReflectionUtils.invokeMethod(writeMethod, object, newVal);
            }
        }
    }

    private void setOptionDate(Object object, Class<?> clazz, Field field, Now now) {
        FillTime fillTime = AnnotatedElementUtils.getMergedAnnotation(field, FillTime.class);
        if (fillTime != null) {
            PropertyDescriptor propertyDescriptor = getPropertyDescriptor(clazz, field);
            boolean canSet = fillTime.override() || ReflectionUtils.invokeMethod(propertyDescriptor.getReadMethod(), object) == null;
            if (canSet) {
                Class<?> type = getDateType(clazz, field);
                Object nowDate = Optional.ofNullable(now.now(type, fillTime.format()))
                        .orElseThrow(() -> new RuntimeException("类：" + clazz + "的字段：" + field.getName()
                                + "的类型不支持。仅支持String、Long、long、Date、LocalDate、LocalDateTime"));
                // 赋值
                ReflectionUtils.invokeMethod(propertyDescriptor.getWriteMethod(), object, nowDate);
            }
        }
    }

    private void setOptionData(Object object, Class<?> clazz, Field field) {
        FillData fillData = AnnotatedElementUtils.getMergedAnnotation(field, FillData.class);
        if (fillData != null) {
            PropertyDescriptor propertyDescriptor = getPropertyDescriptor(clazz, field);
            // 判断原来值为null，或者覆盖选项为true
            boolean canSet = fillData.override() || ReflectionUtils.invokeMethod(propertyDescriptor.getReadMethod(), object) == null;
            if (canSet) {
                Object userInfo = null;
                AutoFillHandler<?> instance = getAutoFillHandler(fillData.value());
                if (instance != null) {
                    userInfo = instance.getVal(object, clazz, field);
                }
                // 如果当前未取到信息，不设置
                if (userInfo != null) {
                    // 先校验类型是否一致
                    if (!this.checkTypeConsistency(userInfo.getClass(), field.getType())) {
                        String errorMsg = clazz.getName() + "中的字段" + field.getName() + "的类型（" + field.getType() + "）与" + instance.getClass() + "返回值的类型（" + userInfo.getClass() + "）不一致";
                        throw new RuntimeException(errorMsg);
                    }
                    // 赋值
                    ReflectionUtils.invokeMethod(propertyDescriptor.getWriteMethod(), object, userInfo);
                }
            }
        }
    }

    /**
     * 校验两个类型是否一致，涵盖了8大基本类型的自动转换
     */
    private boolean checkTypeConsistency(Class<?> aClass, Class<?> bClass) {
        return aClass == bClass ||
                baseTypeClassMap.get(aClass) == bClass ||
                baseTypeClassMap.get(bClass) == aClass;
    }

    /**
     * 缓存AutoFillHandler，同时寻找
     */
    private AutoFillHandler<?> getAutoFillHandler(Class<? extends AutoFillHandler<?>> autoFillHandler) {
        try {
            return SpringContextUtil.getBeanOfType(autoFillHandler);
        } catch (NoUniqueBeanDefinitionException ignore) {
            throw new RuntimeException("发现了多个" + autoFillHandler.getName() + "的实现，请保持Spring中只有一个实例。");
        } catch (NoSuchBeanDefinitionException ignore) {
            if (autoFillHandler.isInterface()) {
                LOGGER.warn("没有找到{}的实现，操作数据无法自动填充。", autoFillHandler.getName());
            } else {
                LOGGER.warn("{}需要注册到spring，否则操作数据无法自动填充。", autoFillHandler.getName());
            }
        }
        return null;
    }

    /**
     * 获取日期类字段的类型
     */
    private Class<?> getDateType(Class<?> clazz, Field field) {
        Class<?> type = field.getType();
        try {
            FieldTypeHandler fieldTypeHandler = SpringContextUtil.getBeanOfType(FieldTypeHandler.class);
            type = fieldTypeHandler.getDateType(clazz, field);
        } catch (BeansException ignore) {
        }

        return type;
    }

    /**
     * 默认值转化
     *
     * @param field        默认值字段
     * @param defaultValue 默认值注解
     *
     * @return 默认值
     */
    private Object convert(Field field, DefaultValue defaultValue) {
        String value = defaultValue.value();
        String format = defaultValue.format();
        Class<?> type = field.getType();
        Map<Class<?>, Function<String, Object>> convertFuncMap = new HashMap<>(16) {{
            put(String.class, value -> value);
            put(Long.class, Long::parseLong);
            put(long.class, Long::parseLong);
            put(Integer.class, Integer::parseInt);
            put(int.class, Integer::parseInt);
            put(Boolean.class, Boolean::parseBoolean);
            put(boolean.class, Boolean::parseBoolean);
            put(Double.class, Double::parseDouble);
            put(double.class, Double::parseDouble);
            put(Float.class, Float::parseFloat);
            put(float.class, Float::parseFloat);
            put(BigDecimal.class, BigDecimal::new);
            put(Date.class, value -> {
                try {
                    return new SimpleDateFormat(format).parse(value);
                } catch (ParseException e) {
                    throw new RuntimeException("日期格式" + format + "与值" + value + "不匹配！");
                }
            });
            put(LocalDate.class, value -> {
                try {
                    return LocalDate.parse(value, DateTimeFormatter.ofPattern(format));
                } catch (Exception e) {
                    throw new RuntimeException("日期格式" + format + "与值" + value + "不匹配！");
                }
            });
            put(LocalDateTime.class, value -> {
                try {
                    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(format));
                } catch (Exception e) {
                    throw new RuntimeException("日期格式" + format + "与值" + value + "不匹配！");
                }
            });
        }};
        Function<String, Object> convertFunc = convertFuncMap.getOrDefault(type, val -> {
            if (type.isEnum()) {
                Object[] enumConstants = type.getEnumConstants();
                for (Object enumConstant : enumConstants) {
                    if (Objects.equals(val, enumConstant.toString())) {
                        return enumConstant;
                    }
                }
                throw new RuntimeException("默认值" + val + "与枚举" + type.getName() + "不匹配！");
            } else {
                return val;
            }
        });
        return convertFunc.apply(value);
    }

    /**
     * 框架内部使用，定义当前时间的时间对象
     */
    private static class Now {

        private final LocalDateTime localDateTime = LocalDateTime.now();
        private final LocalDate localDate = this.localDateTime.toLocalDate();
        private final Date date = Date.from(this.localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        private final Long timestamp = this.date.getTime();

        public Object now(Class<?> type, String format) {

            if (type == String.class) {
                return this.localDateTime.format(DateTimeFormatter.ofPattern(format));
            }

            if (type == long.class || type == Long.class) {
                return timestamp;
            }

            if (type == Date.class) {
                return date;
            }

            if (type == LocalDate.class) {
                return localDate;
            }

            if (type == LocalDateTime.class) {
                return localDateTime;
            }

            return null;
        }

    }


}
