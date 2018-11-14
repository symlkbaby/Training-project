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
 * 封装数据库操作
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DataBaseSource {
	protected static Logger logger = Logger.getLogger(DataBaseSource.class.getName());//导入日志文件
	
	private  DataSource ds = null;//设置数据源为空
	
	public DataBaseSource(String properties) {
		try {
			Properties prop = new Properties();
			// 通过类路径来加载属性文件
			prop.load(DataBaseSource.class.getClassLoader().getResourceAsStream(properties));
			//读取配置文件的数据库连接信息，并连接ds数据源
			ds = BasicDataSourceFactory.createDataSource(prop);
			/*properties文件与Properties类的关系
                通过properties文件可以填充Properties类。
                也可以通过xml文件来填充Properties类。
                     可以通过绝对路径方式加载Properties文件信息，也可以使用相对路径加载。*/
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * <p>获取数据连接</p>
	 *
	 * @return 数据库连接对象
	 * @throws SQLException
	 */
	private   Connection getConnection() 
			throws SQLException{
		return ds.getConnection();
	}
	//这就获取连接对象了
	
	/**
	 * <p>获取PreparedStatement</p>
	 *
	 * @param con
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	private  PreparedStatement prepareStatement(Connection con, String sql,
			Map<String, Object> params) throws Exception {//Map里面String为键，object为值，相当于数据库里面属性列和属性值
		PreparedStatement pstat = null;//PreparedStatement是预编译的,对于批量处理可以大大提高效率. 也叫JDBC存储过程
		if(params != null && !params.isEmpty()){
			List paramList = new ArrayList();//参数列表集合

			String para = ":[[A-Za-z_0-9]]+";//正则表达式，有点懵逼
			Pattern p = Pattern.compile(para);
			Matcher m = p.matcher(sql);
			List<String> keyList = new ArrayList<String>();//<String>是泛型的意思，<String>这里的意思是这个集合里只能放string类型的
			while (m.find()) {
				int index = m.start();
				int end = m.end();
				String key = sql.substring(index+1, end);
				keyList.add(key);
			} 
			
			String[] split = sql.split(para);
			int length = split.length;
			boolean split_last_part_is_space = "".equals(split[length-1].trim());
			//sql语句分隔之后的最后部分是否为空白符
			
			int keyListSize = keyList.size();
			StringBuffer sqlbak = new StringBuffer();//另一种字符串定义
			for (int i = 0; i < length; i++) {
				sqlbak.append(split[i]);
				
				if((split_last_part_is_space && i == length-1) || i==keyListSize){
					//当最后部分为空白符时，最后部分就不存在参数占位符
					continue;
				}
				
				String key = keyList.get(i);
				if(params.containsKey(key)){
					Object object = params.get(key);
					if(object instanceof Collection){//集合参数
						Collection c = (Collection)object;//集合c =（集合）对象
						Iterator iterator = c.iterator();
						StringBuffer part = new StringBuffer();
						if(iterator.hasNext()){
							Object obj = iterator.next();
							paramList.add(obj);
							
							part.append(",?");
						}
						sqlbak.append(part.substring(1));
					}else if(object instanceof Object[]){//数组参数
						Object[] objs = (Object[])object;
						StringBuffer part = new StringBuffer();
						for(Object obj:objs){
							paramList.add(obj);
							
							part.append(",?");
						}
						sqlbak.append(part.substring(1));
					}else{//单个参赛
						paramList.add(object);
						sqlbak.append("?");
					}
				}else{
					logger.error("参数params未找到匹配 :user_id 的值");
				}
			}
			
			logger.info(sqlbak);
			
			if(!paramList.isEmpty()){
				pstat = con.prepareStatement(sqlbak.toString(),ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				for (int i = 0; i < paramList.size(); i++) {
					Object value = paramList.get(i);
					if(value instanceof java.util.Date){//日期格式转换
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
	 * <p>LIST查询</p>
	 *
	 * @param sql	SQL
	 * @param params 参数
	 * @return
	 */
	public   List<Map> queryForList(String sql,Map<String, Object> params)  {
		/*老师起的注释：
		 * 该方法封装所有查询数据库表中的数据的操作，因为方法是通用
		 * ，要针对所有的表，所以不能按常规的写法，出现字段名和字段值，
		 * 该方法中把字段值的通过参数传递进入，字段名的通过方法动态获取的*/
		List<Map> result = new ArrayList<Map>();
		Connection con = null;
		PreparedStatement pstat = null;
		ResultSet rs = null;
		//类似于初始化的操作

		try {
			 con = getConnection();
			pstat = prepareStatement(con, sql, params);
			rs = pstat.executeQuery();//获取数据库中的值

			if (rs != null) {
				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();//获取列数量
				while (rs.next()) {//可以循环知道获取表中所有值
					Map map = new LinkedHashMap();
					for (int i = 0; i < columnCount; i++) {
						String columnName = metaData.getColumnName(i+1);//根据索引得到列的名字
						map.put(columnName, rs.getObject(i + 1));
					}
					result.add(map);//将信息插入到集合中
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
	 * <p>数据更新</p>
	 *
	 * @param sql
	 * @param params
	 * @return		SQL执行影响的数据量
	 */
	public  int executeUpdate(String sql,Map<String, Object> params)  {
		int result = 0;
		Connection con = null;
		PreparedStatement pstat = null;

		try {
			 con = getConnection();//获得列
			pstat = prepareStatement(con, sql, params);//Connection ,String,Map<String,Object>
			result = pstat.executeUpdate();//真正执行SQL语句的处理代码

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
	 * <p>分页查询</p>
	 * <p>类似mysql的limit语法： select * from table limit m,n</p>
	 *
	 * @param sql sql语句
	 * @param params 参数集合
	 * @param m	从第m条数据开始	
	 * @param n 最多显示m条数据
	 * @return
	 */
	//这个方法就是分页查询，如果没有分页即可不用
	public  List<Map> queryForList(String sql,Map<String, Object> params,int m,int n)  {
		/**
		 * limit是mysql的语法 select * from table limit m,n
		 */
		List<Map> result = new ArrayList<Map>();
		Connection con = null;
		PreparedStatement pstat = null;//传递给PreparedStatement对象的参数可以被强制进行类型转换，使开发人员可以确保在插入或查询数据时与底层的数据库格式匹配。
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
					}//一个循环将每一列的值加入到Map中
					result.add(map);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (rs != null) {//如果表中没有值了就结束它！
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}finally{
					rs = null;
				}
			}
			if (pstat != null) {//同上理
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
