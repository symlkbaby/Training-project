package shen_observation;

public interface Operating_observer {

	//add�۲���
	public void addobserver(S_observation s);
	//delete
	public void deleteobserver(S_observation s);
	//֪ͨ�۲���
	public void noticeobserver(String s);
}
