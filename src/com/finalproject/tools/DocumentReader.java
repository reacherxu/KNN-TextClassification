package com.finalproject.tools;

import java.io.*;
import java.util.Scanner;
/**
 * �ĵ���ȡ����
 * @author Administrator
 *
 */
public class DocumentReader {
	/**
	 * ��ȡ�ĵ�
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
		System.out.println(DocumentReader.readFile("c:/test.epf"));
	}
}
