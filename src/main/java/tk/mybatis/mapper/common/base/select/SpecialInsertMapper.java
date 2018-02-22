package tk.mybatis.mapper.common.base.select;

import org.apache.ibatis.annotations.InsertProvider;
import tk.mybatis.mapper.provider.SpecialInsertProvider;

import java.util.List;

/**
 * Created by leo on 2017/5/10.
 */
public interface SpecialInsertMapper<T> {

    /**
     * 批量插入，支持批量插入的数据库可以使用，例如MySQL,H2等，另外该接口限制实体包含`id`属性并且必须为自增列
     *
     * @param recordList
     * @return
     */
    @InsertProvider(type = SpecialInsertProvider.class, method = "dynamicSQL")
    int insertList(List<T> recordList);
}
