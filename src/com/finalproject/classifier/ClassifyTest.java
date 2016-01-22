package com.finalproject.classifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import com.finalproject.classmanage.ClassManager;
import com.finalproject.tools.Log;

/**
 * ʵ�������
 * 
 * @author Administrator
 * 
 */
public class ClassifyTest {

	private String defaultTrainSetPath = "c:/train/";
	private String defaultTestSetPath = "c:/test/";
	private int[] testFileCount;
	private int[][] classifyResult;
	private double[] recalls;
	private double[] precisions;
	private KNNClassifier classifier;
	private ClassManager classManager;

	public ClassifyTest() {
		classifier = new KNNClassifier();
	}

	public ClassifyTest(String function, int lemon) {
		classifier = new KNNClassifier(function, lemon);
	}

	public ClassifyTest(String trainSetPath, String testSetPath,
			String function, int lemon) {
		this.defaultTrainSetPath = pathNormalize(trainSetPath);
		this.defaultTestSetPath = pathNormalize(testSetPath);
		classifier = new KNNClassifier(function, lemon);
	}

	/**
	 * ѵ������ģ��,����������ѡ��
	 * @param trainSet
	 */
	public void prepare(FileSet trainSet) {
		classifier.prepare(trainSet);
		classManager = classifier.getClassManager();
		int count = classManager.getClassCount();
		testFileCount = new int[count];
		classifyResult = new int[count][count];
		recalls = new double[count];
		precisions = new double[count];
	}

	/**
	 * �����ض�����ѡ�񷽷��Է���ģ�ͽ���ѵ��
	 * @param trainSet
	 * @param function
	 * @param lemon
	 */
	public void train(FileSet trainSet, String function, int lemon) {
		classifier.train(trainSet, function, lemon);
		classManager = classifier.getClassManager();
		int count = classManager.getClassCount();
		testFileCount = new int[count];
		classifyResult = new int[count][count];
		recalls = new double[count];
		precisions = new double[count];
	}

	/**
	 * ���Բ��ò�ͬ����ѡ�񷽷��µķ���ģ�͵ķ���Ч��
	 * 
	 * @param testSet
	 */
	public void test(FileSet testSet) {
		Log.log("begine testing!");
		String[] classnameStrings = classManager.getClassNames();
		for (int i = 0; i < classnameStrings.length; i++) {
			int classID = classManager.getClassID(classnameStrings[i]);
			testFileCount[classID] = testSet.getCount(classnameStrings[i]);
		}
		for (int i = 0; i < testSet.size(); i++) {
			Document document = testSet.get(i);
			String className = document.getClassNameString();
			int classID = classManager.getClassID(className);
			doTestStatistic(document, classID);
		}

		for (int i = 0; i < classifyResult.length; i++) {
			for (int j = 0; j < classifyResult.length; j++)
				System.out.print(classifyResult[i][j] + "\t");
			System.out.println();
		}
		doTestCalculate();
		Log.log("testing is done!");
	}

	/**
	 * �������ض�����ѡ�񷽷��µķ���ģ�͵ķ���Ч�� 
	 * @param fileSet
	 * @param function
	 * @param lemon
	 */
	public void test(FileSet fileSet, String function, int lemon) {
		classifier.changeSetting(function, lemon);
		classManager = classifier.getClassManager();
		int count = classManager.getClassCount();
		testFileCount = new int[count];
		classifyResult = new int[count][count];
		recalls = new double[count];
		precisions = new double[count];
		test(fileSet);
	}

	/**
	 * ͳ�Ʒ�����ȷ��,������,��f1��΢f1ֵ
	 */
	private void doTestCalculate() {
		int count = classManager.getClassCount();
		for (int i = 0; i < count; i++) {
			double rightCount = classifyResult[i][i];
			recalls[i] = rightCount / testFileCount[i];
			int currentFileCount = 0;
			for (int j = 0; j < count; j++)
				currentFileCount += classifyResult[j][i];
			precisions[i] = rightCount / currentFileCount;
		}
	}

	/**
	 * �����ĵ��������ͳ��
	 * @param document
	 * @param classID
	 */
	private void doTestStatistic(Document document, int classID) {

		int classifyID = classifier.classifyByID(document);
		classifyResult[classID][classifyID]++;
	}

	/**
	 * ���������ļ�
	 * @param outputPath
	 */
	public void outputResult(String outputPath) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(outputPath)));
			writer.print("subject:\t\t");
			int length = classManager.getClassCount();
			for (int i = 0; i < length; i++) {
				writer.print(classManager.getClassName(i) + "\t\t");
			}
			writer.println();
			writer.println();

			writer.print("recall:\t\t");
			for (int i = 0; i < length; i++) {
				writer.print(getClassRecall(i) + "\t\t");
			}
			writer.println();
			writer.println();

			writer.print("pricise:\t\t");
			for (int i = 0; i < length; i++) {
				writer.print(getClassPrecision(i) + "\t\t");
			}
			writer.println();
			writer.println();

			writer.print("F1:\t\t");
			for (int i = 0; i < length; i++) {
				writer.print(getClassF1(i) + "\t\t");
			}
			writer.println();

			writer.println();
			writer.println();
			writer.println();
			writer.println();

			writer.print("subject:\t\t");
			writer.println("total");
			writer.println();

			writer.print("Mac_r\t\t");
			writer.println(getMacroRecall());
			writer.println();

			writer.print("Mac_p\t\t");
			writer.println(getMacroPrecision());
			writer.println();

			writer.print("Mac_f1\t\t");
			writer.println(getMacroF1());
			writer.println();

			writer.print("Mic_r\t\t");
			writer.println(getMicroRecall());
			writer.println();

			writer.print("Mic_p\t\t");
			writer.println(getMicroPrecision());
			writer.println();

			writer.print("Mic_f1\t\t");
			writer.println(getMicroF1());
			writer.println();

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��������ٻ���
	 * @param classID
	 * @return
	 */
	 
	public double getClassRecall(int classID) {
		return recalls[classID];
	}
	/**
	 * �������׼ȷ��
	 * @param classID
	 * @return
	 */

	public double getClassPrecision(int classID) {
		return precisions[classID];
	}

	/**
	 * �������F1ֵ
	 * @param classID
	 * @return
	 */
	public double getClassF1(int classID) {
		double F1 = (2 * recalls[classID] * precisions[classID])
				/ (recalls[classID] + precisions[classID]);
		return F1;
	}
	/**
	 * ���������ƽ���ٻ���
	 * @return
	 */
	public double getMacroRecall() {
		double recall = 0;
		for (int i = 0; i < recalls.length; i++)
			recall += recalls[i];
		return recall / recalls.length;
	}

	/**
	 * ���������ƽ��׼ȷ��
	 * @return
	 */
	public double getMacroPrecision() {
		double precision = 0;
		for (int i = 0; i < precisions.length; i++)
			precision += precisions[i];
		return precision / precisions.length;
	}

	/**
	 * ���������ƽ��F1ֵ
	 * @return
	 */
	public double getMacroF1() {
		double macroPrecision = getMacroPrecision();
		double macroRecall = getMacroRecall();
		double macroF1 = (2 * macroRecall * macroPrecision)
				/ (macroRecall + macroPrecision);
		return macroF1;
	}
	

	/**
	 * ��������΢ƽ���ٻ���
	 * @return
	 */
	public double getMicroRecall() {
		int totalFileCount = 0;
		double totalRightFileCount = 0;
		for (int i = 0; i < testFileCount.length; i++) {
			totalFileCount += testFileCount[i];
			totalRightFileCount += classifyResult[i][i];
		}
		return totalRightFileCount / totalFileCount;
	}

	/**
	 * ��������΢ƽ��׼ȷ��
	 * @return
	 */
	public double getMicroPrecision() {
		int totalFileCount = 0;
		double totalRightFileCount = 0;
		for (int i = 0; i < testFileCount.length; i++) {
			totalFileCount += testFileCount[i];
			totalRightFileCount += classifyResult[i][i];
		}
		return totalRightFileCount / totalFileCount;
	}

	/**
	 * ��������΢ƽ��F1ֵ
	 * @return
	 */
	public double getMicroF1() {
		double microPrecision = getMicroPrecision();
		double microRecall = getMicroRecall();
		double microF1 = (2 * microRecall * microPrecision)
				/ (microRecall + microPrecision);
		return microF1;
	}

	/**
	 * ��·���������б�׼��
	 * @param path
	 * @return
	 */
	public String pathNormalize(String path) {
		if (!path.endsWith("/"))
			path = path + "/";
		return path;
	}
	
	/**
	 * ����ѵ��,�����ı���·��
	 * @param trainPath
	 * @param testPath
	 */
	public void setPath(String trainPath, String testPath) {
		this.defaultTrainSetPath = trainPath;
		this.defaultTestSetPath = testPath;
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {

		//�����ĵ���
		FileSet trainSet = new FileSet("d:/trainSetFiles.txt");
		FileSet testSet = new FileSet("d:/testSetFiles.txt");
		ClassifyTest test = new ClassifyTest();
		//����׼��
		test.prepare(trainSet);
		//��������б�
		Scanner scanner = new Scanner(new File("file/parameters1.txt"));
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			if (!line.trim().equals("")) {
				String[] parameters = line.split(" ");
				String function = parameters[0];
				int lemon = Integer.parseInt(parameters[1]);
				test.test(testSet, function, lemon);
				String outputPath = "result/" + function + "_" + lemon + ".txt";
				test.outputResult(outputPath);
				System.out.println();
				System.out
						.println("********************************************");
				System.out.println();
			}
		}
	}
}
