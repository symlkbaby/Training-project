package observer;

import java.util.Observable;
import java.util.Observer;
/**
 * 观察者的实现
 * 监听线程
 * 一个线程结束，启动另一个线程
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
		logger.info(">>>>>>>>>>"+name+"-重新已启动>>>>>>>>>>");
		//增加一个观察者
		thread.addObserver(this);
		//另一个线程结束
		new Thread(thread).start();
		
		
	}
	public void start(){
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		String name = thread.getClass().getName();
		logger.info(">>>>>>>>>>"+name+"-启动>>>>>>>>>>");
		new Thread(thread).start();
	}

}
