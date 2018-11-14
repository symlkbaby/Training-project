package shen_observation;

public interface Operating_observer {

	//add观察者
	public void addobserver(S_observation s);
	//delete
	public void deleteobserver(S_observation s);
	//通知观察者
	public void noticeobserver(String s);
}
