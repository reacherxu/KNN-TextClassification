package com.finalproject.tools;

import java.io.File;
/**
 * �ĵ������⹤��
 * @author Administrator
 *
 */
public class CharsetDetector {

	/**
	 * ����ĵ�����
	 * @param path
	 */
	public static void detect(String path) {
		cpdetector.io.CodepageDetectorProxy detector = cpdetector.io.CodepageDetectorProxy
				.getInstance();

		detector.add(new cpdetector.io.ParsingDetector(false));

		detector.add(cpdetector.io.JChardetFacade.getInstance());
		// ASCIIDetector����ASCII����ⶨ
		detector.add(cpdetector.io.ASCIIDetector.getInstance());
		// UnicodeDetector����Unicode�������Ĳⶨ
		detector.add(cpdetector.io.UnicodeDetector.getInstance());
		java.nio.charset.Charset charset = null;
		File f = new File(path);
		try {
			charset = detector.detectCodepage(f.toURL());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (charset != null) {
			if (!charset.name().equals("GB2312"))
				System.out.println(f.getName() + "�����ǣ�" + charset.name());
			System.out.println(DocumentReader.readFile(path));
			System.out.println();
		} else
			System.out.println(f.getName() + "δ֪");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String path="c:/train/";
		File root=new File(path);
		String []dirnameStrings=root.list();
		for(int i=0;i<dirnameStrings.length;i++){
			String newPathString=path+dirnameStrings[i]+"/";
			File dirFile=new File(newPathString);
			String []fileStrings=dirFile.list();
			for(int j=0;j<fileStrings.length;j++){
				CharsetDetector.detect(newPathString+fileStrings[j]);
			}
		}
	}
}
