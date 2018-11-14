package pool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;

public class DBUtil {

	public static Connection getConn(){
		Connection conn=null;
		 //��������Դʵ��D
		BasicDataSource ds=new BasicDataSource();
		//����mysql����
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		//��������
		ds.setUrl("jdbc:mysql://localhost:3306/shenyingming");
		//�����û���
		ds.setUsername("root");
		//��������
		ds.setPassword("123456");
		//��ʼ������5������
		ds.setInitialSize(5);
		//�������������--Ӧ�Ը߷�
		ds.setMaxActive(20);
		//������С������--�ͳ��������÷�
		ds.setMinIdle(2);
		try {
			conn=ds.getConnection();
			System.out.println("������Ӷ���"+conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	/*
	 * ���ã���ѯ���ݿ��data_xsxx�������
	 */
	public static List<Map<String,Object>> getAll() throws SQLException{
		Connection connection=getConn();
		String select_sql="select * from data_xsxx";
		//��������
		PreparedStatement ps=connection.prepareStatement(select_sql);
		//�õ������
		ResultSet rs=ps.executeQuery();
		//���ƶ������һ�����ݵ�next������Ϊfalse
		List<Map<String,Object>>list=new ArrayList<>();
		while(rs.next()){
			Map<String, Object>map=new HashMap<>();
			String id=rs.getString("id");
			String xh=rs.getString("xh");
			String xm=rs.getString("xm");
			map.put("id", id);
			map.put("xh", xh);
			map.put("xm", xm);
			list.add(map);
		}
			
		return list;
	}
	
}
