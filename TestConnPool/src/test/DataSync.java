package test;

import org.apache.log4j.Logger;

import observer.ThreadObserver;
import shen.DataBaseSource;
import shen.ShenyingmingSql;
import shen.Xin;
import thread.XGtoOAThread;

public class DataSync {
	public static void main(String[] args) {
		// TODO �Զ����ɵķ������

		Logger logger = Logger.getLogger(DataSync.class.getName());
		logger.info(">>>>>");
		logger.info("������������");
		DataBaseSource shenyingming = new ShenyingmingSql();
		DataBaseSource xin = new Xin();
		new ThreadObserver(new XGtoOAThread(xin,shenyingming,5000L)).start();;
	}

}
