package shen_observation.opreating;

import java.util.Vector;

import shen_observation.Operating_observer;
import shen_observation.S_observation;

public class Operating implements Operating_observer {

	Vector<S_observation> vector = new Vector<>();
	@Override
	public void addobserver(S_observation s) {
		// TODO �Զ����ɵķ������
		vector.add(s);
	}

	@Override
	public void deleteobserver(S_observation s) {
		// TODO �Զ����ɵķ������
		vector.remove(s);
	}

	@Override
	public void noticeobserver(String shen) {
		// TODO �Զ����ɵķ������
		for(S_observation s:vector) {
			s.update(shen);
		}
	}

}
