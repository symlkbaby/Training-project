package test;

import shen_observation.S_observation;
import shen_observation.opreating.Operating;
import shen_observation.opreating.True_observer;

public class test {

	public static void main(String[] args) {
		Operating shen = new Operating();
		shen.addobserver(new True_observer());
		shen.addobserver(new True_observer());
		shen.addobserver(new True_observer());
		shen.noticeobserver("加入一个新观察者");
	}
}
