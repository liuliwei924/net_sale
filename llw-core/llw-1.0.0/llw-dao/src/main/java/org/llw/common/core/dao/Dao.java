/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package org.llw.common.core.dao;

import java.util.List;
import java.util.Map;

public abstract interface Dao
{
  public static final String MYBATIS_DAO = "daoMybatis";
  
 /***
  * 查寻操作  无参数
  * @param context  参数信息
  * @param namespace  命名空间
  * @return
  */
  public abstract List<Map<String,Object>> query(String namespace,String statement,String db);
  
  /***
   * 分页查寻 无参数
   * @param namespace 命名空间
   * @param statement 方法名
   * @param limit 最大查寻条数
   * @param offset 从第几条开始
   * @return
   */
  public abstract List<Map<String,Object>> query(String namespace,String statement,int limit, int offset,String db);
  /***
   * 查寻操作 有参数
   * @param namespace 命名空间
   * @param statement 方法名
   * @param limit 最大查寻条数
   * @param offset 从第几条开始
   * @return
   */
  public abstract List<Map<String,Object>> query(String namespace,String statement, Map<String,Object> paramData,String db);
  
  /***
   * 分页查寻存在参数
   * @param namespace 命名空间
   * @param statement 方法名
   * @param paramData 参数信息
   * @param limit 最大查寻条数
   * @param offset 从第几条开始
   * @return
   */
  public abstract List<Map<String,Object>> query(String namespace,String statement,Map<String,Object> paramData, int limit, int offset,String db);
  
  /**
   * 根据参数进行计数操作
   * @param namespace 命名空间
   * @param statement 方法名
   * @param paramData 参数信息
   * @return
   */
  public abstract int count(String namespace,String statement, Map<String,Object> paramData,String db);
  /**
   * 获取一条数据信息
   * @param namespace 命名空间
   * @param statement 方法名
   * @param paramData 参数信息
   * @return
   */
  public abstract Map<String,Object> get(String namespace,String statement, Map<String,Object> paramData,String db);

  /**
   * 获取一条数据信息
   * @param namespace 命名空间
   * @param key 主建名
   * @param value 值
   * @return
   */
  public abstract Map<String,Object> load(String namespace, String key, String value,String db);

  /**
   * 添加数据
   * @param namespace 命名空间
   * @param statement 方法名
   * @param paramData 参数信息
   * @return
   */
  public abstract int insert(String namespace,String statement, Map<String,Object> paramData,String db);
  
  /**
   * 
   * @param namespace 命名空间
   * @param statement 方法名
   * @param paramData 参数信息
   * @param db
   * @return
   */
  public abstract int batchInsert(String namespace,String statement, List<Map<String,Object>> paramData,String db);
  
  /**
   * 修改数据
   * @param namespace 命名空间
   * @param statement 方法名
   * @param paramData 参数信息
   * @return
   */
  public abstract int update(String namespace,String statement, Map<String,Object> paramData,String db);
  /**
   * 删除数据
   * @param namespace 命名空间
   * @param statement 方法名
   * @param paramData 参数信息
   * @return
   */
  public abstract int delete(String namespace,String statement, Map<String,Object> paramData,String db);
  
  /**
   * 删除数据
   * @param namespace 命名空间
   * @param statement 方法名
   * @param paramData 参数信息
   * @return
   */
  public abstract int batchDelete(String namespace,String statement, List<Map<String,Object>> paramData,String db);
  
  /**
   * 获取sql
   * @param namespace 命名空间
   * @param statement 方法名
   * @param paramData 参数信息
   * @return
   */
  public abstract String getSql(String namespace,String statement, Map<String,Object> paramData,String db);

}