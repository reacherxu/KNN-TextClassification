package com.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

/**
 * �ĵ���ʵ���࣬Ϊ�˶�ε��ö���פ�ڴ�����ݽṹ
 * 
 * @author Administrator
 * 
 */
public class FileSet extends ArrayList<Document> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * �ļ�����Լ���Ӧ���ļ���
	 */
	private HashMap<String, Integer> map;
	/**
	 * fileset�е��ĵ���
	 */
	private int total;  

	/**
	 * ����map��ȡ����
	 * @return
	 */
	public String[] getClassNameStrings() {
		String[] classNameStrings;
		int count = map.size();
		classNameStrings = new String[count];
		Set<String> set = map.keySet();
		set.toArray(classNameStrings);
		return classNameStrings;
	}

	public int getCount(String className) {
		if(map.containsKey(className))
			return map.get(className);
		else
			return 0;
	}

	/**
	 * ���fileset�����е��ļ���
	 * @return
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * �����ĸ���
	 * @return
	 */
	public int getClassCount() {
		return map.size();
	}
	
	public FileSet(String filePath) {
		map = new HashMap<String, Integer>();
		try {
			Scanner scanner = new Scanner(new File(filePath));
			
			/* ��һ�м�¼ ����Լ���Ӧ���ļ���*/
			String line = scanner.nextLine();
			String[] temps = line.split(" ");
			String[] temps2 = temps[0].split(":");
			total = Integer.parseInt(temps2[1]);
			for (int i = 1; i < temps.length; i++) {
				temps2 = temps[i].split(":");
				map.put(temps2[0], Integer.parseInt(temps2[1]));
			}

			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				Document doc = new Document(line);
				add(doc);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


}
