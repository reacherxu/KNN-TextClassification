package com.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
/**
 * �ĵ���ȡ����
 * @author Administrator
 *
 */
public class DocumentReader {
	/**
	 * ��ȡ�ĵ�,һ���ĵ���һ��
	 * @param path
	 * @return ��ʾ�ĵ����ַ���
	 */
	public static String readFile(String path) {
		Scanner scanner;
		String content = new String();
		try {
			scanner = new Scanner(new File(path));

			while (scanner.hasNextLine())
				content += scanner.nextLine();
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found ["+path+"]");
		}
		
		return content;
	}

	public static void main(String[] args) {
		System.out.println(DocumentReader.readFile("file/stop_words_zh.txt"));
	}
}
