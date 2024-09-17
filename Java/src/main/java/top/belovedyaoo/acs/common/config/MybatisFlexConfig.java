package top.belovedyaoo.acs.common.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.belovedyaoo.acs.core.base.LogicDeleteProcessor;
import top.belovedyaoo.acs.core.eo.EntityInsertListener;
import top.belovedyaoo.acs.core.eo.EntityUpdateListener;

/**
 * Mybatis-Flex 框架的配置类
 *
 * @author BelovedYaoo
 * @version 1.1
 */
@Configuration
public class MybatisFlexConfig {

    /**
     * Mybatis-Flex 配置初始化
     */
    @Bean
    public static void init() {
        FlexGlobalConfig globalConfig = FlexGlobalConfig.getDefaultConfig();
        // 设置逻辑删除处理器
        LogicDeleteManager.setProcessor(new LogicDeleteProcessor());
        // 开启 SQL 审计功能
        AuditManager.setAuditEnable(true);
        // 设置 SQL 审计收集器
        AuditManager.setMessageCollector(new ConsoleMessageCollector());
        // 注册插入监听器、更新监听器
        globalConfig.registerInsertListener(new EntityInsertListener(), Object.class);
        globalConfig.registerUpdateListener(new EntityUpdateListener(), Object.class);
    }

}
