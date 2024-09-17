package top.belovedyaoo.acs.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Sa-Token 异常枚举
 *
 * @author BelovedYaoo
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum SaTokenExceptionEnum {

    // 此类型仅 Sa-Token 内部使用
    NOT_TOKEN("-1", "未提供Token"),

    INVALID_TOKEN("-2", "Token无效"),

    TOKEN_TIMEOUT("-3", "Token已过期"),

    BE_REPLACED("-4", "账号在别处登录"),

    KICK_OUT("-5", "您已被踢下线"),

    DEFAULT_MESSAGE("0", "会话未登录");

    private final String type;

    private final String desc;

    public static String getDescByType(String type) {
        for (SaTokenExceptionEnum value : SaTokenExceptionEnum.values()) {
            if (type.equals(value.getType())) {
                return value.getDesc();
            }
        }
        return DEFAULT_MESSAGE.getDesc();
    }

}
