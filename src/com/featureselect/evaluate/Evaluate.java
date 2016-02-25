package com.featureselect.evaluate;

import com.classmanage.ClassManager;
import com.featureselect.FeatureSelector;
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
