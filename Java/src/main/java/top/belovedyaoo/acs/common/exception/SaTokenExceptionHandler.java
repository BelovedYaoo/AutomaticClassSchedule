package top.belovedyaoo.acs.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.belovedyaoo.acs.common.toolkit.LogUtil;
import top.belovedyaoo.acs.core.result.Result;
import top.belovedyaoo.acs.core.result.ResultEnum;

/**
 * Sa-Token异常捕捉
 *
 * @author BelovedYaoo
 * @version 1.0
 */
@ControllerAdvice
@RestControllerAdvice
@RequiredArgsConstructor
public class SaTokenExceptionHandler {

    /**
     * Sa-Token登录异常处理
     *
     * @param nle 异常对象
     *
     * @return 统一接口返回值
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseBody
    public Result notLoginException(NotLoginException nle) {

        String message = SaTokenExceptionEnum.getDescByType(nle.getType());

        LogUtil.error(message);

        return Result.failed().resultType(ResultEnum.SESSION_INVALID).message(message);

    }

}
