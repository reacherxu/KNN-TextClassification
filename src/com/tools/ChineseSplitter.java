package com.tools;

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
			String resultString = ictclas30.NLPIR_ParagraphProcess(source, 0);
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ChineseSplitter splitter = ChineseSplitter.getInstance();
		String sInput = "��~�Ǹ����Ȧ�ߴ�̫�ǰ�غ��ᣬ�����խ��������ȥ�ܲ������"
				+ "����ʧ�߻������������Ȼ��ֻ���ӣ�����Ҳ�����������������ٸ�֪����˵��Ű������ģ�"
				+ "˵���Ǹ����Ȧ����~ȥ�����ڳ¼Ҵ���ʶ��һ�����������ֹ���������Ǯ�ֹ�����ͯ�����ۣ�"
				+ "�ɴ��ҽ������ٶ���һ���ɣ�";
		String[] words = splitter.split(sInput);
		for (int i = 0; i < words.length; i++)
			System.out.print(words[i] + " ");
		splitter.close();
	}
}
