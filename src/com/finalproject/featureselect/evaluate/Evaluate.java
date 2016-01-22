package com.finalproject.featureselect.evaluate;

import com.finalproject.classmanage.ClassManager;
import com.finalproject.featureselect.FeatureSelector;
/**
 * ����ѡ�񷽷��ӿ�
 * @author Administrator
 *
 */
public interface Evaluate {
	/**
	 * mark the feature
	 * @param feature
	 * @param featureItemManager
	 * @return mark of the giving feature
	 */
	public double mark(String feature,ClassManager classManager,FeatureSelector featureItemManager);
}
