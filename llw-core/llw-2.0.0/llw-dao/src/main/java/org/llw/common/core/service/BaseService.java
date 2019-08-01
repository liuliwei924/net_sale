package org.llw.common.core.service;


import org.llw.com.constant.DuoduoConstant;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.web.page.Page;
import org.llw.com.web.page.PageUtil;
import org.llw.common.core.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


/**
 * 
 * @author liulw
 *
 */
public class BaseService {
	protected final String QUERY = "query";
	protected final String COUNT = "count";
	protected final String UPDATE = "update";
	protected final String DELETE = "delete";
	protected final String INSERT = "insert";
	
	
	@Autowired
	@Qualifier("daoMybatis")
	private Dao dao;
	
	public Dao getDao() {
		return dao;
	}
	
	/***
	 * 分页查询 根据 namespace中的query
	 * @param param 参数信息
	 * @param namespace 命名空间
	 * @return
	 */
	public AppResult query(AppParam params,String namespace){
		this.setOrderBy(params);
		AppResult backContext = new AppResult();
		if(params.getAttr().size() == 0){
			backContext.setRows(dao.query(namespace, QUERY, params.getDataBase()));
		}else{
			backContext.setRows(dao.query(namespace, QUERY, params.getAttr(), params.getDataBase()));
		}
		return backContext;
	}
	
	/***
	 * 分页查询 根据 namespace中的query 和 count
	 * @param param 参数信息
	 * @param namespace 命名空间
	 * @return
	 */
	public AppResult queryByPage(AppParam param,String namespace){
		this.setOrderBy(param);
		AppResult backContext = new AppResult();
		Page page = new Page(param.getCurrentPage(), param.getEveryPage());
		if(param.getAttr().size() == 0){
			backContext.setRows(dao.query(namespace,QUERY, param.getEveryPage(), param.getCurrentPage(), param.getDataBase()));
		}else{
			backContext.setRows(dao.query(namespace,QUERY, param.getAttr(), param.getEveryPage(), param.getCurrentPage(), param.getDataBase()));
		}
		PageUtil.createPage(page, dao.count(namespace, COUNT, param.getAttr(), param.getDataBase()));
		backContext.setPage(page);
		return backContext;
	}
	
	/***
	 * 查询数据 根据 namespace中的statement
	 * @param param 参数信息
	 * @param namespace 命名空间
	 * @param statement 方法名
	 * @return
	 */
	public AppResult query(AppParam param,String namespace,String statement){
		if(statement == null || statement.length() == 0){
			statement = QUERY;
		}
		this.setOrderBy(param);
		AppResult backContext = new AppResult();
		backContext.setRows(dao.query(namespace,statement, param.getAttr(), param.getDataBase()));
	
		return backContext;
	}
	/***
	 * 分页查询 根据 namespace中的queryStatement和countStatement
	 * @param param 参数信息
	 * @param namespace 命名空间
	 * @param queryStatement 查询语句
	 * @param countStatement 计数
	 * @return
	 */
	public AppResult queryByPage(AppParam param,String namespace,String queryStatement,String countStatement){
		
		if(queryStatement == null || queryStatement.length() == 0){
			queryStatement = QUERY;
		}
		if(countStatement == null || countStatement.length() == 0){
			countStatement = COUNT;
		}
		this.setOrderBy(param);
		Page page = new Page(param.getCurrentPage(), param.getEveryPage());
		AppResult backContext = new AppResult();
		if(param.getAttr().size() == 0){
			backContext.setRows(dao.query(namespace, queryStatement, param.getEveryPage(), param.getCurrentPage(), param.getDataBase()));
		}else{
			backContext.setRows(dao.query(namespace, queryStatement, param.getAttr(), param.getEveryPage(), param.getCurrentPage(), param.getDataBase()));
		}
		PageUtil.createPage(page, dao.count(namespace, countStatement, param.getAttr(), param.getDataBase()));
		backContext.setPage(page);
		return backContext;
	}
	
	/***
	 * insert 处理根据 insert 方法
	 * @param param 参数信息
	 * @param namespace 命名空间
	 * @return
	 */
	public AppResult insert(AppParam param,String namespace){
		int size = dao.insert(namespace, INSERT, param.getAttr(), param.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return backContext;
	}
	/***
	 * update 处理根据 update 方法
	 * @param param 参数信息
	 * @param namespace 命名空间
	 * @return
	 */
	public AppResult update(AppParam param,String namespace){
	
		int size = dao.update(namespace, UPDATE, param.getAttr(), param.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}
	/***
	 * delete 处理根据 delete 方法
	 * @param param 参数信息
	 * @param namespace 命名空间
	 * @return
	 */
	public AppResult delete(AppParam param,String namespace){
		int size = dao.delete(namespace, DELETE, param.getAttr(), param.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Delete_SIZE, size);
		return backContext;
	}
	/***
	 * 设置查询参数
	 * @param param
	 */
	private void setOrderBy(AppParam param){
		if(param.getOrderBy()!=null && param.getOrderBy().trim().length()!=0){
			StringBuffer orderSql = new StringBuffer();
			int i=0;
			String[] orderValues =(param.getOrderValue()==null? "":param.getOrderValue()).split(",");
			int orderSize = orderValues.length -1;
			for(String order:param.getOrderBy().split(",")){
				if (order == null || order.trim().length() == 0) {
					continue;
				}
				//order = StringUtil.getOderBy(order);
				String orderValue = PageUtil.ORDER_ASC;
				if (orderSize >= i) {
					orderValue = PageUtil.ORDER_ASC.equals(orderValues[i].toUpperCase())? PageUtil.ORDER_ASC:PageUtil.ORDER_DESC;
				}
				if(i==0){
					orderSql.append(" " + order + " " + orderValue);
					i++;
					continue;
				}
				orderSql.append(", " + order + " " + orderValue);
				i++;
			}
			param.getAttr().put("orderSql",orderSql.toString());
			if(orderSql.indexOf(",")<0){
				param.getAttr().put("orderBy",param.getOrderBy());
				String orderValue = param.getOrderValue()==null? "":param.getOrderValue();
				param.getAttr().put("orderValue",
					PageUtil.ORDER_ASC.equals(orderValue.toUpperCase())? PageUtil.ORDER_ASC:PageUtil.ORDER_DESC);
			}
		}
	}
}