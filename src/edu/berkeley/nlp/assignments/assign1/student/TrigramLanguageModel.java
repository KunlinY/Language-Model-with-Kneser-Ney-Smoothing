package edu.berkeley.nlp.assignments.assign1.student;

import edu.berkeley.nlp.langmodel.EmpiricalUnigramLanguageModel;
import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class TrigramLanguageModel implements NgramLanguageModel
{
    static final String STOP = NgramLanguageModel.STOP;
    static final int order = 3;
    static final int InitSize = 1 << 20;

    long total = 0;

    long[] wordCounter = new long[10];

    Vocabulary vocab = new Vocabulary();
    ArrayList<NGramVector> vectors = new ArrayList<>(order + 1);

    ArrayList<ArrayList<Integer>> backoffVectors = new ArrayList<>(order + 1);
    ArrayList<ArrayList<Integer>> countVectors = new ArrayList<>(order + 1);

    ArrayList<ArrayList<Integer>> probVectors = new ArrayList<>(order + 1);
    ArrayList<ArrayList<Integer>> bowVectors = new ArrayList<>(order + 1);

    ArrayList<ArrayList<ArrayList<Double>>> featureList = new ArrayList<>(order + 1);

    public TrigramLanguageModel(Iterable<List<String>> sentenceCollection) {
        System.out.println("Building TrigramLanguageModel . . .");

        ArrayList<Integer> cntVec = new ArrayList<>(1);
        cntVec.add(0);
        countVectors.add(cntVec);
        for (int i = 1; i <= order; i++) {
            cntVec = new ArrayList<>(InitSize);
            for (int j = 0; j < InitSize; j++) {
                cntVec.add(0);
            }
            countVectors.add(cntVec);
        }

        ArrayList<Integer> hists = new ArrayList<>(order + 1);
        for (int i = 0; i <= order; i++) {
            hists.add(-1);
            vectors.add(new NGramVector());
        }
        vectors.get(0).Add(0, 0);

        int sent = 0;
        long startTime = System.nanoTime();
        for (List<String> sentence : sentenceCollection) {
            sent++;
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);

            ArrayList<Integer> words = new ArrayList<>(sentence.size() +2);
            words.add(vocab.Add(NgramLanguageModel.START));
            for (String word: sentence) {
                words.add(vocab.Add(word));
            }
            words.add(vocab.Add(NgramLanguageModel.STOP));

            hists.set(1, vectors.get(1).Add(0, vocab.Find(NgramLanguageModel.START)));
            for (int i = 1; i < words.size(); i++) {
                int word = words.get(i);
                int hist = 0;

                for (int j = 1; j < Math.min(i + 2, order); j++) {
                    if (word != Vocabulary.Invalid && hist != NGramVector.Invalid) {
                        int index = vectors.get(j).Add(hist, word);

                        if (index >= countVectors.get(j).size()) {
                            int newCapacity = countVectors.get(j).size() * 2;
                            countVectors.get(j).ensureCapacity(newCapacity);

                            for (int k = countVectors.get(j).size(); k < newCapacity; k++) {
                                countVectors.get(j).add(0);
                            }
                        }

                        countVectors.get(j).set(index, countVectors.get(j).get(index) + 1);
                        hist = hists.get(j);
                        hists.set(j, index);
                    } else {
                        hist = hists.get(j);
                        hists.set(j, NGramVector.Invalid);
                    }
                }
            }

//            List<String> stoppedSentence = new ArrayList<String>(sentence);
//            stoppedSentence.add(0, NgramLanguageModel.START);
//            stoppedSentence.add(STOP);
//            for (String word : stoppedSentence) {
//                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
//                if (index >= wordCounter.length) wordCounter = CollectionUtils.copyOf(wordCounter, wordCounter.length * 2);
//                wordCounter[index]++;
//            }
        }

        System.out.println("Finish count");
        System.out.println("Current Memory Usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

        ArrayList<Integer> backoff = new ArrayList<>(vectors.get(0).length);
        for (int i = 0; i < vectors.get(0).length; i++) {
            backoff.add(0);
        }
        backoffVectors.add(backoff);

        backoff = new ArrayList<>(vectors.get(1).length);
        for (int i = 0; i < vectors.get(1).length; i++) {
            backoff.add(0);
        }
        backoffVectors.add(backoff);

        backoff = new ArrayList<>(vectors.get(2).length);
        for (int i = 0; i < vectors.get(2).length; i++) {
            backoff.add(vectors.get(1).Find(0, vectors.get(2).words.get(i)));
        }
        backoffVectors.add(backoff);

        for (int o = 3; o <= order; o++) {
            backoff = new ArrayList<>(vectors.get(o).length);
            for (int i = 0; i < vectors.get(o).length; i++) {
                backoff.add(vectors.get(o - 1).Find(
                        backoffVectors.get(o - 1).get(vectors.get(o).hists.get(i)),
                        vectors.get(o).words.get(i)));
            }
            backoffVectors.add(backoff);
        }

        System.out.println("Finish backoff");
        System.out.println("Current Memory Usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

//        ArrayList<Integer> vocabMap = vocab.Sort(countVectors.get(1));
//        ArrayList<Integer> nGramMap = new ArrayList<>(1);
//        nGramMap.add(0);
//        ArrayList<Integer> boNgramMap;
//        for (int i = 0; i <= order; i++) {
//            boNgramMap = nGramMap;
//
//            nGramMap = vectors.get(i).Sort(vocabMap, boNgramMap);
//
//            if (nGramMap.size() == 0) {
//                countVectors.get(i).ensureCapacity(vectors.get(i).length);
//            } else {
//                ArrayList<Integer> sorted = new ArrayList<>(nGramMap.size());
//                for (int j = 0; j < nGramMap.size(); j++) {
//                    sorted.add(0);
//                }
//                for (int j = 0; j < nGramMap.size(); j++) {
//                    sorted.set(nGramMap.get(j), countVectors.get(i).get(j));
//                }
//                countVectors.set(i, sorted);
//            }
//        }

        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        System.out.println("Execution time in seconds : " + timeElapsed / 1000000000);
        System.out.println("Done building EmpiricalUnigramLanguageModel.");
//        wordCounter = CollectionUtils.copyOf(wordCounter, EnglishWordIndexer.getIndexer().size());
//        total = CollectionUtils.sum(wordCounter);
    }

    public int getOrder() {
        return order;
    }

    public double getNgramLogProbability(int[] ngram, int from, int to) {
        if (to - from != 1) {
//            System.out.println("WARNING: to - from > 1 for EmpiricalUnigramLanguageModel");
        }
        int word = ngram[from];
        return Math.log((word < 0 || word >= wordCounter.length) ? 1.0 : wordCounter[word] / (total + 1.0));
    }

    public long getCount(int[] ngram) {
        if (ngram.length > 1) return 0;
        final int word = ngram[0];
        if (word < 0 || word >= wordCounter.length) return 0;
        return wordCounter[word];
    }
}
