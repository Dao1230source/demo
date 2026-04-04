package org.source.spring.doc.domain.database;

import lombok.Data;

/**
 * 数据库列信息
 * <p>
 * 表示数据库表的列元数据信息，包含列名、类型、大小、
 * 是否可空、默认值、是否为主键、是否自增等属性。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Data
public class ColumnInfo {

    /**
     * 列名
     */
    private String columnName;

    /**
     * 列类型（数据库类型名称）
     */
    private String columnType;

    /**
     * 数据类型代码（SQL 类型代码）
     */
    private int dataType;

    /**
     * 列大小（长度或精度）
     */
    private int columnSize;

    /**
     * 小数位数（仅适用于数值类型）
     */
    private int decimalDigits;

    /**
     * 是否允许为空
     */
    private boolean nullable;

    /**
     * 列默认值
     */
    private String columnDefault;

    /**
     * 是否为主键
     */
    private boolean primaryKey;

    /**
     * 是否自增
     */
    private boolean autoIncrement;
}