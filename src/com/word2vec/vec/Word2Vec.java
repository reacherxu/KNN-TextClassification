package com.word2vec.vec;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.word2vec.util.Counter;
import com.word2vec.util.HuffmanNode;
import com.word2vec.util.HuffmanTree;
import com.word2vec.util.LineIterator;
import com.word2vec.util.Tokenizer;

public class Word2Vec {

    private Logger logger = Logger.getLogger("Word2Vec");

    private int windowSize; //���ִ��ڴ�С
    private int vectorSize; //��������Ԫ�ظ���

    public static enum Method{
        CBow, Skip_Gram
    }

    private Method trainMethod; // ������ѧϰ����

    private double sample;
//    private int negativeSample;
    private double alpha;       // ѧϰ�ʣ�����ʱ���̸߳���
    double alphaThresold;
    private double initialAlpha;  // ��ʼѧϰ��
    private int freqThresold = 5;
    private final byte[] alphaLock = new byte[0];  // alphaͬ����
    final byte[] treeLock = new byte[0];  // alphaͬ����
    final byte[] vecLock = new byte[0];  // alphaͬ����

    private double[] expTable;
    private static final int EXP_TABLE_SIZE = 1000;
    private static final int MAX_EXP = 6;

    private Map<String, WordNeuron> neuronMap;
//    private List<Word> words;
    private int totalWordCount;     // �����е��ܴ���
    private int currentWordCount;   // ��ǰ���ĵĴ���������ʱ���̸߳���
    private int numOfThread;        // �̸߳���

    // ���ʻ���������
    private Counter<String> wordCounter = new Counter<String>();

    private File tempCorpus = null;
    private BufferedWriter tempCorpusWriter;

    public static class Factory {

        private int vectorSize = 50;
        private int windowSize = 5;
        private int freqThresold = 5;

        private Method trainMethod = Method.Skip_Gram;

        private double sample = 1e-3;
//        private int negativeSample = 0;

        private double alpha = 0.025, alphaThreshold = 0.0001;
        private int numOfThread = 1;


        public Factory setVectorSize(int size){
            vectorSize = size;
            return this;
        }

        public Factory setWindow(int size){
            windowSize = size;
            return this;
        }

        public Factory setFreqThresold(int thresold){
            freqThresold = thresold;
            return this;
        }

        public Factory setMethod(Method method){
            trainMethod = method;
            return this;
        }

        public Factory setSample(double rate){
            sample = rate;
            return this;
        }

//        public Factory setNegativeSample(int sample){
//            negativeSample = sample;
//            return this;
//        }

        public Factory setAlpha(double alpha){
            this.alpha = alpha;
            return this;
        }

        public Factory setAlphaThresold(double alpha){
            this.alphaThreshold = alpha;
            return this;
        }

        public Factory setNumOfThread(int numOfThread) {
            this.numOfThread = numOfThread;
            return this;
        }

        public Word2Vec build(){
            return new Word2Vec(this);
        }

    }

    private Word2Vec(Factory factory){
        vectorSize = factory.vectorSize;
        windowSize = factory.windowSize;
        freqThresold = factory.freqThresold;
        trainMethod = factory.trainMethod;
        sample = factory.sample;
//        negativeSample = factory.negativeSample;
        alpha = factory.alpha;
        initialAlpha = alpha;
        alphaThresold = factory.alphaThreshold;
        numOfThread = factory.numOfThread;

        totalWordCount = 0;
        expTable = new double[EXP_TABLE_SIZE];
        computeExp();
    }

    /**
     * Ԥ�ȼ��㲢����sigmoid����������ӿ���������ٶ�
     * f(x) = x / (x + 1)
     */
    private void computeExp() {
        for (int i = 0; i < EXP_TABLE_SIZE; i++) {
            expTable[i] = Math.exp(((i / (double) EXP_TABLE_SIZE * 2 - 1) * MAX_EXP));
            expTable[i] = expTable[i] / (expTable[i] + 1);
        }
    }

    /**
     * ��ȡһ���ı���ͳ�ƴ�Ƶ�����ڴ�����ֵ�Ƶ�ʣ�
     * �ı��������һ����ʱ�ļ��У��Է���֮���ѵ��
     * @param tokenizer ���
     */
    public void readTokens(Tokenizer tokenizer){

        if (tokenizer == null || tokenizer.size() < 1){
            return;
        }
        currentWordCount += tokenizer.size();
        // ��ȡ�ı��еĴʣ���������Ƶ
        while (tokenizer.hasMoreTokens()){
            wordCounter.add(tokenizer.nextToken());
        }
        // ���ı��������ʱ�ļ��У�������ѵ��֮��
        try {
            if (tempCorpus == null){
                File tempDir = new File("temp");
                if (!tempDir.exists() && !tempDir.isDirectory()){
                    boolean tempCreated = tempDir.mkdir();
                    if (!tempCreated){
                        logger.severe("unable to create temp file in " + tempDir.getAbsolutePath());
//                        System.out.println("��ʱ�ļ��д���ʧ�ܣ�λ��" + tempDir.getAbsolutePath());
                    }
                }
                tempCorpus = File.createTempFile("tempCorpus", ".txt", tempDir);
//                tempCorpus = File.createTempFile("tempCorpus", ".txt");
                if (tempCorpus.exists()){
                    logger.info("create temp file successfully in" + tempCorpus.getAbsolutePath());
//                    System.out.println("��ʱ�ļ������ɹ���λ��" + tempCorpus.getAbsolutePath());
                }
                tempCorpusWriter = new BufferedWriter(new FileWriter(tempCorpus));
            }
            tempCorpusWriter.write(tokenizer.toString(" "));
            tempCorpusWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                tempCorpusWriter.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void buildVocabulary() {

        neuronMap = new HashMap<String, WordNeuron>();
        for (String wordText : wordCounter.keySet()){
            int freq = wordCounter.get(wordText);
            if (freq < freqThresold){
                continue;
            }
            neuronMap.put(wordText,
                    new WordNeuron(wordText, wordCounter.get(wordText), vectorSize));
        }
        logger.info("read " + neuronMap.size() + " word totally.");
//        System.out.println("����ȡ�� " + neuronMap.size() + " ���ʡ�");

    }

    @SuppressWarnings("rawtypes")
	public void training(){

        if (tempCorpus == null){
            throw new NullPointerException("ѵ������Ϊ�գ����֮ǰ������training()��" +
                    "�����readLine(String sentence)������������");
        }

        buildVocabulary();
        HuffmanTree.make(neuronMap.values());
        // ���±�������
        totalWordCount = currentWordCount;
        currentWordCount = 0;
        // �����̳߳ض���
        ExecutorService threadPool = Executors.newFixedThreadPool(numOfThread);

        LineIterator li = null;
        try {
            BlockingQueue<LinkedList<String>> corpusQueue = new ArrayBlockingQueue<LinkedList<String>>(numOfThread);
            LinkedList<Future> futures = new LinkedList<Future>(); //ÿ���̵߳ķ��ؽ�������ڵȴ��߳�

            for (int thi = 0; thi < numOfThread; thi++){
//                threadPool.execute(new Trainer(corpusQueue));
                futures.add(threadPool.submit(new Trainer(corpusQueue)));
            }

            tempCorpusWriter.close();
            li = new LineIterator(new FileReader(tempCorpus));
            LinkedList<String> corpus = new LinkedList<String>();   //�����ı���ɵ�����

            int trainBlockSize = 500;  //�����о��Ӹ���
            while (li.hasNext()){
                corpus.add(li.nextLine());
                if (corpus.size() == trainBlockSize){
                    //�Ž�������У����̴߳���
//                    futures.add(threadPool.submit(new Trainer(corpus)));

                    corpusQueue.put(corpus);
//                    System.out.println("put a corpus");

                    corpus = new LinkedList<String>();
                }
            }
//            futures.add(threadPool.submit(new Trainer(corpus)));
            corpusQueue.put(corpus);
            logger.info("the task queue has been allocated completely, " +
                    "please wait the thread pool (" + numOfThread + ") to process...");

            // �ȴ��̴߳���������
            for (Future future : futures){
                future.get();
            }
            threadPool.shutdown();  // �ر��̳߳�
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            LineIterator.closeQuietly(li);
            if (!tempCorpus.delete()){
                logger.severe("unable to delete temp file in "+tempCorpus.getAbsolutePath());
//                System.err.println("��ʱ�ļ�δ����ȷɾ����λ��"+tempCorpus.getAbsolutePath());
            }
            tempCorpus = null;
        }
    }

    private void skipGram(int index, List<WordNeuron> sentence, int b, double alpha) {

        WordNeuron word = sentence.get(index);
        int a, c = 0;
        for (a = b; a < windowSize * 2 + 1 - b; a++) {
            if (a == windowSize) {
                continue;
            }
            c = index - windowSize + a;
            if (c < 0 || c >= sentence.size()) {
                continue;
            }

            double[] neu1e = new double[vectorSize];//�����
            //Hierarchical Softmax
            List<HuffmanNode> pathNeurons = word.getPathNeurons();
            WordNeuron we = sentence.get(c);
            for (int neuronIndex = 0; neuronIndex < pathNeurons.size() - 1; neuronIndex++){
                HuffmanNeuron out = (HuffmanNeuron) pathNeurons.get(neuronIndex);
                double f = 0;
                // Propagate hidden -> output
                for (int j = 0; j < vectorSize; j++) {
                    f += we.vector[j] * out.vector[j];
                }
                if (f <= -MAX_EXP || f >= MAX_EXP) {
//                    System.out.println("F: " + f);
                    continue;
                } else {
                    f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
                    f = expTable[(int) f];
                }
                // 'g' is the gradient multiplied by the learning rate
                HuffmanNeuron outNext = (HuffmanNeuron) pathNeurons.get(neuronIndex+1);
                double g = (1 - outNext.code - f) * alpha;
                for (c = 0; c < vectorSize; c++) {
                    neu1e[c] += g * out.vector[c];
                }
                // Learn weights hidden -> output
                for (c = 0; c < vectorSize; c++) {
                    out.vector[c] += g * we.vector[c];
                }
            }
            // Learn weights input -> hidden
            for (int j = 0; j < vectorSize; j++) {
                we.vector[j] += neu1e[j];
            }
        }

//        if (word.getName().equals("��")){
//            for (Double value : word.vector){
//                System.out.print(value + "\t");
//            }
//            System.out.println();
//        }
    }

    private void cbowGram(int index, List<WordNeuron> sentence, int b, double alpha) {

        WordNeuron word = sentence.get(index);
        int a, c = 0;

        double[] neu1e = new double[vectorSize];//�����
        double[] neu1 = new double[vectorSize];//�����
        WordNeuron last_word;

        for (a = b; a < windowSize * 2 + 1 - b; a++)
            if (a != windowSize) {
                c = index - windowSize + a;
                if (c < 0)
                    continue;
                if (c >= sentence.size())
                    continue;
                last_word = sentence.get(c);
                if (last_word == null)
                    continue;
                for (c = 0; c < vectorSize; c++)
                    neu1[c] += last_word.vector[c];
            }
        //Hierarchical Softmax
        List<HuffmanNode> pathNeurons = word.getPathNeurons();
        for (int neuronIndex = 0; neuronIndex < pathNeurons.size() - 1; neuronIndex++){
            HuffmanNeuron out = (HuffmanNeuron) pathNeurons.get(neuronIndex);

            double f = 0;
            // Propagate hidden -> output
            for (c = 0; c < vectorSize; c++)
                f += neu1[c] * out.vector[c];
            if (f <= -MAX_EXP)
                continue;
            else if (f >= MAX_EXP)
                continue;
            else
                f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
            // 'g' is the gradient multiplied by the learning rate
            HuffmanNeuron outNext = (HuffmanNeuron) pathNeurons.get(neuronIndex+1);
            double g = (1 - outNext.code - f) * alpha;
            //
            for (c = 0; c < vectorSize; c++) {
                neu1e[c] += g * out.vector[c];
            }
            // Learn weights hidden -> output
            for (c = 0; c < vectorSize; c++) {
                out.vector[c] += g * neu1[c];
            }
        }
        for (a = b; a < windowSize * 2 + 1 - b; a++) {
            if (a != windowSize) {
                c = index - windowSize + a;
                if (c < 0)
                    continue;
                if (c >= sentence.size())
                    continue;
                last_word = sentence.get(c);
                if (last_word == null)
                    continue;
                for (c = 0; c < vectorSize; c++)
                    last_word.vector[c] += neu1e[c];
            }

        }
    }

    private long nextRandom = 5;

    public class Trainer implements Runnable{

        private BlockingQueue<LinkedList<String>> corpusQueue;

        private LinkedList<String> corpusToBeTrained;
        int trainingWordCount;
        double tempAlpha;

        public Trainer(LinkedList<String> corpus){
            corpusToBeTrained = corpus;
            trainingWordCount = 0;
        }

        public Trainer(BlockingQueue<LinkedList<String>> corpusQueue){
            this.corpusQueue = corpusQueue;
        }

        private void computeAlpha(){
            synchronized (alphaLock){
                currentWordCount += trainingWordCount;
                alpha = initialAlpha * (1 - currentWordCount / (double) (totalWordCount + 1));
                if (alpha < initialAlpha * 0.0001) {
                    alpha = initialAlpha * 0.0001;
                }
//                logger.info("alpha:" + tempAlpha + "\tProgress: "
//                        + (int) (currentWordCount / (double) (totalWordCount + 1) * 100) + "%");
                System.out.println("alpha:" + tempAlpha + "\tProgress: "
                        + (int) (currentWordCount / (double) (totalWordCount + 1) * 100)
                        + "%\t");
            }
        }

        private void training(){
//            long nextRandom = 5;
            for( String line : corpusToBeTrained){
                List<WordNeuron> sentence = new ArrayList<WordNeuron>();
                Tokenizer tokenizer = new Tokenizer(line, " ");
                trainingWordCount += tokenizer.size();
                while (tokenizer.hasMoreTokens()){
                    String token = tokenizer.nextToken();
                    WordNeuron entry = neuronMap.get(token);
                    if (entry == null) {
                        continue;
                    }
                    // The subsampling randomly discards frequent words while keeping the ranking same
                    if (sample > 0) {
                        double ran = (Math.sqrt(entry.getFrequency() / (sample * totalWordCount)) + 1)
                                * (sample * totalWordCount) / entry.getFrequency();
                        nextRandom = nextRandom * 25214903917L + 11;
                        if (ran < (nextRandom & 0xFFFF) / (double) 65536) {
                            continue;
                        }
                        sentence.add(entry);
                    }
                }
                for (int index = 0; index < sentence.size(); index++) {
                    nextRandom = nextRandom * 25214903917L + 11;
                    switch (trainMethod){
                        case CBow:
                            cbowGram(index, sentence, (int) nextRandom % windowSize, tempAlpha);
                            break;
                        case Skip_Gram:
                            skipGram(index, sentence, (int) nextRandom % windowSize, tempAlpha);
                            break;
                    }
                }

            }


        }

        @Override
        public void run() {
            boolean hasCorpusToBeTrained = true;

            try {
                while (hasCorpusToBeTrained){
//                    System.out.println("get a corpus");
                    corpusToBeTrained = corpusQueue.poll(2, TimeUnit.SECONDS);
//                    System.out.println("���г���:" + corpusQueue.size());
                    if (null != corpusToBeTrained) {
                        tempAlpha = alpha;
                        trainingWordCount = 0;
                        training();
                        computeAlpha(); //����alpha
                    } else {
                        // ����2s��û������ݣ���Ϊ���߳��Ѿ�ֹͣͶ�����ϣ�����ֹͣѵ����
                        hasCorpusToBeTrained = false;
                    }
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        }
    }

    /**
     * ����ѵ���õ���ģ��
     * @param file ģ�ʹ��·��
     */
    public void saveModel(File file) {

        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(file)));
            dataOutputStream.writeInt(neuronMap.size());
            dataOutputStream.writeInt(vectorSize);
            for (Map.Entry<String, WordNeuron> element : neuronMap.entrySet()) {
                dataOutputStream.writeUTF(element.getKey());
                for (double d : element.getValue().vector) {
                    dataOutputStream.writeFloat(((Double) d).floatValue());
                }
            }
            logger.info("saving model successfully in " + file.getAbsolutePath());
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

    public VectorModel outputVector(){

        Map<String, float[]> wordMapConverted = new HashMap<String, float[]>();
        String wordKey;
        float[] vector;
        double vectorLength;
        double[] vectorNorm;

        for (Map.Entry<String, WordNeuron> element : neuronMap.entrySet()) {

            wordKey = element.getKey();

            vectorNorm = element.getValue().vector;
            vector = new float[vectorSize];
            vectorLength = 0;

            for (int vi = 0; vi < vectorNorm.length ; vi++){
                vectorLength += (float) vectorNorm[vi] * vectorNorm[vi];
                vector[vi] = (float) vectorNorm[vi];
            }

            vectorLength = Math.sqrt(vectorLength);

            for (int vi = 0; vi < vector.length; vi++) {
                vector[vi] /= vectorLength;
            }
            wordMapConverted.put(wordKey, vector);
        }

        return new VectorModel(wordMapConverted, vectorSize);
    }

}
