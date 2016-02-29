package com.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * �ĵ���ת������ �����ĵ������ÿ���ļ����д���������ͳ����Ϣд��һ���ļ��Ա��ζ�ȡ
 */
public class FileSetTransformation {

	public void trainformation(String root, String savePath) throws IOException {
		root = pathNormalize(root);
		File saveFile = new File(savePath);
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
				saveFile)));
		
		//��¼ÿ�����ļ�����
		File rootDir = new File(root);
		String fileNames[] = rootDir.list();
		int count = 0;
		for (int i = 0; i < fileNames.length; i++) {
			String classPath = root + fileNames[i] + "/";
			count += new File(classPath).list().length;
		}
		writer.print("total:");
		writer.print(count);
		writer.print(" ");
		
		for (int i = 0; i < fileNames.length; i++) {
			String classPath = root + fileNames[i] + "/";
			writer.print(fileNames[i]);
			writer.print(":");
			writer.print(new File(classPath).list().length);
			writer.print(" ");
		}
		writer.println();

		/*
		 *��ÿ�����µ��ļ���¼��Ƶ 
		 */
		for (int i = 0; i < fileNames.length; i++) {
			String classPath = root + fileNames[i] + "/";

			classTrainformation(fileNames[i], classPath, writer);
		}
		writer.close();
	}

	/**
	 * ��ĳ�����µ�ȫ���ļ�ת��
	 * @param className  ����
	 * @param classPath	 ��·��
	 * @param writer
	 */
	public void classTrainformation(String className, String classPath,
			PrintWriter writer) {
		File classFile = new File(classPath);
		String[] filelist = classFile.list();  
		
		for (int i = 0; i < filelist.length; i++) {
			String filePath = classPath + filelist[i];
			
			String content = DocumentReader.readFile(filePath);
			
			writer.print(className + ":" + filelist[i]);
			writer.print(" ");
			fileTrainFormation(className, content, writer);
		}
	}
	
	/**
	 * �Ե����ļ�����ת��
	 * @param file
	 */
	public void fileTransformation(String file) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(
					"d:/document_test.txt")));
			String content = DocumentReader.readFile(file);
			
			fileTrainFormation(null, content, writer);
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.log("File transformation fininshed");
		
	}

	/**
	 * ��¼�ĵ���Ƶ
	 * @param className
	 * @param content
	 * @param writer
	 */
	public void fileTrainFormation(String className, String content,
			PrintWriter writer) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		content = DocumentPrepare.prepare(content);
		ChineseSplitter splitter = ChineseSplitter.getInstance();
		String[] words = splitter.split(content);
		for (int i = 0; i < words.length; i++) {
			if (!words[i].trim().equals("")) {
				if (map.containsKey(words[i])) {
					int count = map.get(words[i]);
					map.put(words[i], count + 1);
				} else {
					map.put(words[i], 1);
				}
			}
		}
		
		for (String word : map.keySet()) {
			writer.print(word);
			writer.print(":");
			writer.print(map.get(word));
			writer.print(" ");
		}
		writer.println();
	}

	public String pathNormalize(String path) {
		if (!path.endsWith("/"))
			path = path + "/";
		return path;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		FileSetTransformation transformation = new FileSetTransformation();
//		transformation.fileTransformation("d:/C11-Space0028.txt");
		System.out.println("transformating test files......");
		transformation.trainformation("D:\\temp\\fudan_subset_subset_skewed\\test", "D:\\temp\\fudan_subset_subset_skewed\\testSetFiles.txt");
		System.out.println("transformating train files......");
		transformation.trainformation("D:\\temp\\fudan_subset_subset_skewed\\train", "D:\\temp\\fudan_subset_subset_skewed\\trainSetFiles.txt");
	}
}
