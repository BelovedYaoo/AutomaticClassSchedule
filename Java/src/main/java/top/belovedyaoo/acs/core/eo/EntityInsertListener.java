package top.belovedyaoo.acs.core.eo;

import com.mybatisflex.annotation.InsertListener;
import com.tangzc.mybatisflex.annotation.FieldFill;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 实体插入监听器
 *
 * @author BelovedYaoo
 * @version 1.0
 */
public class EntityInsertListener extends EntityOperate implements InsertListener {

    @Override
    public void onInsert(Object obj) {
        Class<?> clazz = obj.getClass();
        List<Field> fieldList = getFieldList(clazz);

        if (!fieldList.isEmpty()) {
            fill(FieldFill.INSERT, obj, clazz, fieldList);
        }
    }


}
