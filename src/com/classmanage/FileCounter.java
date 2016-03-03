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
	 * ��Ӧ�����ĵ� �������ڴ���
	 */
//	private List<ArrayList<Document>> classFiles;
	
	/**
	 * fileset�е��ļ���
	 * ��fileset�е�total��Ӧ
	 */
	private int totalFileCount;

	public FileCounter(int classCount) {
		classFileCount = new int[classCount];
		totalFileCount = 0;
//		initClassFiles(classCount);
	}

	/*private void initClassFiles(int classCount) {
		classFiles = new ArrayList<ArrayList<Document>>();
		for (int i = 0; i < classCount; i++) {
			classFiles.add(new ArrayList<Document>());
		}
	}

	public void setClassFile(Document document, int classID) {
		classFiles.get(classID).add(document);
	}
	
	public List<Document> getClassFile(int classID) {
		return classFiles.get(classID);
	}*/
	
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
