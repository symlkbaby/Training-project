package observer;

import java.util.Observable;
import java.util.Observer;
/**
 * �۲��ߵ�ʵ��
 * �����߳�
 * һ���߳̽�����������һ���߳�
 * @author yao
 *
 */

import org.apache.log4j.Logger;

import thread.AbstractThread;

public class ThreadObserver implements Observer {
	Logger logger = Logger.getLogger(ThreadObserver.class.getName());
	AbstractThread thread;
	public ThreadObserver(AbstractThread thread) {
		this.thread=thread;
		this.thread.addObserver(this);
		
	}
	
	@Override
	public void update(Observable o, Object arg) {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		String name = thread.getClass().getName();
		logger.info(">>>>>>>>>>"+name+"-����������>>>>>>>>>>");
		//����һ���۲���
		thread.addObserver(this);
		//��һ���߳̽���
		new Thread(thread).start();
		
		
	}
	public void start(){
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		String name = thread.getClass().getName();
		logger.info(">>>>>>>>>>"+name+"-����>>>>>>>>>>");
		new Thread(thread).start();
	}

}
