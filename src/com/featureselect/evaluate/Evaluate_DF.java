package com.featureselect.evaluate;

import com.classmanage.ClassManager;
import com.featureselect.FeatureSelector;
/**
 * ����ѡ�񷽷�DF��ʵ����
 * @author Administrator
 *
 */
public class Evaluate_DF implements Evaluate{

	@Override
	public double mark(String feature,ClassManager classManager, FeatureSelector featureItemManager) {
		return featureItemManager.getTotalFileCountOfFeature(feature);
	}
}
