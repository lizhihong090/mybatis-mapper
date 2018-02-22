package tk.mybatis.mapper.provider;

import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

/**
 * Created by leo on 2017/3/31.
 */
public class SpecialDeleteProvider extends MapperTemplate {
    /**
     * 参数实体名
     */
    private static final String entityName = "_params";

    public SpecialDeleteProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    /**
     * 通过条件删除
     *
     * @param ms
     * @return
     */
    public String deleteBy(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlHelper.addParams(entityClass, entityName));
        sql.append(SqlHelper.deleteFromTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.whereAllIfColumns(entityName, entityClass, isNotEmpty()));
        return sql.toString();
    }
}
