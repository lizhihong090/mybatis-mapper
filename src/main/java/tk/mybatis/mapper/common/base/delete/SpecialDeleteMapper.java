package tk.mybatis.mapper.common.base.delete;

import org.apache.ibatis.annotations.DeleteProvider;
import tk.mybatis.mapper.provider.SpecialDeleteProvider;

/**
 * Created by leo on 2017/3/31.
 */
public interface SpecialDeleteMapper<T> {

    /**
     * 根据定义的不定长参数删除一个对象
     * 参数格式应为：参数名1，参数值1，参数名2，参数值2... 以此类推
     *
     * @param args
     * @return
     */
    @DeleteProvider(type = SpecialDeleteProvider.class, method = "dynamicSQL")
    int deleteBy(Object... args);
}
