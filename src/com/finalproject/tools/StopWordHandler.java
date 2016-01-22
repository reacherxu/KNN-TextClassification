package com.finalproject.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
/**
 * ͣ�ôʹ��˹���
 * @author Administrator
 *
 */
public class StopWordHandler {
	private static StopWordHandler instance;
	private HashSet<String> stopWordSet;

	private StopWordHandler() {
		stopWordSet = new HashSet<String>();
		try {
			Scanner scanner = new Scanner(new File("file/stop_words_zh.txt"));
			while (scanner.hasNextLine()) {
				String tempString = scanner.nextLine().trim();
				if (!tempString.equals("")) {
					stopWordSet.add(tempString);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * �������ýӿ�
	 * @return
	 */
	public static StopWordHandler getInstance(){
		if(instance==null){
			instance=new StopWordHandler();
		}
		return instance;
	}

    /**
     * �ж�һ�����Ƿ�Ϊͣ�ô�
     * @param word
     * @return
     */
	public boolean isStopWord(String word) {
		if (stopWordSet.contains(word))
			return true;
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StopWordHandler stopWordHandler=new StopWordHandler();
		System.out.print(stopWordHandler.isStopWord("ս��"));

	}

}
