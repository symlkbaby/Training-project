package thread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.ListComparatorUtil;
import shen.DataBaseSource;

public class XGtoOAThread extends AbstractThread{

	public XGtoOAThread(DataBaseSource oaDataSource, DataBaseSource yxDataSource, Long sleep) {
		super(oaDataSource, yxDataSource, sleep);
		// TODO 自动生成的构造函数存根
		
	}

	public boolean initialContext() {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>");
		logger.info(""+this.getClass().getName()+"线程启动");
		try {
			logger.info(">>>>>>>>>>>>>线程初始化成功>>>>>>>>>>>>>>");
			return true;
		}catch(Exception e) {
			//发现异常的时候，需要启动另一个线程
			notifyListener();
			logger.error(e.getMessage(),e);
			logger.info(">>>>>>>>>>>>>线程启动失败>>>>>>>>>>>>>>");
			return false;
		}
	}
	/**
	 * 通知监听器
	 */
	public void notifyListener() {
		//通知被观察着有更新
		super.setChanged();
		notifyObservers();
	}
	
	public void run() {
		//如果线程正常启动，需要开始处理业务
		if(initialContext()) {
			try {
				//处理业务
				doBussiness();
			}catch(Exception e){
				notifyListener();
				logger.error(e.getMessage(),e);
			}
		}
	}
	
	/**
	 * xg系统更新之后，oa系统同步更新
	 * data_xsxx,data_pjzy,data_xscj
	 * @throws InterruptedException 
	 */
	public void doBussiness() throws InterruptedException {
		boolean beStart = true;
		while(beStart) {
			logger.info(">>>>>>>>>>>>>线程执行——"+this.getClass().getName()+">>>>>>>>>>>>>>");
			//同步数据的方法
			syncTableData("data_xsxx",null);
			syncTableData("data_xscj",null);
			syncTableData("data_pjzy",null);
			//每隔一段时间重新同步数据
			Thread.sleep(sleep);
		}
	}
	
	/**
	 * oa系统同步xg系统中表的数据
	 * 哪些东西是变化的，表名，SQL语句
	 * select * from data_xsxx where xscj>90
	 */
	public void syncTableData(String tableName, Map<String, Object> filter) {
		//根据产传入的表名查询表中的字段名
		StringBuffer query_sql = new StringBuffer
				("select * from ").append(tableName).append(" where 1=1");
		//调用方法查询
		if(filter != null && !filter.isEmpty()) {
			Set<String> filter_keySet = filter.keySet();
			for(Object key:filter_keySet) {
				query_sql.append(" and ").append(key).append("=:").append(key);
			}
		}
		List<Map> oaQueryList =oaDataSource.queryForList(query_sql.toString(),null);
		List<Map> yxQueryList = yxDataSource.queryForList(query_sql.toString(),null);
		//判断当前的操作是更新还是插入
		List[] compare = ListComparatorUtil.compare(yxQueryList,oaQueryList);
		List<Map> add_Result = compare[0];
		if(add_Result != null && !add_Result.isEmpty()) {
			for(int i = 0;i < add_Result.size();i++) {
				Map map = add_Result.get(i);
				Set keySet = map.keySet();
				Map<String,Object> param = new HashMap<>();
				StringBuffer sql = new StringBuffer();
				StringBuffer fields = new StringBuffer();
				StringBuffer values = new StringBuffer();
				for(Object key:keySet) {
					fields.append(",").append(key);
					values.append(",:").append(key);
					param.put(key.toString(),map.get(key));
				}
				sql.append("insert into ").append(tableName).append("(")
				.append(fields.deleteCharAt(0)).append(")")
				.append(" VALUES(").append(values.deleteCharAt(0)).append(")");
				oaDataSource.executeUpdate(sql.toString(), param);
			}
		}
		List<Map> update_Result = compare[1];
		if(update_Result !=null && !update_Result.isEmpty()) {
			for(int i=0;i<update_Result.size();i++) {
				Map map = update_Result.get(i);
				Set keySet = map.keySet();
				Map<String,Object> param = new HashMap<>();
				StringBuffer sql = new StringBuffer();
				StringBuffer fields = new StringBuffer();
				for(Object key:keySet) {
					fields.append(",").append(key).append("=:").append(key);
					param.put(key.toString(),map.get(key));
				}
				sql.append("UPDATE ").append(tableName).append(" set ").
				append(fields.deleteCharAt(0)).append(" WHERE id=:id");

				oaDataSource.executeUpdate(sql.toString(), param);

			}
			
		}

	}
}










