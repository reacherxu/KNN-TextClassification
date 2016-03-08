package com.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import ICTCLAS.I3S.AC.CLibrary;

import com.sun.jna.Native;
/**
 * ���ķִʹ��ߣ�ʹ���п�Ժ��ICTCLAS3.0�ִ�������зִ�
 * @author Administrator
 *
 */
public class ChineseSplitter {
	private static ChineseSplitter instance;
	private CLibrary ictclas30;

	private ChineseSplitter() {
		ictclas30 = (CLibrary)Native.loadLibrary(
				System.getProperty("user.dir")+"\\source\\NLPIR", CLibrary.class);
		int init_flag = ictclas30.NLPIR_Init("", 1, "0");
		String resultString = null;
		if (0 == init_flag) {
			resultString = ictclas30.NLPIR_GetLastErrorMsg();
			System.err.println("��ʼ��ʧ�ܣ�\n"+resultString);
			return;
		}

	}

	/**
	 * ���طִʽ��
	 * @param source
	 * @return
	 */
	public String[] split(String source) {
		try {
			String resultString = ictclas30.NLPIR_ParagraphProcess(source, 1);
			//	            System.out.println("�ִʽ��Ϊ��\n " + resultString);

			String[] allWords = resultString.split("\\s");
			return allWords;
		} catch (Exception e) {
			System.out.println("������Ϣ��");
			e.printStackTrace();
		}

		return null;
	}
	
	public String[] split(String source, int pos) {
		try {
			String resultString = ictclas30.NLPIR_ParagraphProcess(source, pos);
			//	            System.out.println("�ִʽ��Ϊ��\n " + resultString);

			String[] allWords = resultString.split("\\s");
			return allWords;
		} catch (Exception e) {
			System.out.println("������Ϣ��");
			e.printStackTrace();
		}

		return null;
	}


	public static ChineseSplitter getInstance() {
		if (instance == null) {
			instance = new ChineseSplitter();
		}
		return instance;
	}

	public void close() {
		ictclas30.NLPIR_Exit();
	}

	
	public String pathNormalize(String path) {
		if (!path.endsWith("/"))
			path = path + "/";
		return path;
	}
	
	public void trainformation(String root, String savePath) throws IOException {
		root = FilePathHandler.pathNormalize(root);
		File saveFile = new File(savePath);
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
				saveFile)));
		
		//��¼ÿ�����ļ�����
		File rootDir = new File(root);
		String fileNames[] = rootDir.list();
		for (int i = 0; i < fileNames.length; i++) {
			String classPath = root + fileNames[i] + "/";

			classTrainformation(fileNames[i], classPath, writer);
		}
		writer.close();
	}
	
	public void classTrainformation(String className, String classPath,
			PrintWriter writer) {
		File classFile = new File(classPath);
		String[] filelist = classFile.list();  
		
		for (int i = 0; i < filelist.length; i++) {
			String filePath = classPath + filelist[i];
			
			String content = DocumentReader.readFile(filePath);
			
			fileTrainFormation(className, content, writer);
		}
	}
	
	public void fileTrainFormation(String className, String content,
			PrintWriter writer) {
		content = DocumentPrepare.prepare(content);
		ChineseSplitter splitter = ChineseSplitter.getInstance();
		String[] words = splitter.split(content, 0);
		
		for (String word :words) {
			writer.print(word);
			writer.print(" ");
		}
		writer.println();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*ChineseSplitter splitter = ChineseSplitter.getInstance();
		String sInput = "��~�Ǹ����Ȧ�ߴ�̫�ǰ�غ��ᣬ�����խ��������ȥ�ܲ������"
				+ "����ʧ�߻������������Ȼ��ֻ���ӣ�����Ҳ�����������������ٸ�֪����˵��Ű������ģ�"
				+ "˵���Ǹ����Ȧ����~ȥ�����ڳ¼Ҵ���ʶ��һ�����������ֹ���������Ǯ�ֹ�����ͯ�����ۣ�"
				+ "�ɴ��ҽ������ٶ���һ���ɣ�";
		String[] words = splitter.split(sInput, 0);
		for (int i = 0; i < words.length; i++)
			System.out.print(words[i] + " ");
		splitter.close();*/
		
		ChineseSplitter splitter = ChineseSplitter.getInstance();
		try {
			Log.log("w2v file transformation started.....");
			splitter.trainformation("D:\\temp\\fudan_subset_subset\\train", "D:\\temp\\fudan_subset_subset\\file.w2v");
			Log.log("w2v file transformation ended.....");
		} catch (IOException e) {
			e.printStackTrace();
		}
		splitter.close();
	}
}
