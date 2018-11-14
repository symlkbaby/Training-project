package shen;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.log4j.Logger;
/**
 * ��װ���ݿ����
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DataBaseSource {
	protected static Logger logger = Logger.getLogger(DataBaseSource.class.getName());//������־�ļ�
	
	private  DataSource ds = null;//��������ԴΪ��
	
	public DataBaseSource(String properties) {
		try {
			Properties prop = new Properties();
			// ͨ����·�������������ļ�
			prop.load(DataBaseSource.class.getClassLoader().getResourceAsStream(properties));
			//��ȡ�����ļ������ݿ�������Ϣ��������ds����Դ
			ds = BasicDataSourceFactory.createDataSource(prop);
			/*properties�ļ���Properties��Ĺ�ϵ
                ͨ��properties�ļ��������Properties�ࡣ
                Ҳ����ͨ��xml�ļ������Properties�ࡣ
                     ����ͨ������·����ʽ����Properties�ļ���Ϣ��Ҳ����ʹ�����·�����ء�*/
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * <p>��ȡ��������</p>
	 *
	 * @return ���ݿ����Ӷ���
	 * @throws SQLException
	 */
	private   Connection getConnection() 
			throws SQLException{
		return ds.getConnection();
	}
	//��ͻ�ȡ���Ӷ�����
	
	/**
	 * <p>��ȡPreparedStatement</p>
	 *
	 * @param con
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	private  PreparedStatement prepareStatement(Connection con, String sql,
			Map<String, Object> params) throws Exception {//Map����StringΪ����objectΪֵ���൱�����ݿ����������к�����ֵ
		PreparedStatement pstat = null;//PreparedStatement��Ԥ�����,��������������Դ�����Ч��. Ҳ��JDBC�洢����
		if(params != null && !params.isEmpty()){
			List paramList = new ArrayList();//�����б���

			String para = ":[[A-Za-z_0-9]]+";//������ʽ���е��±�
			Pattern p = Pattern.compile(para);
			Matcher m = p.matcher(sql);
			List<String> keyList = new ArrayList<String>();//<String>�Ƿ��͵���˼��<String>�������˼�����������ֻ�ܷ�string���͵�
			while (m.find()) {
				int index = m.start();
				int end = m.end();
				String key = sql.substring(index+1, end);
				keyList.add(key);
			} 
			
			String[] split = sql.split(para);
			int length = split.length;
			boolean split_last_part_is_space = "".equals(split[length-1].trim());
			//sql���ָ�֮�����󲿷��Ƿ�Ϊ�հ׷�
			
			int keyListSize = keyList.size();
			StringBuffer sqlbak = new StringBuffer();//��һ���ַ�������
			for (int i = 0; i < length; i++) {
				sqlbak.append(split[i]);
				
				if((split_last_part_is_space && i == length-1) || i==keyListSize){
					//����󲿷�Ϊ�հ׷�ʱ����󲿷־Ͳ����ڲ���ռλ��
					continue;
				}
				
				String key = keyList.get(i);
				if(params.containsKey(key)){
					Object object = params.get(key);
					if(object instanceof Collection){//���ϲ���
						Collection c = (Collection)object;//����c =�����ϣ�����
						Iterator iterator = c.iterator();
						StringBuffer part = new StringBuffer();
						if(iterator.hasNext()){
							Object obj = iterator.next();
							paramList.add(obj);
							
							part.append(",?");
						}
						sqlbak.append(part.substring(1));
					}else if(object instanceof Object[]){//�������
						Object[] objs = (Object[])object;
						StringBuffer part = new StringBuffer();
						for(Object obj:objs){
							paramList.add(obj);
							
							part.append(",?");
						}
						sqlbak.append(part.substring(1));
					}else{//��������
						paramList.add(object);
						sqlbak.append("?");
					}
				}else{
					logger.error("����paramsδ�ҵ�ƥ�� :user_id ��ֵ");
				}
			}
			
			logger.info(sqlbak);
			
			if(!paramList.isEmpty()){
				pstat = con.prepareStatement(sqlbak.toString(),ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				for (int i = 0; i < paramList.size(); i++) {
					Object value = paramList.get(i);
					if(value instanceof java.util.Date){//���ڸ�ʽת��
						java.util.Date dateValue = (java.util.Date)value;
						java.sql.Timestamp timestamp = new Timestamp(dateValue.getTime());
						pstat.setObject(i + 1, timestamp);
					}else{
						pstat.setObject(i + 1, value);
					}
				}
			}
		}else{
			pstat = con.prepareStatement(sql);
			logger.info(sql);
		}
		
		
		
		return pstat;
	}
	
	
	
	
	/**
	 * <p>LIST��ѯ</p>
	 *
	 * @param sql	SQL
	 * @param params ����
	 * @return
	 */
	public   List<Map> queryForList(String sql,Map<String, Object> params)  {
		/*��ʦ���ע�ͣ�
		 * �÷�����װ���в�ѯ���ݿ���е����ݵĲ�������Ϊ������ͨ��
		 * ��Ҫ������еı����Բ��ܰ������д���������ֶ������ֶ�ֵ��
		 * �÷����а��ֶ�ֵ��ͨ���������ݽ��룬�ֶ�����ͨ��������̬��ȡ��*/
		List<Map> result = new ArrayList<Map>();
		Connection con = null;
		PreparedStatement pstat = null;
		ResultSet rs = null;
		//�����ڳ�ʼ���Ĳ���

		try {
			 con = getConnection();
			pstat = prepareStatement(con, sql, params);
			rs = pstat.executeQuery();//��ȡ���ݿ��е�ֵ

			if (rs != null) {
				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();//��ȡ������
				while (rs.next()) {//����ѭ��֪����ȡ��������ֵ
					Map map = new LinkedHashMap();
					for (int i = 0; i < columnCount; i++) {
						String columnName = metaData.getColumnName(i+1);//���������õ��е�����
						map.put(columnName, rs.getObject(i + 1));
					}
					result.add(map);//����Ϣ���뵽������
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}finally{
					rs = null;
				}
			}
			if (pstat != null) {
				try {
					pstat.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}finally{
					pstat = null;
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				} finally {
					con = null;
				}
			}
		}
		return result;
	}
	
	
	/**
	 * <p>���ݸ���</p>
	 *
	 * @param sql
	 * @param params
	 * @return		SQLִ��Ӱ���������
	 */
	public  int executeUpdate(String sql,Map<String, Object> params)  {
		int result = 0;
		Connection con = null;
		PreparedStatement pstat = null;

		try {
			 con = getConnection();//�����
			pstat = prepareStatement(con, sql, params);//Connection ,String,Map<String,Object>
			result = pstat.executeUpdate();//����ִ��SQL���Ĵ������

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (pstat != null) {
				try {
					pstat.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}finally{
					pstat = null;
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				} finally {
					con = null;
				}
			}
		}
		return result;
	}
	
	/**
	 * <p>��ҳ��ѯ</p>
	 * <p>����mysql��limit�﷨�� select * from table limit m,n</p>
	 *
	 * @param sql sql���
	 * @param params ��������
	 * @param m	�ӵ�m�����ݿ�ʼ	
	 * @param n �����ʾm������
	 * @return
	 */
	//����������Ƿ�ҳ��ѯ�����û�з�ҳ���ɲ���
	public  List<Map> queryForList(String sql,Map<String, Object> params,int m,int n)  {
		/**
		 * limit��mysql���﷨ select * from table limit m,n
		 */
		List<Map> result = new ArrayList<Map>();
		Connection con = null;
		PreparedStatement pstat = null;//���ݸ�PreparedStatement����Ĳ������Ա�ǿ�ƽ�������ת����ʹ������Ա����ȷ���ڲ�����ѯ����ʱ��ײ�����ݿ��ʽƥ�䡣
		ResultSet rs = null;
		
		try {
			con = getConnection();
			pstat = prepareStatement(con, sql, params);
			
			pstat.setMaxRows(m+n-1);
			rs = pstat.executeQuery();
			
			if (rs != null) {
				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();
				
				
				if (m-1 > 0) {
					rs.absolute(m-1);
				}
				
				while (rs.next()) {
					Map map = new LinkedHashMap();
					for (int i = 0; i < columnCount; i++) {
						String columnName = metaData.getColumnName(i+1);
						map.put(columnName, rs.getObject(i + 1));
					}//һ��ѭ����ÿһ�е�ֵ���뵽Map��
					result.add(map);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (rs != null) {//�������û��ֵ�˾ͽ�������
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}finally{
					rs = null;
				}
			}
			if (pstat != null) {//ͬ����
				try {
					pstat.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}finally{
					pstat = null;
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				} finally {
					con = null;
				}
			}
		}
		return result;
	}

}
