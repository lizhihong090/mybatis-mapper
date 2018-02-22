package tk.mybatis.mapper.provider;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.type.JdbcType;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;
import tk.mybatis.mapper.util.StringUtil;

import java.util.Set;

/**
 * Created by leo on 2017/5/10.
 */
public class SpecialInsertProvider extends MapperTemplate {

    public SpecialInsertProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    /**
     * 批量插入(Oracle)
     *
     * @param ms
     */
    public String insertList(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        //开始拼sql
        StringBuilder sql = new StringBuilder();
        sql.append(SqlHelper.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.insertColumns(entityClass, false, false, false));
        sql.append(" ( ");
        sql.append("<foreach collection=\"list\" item=\"record\" separator=\"UNION ALL\" >");
        sql.append("<trim suffixOverrides=\"UNION ALL\">");
        sql.append("select ");
        //获取全部列
        Set<EntityColumn> columnList = EntityHelper.getColumns(entityClass);
        //当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            if (column.isId() && column.isInsertable()) {
                sql.append(getIdColumnHolder(column, "record"));
            } else if (!column.isId() && column.isInsertable()) {
                sql.append(getColumnHolder(column, "record"));
            }

            sql.append(",");
        }
        sql.setCharAt(sql.toString().length() - 1, ' ');
        sql.append(" from dual");
        sql.append("</trim>");
        sql.append("</foreach>");
        sql.append(")");
        return sql.toString();
    }

    private String getIdColumnHolder(EntityColumn column, String entityName) {
        if (StringUtil.isEmpty(column.getSequenceName())) {
            return getColumnHolder(column, entityName);
        }

        StringBuilder sql = new StringBuilder();
        sql.append("<choose><when test=\"");
        sql.append(entityName);
        sql.append(".");
        sql.append(column.getProperty());
        sql.append(" == null\"> GET_SEQ_NEXT('");
        sql.append(column.getSequenceName());
        sql.append("') as ");
        sql.append(column.getColumn());
        sql.append("</when><otherwise>");
        sql.append(getColumnHolder(column, entityName));
        sql.append("</otherwise></choose>");

        return sql.toString();
    }

    private String getColumnHolder(EntityColumn column, String entityName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" #{");
        sql.append(entityName);
        sql.append(".");
        sql.append(column.getProperty());
        getJdbcType(column, sql);
        sql.append("} as ");
        sql.append(column.getColumn());

        return sql.toString();
    }

    private void getJdbcType(EntityColumn column, StringBuilder sb) {
        JdbcType jdbcType = column.getJdbcType();
        if (jdbcType != null) {
            sb.append(",jdbcType=");
            sb.append(jdbcType.toString());
        } else if (column.getTypeHandler() != null) {
            sb.append(",typeHandler=");
            sb.append(column.getTypeHandler().getCanonicalName());
        } else if (!column.getJavaType().isArray()) {//当类型为数组时，不设置javaType#103
            sb.append(",javaType=");
            sb.append(column.getJavaType().getCanonicalName());
        }
    }
}
