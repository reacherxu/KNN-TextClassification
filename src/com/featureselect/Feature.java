package com.featureselect;

import com.classmanage.ClassManager;

/**
 * ���ݽṹ ����ͳ����Ϣ������
 * @author Administrator
 * 
 */
public class Feature {
	String name;
	int totalFileCount;
	int classFileCountS[];
	int classWordFreq[];

	public Feature(String name, ClassManager classManager) {
		this.name = name;
		classFileCountS = new int[classManager.getClassCount()];
		classWordFreq = new int[classManager.getClassCount()];
	}

	public void increaseFileCount(String feature, int classID) {
		totalFileCount++;
		classFileCountS[classID]++;
	}

	public void increaseWordFreq( int freq, int classID) {
		classWordFreq[classID] += freq;
	}
	
	public int getClassCount() {
		int count = 0;
		for (int i = 0; i < classFileCountS.length; i++) {
			if(classFileCountS[i] != 0)
				count ++;
		}
		return count ;
	}
	
	public String getName() {
		return name;
	}

	public int getTotalFileCount() {
		return totalFileCount;
	}

	public int getClassFileCount(int classID) {
		return classFileCountS[classID];
	}

	public int getClassWordFreq(int classID) {
		return classWordFreq[classID];
	}
	
}
