package pool;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.log4j.Logger;
import org.junit.Test;


public class TestPool {
	static Logger logger = Logger.getLogger(TestPool.class.getName());
	@Test
	public void testConn() {

		// DBUtil.getConn();
		// ��ȡ�����ļ�
		// ����properties�ļ�
	    
		Properties pro = new Properties();
		try {
			// ��ȡsrc/xg_dbcp.properties�ļ�
			pro.load(TestPool.class.getClassLoader().getResourceAsStream("xg_dbcp.properties"));
			// ��ȡ����Դprotected static Logger logger = Logger.getLogger(DataBaseSource.class.getName());
			DataSource ds = BasicDataSourceFactory.createDataSource(pro);
			// ������Ӷ���
			Connection connection = ds.getConnection();
			//System.out.println("yyh:" + connection);
			logger.info("yyh:"+connection);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
    @Test
	public void testQuery() throws SQLException{
    	List<Map<String,Object>>list=DBUtil.getAll();
    	for(int i=0;i<list.size();i++){
    		Map<String,Object>map=list.get(i);
    		String id=(String)map.get("id");
    		String xh=(String)map.get("xh");
    		String xm=(String)map.get("xm");
    		logger.info("yyh:"+id+","+xh+","+xm);
    	}
	}
}
