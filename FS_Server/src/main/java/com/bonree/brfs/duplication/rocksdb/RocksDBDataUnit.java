package com.bonree.brfs.duplication.rocksdb;

import java.util.Arrays;

/*******************************************************************************
 * 版权信息：北京博睿宏远数据科技股份有限公司
 * Copyright: Copyright (c) 2007博睿宏远科技发展有限公司,Inc.All Rights Reserved.
 *
 * @Date 2020/3/19 18:11
 * @Author: <a href=mailto:zhangqi@bonree.com>张奇</a>
 * @Description:
 ******************************************************************************/
public class RocksDBDataUnit {
    String columnFamily;
    byte[] key;
    byte[] value;

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public RocksDBDataUnit(String columnFamily, byte[] key, byte[] value) {
        this.columnFamily = columnFamily;
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "RocksDBDataUnit{" +
                "columnFamily='" + columnFamily + '\'' +
                ", key=" + Arrays.toString(key) +
                ", value=" + Arrays.toString(value) +
                '}';
    }
}
