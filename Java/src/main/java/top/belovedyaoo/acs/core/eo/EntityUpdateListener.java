package top.belovedyaoo.acs.core.eo;

import com.mybatisflex.annotation.UpdateListener;
import com.tangzc.mybatisflex.annotation.FieldFill;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 实体更新监听器
 *
 * @author BelovedYaoo
 * @version 1.0
 */
public class EntityUpdateListener extends EntityOperate implements UpdateListener {

    @Override
    public void onUpdate(Object obj) {
        Class<?> clazz = obj.getClass();
        List<Field> fieldList = getFieldList(clazz);

        if (!fieldList.isEmpty()) {
            fill(FieldFill.UPDATE, obj, clazz, fieldList);
        }
    }

}
