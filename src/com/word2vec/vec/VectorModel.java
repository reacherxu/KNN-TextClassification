package com.word2vec.vec;

import java.io.*;
import java.util.*;

public class VectorModel {

    private Map<String, float[]> wordMap = new HashMap<String, float[]>();
    private int vectorSize = 200; //������

    private int topNSize = 40;

    public Map<String, float[]> getWordMap() {
        return wordMap;
    }

    public void setWordMap(Map<String, float[]> wordMap){
        this.wordMap = wordMap;
    }

    /**
     * ��ȡ�����ƴʵ�����
     * @return �����ƴʵ�����
     */
    public int getTopNSize() {
        return topNSize;
    }

    /**
     * ���������ƴʵ�����
     * @param topNSize ����
     */
    public void setTopNSize(int topNSize) {
        this.topNSize = topNSize;
    }

    public int getVectorSize() {
        return vectorSize;
    }

    public void setVectorSize(int vectorSize) {
        this.vectorSize = vectorSize;
    }

    /**
     * ˽�й��캯��
     * @param wordMap ��������ϣ��
     * @param vectorSize ����������
     */
    public VectorModel(Map<String, float[]> wordMap, int vectorSize){

        if (wordMap == null || wordMap.isEmpty()){
            throw new IllegalArgumentException("word2vec�Ĵ�����Ϊ�գ�����ѵ��ģ�͡�");
        }
        if (vectorSize <= 0){
            throw new IllegalArgumentException("���������ȣ�layerSize��Ӧ����0");
        }

        this.wordMap = wordMap;
        this.vectorSize = vectorSize;
    }


    /**
     * ʹ��Word2Vec�����ģ�ͼ��ش�����ģ��
     * @param path ģ���ļ�·��
     * @return ������ģ��
     */
    public static VectorModel loadFromFile(String path){

        if (path == null || path.isEmpty()){
            throw new IllegalArgumentException("ģ��·������Ϊnull��ա�");
        }

        DataInputStream dis = null;
        int wordCount, layerSizeLoaded = 0;
        Map<String, float[]> wordMapLoaded = new HashMap<String, float[]>();
        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
            wordCount = dis.readInt();
            layerSizeLoaded = dis.readInt();
            float vector;

            String key;
            float[] value;
            for (int i = 0; i < wordCount; i++) {
                key = dis.readUTF();
                value = new float[layerSizeLoaded];
                double len = 0;
                for (int j = 0; j < layerSizeLoaded; j++) {
                    vector = dis.readFloat();
                    len += vector * vector;
                    value[j] = vector;
                }

                len = Math.sqrt(len);

                for (int j = 0; j < layerSizeLoaded; j++) {
                    value[j] /= len;
                }
                wordMapLoaded.put(key, value);
            }

        } catch (IOException ioe){
            ioe.printStackTrace();
        }finally {
            try {
                if (dis != null){
                    dis.close();
                }
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
        }

        return new VectorModel(wordMapLoaded, layerSizeLoaded);

    }

    /**
     * ���������ģ��
     * @param file ģ�ʹ��·��
     */
    public void saveModel(File file) {

        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(file)));
            dataOutputStream.writeInt(wordMap.size());
            dataOutputStream.writeInt(vectorSize);
            for (Map.Entry<String, float[]> element : wordMap.entrySet()) {
                dataOutputStream.writeUTF(element.getKey());
                for (float d : element.getValue()) {
                    dataOutputStream.writeFloat(d);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dataOutputStream != null){
                    dataOutputStream.close();
                }
            }catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * ��ȡ���word�����topNSize����
     * @param queryWord ��
     * @return ����ʼ�����ģ�Ͳ�������word���򷵻ؿռ�
     */
    public Set<WordScore> similar(String queryWord){

        float[] center = wordMap.get(queryWord);
        if (center == null){
            return Collections.emptySet();
        }

        int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize + 1;
        TreeSet<WordScore> result = new TreeSet<WordScore>();
        for (int i = 0; i < resultSize; i++){
            result.add(new WordScore("^_^", -Float.MAX_VALUE));
        }
        float minDist = -Float.MAX_VALUE;
        for (Map.Entry<String, float[]> entry : wordMap.entrySet()){
            float[] vector = entry.getValue();
            float dist = 0;
            for (int i = 0; i < vector.length; i++){
                dist += center[i] * vector[i];
            }
            if (dist > minDist){
                result.add(new WordScore(entry.getKey(), dist));
                minDist = result.pollLast().score;
            }
        }
        result.pollFirst();

        return result;
    }

    public Set<WordScore> similar(float[] center){
        if (center == null || center.length != vectorSize){
            return Collections.emptySet();
        }

        int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
        TreeSet<WordScore> result = new TreeSet<WordScore>();
        for (int i = 0; i < resultSize; i++){
            result.add(new WordScore("^_^", -Float.MAX_VALUE));
        }
        float minDist = -Float.MAX_VALUE;
        for (Map.Entry<String, float[]> entry : wordMap.entrySet()){
            float[] vector = entry.getValue();
            float dist = 0;
            for (int i = 0; i < vector.length; i++){
                dist += center[i] * vector[i];
            }
            if (dist > minDist){
                result.add(new WordScore(entry.getKey(), dist));
                minDist = result.pollLast().score;
            }
        }
//        result.pollFirst();

        return result;
    }

    /**
     * ��Ǩ�ƣ���word1 - word0 + word2 �Ľ����������������һ������ģ���У�
     * Ҳ����û�д��������򷵻ؿռ�
     * @param word0 ��
     * @param word1 ��
     * @param word2 ��
     * @return �����������ǰtopNSize����
     */
    public TreeSet<WordScore> analogy(String word0, String word1, String word2) {
        float[] wv0 = wordMap.get(word0);
        float[] wv1 = wordMap.get(word1);
        float[] wv2 = wordMap.get(word2);

        if (wv1 == null || wv2 == null || wv0 == null) {
            return null;
        }
        float[] center = new float[vectorSize];
        for (int i = 0; i < vectorSize; i++) {
            center[i] = wv1[i] - wv0[i] + wv2[i];
        }

        int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
        TreeSet<WordScore> result = new TreeSet<WordScore>();
        for (int i = 0; i < resultSize; i++){
            result.add(new WordScore("^_^", -Float.MAX_VALUE));
        }
        String name;
        float minDist = -Float.MAX_VALUE;
        for (Map.Entry<String, float[]> entry : wordMap.entrySet()){
            name = entry.getKey();
            if (name.equals(word1) || name.equals((word2))){
                continue;
            }
            float[] vector = entry.getValue();
            float dist = 0;
            for (int i = 0; i < vector.length; i++){
                dist += center[i] * vector[i];
            }
            if (dist > minDist){
                result.add(new WordScore(entry.getKey(), dist));
                minDist = result.pollLast().score;
            }
        }
        return result;
    }

	public float[] getWordVector(String word) {
        return wordMap.get(word);
    }

	public float dist(String word1, String word2) {
		float[] vec1 = getWordVector(word1);
		float[] vec2 = getWordVector(word2);
		float result = 0;
		for (int i = 0; i < vec1.length; i++){
			result += vec1[i] * vec2[i];
		}
		return (float) Math.sqrt(result);
	}

    public class WordScore implements Comparable<WordScore> {

        public String name;
        public float score;

        public WordScore(String name, float score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public String toString() {
            return this.name + "\t" + score;
        }

        @Override
        public int compareTo(WordScore o) {
            if (this.score < o.score) {
                return 1;
            } else {
                return -1;
            }
        }
    }

}
