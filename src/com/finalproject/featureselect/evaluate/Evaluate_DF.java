package com.finalproject.featureselect.evaluate;

import com.finalproject.classmanage.ClassManager;
import com.finalproject.featureselect.FeatureSelector;
/**
 * ����ѡ�񷽷�DF��ʵ����
 * @author Administrator
 *
 */
public class Evaluate_DF implements Evaluate{

	@Override
	public double mark(String feature,ClassManager classManager, FeatureSelector featureItemManager) {
		// TODO Auto-generated method stub
		return featureItemManager.getTotalFileCountOfFeature(feature);
	}
}
