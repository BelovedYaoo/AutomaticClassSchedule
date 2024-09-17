package top.belovedyaoo.acs.core.base;

import com.mybatisflex.core.logicdelete.NullableColumnLogicDeleteProcessor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 逻辑删除处理器
 *
 * @author BelovedYaoo
 * @version 1.0
 */
public class LogicDeleteProcessor extends NullableColumnLogicDeleteProcessor {

    public LogicDeleteProcessor() {
    }

    @Override
    public String getLogicDeletedValue() {
        // 当前时间的毫秒级时间戳
        long timestampMillis = System.currentTimeMillis();
        // 将时间戳转换为Instant
        Instant instant = Instant.ofEpochMilli(timestampMillis);
        // 将Instant转换为东八区的ZonedDateTime
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Shanghai"));
        // 格式化日期时间格式
        String formattedDateTime = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        // 拼接到SQL中
        return "'" + formattedDateTime + "'";
    }

}
