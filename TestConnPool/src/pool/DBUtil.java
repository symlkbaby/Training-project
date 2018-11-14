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
		 //声明数据源实例D
		BasicDataSource ds=new BasicDataSource();
		//设置mysql驱动
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		//建立连接
		ds.setUrl("jdbc:mysql://localhost:3306/shenyingming");
		//设置用户名
		ds.setUsername("root");
		//设置密码
		ds.setPassword("123456");
		//初始化建立5个连接
		ds.setInitialSize(5);
		//设置最大连接数--应对高峰
		ds.setMaxActive(20);
		//设置最小连接数--低潮，避免让费
		ds.setMinIdle(2);
		try {
			conn=ds.getConnection();
			System.out.println("获得连接对象："+conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	/*
	 * 作用：查询数据库的data_xsxx表的数据
	 */
	public static List<Map<String,Object>> getAll() throws SQLException{
		Connection connection=getConn();
		String select_sql="select * from data_xsxx";
		//发送语句块
		PreparedStatement ps=connection.prepareStatement(select_sql);
		//得到结果集
		ResultSet rs=ps.executeQuery();
		//当移动到最后一条数据的next，返回为false
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
