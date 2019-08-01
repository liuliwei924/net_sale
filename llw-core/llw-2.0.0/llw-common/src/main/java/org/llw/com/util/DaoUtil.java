package org.llw.com.util;

import org.apache.ibatis.mapping.MappedStatement;
import org.llw.com.core.SpringAppContext;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

public class DaoUtil {
	private static JdbcTemplate jdbcTmeplate = null;
	private static SqlSessionTemplate sqlSessionTemplate = null;
	
	private DaoUtil(){}
	private static class InnerClassSingleton {
		private final static DaoUtil dataUtil = new DaoUtil();
	 }
	
	public static DaoUtil getInstance(){
		return InnerClassSingleton.dataUtil;
	}

	public  JdbcTemplate getJdbcTemplate(){
		if(jdbcTmeplate == null){
			jdbcTmeplate = (JdbcTemplate)SpringAppContext.getBean("jdbcTemplate");
		}
		return jdbcTmeplate;
	}
	
	
	public  SqlSessionTemplate getSqlSessionTemplate(){
		if(sqlSessionTemplate == null){
			sqlSessionTemplate = (SqlSessionTemplate)SpringAppContext.getBean("sqlSessionTemplate");
		}
		return sqlSessionTemplate;
	}
	
	public static String getSql(String mothodMame,Object paramObject){
		MappedStatement mappedStatement = sqlSessionTemplate.getConfiguration().getMappedStatement(mothodMame);
		return mappedStatement.getBoundSql(paramObject).getSql();
	}
}