package com.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import com.classmanage.ClassManager;
import com.hankcs.lda.TestCorpus;
import com.svm.SVMMain;
import com.svm.svm_predict;
import com.svm.svm_scale;
import com.svm.svm_train;
import com.tools.FormatDecimal;
import com.tools.Log;

/**
 * ʵ�������
 * 
 * @author Administrator
 * 
 */
public class ClassifyTest {

	@SuppressWarnings("unused")
	private String defaultTrainSetPath = "d:/train/";
	@SuppressWarnings("unused")
	private String defaultTestSetPath = "d:/test/";
	private int[] testFileCount;
	private int[][] classifyResult;
	private double[] recalls;
	private double[] precisions;
	private KNNClassifier classifier;
	private ClassManager classManager;

	public ClassifyTest() {
		classifier = new KNNClassifier();
	}

	public ClassifyTest(String function, int dimension) {
		classifier = new KNNClassifier(function, dimension);
	}

	public ClassifyTest(String trainSetPath, String testSetPath,
			String function, int dimension) {
		this.defaultTrainSetPath = pathNormalize(trainSetPath);
		this.defaultTestSetPath = pathNormalize(testSetPath);
		classifier = new KNNClassifier(function, dimension);
	}

	
	public void svm_train_dateset(String function, int dimension,String path) {
		classifier.changeSetting(function, dimension);
		TrainingSetForKNN set = classifier.getCore().getTrainingSetForKNN();
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
			for (int i = 0; i < set.size(); i++) {
				writer.write(set.get(i).toString());
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.log("svm_train_dateset generated....");
		
	}
	
	public void svm_test_dateset(FileSet testSet, String function, int dimension,String path) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
			for (int i = 0; i < testSet.size(); i++) {
				Document document = testSet.get(i);
				DocumentVector vector = classifier.getCore().getDocumentVector(document);

				writer.write(vector.toString());
				writer.newLine();
			}

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.log("svm_test_dateset generated....");
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
	 * @param dimension
	 */
	public void train(FileSet trainSet, String function, int dimension) {
		classifier.train(trainSet, function, dimension);
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
		Log.log("begin testing!");
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
	 * @param dimension
	 * @param K
	 */
	public void test(FileSet fileSet, String function, int dimension, int K) {
		classifier.changeSetting(function, dimension, K);
		classManager = classifier.getClassManager();
		int count = classManager.getClassCount();
		testFileCount = new int[count];
		classifyResult = new int[count][count];
		recalls = new double[count];
		precisions = new double[count];
		test(fileSet);
	}
	
	/**
	 * �������ض�����ѡ�񷽷��µķ���ģ�͵ķ���Ч��
	 * @param fileSet
	 * @param function
	 * @param dimension
	 */
	public void test(FileSet fileSet, String function, int dimension) {
		classifier.changeSetting(function, dimension);
		classManager = classifier.getClassManager();
		int count = classManager.getClassCount();
		testFileCount = new int[count];
		classifyResult = new int[count][count];
		recalls = new double[count];
		precisions = new double[count];
		test(fileSet);
	}
	
	
	public void test(FileSet testSet, String result_path) {
		Log.log("begin testing!");
		int count = classManager.getClassCount();
		testFileCount = new int[count];
		classifyResult = new int[count][count];
		recalls = new double[count];
		precisions = new double[count];
		
		String[] classnameStrings = classManager.getClassNames();
		
		for (int i = 0; i < classnameStrings.length; i++) {
			int classID = classManager.getClassID(classnameStrings[i]);
			testFileCount[classID] = testSet.getCount(classnameStrings[i]);
		}
		
		ArrayList<Integer> classifyID = readResult(result_path);
		for (int i = 0; i < testSet.size(); i++) {
			Document document = testSet.get(i);
			String className = document.getClassNameString();
			int classID = classManager.getClassID(className);
			classifyResult[classID][classifyID.get(i)]++;
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
	 * ֱ�Ӹ���svm�Ľ�������ж�
	 * @param test_path
	 * @param result_path
	 */
	public void test(FileSet testSet, String test_path, String result_path) {
		Log.log("begin testing!");
		int count = classManager.getClassCount();
		testFileCount = new int[count];
		classifyResult = new int[count][count];
		recalls = new double[count];
		precisions = new double[count];
		
		String[] classnameStrings = classManager.getClassNames();

		for (int i = 0; i < classnameStrings.length; i++) {
			int classID = classManager.getClassID(classnameStrings[i]);
			testFileCount[classID] = testSet.getCount(classnameStrings[i]);
		}
		
		ArrayList<Integer> origin = readResult(test_path);
		ArrayList<Integer> classifyID = readResult(result_path);
		
		for (int i = 0; i < origin.size(); i++) {
			int classID = origin.get(i);
			classifyResult[classID][classifyID.get(i)]++;
		}

		for (int i = 0; i < classifyResult.length; i++) {
			for (int j = 0; j < classifyResult.length; j++)
				System.out.print(classifyResult[i][j] + "\t");
			System.out.println();
		}
		doTestCalculate();
		Log.log("testing is done!");
	}

	private ArrayList<Integer> readResult(String path) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			while(reader.ready()) {
				String line = reader.readLine();
				String label = line.split("\\s",2)[0];
				result.add((int)Double.parseDouble(label));
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
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
				writer.print(FormatDecimal.format(getClassRecall(i)) + "\t\t");
			}
			writer.println();
			writer.println();

			writer.print("pricise:\t\t");
			for (int i = 0; i < length; i++) {
				writer.print(FormatDecimal.format(getClassPrecision(i)) + "\t\t");
			}
			writer.println();
			writer.println();

			writer.print("F1:\t\t");
			for (int i = 0; i < length; i++) {
				writer.print(FormatDecimal.format(getClassF1(i)) + "\t\t");
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
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		/*FileSet trainSet = new FileSet("D:\\temp\\fudan_subset_subset\\trainSetFiles.txt");
		ClassifyTest test = new ClassifyTest();
		//����׼��
		String function = FeatureSelector.X2;
		int dimension = 1000;
		test.train(trainSet,function,dimension);
		
		FileSetTransformation transformation = new FileSetTransformation();
		transformation.fileTransformation("d:/C32-Agriculture0629.txt");
		Document tmpDoc = new Document();
		test.docPrediction(tmpDoc);
		test.similarDocs(tmpDoc);
		*/
		/*//�����ĵ���
		FileSet trainSet = new FileSet("D:\\temp\\fudan_subset_subset\\trainSetFiles.txt");
		FileSet testSet = new FileSet("D:\\temp\\fudan_subset_subset\\testSetFiles.txt");
		ClassifyTest test = new ClassifyTest();
		//����׼��
		test.prepare(trainSet);
		
		//��������б�
		Scanner scanner = new Scanner(new File("file/parameters_test"));
		while (scanner.hasNext()) {
			long start = System.currentTimeMillis();
			
			String line = scanner.nextLine();
			if (!line.trim().equals("")) {
				String[] parameters = line.split(" ");
				String function = parameters[0];
				int dimension = Integer.parseInt(parameters[1]);

				String result_path = "D:/fudan/result.txt";
				test.svm_predict(function, dimension, testSet, result_path);
				test.test(testSet, result_path);

//				test.test(testSet, function, dimension);
				String outputPath = "result/lda/" + function + "_" + dimension + ".txt";
				test.outputResult(outputPath);
				
				long end = System.currentTimeMillis();
				Log.log(function + "_" + dimension + " take " + (end-start) + "����");
				System.out.println();
				System.out.println("********************************************");
				System.out.println();
				
			}
			
			
		}
		scanner.close();*/
		
		int t[] = {20,30,40,60,70,80,90};
		
		for (int i = 0; i < t.length; i++) {
			//�����ĵ���
			FileSet testSet = new FileSet("D:\\data\\fudan_subset_subset\\testSetFiles.txt");
			ClassifyTest test = new ClassifyTest();
			test.prepare(testSet);
			
			//���svm�ļ�
			TestCorpus.generateSVMModel(t[i], test.classManager);
			//libsvm����Ԥ��
			SVMMain.predict(t[i]);
					
			String test_path = "d:/lda/test_"+t[i]+".lda";
			String result_path = "D:/lda/result_"+t[i]+".txt";
			String outputPath = "result/lda/lda_theta_" + t[i] + ".txt";
			
			test.test(testSet, test_path,result_path);
			test.outputResult(outputPath);
		}
		
		
	}

	/**
	 * Ѱ�������Ƶ��ĵ�
	 * @param tmpDoc
	 */
	@SuppressWarnings("unused")
	private void similarDocs(Document tmpDoc) {
		classifier.maxSimilarity(tmpDoc);
	}

	/**
	 * Ϊ��ƪ�ĵ�Ԥ��
	 * @param tmpDoc
	 */
	@SuppressWarnings("unused")
	private void docPrediction(Document tmpDoc) {
		int classifyID = classifier.classifyByID(tmpDoc);
		System.out.println("Ԥ��Ϊ��" + classManager.getClassName(classifyID));
		
	}

	private void svm_predict(String function, int dimension, FileSet testSet,
			String result_path) throws IOException {
		String train_path = "D:/fudan/train.txt";
		svm_train_dateset(function, dimension,train_path);
		String test_path = "D:/fudan/test.txt";
		svm_test_dateset(testSet, function, dimension, test_path);
		
		//scale����
		String train_scale_path = "D:/fudan/fudan_svm_scale.train";
		String test_scale_path = "D:/fudan/fudan_svm_scale.test";
		String[] sarg_train = {"-l","0","-o", train_scale_path,train_path};
		String[] sarg_test = {"-l","0","-o", test_scale_path,test_path};

		Log.log("ѵ������ʼ����");
		svm_scale.main(sarg_train);
		Log.log("ѵ�������Ž���");

		Log.log("���Լ���ʼ����");
		svm_scale.main(sarg_test);
		Log.log("���Լ����Ž���");


		//train����
		String[] arg = {"-t","0",train_scale_path,"svm.model"};
		//predict����
		String[] parg = {test_scale_path,"svm.model", result_path};

		Log.log("ѵ����ʼ");
		svm_train.main(arg);
		Log.log("ѵ������");

		Log.log("���࿪ʼ");
		svm_predict.main(parg);
		Log.log("�������");
		
		
		
	}

}
