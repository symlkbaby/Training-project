package thread;

import java.util.Observable;

import org.apache.log4j.Logger;
import shen.DataBaseSource;;

//线程的基类，这是关于线程的数据库
public  abstract class AbstractThread  extends Observable implements Runnable{
	protected static Logger logger = Logger.getLogger(DataBaseSource.class.getName());
	protected DataBaseSource oaDataSource;
	protected DataBaseSource yxDataSource;
	protected Long sleep;
	
    public AbstractThread(DataBaseSource ShenyingmingSql,DataBaseSource Xin,Long sleep) {
		this.oaDataSource = ShenyingmingSql;
		this.yxDataSource = Xin;
		this.sleep = sleep;
	}
}
