package tk.mybatis.mapper.provider;

import org.apache.ibatis.mapping.MappedStatement;

import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

public class SpecialSelectProvider extends MapperTemplate {

    public SpecialSelectProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    /**
     * 参数实体名
     */
    private static final String entityName = "_params";

    /**
     * 根据主键进行查询并进行锁表
     *
     * @param ms
     */
    public String selectByPrimaryKeyForUpdate(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        //将返回值修改为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlHelper.selectAllColumns(entityClass));
        sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.wherePKColumns(entityClass));
        sql.append(SqlHelper.addForUpdate());
        return sql.toString();
    }

    /**
     * 查询并进行锁表
     *
     * @param ms
     * @return
     */
    public String selectForUpdate(MappedStatement ms) {
        StringBuilder sql = new StringBuilder();
        sql.append(selectBy(ms));
        sql.append(SqlHelper.addForUpdate());
        return sql.toString();
    }

    /**
     * 查询并进行锁表
     *
     * @param ms
     * @return
     */
    public String selectOneForUpdate(MappedStatement ms) {
        StringBuilder sql = new StringBuilder();
        sql.append(selectOneBy(ms));
        sql.append(SqlHelper.addForUpdate());
        return sql.toString();
    }

    /**
     * 根据指定格式参数进行查询
     *
     * @param ms
     * @return
     */
    public String selectBy(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        // 修改返回值类型为实体类型
        setResultType(ms, entityClass);
        // 实体名称
        StringBuilder sql = new StringBuilder();
        addWhereParams(sql, entityClass, entityName);
        return sql.toString();
    }

    public void addWhereParams(StringBuilder sql, Class entityClass, String entityName) {
        sql.append(SqlHelper.addParams(entityClass, entityName));
        sql.append(SqlHelper.selectAllColumns(entityClass));
        sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.whereAllIfColumns(entityName, entityClass, isNotEmpty()));
        sql.append(SqlHelper.orderByClause(entityName));
    }

    /**
     * 查询单条记录
     *
     * @param ms
     * @return
     */
    public String selectOneBy(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        // 修改返回值类型为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        addWhereParams(sql, entityClass, entityName);

        return sql.toString();
    }

    /**
     * 查询总数
     *
     * @param ms
     * @return
     */
    public String selectCountBy(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlHelper.addParams(entityClass, entityName));
        sql.append(SqlHelper.selectCount(entityClass));
        sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.whereAllIfColumns(entityName, entityClass, isNotEmpty()));

        return sql.toString();
    }

}
