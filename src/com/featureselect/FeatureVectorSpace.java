package com.featureselect;

import java.util.Vector;

/**
 * �����ռ�ģ�ͣ���������������ѵ�����г��ֵ��ĵ�����
 * 
 * @author Administrator
 * 
 */
public class FeatureVectorSpace {
	// �����ַ���
	private Vector<String> featureVector;
	//TODO �����������ĵ���
	private int[] fileCountOfFeature;

	public FeatureVectorSpace(Vector<String> stringVector,
			FeatureManager featureManager) {
		this.featureVector = stringVector;
		fileCountOfFeature = new int[featureVector.size()];
		for (int i = 0; i < featureVector.size(); i++) {
			fileCountOfFeature[i] = featureManager
					.getTotalFileCountOfFeature(featureVector.get(i));
		}
	}

	public String getFeatureName(int featureID) {
		return featureVector.get(featureID);
	}

	public int getFeatureID(String featureName) {
		return featureVector.indexOf(featureName);
	}

	public int getFeatureCount() {
		return featureVector.size();
	}

	public int getFileCount(String feature) {
		return fileCountOfFeature[featureVector.indexOf(feature)];
	}

	public boolean isFeature(String featureName) {
		if (featureVector.contains(featureName))
			return true;
		else
			return false;
	}
}
