package top.belovedyaoo.acs.common.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 日志工具类
 *
 * @author BelovedYaoo
 * @version 1.0
 */
@Component
public class LogUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

}
