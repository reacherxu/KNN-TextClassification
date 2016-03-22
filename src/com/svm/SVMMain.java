package com.svm;

import java.io.IOException;

/**
 * ��̨�õ�����ģ�ͣ�ֱ�����м���
 * @author Administrator
 *
 */
public class SVMMain {
	
	public static void predict(int t) throws Exception {
		//scale����
		String[] sarg_train = {"-l","0","-o","D:/lda/train.svm","D:/lda/train_"+t+".lda"};
		String[] sarg_test = {"-l","0","-o","D:/lda/test.svm","D:/lda/test_"+t+".lda"};

		System.out.println("ѵ������ʼ����");
		svm_scale.main(sarg_train);
		System.out.println("���Ž���");

		System.out.println("���Լ���ʼ����");
		svm_scale.main(sarg_test);
		System.out.println("���Ž���");


		//train����
		String[] arg = {"-t","0","D:/lda/train.svm","svm.model"};
		//predict����
		String[] parg = {"D:/lda/test.svm","svm.model","D:/lda/result_"+t+".txt"};

		System.out.println("ѵ����ʼ");
		svm_train.main(arg);
		System.out.println("ѵ������");

		System.out.println("���࿪ʼ");
		svm_predict.main(parg);
		System.out.println("�������");
	}

	public static void main(String[] args) throws IOException{
//		int t = 10;
		
	/*	//scale����
		String[] sarg = {"-l","0","-s","corpus_train/svm.scale","-o","corpus_train/svmscale.train","corpus_train/svm.train"};
		//train����
		String[] arg = {"-t","0","corpus_train/svmscale.train","corpus_train/svm.model"};
		//predict����
		String[] parg = {"corpus_test/svmscale.test","corpus_train/svm.model","corpus_test/result.txt"};

		System.out.println("��ʼ����");
		svm_scale scale = new svm_scale();
		scale.main(sarg);
		System.out.println("���Ž���");

		System.out.println("ѵ����ʼ");
		svm_train.main(arg);
		System.out.println("ѵ������");

		System.out.println("���࿪ʼ");
		svm_predict.main(parg);
		System.out.println("�������");*/



		/*//scale����
		String[] sarg = {"-l","0","-s","trainfile/svm.scale","-o","trainfile/svmscale.train","trainfile/svm.train"};
		//train����
		String[] arg = {"-t","0","-v","5","trainfile/svmscale.train","trainfile/svm.model"};
		//predict����
		String[] parg = {"testfile/svmscale.test","trainfile/svm.model","testfile/result.txt"};

		System.out.println("��ʼ����");
		svm_scale scale = new svm_scale();
		scale.main(sarg);
		System.out.println("���Ž���");

		System.out.println("ѵ����ʼ");
		svm_train.main(arg);
		System.out.println("ѵ������");

		System.out.println("���࿪ʼ");
		svm_predict.main(parg);
		System.out.println("�������");*/

	}
}
