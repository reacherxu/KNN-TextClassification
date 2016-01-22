package com.finalproject.featureselect;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.finalproject.tools.*;
import com.finalproject.classifier.Document;
import com.finalproject.classifier.FileSet;
import com.finalproject.classmanage.ClassManager;
import com.finalproject.featureselect.evaluate.Evaluate;
import com.finalproject.featureselect.evaluate.EvaluateFactory;
/**
 * ����ѡ������ʹ������ѡ�񷽷����������д�ֲ����ݸ�������ֵ��ȡ����
 * @author Administrator
 *
 */
public class FeatureSelector {

	public static FeatureSelector instance;
	public static final String DF = "DF";
	public static final String IG = "IG";
	public static final String MI = "MI";
	public static final String CE = "CE";
	public static final String X2 = "X2";
	public static final String IMPROVE = "IMPROVE";
	private FeatureManager featureManager;
	private StopWordHandler stopWordHandler;
	private ClassManager classManager;
	private ChineseSplitter splitter;
	private int count = 0;

	private FeatureSelector(String path, ClassManager classManager) {
		// TODO Auto-generated constructor stub
		this.classManager = classManager;
		stopWordHandler = StopWordHandler.getInstance();
		splitter = ChineseSplitter.getInstance();
		featureManager = new FeatureManager(classManager);

		Log.log("preparing for feature-selecting......");

		doStatistic(path);
	}

	public FeatureSelector(FileSet trainSet, ClassManager classManager) {
		this.classManager = classManager;
		stopWordHandler = StopWordHandler.getInstance();
		splitter = ChineseSplitter.getInstance();
		featureManager = new FeatureManager(classManager);

		Log.log("preparing for feature-selecting......");

		doStatistic(trainSet);
	}
 
	/**
	 * ͳ��ѵ�������ı��������
	 * @param trainSet
	 */
	private void doStatistic(FileSet trainSet) {
		for (int i = 0; i < trainSet.size(); i++) {
			Document document = trainSet.get(i);
			Set<String> tempSet = document.keySet();
			Iterator<String> iterator = tempSet.iterator();
			int classID = classManager
					.getClassID(document.getClassNameString());
			while (iterator.hasNext()) {
				String feature = iterator.next();
				if (!stopWordHandler.isStopWord(feature)) {
					if (!hasFeature(feature))
						addFeature(feature);
					increaseFeatureCount(feature, classID);
				}
			}
		}
	}

	private void doStatistic(String rootPath) {
		String[] classNames = classManager.getClassNames();
		for (int i = 0; i < classNames.length; i++) {
			String newPath = rootPath + classNames[i] + "/";
			int currentClassID = classManager.getClassID(classNames[i]);
			doClassStatistic(newPath, currentClassID);
			System.gc();
		}
	}

	private void doClassStatistic(String path, int classID) {
		File classRoot = new File(path);
		String[] fileNameStrings = classRoot.list();
		for (int i = 0; i < fileNameStrings.length; i++) {
			String newPath = path + fileNameStrings[i] + "/";
			// System.out.println(++count+"\t\t"+fileNameStrings[i]);
			doFileStatistic(newPath, classID);
		}
	}

	private void doFileStatistic(String path, int classID) {
		String content = DocumentReader.readFile(path);
		content = DocumentPrepare.prepare(content);
		String[] word = splitter.split(content);
		Set<String> tempSet = new HashSet<String>();
		for (int i = 0; i < word.length; i++) {
			if (!stopWordHandler.isStopWord(word[i])
					&& !word[i].trim().equals(""))
				tempSet.add(word[i]);
		}
		Iterator<String> iterator = tempSet.iterator();
		while (iterator.hasNext()) {
			String feature = iterator.next();
			if (!hasFeature(feature))
				addFeature(feature);
			increaseFeatureCount(feature, classID);
		}
	}

	/**
	 * ����ѡ�����
	 * @param function
	 * @param lemon
	 * @return
	 */
	public FeatureManager doFeatureSelect(String function, int lemon) {
		Log.log("selecting features with function at " + function
				+ " and lemon at " + lemon + "......");
		MarkedFeature[] markedFeatures = new MarkedFeature[getFeatureCount()];
		String[] featureStrings = getFeature();
		EvaluateFactory evaluateFactory = EvaluateFactory.getInstance();
		Evaluate evaluate = evaluateFactory.getEvaluate(function);
		for (int i = 0; i < featureStrings.length; i++) {
			MarkedFeature markedFeature = new MarkedFeature(featureStrings[i]);
			double mark = evaluate.mark(featureStrings[i], classManager, this);
			markedFeature.setMark(mark);
			markedFeatures[i] = markedFeature;
		}
		// System.out.println(getFeatureCount());
		count = 0;
		sort(markedFeatures);
		FeatureManager result = select(markedFeatures, lemon);
		return result;
	}

	/**
	 * ������
	 * @param features
	 */
	private void sort(MarkedFeature[] features) {
		quickSort(features, 0, features.length - 1);
	}

	/**
	 * ���������㷨
	 * @param features
	 * @param low
	 * @param high
	 */
	private void quickSort(MarkedFeature[] features, int low, int high) {
		if (low < high) {
			int pivotloc = quickSort_Patition(features, low, high);
			// System.out.println(++count);
			quickSort(features, low, pivotloc - 1);
			quickSort(features, pivotloc + 1, high);
		}
	}

	private int quickSort_Patition(MarkedFeature[] features, int low, int high) {
		MarkedFeature tempMarkedFeature = features[low];
		double piovotkey = features[low].getMark();
		while (low < high) {
			while (low < high && features[high].getMark() <= piovotkey)
				--high;
			features[low] = features[high];
			while (low < high && features[low].getMark() >= piovotkey)
				++low;
			features[high] = features[low];
		}
		features[low] = tempMarkedFeature;
		return low;
	}
    /**
     * ���ø�������ֵ�����������н�������ѡȡ
     * @param features
     * @param limen
     * @return
     */
	private FeatureManager select(MarkedFeature[] features, int limen) {
		if (limen < features.length) {
			FeatureManager tempFeatureManager = new FeatureManager(classManager);
			for (int i = 0; i < limen; i++) {
				Feature tempFeature = featureManager.getFeature(features[i]
						.getName());
				tempFeatureManager.addFeature(tempFeature);
			}
			return tempFeatureManager;
		} else {
			return featureManager;
		}
	}

	private void addFeature(String feature) {
		featureManager.addFeature(feature);
	}

	// private void removeFeature(String feature){
	// featureManager.removeFeature(feature);
	// }

	private void increaseFeatureCount(String feature, int classID) {
		featureManager.increaseFeatureCount(feature, classID);
	}

	private boolean hasFeature(String feature) {
		return featureManager.hasFeature(feature);
	}

	public int getFeatureCount() {
		return featureManager.getFeatureCount();
	}

	public String[] getFeature() {
		return featureManager.getFeatures();
	}

	public int getTotalFileCountOfFeature(String feature) {
		return featureManager.getTotalFileCountOfFeature(feature);
	}

	public int getClassFileCountOfFeature(int classID, String feature) {
		return featureManager.getClassFileCountOfFeature(classID, feature);
	}

	public static FeatureSelector getInstance(String path,
			ClassManager classManager) {
		if (instance == null) {
			instance = new FeatureSelector(path, classManager);
		}
		return instance;
	}

	public static FeatureSelector getInstance(FileSet trainSet,
			ClassManager classManager) {
		if (instance == null) {
			instance = new FeatureSelector(trainSet, classManager);
		}
		return instance;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClassManager classManager = new ClassManager("c:/train/");
		FeatureSelector featureItemManager = FeatureSelector.getInstance(
				"c:/train/", classManager);
		FeatureManager featureManager = featureItemManager.doFeatureSelect(
				FeatureSelector.IG, 500);
		String[] featureStrings = featureManager.getFeatures();
		for (int i = 0; i < featureStrings.length; i++)
			System.out.println(featureStrings[i]
					+ "\t"
					+ featureItemManager
							.getTotalFileCountOfFeature(featureStrings[i]));
		System.out.println("done");
	}

}
