package com.finalproject.classifier;

import java.util.HashMap;
/**
 * ��ʾԭʼ�ĵ������ݽṹ���������ĵ��г��ֵ�ÿ���ʻ㼰����ָ���
 * @author Administrator
 *
 */
public class Document extends HashMap<String, Integer>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String classNameString;
	public Document(String fileContent){
		String[] wordStrings=fileContent.split(" ");
		classNameString=wordStrings[0];
		for(int i=1;i<wordStrings.length;i++){
			String []tempStrings=wordStrings[i].split(":");
			put(tempStrings[0], Integer.parseInt(tempStrings[1]));
		}
	}
	public String getClassNameString() {
		return classNameString;
	}
}
