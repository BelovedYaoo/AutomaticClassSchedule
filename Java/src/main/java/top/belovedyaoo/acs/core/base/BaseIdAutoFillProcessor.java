package top.belovedyaoo.acs.core.base;

import cn.hutool.core.util.IdUtil;
import com.tangzc.mybatisflex.annotation.handler.AutoFillHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 插入数据时自动生成主键ID
 * 
 * @author BelovedYaoo
 * @version 1.0
 */
@Component
public class BaseIdAutoFillProcessor implements AutoFillHandler<String> {

    @Override
    public String getVal(Object object, Class clazz, Field field) {
        return IdUtil.simpleUUID();
    }

}
