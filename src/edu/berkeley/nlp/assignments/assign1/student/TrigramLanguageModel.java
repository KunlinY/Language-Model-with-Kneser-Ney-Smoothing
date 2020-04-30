package edu.berkeley.nlp.assignments.assign1.student;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.StringIndexer;

import java.util.ArrayList;
import java.util.List;

public class TrigramLanguageModel implements NgramLanguageModel
{
    static final int order = 3;
    static final int InitSize = 1 << 20;

    NGramVector[] vectors = new NGramVector[order + 1];
    NGramVector[] invVectors = new NGramVector[order];
    BitPackVector[] countVectors = new BitPackVector[order + 1];
    float[][] pseudo = new float[order + 1][];
    float[][] backoff = new float[order + 1][];

    StringIndexer vocab = EnglishWordIndexer.getIndexer();
    int[] hists = new int[order + 1];
    float[][] D = new float[order + 1][4];
    int vocabTotal = 0;

    public TrigramLanguageModel(Iterable<List<String>> sentenceCollection) {
        System.out.println("Building TrigramLanguageModel . . .");

        BitPackVector cntVec = new BitPackVector(1);
        countVectors[0] = cntVec;

        for (int i = 1; i <= order; i++) {
            countVectors[i] = new BitPackVector(16);
        }

        for (int i = 1; i <= order; i++) {
            hists[i] = -1;
            vectors[i] = new NGramVector();
        }

        for (int i = 1; i < order; i++) {
            invVectors[i] = new NGramVector();
        }

        int sent = 0;
        long startTime = System.nanoTime();
        for (List<String> sentence : sentenceCollection) {
            sent++;
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            sentenceCount(sentence);
//            trieCount(sentence);
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

        for (int i = 1; i <= order; i++) {
            vectors[i].Trim();
        }
//        ArrayList<Integer> backoff = new ArrayList<>(vectors.get(0).length);
//        for (int i = 0; i < vectors.get(0).length; i++) {
//            backoff.add(0);
//        }
//        backoffVectors.add(backoff);
//
//        backoff = new ArrayList<>(vectors.get(1).length);
//        for (int i = 0; i < vectors.get(1).length; i++) {
//            backoff.add(0);
//        }
//        backoffVectors.add(backoff);
//
//        backoff = new ArrayList<>(vectors.get(2).length);
//        for (int i = 0; i < vectors.get(2).length; i++) {
//            backoff.add(vectors.get(1).Find(0, vectors.get(2).words.get(i)));
//        }
//        backoffVectors.add(backoff);
//
//        for (int o = 3; o <= order; o++) {
//            backoff = new ArrayList<>(vectors.get(o).length);
//            for (int i = 0; i < vectors.get(o).length; i++) {
//                backoff.add(vectors.get(o - 1).Find(
//                        backoffVectors.get(o - 1).get(vectors.get(o).hists.get(i)),
//                        vectors.get(o).words.get(i)));
//            }
//            backoffVectors.add(backoff);
//        }
//
//        System.out.println("Finish backoff");
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

        vocabTotal = vocab.size();

        for (int i = 1; i <= order; i++) {
            System.out.println("vectors " + i + " length " + vectors[i].length + " capacity " + vectors[i].capacity);
            System.out.println("count vectors " + i + " length " + countVectors[i].size);
        }

        adjustCount();
        calcProb();
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        System.out.println("Execution time in seconds : " + timeElapsed / 1000000000);
        System.out.println("Done building EmpiricalUnigramLanguageModel.");
    }

    public int getOrder() {
        return order;
    }

    public double getNgramLogProbability(int[] ngram, int from, int to) {
//        if (to - from != 1) {
//            System.out.println("WARNING: to - from > 1 for EmpiricalUnigramLanguageModel");
//        }
//        int word = ngram[from];
//        return Math.log((word < 0 || word >= wordCounter.length) ? 1.0 : wordCounter[word] / (total + 1.0));
        return 0.5;
    }

    public long getCount(int[] ngram) {
        if (ngram.length > order) return 0;

        int hist = 0;
        int index = 0;
        for (int i = 0; i < ngram.length; i++) {
            if (ngram[i] < 0 || ngram[i] > vectors[1].length) {
                return 0;
            }

            index = vectors[i + 1].Find(hist, ngram[i]);
            hist = index;
        }

        return countVectors[ngram.length].Get(index);
    }

//    public void trieCount(List<String> sentence) {
//        int[] words = new int[sentence.size() + 2];
//        words[0] = vocab.addAndGetIndex(NgramLanguageModel.START);
//
//        for (int i = 1; i < sentence.size(); i++) {
//            words[i] = vocab.addAndGetIndex(sentence.get(i));
//        }
//
//        for (int i = 0; i < words.length; i++) {
//            for (int j = 0; j < order && i + j < words.length; j++) {
//                trie.Add(words, i, i + j);
//            }
//        }
//    }

    public void sentenceCount(List<String> sentence) {
        ArrayList<Integer> words = new ArrayList<>(sentence.size() +2);
        words.add(vocab.addAndGetIndex(NgramLanguageModel.START));
        for (String word: sentence) {
            words.add(vocab.addAndGetIndex(word));
        }
        words.add(vocab.addAndGetIndex(NgramLanguageModel.STOP));

        hists[1] = vectors[1].Add(0, vocab.indexOf(NgramLanguageModel.START));
        for (int i = 1; i < words.size(); i++) {
            int word = words.get(i);
            int hist = 0;

            for (int j = 1; j < Math.min(i + 2, order + 1); j++) {
                if (word != NGramVector.Invalid && hist != NGramVector.Invalid) {
                    int index = vectors[j].Add(hist, word);
                    hist = hists[j];
                    hists[j] = index;
//                    int old = countVectors[j].Get(index);
//                    countVectors[j].Set(old + 1, index);

                    if (j != order && i - j >= 0) {
                        invVectors[j].Add(index, words.get(i - j));
                    } else if (j == order) {
                        int old = countVectors[j].Get(index);
                        countVectors[j].Set(old + 1, index);
                    }
                } else {
                    hist = hists[j];
                    hists[j] = NGramVector.Invalid;
                }
            }
        }
    }

    public void adjustCount() {
        for (int i = 1; i < order; i++) {
            for (int j = 0; j < invVectors[i].length; j++) {
                int index = invVectors[i].hists.Get(j);
                int old = countVectors[i].Get(index);
                countVectors[i].Set(old + 1, index);
            }
        }
        invVectors = null;

        int[][] t = new int[order + 1][5];
        for (int i = 1; i < order + 1; i++) {
            for (int j = 0; j < countVectors[i].size; j++) {
                int val = countVectors[i].Get(j);

                if (val > 4 || val == 0) {
                    continue;
                }

                t[i][val] += 1;
            }
        }

        for (int i = 1; i < order + 1; i++) {
            D[i][0] = (float) 0.0;
            float Y = (float)(long) t[i][1] / ((long)t[i][1] + 2 * (long)t[i][2]);
            for (int j = 1; j < 4; j++) {
                D[i][j] = (float)j - (float)(j +1) * Y * (float)t[i][j +1] / t[i][j];
                System.out.println("D " + i + " " + j + " " + D[i][j]);
            }
        }
    }

    public void calcProb() {
        for (int i = 2; i <= order; i++) {
            System.out.println(i);
            pseudo[i] = new float[vectors[i].length];
            backoff[i] = new float[vectors[i].length];
            for (int j = 0; j < vectors[i].length; j++) {
                int idx = vectors[i].indices[j];

                int cnt = 0;
                if (idx > 0)
                    cnt = countVectors[i].Get(idx);

                float Dcnt = cnt > 3 ? D[i][3] : D[i][cnt];
                int sum = 0;
                int wsum = 0;
                int histIdx = vectors[i].hists.Get(j);

                for (int k = 0; k < vectors[i].length; k++) {
                    int curHist = vectors[i].hists.Get(k);
                    if (curHist == histIdx) {
                        int ccnt = countVectors[i - 1].Get(curHist);
                        float DDcnt = ccnt > 3 ? D[i][3] : D[i][ccnt];
                        sum += ccnt;
                        wsum += DDcnt * ccnt;
                    }
                }

                pseudo[i][idx] = (float)1.0 * (cnt - Dcnt) / (float)sum;
                backoff[i][idx] = (float)1.0 * wsum / (float)sum;
            }
        }

        pseudo[1] = new float[vectors[1].length];
        for (int j = 0; j < vectors[1].length; j++) {
            int idx = vectors[1].indices[j];
            int cnt = countVectors[1].Get(idx);
            float Dcnt = cnt > 3 ? D[1][3] : D[1][cnt];
            int sum = vocabTotal;

            pseudo[1][idx] = (float)1.0 * (cnt - Dcnt) / sum;
        }
    }
}
