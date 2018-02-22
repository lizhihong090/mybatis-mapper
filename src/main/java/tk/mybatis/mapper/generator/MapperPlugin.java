/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package tk.mybatis.mapper.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.model.SimpleModelGenerator;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;
import tk.mybatis.mapper.MapperException;
import tk.mybatis.mapper.entity.Constant;
import tk.mybatis.mapper.util.StringUtil;

import java.util.*;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;

/**
 * 通用Mapper生成器插件
 *
 * @author liuzh
 */
public class MapperPlugin extends PluginAdapter {
    private Set<String> mappers = new HashSet<String>();
    private boolean caseSensitive = false;
    //开始的分隔符，例如mysql为`，sqlserver为[
    private String beginningDelimiter = "";
    //结束的分隔符，例如mysql为`，sqlserver为]
    private String endingDelimiter = "";
    //数据库模式
    private String schema;
    //注释生成器
    private CommentGeneratorConfiguration commentCfg;

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        //设置默认的注释生成器
        commentCfg = new CommentGeneratorConfiguration();
        commentCfg.setConfigurationType(MapperCommentGenerator.class.getCanonicalName());
        context.setCommentGeneratorConfiguration(commentCfg);
        //支持oracle获取注释#114
        context.getJdbcConnectionConfiguration().addProperty("remarksReporting", "true");
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        String mappers = this.properties.getProperty("mappers");
        if (StringUtility.stringHasValue(mappers)) {
            for (String mapper : mappers.split(",")) {
                this.mappers.add(mapper);
            }
        } else {
            throw new MapperException("Mapper插件缺少必要的mappers属性!");
        }
        String caseSensitive = this.properties.getProperty("caseSensitive");
        if (StringUtility.stringHasValue(caseSensitive)) {
            this.caseSensitive = caseSensitive.equalsIgnoreCase("TRUE");
        }
        String beginningDelimiter = this.properties.getProperty("beginningDelimiter");
        if (StringUtility.stringHasValue(beginningDelimiter)) {
            this.beginningDelimiter = beginningDelimiter;
        }
        commentCfg.addProperty("beginningDelimiter", this.beginningDelimiter);
        String endingDelimiter = this.properties.getProperty("endingDelimiter");
        if (StringUtility.stringHasValue(endingDelimiter)) {
            this.endingDelimiter = endingDelimiter;
        }
        commentCfg.addProperty("endingDelimiter", this.endingDelimiter);
        String schema = this.properties.getProperty("schema");
        if (StringUtility.stringHasValue(schema)) {
            this.schema = schema;
        }
    }

    public String getDelimiterName(String name) {
        StringBuilder nameBuilder = new StringBuilder();
        if (StringUtility.stringHasValue(schema)) {
            nameBuilder.append(schema);
            nameBuilder.append(".");
        }
        nameBuilder.append(beginningDelimiter);
        nameBuilder.append(name);
        nameBuilder.append(endingDelimiter);
        return nameBuilder.toString();
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 生成的Mapper接口
     *
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //获取实体类
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        //import接口
        for (String mapper : mappers) {
            interfaze.addImportedType(new FullyQualifiedJavaType(mapper));
            interfaze.addSuperInterface(new FullyQualifiedJavaType(mapper + "<" + entityType.getShortName() + ">"));
        }
        //import实体类
        interfaze.addImportedType(entityType);
        return true;
    }

    /**
     * 处理实体类的包和@Table注解
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    private void processEntityClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //引入JPA注解
		// 引入ColumnType注解
        topLevelClass.addImportedType("org.apache.ibatis.type.JdbcType");
        topLevelClass.addImportedType("tk.mybatis.mapper.annotation.ColumnType");
        topLevelClass.addImportedType("javax.persistence.*");
        String tableName = getTableName(introspectedTable);
        //是否忽略大小写，对于区分大小写的数据库，会有用
        if (caseSensitive && !topLevelClass.getType().getShortName().equals(tableName)) {
            topLevelClass.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
        } else if (!topLevelClass.getType().getShortName().equalsIgnoreCase(tableName)) {
            topLevelClass.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
        } else if (StringUtility.stringHasValue(schema)
                || StringUtility.stringHasValue(beginningDelimiter)
                || StringUtility.stringHasValue(endingDelimiter)) {
            topLevelClass.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
        }
    }

    /**
     * 获取表名
     * @param introspectedTable
     * @return
     */
    private String getTableName(IntrospectedTable introspectedTable) {
    	String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
        //如果包含空格，或者需要分隔符，需要完善
        if (StringUtility.stringContainsSpace(tableName)) {
            tableName = context.getBeginningDelimiter()
                    + tableName
                    + context.getEndingDelimiter();
        }
        
        return tableName;
    }

    /**
     * 生成基础实体类
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		processEntityClass(topLevelClass, introspectedTable);
		String propertyKey = introspectedTable.getFullyQualifiedTableNameAtRuntime().toLowerCase();
        String valueStr = this.properties.getProperty(propertyKey);
        if (null != valueStr) {
            String[] valueFields = valueStr.split(",");
            for (String valueField : valueFields) {
                if (!valueField.matches("\\w+\\(\\w+\\)")) {
                    throw new RuntimeException("额外属性添加失败，表【" + propertyKey +
                            "】，字段【" + valueField + "】");
                }
                Field extendField = new Field();

                String fieldName = valueField.substring(0, valueField.indexOf("("));
                extendField.setName(fieldName);
                extendField.setVisibility(JavaVisibility.PRIVATE);
                String type = valueField.substring(valueField.indexOf("(") + 1, valueField.length() -1);
                extendField.setType(getJavaType(type));
                extendField.addAnnotation("@CustomColumn(name = \"" + fieldName.toUpperCase() + "\")");
                topLevelClass.addField(extendField);
                Method getterMethod = SimpleModelGenerator.getGetter(extendField);
                Method setterMethod = getJavaBeanSetter(extendField);
                topLevelClass.addMethod(getterMethod);
                topLevelClass.addMethod(setterMethod);
            }
            topLevelClass.addImportedType("com.iquantex.generator.CustomColumn");
        }

		// 需要增加的所有属性的集合
		List<Field> fields = new ArrayList<Field>();
		
		// 添加表名常量
		String tableName = getTableName(introspectedTable);
		Field tableField = getConstantField("TABLE", "\"" + getDelimiterName(tableName) + "\"", FullyQualifiedJavaType.getStringInstance());
		fields.add(tableField);
		
		// 遍历列，获取列名常量和列数据字典常量
		List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
		List<Field> constantFields = new ArrayList<Field>();
		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			Field oriField = getJavaBeansField(introspectedColumn, context, introspectedTable);
			if (this.modelFieldGenerated(oriField, topLevelClass, introspectedColumn, introspectedTable,
					Plugin.ModelClassType.BASE_RECORD)) {
				// 属性名常量
				Field nameField = getConstantField("f_" + oriField.getName(), "\"" + oriField.getName() + "\"", FullyQualifiedJavaType.getStringInstance());
				fields.add(nameField);
				
				// 属性注释数据字典常量
				String comment = introspectedColumn.getRemarks();
				constantFields.addAll(getFieldDict(comment, oriField));
			}

		}

		// 将所有常量合并
		fields.addAll(constantFields);
		for (Field field : fields) {
			topLevelClass.addField(field);
		}
		
		return true;
	}

    /**
     * 获取java类型
     *
     * @param type
     * @return
     */
	private FullyQualifiedJavaType getJavaType(String type) {
        FullyQualifiedJavaType fqjt = typeMap.get(type);
        if (null == fqjt) {
            throw new RuntimeException("不支持的类型【" + type + "】");
        }

        return fqjt;
    }

    private static final Map<String, FullyQualifiedJavaType> typeMap = new HashMap();
    static {
        typeMap.put("string", FullyQualifiedJavaType.getStringInstance());
        typeMap.put("short", new FullyQualifiedJavaType(Short.class.getName()));
        typeMap.put("int", new FullyQualifiedJavaType(Integer.class.getName()));
        typeMap.put("long", new FullyQualifiedJavaType(Long.class.getName()));
        typeMap.put("boolean", new FullyQualifiedJavaType(Boolean.class.getName()));
        typeMap.put("double", new FullyQualifiedJavaType(Double.class.getName()));
        typeMap.put("float", new FullyQualifiedJavaType(Float.class.getName()));
        typeMap.put("char", new FullyQualifiedJavaType(Character.class.getName()));
        typeMap.put("byte", new FullyQualifiedJavaType(Byte.class.getName()));
    }

    /**
     * 获取setterMethod
     *
     * @param field
     * @return
     */
    private Method getJavaBeanSetter(Field field) {
        String property = field.getName();
        FullyQualifiedJavaType fqjt = field.getType();

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(JavaBeansUtil.getSetterMethodName(property));
        method.addParameter(new Parameter(fqjt, property));

        StringBuilder sb = new StringBuilder();
        sb.append("this."); //$NON-NLS-1$
        sb.append(property);
        sb.append(" = ");
        sb.append(property);
        sb.append(';');
        method.addBodyLine(sb.toString());

        return method;
    }

    /**
     * 获取属性名常量
     *
     * @param fieldName
     * @param value
     * @param type
     * @return
     */
    private Field getConstantField(String fieldName, String value, FullyQualifiedJavaType type) {
    	Field field = new Field();
    	field.setName(fieldName);
    	field.setFinal(true);
    	field.setStatic(true);
    	field.setType(type);
    	field.setVisibility(JavaVisibility.PUBLIC);
    	field.setInitializationString(value);
    	return field;
    }
    
    /**
     * 获取数据字典常量
     *
     * @param comment
     * @param field
     */
    private List<Field> getFieldDict(String comment, Field field) {
    	List<Field> fields = new ArrayList<Field>();
    	List<Constant> constants = parseComment(comment, field);
    	for (Constant constant : constants) {
    		String fieldName = "c_" + field.getName() + "_" + constant.getName();
    		FullyQualifiedJavaType type = field.getType();
    		String value = constant.getValue();

    		// long类型值需要添加L标识，其他类型有需要的时候再添加
    		if (Long.class.getName().equals(type.getFullyQualifiedName())) {
    		    value += "L";
            }

    		Field constField = getConstantField(fieldName, value, field.getType());
    		addFieldComment(constField, constant.getComment());
    		fields.add(constField);
    	}
    	
    	return fields;
    }
    
    private void addFieldComment(Field field, String comment) {
    	field.addJavaDocLine("/**");
        field.addJavaDocLine(" * " + comment);
        field.addJavaDocLine(" */");
    }
    
    /**
     * 注释解析
     * @param comment
     * @param field
     * @return
     */
	private List<Constant> parseComment(String comment, Field field) {
		List<Constant> constantList = new ArrayList<Constant>();
        if (StringUtil.isEmpty(comment)) {
            return constantList;
        }

        int pos = comment.indexOf("<>");
		if (pos != -1) {
			String[] constants = comment.substring(pos + 2).split("&");

			for (String c : constants) {
				try {
					String[] f = c.split("=");
					Constant constant = new Constant();
					constant.name = f[0].trim();
					constant.value = f[1].trim();
					if (FullyQualifiedJavaType.getStringInstance().equals(field.getType())
							&& !constant.value.startsWith("\"")) {
						// 如果是字符串,常量值前后要有双引号
						constant.value = "\"" + f[1] + "\"";
					}
					constant.comment = f[2];
					constantList.add(constant);

				} catch (Exception e) {
					continue;
				}
			}
		}
		
		return constantList;
	}
    
    /**
     * 生成实体类注解KEY对象
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        processEntityClass(topLevelClass, introspectedTable);
        return true;
    }

    /**
     * 生成带BLOB字段的对象
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        processEntityClass(topLevelClass, introspectedTable);
        return false;
    }

    //下面所有return false的方法都不生成。这些都是基础的CRUD方法，使用通用Mapper实现
    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerApplyWhereMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }
}
