package top.belovedyaoo.acs.common.config;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 *
 * @author BelovedYaoo
 * @version 1.0
 */
@Configuration
public class SaTokenConfigurer implements WebMvcConfigurer {

    /**
     * Sa-Token 整合 jwt (Simple 简单模式)
     * @return 整合规则
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    // 注册 Sa-Token 的路由拦截器，自定义认证规则
    // @Override
    // public void addInterceptors(InterceptorRegistry registry) {
    //     registry.addInterceptor(new SaInterceptor((handle) -> {
    //         StpUtil.checkLogin();
    //     })).addPathPatterns("/**").excludePathPatterns("/auth/**");
    // }

}
