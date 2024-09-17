package top.belovedyaoo.acs.core.sensitization;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 数据脱敏序列化器
 *
 * @author BelovedYaoo
 * @version 1.0
 */
public class SensitiveInfoSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private SensitizationType type;

    private boolean useMasking = false;

    private int prefixLen;

    private int suffixLen;

    private String maskingChar;

    public static String email(String email, int front, int end) {
        if (StrUtil.isBlank(email)) {
            return "";
        } else {
            int index = StrUtil.indexOf(email, '@');
            return index <= 1 ? email : StrUtil.hide(email, front, index - end);
        }
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (useMasking && value != null) {
            switch (type) {
                case MOBILE_PHONE:
                    gen.writeString(DesensitizedUtil.mobilePhone(value));
                    break;
                case ID_CARD:
                    gen.writeString(DesensitizedUtil.idCardNum(value, prefixLen, suffixLen));
                    break;
                case EMAIL:
                    gen.writeString(email(value, prefixLen, suffixLen));
                    break;
                case CUSTOMIZE_RULE:
                    gen.writeString(StrUtil.hide(value, prefixLen, suffixLen));
                    break;
                case CHINESE_NAME:
                    gen.writeString(DesensitizedUtil.chineseName(value));
                    break;
                case DEFAULT:
                default:
                    gen.writeString(value);
            }
        } else {
            gen.writeObject(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            Sensitization desensitization = property.getAnnotation(Sensitization.class);
            if (desensitization != null) {
                this.type = desensitization.type();
                this.prefixLen = desensitization.prefixLen();
                this.suffixLen = desensitization.suffixLen();
                this.maskingChar = desensitization.maskingChar();
                useMasking = true;
            }
        }
        return this;
    }

}