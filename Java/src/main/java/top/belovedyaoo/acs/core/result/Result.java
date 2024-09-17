package top.belovedyaoo.acs.core.result;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回结果统一封装类
 *
 * @author BelovedYaoo
 * @version 1.2
 */
@Data
@NoArgsConstructor
@Getter(onMethod_ = @JsonGetter)
@Accessors(chain = true, fluent = true)
public class Result {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 状态
     */
    private Boolean state;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息描述
     */
    private String description;

    /**
     * 返回数据
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 成功方法
     */
    public static Result success() {
        Result result = new Result();
        result.resultType(ResultEnum.SUCCESS);
        return result;
    }

    /**
     * 失败方法
     */
    public static Result failed() {
        Result result = new Result();
        result.resultType(ResultEnum.FAILED);
        return result;
    }

    public Result data(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public Result data(Map<String, Object> map) {
        data.putAll(map);
        return this;
    }

    public Result resultType(ResultType resultEnum) {
        code(resultEnum.getCode());
        message(resultEnum.getMessage());
        description(resultEnum.getDescription());
        return this;
    }

}
