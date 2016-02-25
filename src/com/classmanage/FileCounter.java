package com.classmanage;

/**
 * �ļ���������ͳ�����弰���������ĵ�����
 * @author Administrator
 * 
 */
public class FileCounter {
	/**
	 * �ĵ����ĸ���
	 * ��fileset�е�map��Ӧ
	 */
	private int[] classFileCount;
	/**
	 * fileset�е��ļ���
	 * ��fileset�е�total��Ӧ
	 */
	private int totalFileCount;

	public FileCounter(int classCount) {
		classFileCount = new int[classCount];
		totalFileCount = 0;
	}

	/**
	 * ���ĳ�������ĵ�����
	 * @param classID
	 * @return
	 */
	public int getClassFileCount(int classID) {
		return classFileCount[classID];
	}

	public void setClassFileCount(int classID, int fileCount) {
		this.classFileCount[classID] = fileCount;
	}

	public int getTotalFileCount() {
		return totalFileCount;
	}

	public void setTotalFileCount(int totalFileCount) {
		this.totalFileCount = totalFileCount;
	}
}
