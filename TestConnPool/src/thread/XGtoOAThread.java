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
		// TODO �Զ����ɵĹ��캯�����
		
	}

	public boolean initialContext() {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>");
		logger.info(""+this.getClass().getName()+"�߳�����");
		try {
			logger.info(">>>>>>>>>>>>>�̳߳�ʼ���ɹ�>>>>>>>>>>>>>>");
			return true;
		}catch(Exception e) {
			//�����쳣��ʱ����Ҫ������һ���߳�
			notifyListener();
			logger.error(e.getMessage(),e);
			logger.info(">>>>>>>>>>>>>�߳�����ʧ��>>>>>>>>>>>>>>");
			return false;
		}
	}
	/**
	 * ֪ͨ������
	 */
	public void notifyListener() {
		//֪ͨ���۲����и���
		super.setChanged();
		notifyObservers();
	}
	
	public void run() {
		//����߳�������������Ҫ��ʼ����ҵ��
		if(initialContext()) {
			try {
				//����ҵ��
				doBussiness();
			}catch(Exception e){
				notifyListener();
				logger.error(e.getMessage(),e);
			}
		}
	}
	
	/**
	 * xgϵͳ����֮��oaϵͳͬ������
	 * data_xsxx,data_pjzy,data_xscj
	 * @throws InterruptedException 
	 */
	public void doBussiness() throws InterruptedException {
		boolean beStart = true;
		while(beStart) {
			logger.info(">>>>>>>>>>>>>�߳�ִ�С���"+this.getClass().getName()+">>>>>>>>>>>>>>");
			//ͬ�����ݵķ���
			syncTableData("data_xsxx",null);
			syncTableData("data_xscj",null);
			syncTableData("data_pjzy",null);
			//ÿ��һ��ʱ������ͬ������
			Thread.sleep(sleep);
		}
	}
	
	/**
	 * oaϵͳͬ��xgϵͳ�б������
	 * ��Щ�����Ǳ仯�ģ�������SQL���
	 * select * from data_xsxx where xscj>90
	 */
	public void syncTableData(String tableName, Map<String, Object> filter) {
		//���ݲ�����ı�����ѯ���е��ֶ���
		StringBuffer query_sql = new StringBuffer
				("select * from ").append(tableName).append(" where 1=1");
		//���÷�����ѯ
		if(filter != null && !filter.isEmpty()) {
			Set<String> filter_keySet = filter.keySet();
			for(Object key:filter_keySet) {
				query_sql.append(" and ").append(key).append("=:").append(key);
			}
		}
		List<Map> oaQueryList =oaDataSource.queryForList(query_sql.toString(),null);
		List<Map> yxQueryList = yxDataSource.queryForList(query_sql.toString(),null);
		//�жϵ�ǰ�Ĳ����Ǹ��»��ǲ���
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










