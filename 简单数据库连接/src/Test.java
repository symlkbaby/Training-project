import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;

public class Test {
	String dbDriver = "com.mysql.jdbc.Driver";
	String dbUrl = "jdbc:mysql://localhost:3306/xin?useSSL=false";
	String dbUser = "root";
	String dbPwd = "123456";
	
	public Test(){
		try {
			Class.forName(dbDriver);
			Connection con = (Connection) DriverManager.getConnection(dbUrl,dbUser,dbPwd);
			String sql = "select * from data_pjsq where id='p001'";
			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet resultSet = ps.executeQuery();
			while(resultSet.next()) {
				for(int i = 1; i < 6; i++) {
					System.out.println(resultSet.getString(i));
				}
				
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		new Test();
	}

}
