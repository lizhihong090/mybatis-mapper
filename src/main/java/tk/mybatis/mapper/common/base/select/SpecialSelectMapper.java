package tk.mybatis.mapper.common.base.select;

import org.apache.ibatis.annotations.SelectProvider;
import tk.mybatis.mapper.provider.SpecialSelectProvider;

import java.util.List;

public interface SpecialSelectMapper<T> {

    /**
     * 根据主键字段进行查询，并且将该记录锁定
     *
     * @param key
     * @return
     */
    @SelectProvider(type = SpecialSelectProvider.class, method = "dynamicSQL")
    T selectByPrimaryKeyForUpdate(Object key);

    /**
     * 根据实体中的属性值进行查询，并且锁定表
     *
     * @param args
     * @return
     */
    @SelectProvider(type = SpecialSelectProvider.class, method = "dynamicSQL")
    List<T> selectForUpdate(Object... args);

    /**
     * 根据实体中的属性值进行查询，并且锁定表
     *
     * @param args
     * @return
     */
    @SelectProvider(type = SpecialSelectProvider.class, method = "dynamicSQL")
    T selectOneForUpdate(Object... args);

    /**
     * 根据定义的不定长参数查询
     * 参数格式应为：参数名1，参数值1，参数名2，参数值2... 以此类推
     * 特殊参数名：orderBy用于排序，格式为："orderBy", "id asc, name desc"
     *
     * @param args
     * @return
     */
    @SelectProvider(type = SpecialSelectProvider.class, method = "dynamicSQL")
    List<T> selectBy(Object... args);

    /**
     * 根据定义的不定长参数查询一个对象
     * 参数格式应为：参数名1，参数值1，参数名2，参数值2... 以此类推
     *
     * @param args
     * @return
     */
    @SelectProvider(type = SpecialSelectProvider.class, method = "dynamicSQL")
    T selectOneBy(Object... args);

    /**
     * 根据不定长参数查询数量
     * 参数格式应为：参数名1，参数值1，参数名2，参数值2... 以此类推
     *
     * @param args
     * @return
     */
    @SelectProvider(type = SpecialSelectProvider.class, method = "dynamicSQL")
    int selectCountBy(Object... args);
}
