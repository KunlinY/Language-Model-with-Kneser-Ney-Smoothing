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
    double[][] pseudo = new double[order + 1][];

    StringIndexer vocab = EnglishWordIndexer.getIndexer();
    int[] hists = new int[order + 1];
    double[][] D = new double[order + 1][4];
    int vocabTotal;

    double unkProb;
    int allUniCnt = 0;

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
        }

        vocabTotal = vocab.size();

        adjustCount();

        for (int i = 1; i <= order; i++) {
            vectors[i].Trim();
            countVectors[i].Trim();
        }

        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        System.out.println("Execution time in seconds : " + timeElapsed / 1000000000);

        System.out.println("Finish count");
        System.out.println("Current Memory Usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

        for (int i = 1; i <= order; i++) {
            System.out.println("vectors " + i + " length " + vectors[i].length + " capacity " + vectors[i].capacity);
            System.out.println("count vectors " + i + " length " + countVectors[i].size);
        }

        calcProb();

        endTime = System.nanoTime();
        timeElapsed = endTime - startTime;
        System.out.println("Execution time in seconds : " + timeElapsed / 1000000000);
        System.out.println("Current Memory Usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        System.out.println("Done building EmpiricalUnigramLanguageModel.");
    }

    public int getOrder() {
        return order;
    }

    public double getNgramLogProbability(int[] ngram, int from, int to) {
        int idx = 0;
        for (int i = from; i < to; i++) {
            if (idx < 0)
                idx = 0;
            idx = vectors[i - from + 1].Find(idx, ngram[i]);
        }

        if (idx < 0)
            return Math.log(unkProb) * (to - from);

        double score = pseudo[to - from][idx];
        if (Double.isNaN(score))
            return Math.log(unkProb) * (to - from);
        else
            return Math.log(score);

//        int idx = vectors[1].Find(0, ngram[to - 1]);
//        double score;
//        if (idx < 0)
//            score = 1.0 / (double)vocabTotal;
//        else
//            score = pseudo[1][idx] + 1.0 / (double)vocabTotal;
//
//        for (int i = 2; i <= to - from && i < order + 1; i++) {
//            int pIdx = 0;
//            int bIdx = 0;
//
//            for (int j = 1; j <= i; j++) {
//                pIdx = vectors[j].Find(pIdx, ngram[to - i + j - 1]);
//            }
//            for (int j = 1; j < i; j++) {
//                bIdx = vectors[j].Find(bIdx, ngram[to - i + j - 1]);
//            }
//
//            double p;
//            double back;
//            if (pIdx < 0)
//                p = 0.0;
//            else
//                p = pseudo[i][pIdx];
//
//            if (bIdx < 0)
//                back = 1.0 / (double)vocabTotal;
//            else
//                back = backoff[i - 1][bIdx];
//
//            score = p + back * score;
//        }
//
//        if (Double.isNaN(score))
//            return Math.log(1.0 / (double)vocabTotal) * (to - from);
//        else
//            return Math.log(score);
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
                int index = invVectors[i].hists[j];
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
            D[i][0] = 0.0;
            double Y = (double)(long) t[i][1] / ((long)t[i][1] + 2 * (long)t[i][2]);
            for (int j = 1; j < 4; j++) {
                D[i][j] = (double)j - (double)(j +1) * Y * (double)t[i][j +1] / t[i][j];
                System.out.println("D " + i + " " + j + " " + D[i][j]);
            }
        }
    }

    public void calcProb() {
        double[][] backoff = new double[order + 1][];
        int[] dcnt = new int[4];
        for (int i = 0; i < countVectors[1].size; i++) {
            int cnt = countVectors[1].Get(i);
            allUniCnt += cnt;
            if (cnt < 3)
                dcnt[cnt] += 1;
            else
                dcnt[3] += 1;
        }
        unkProb = 1.0 * (D[1][1] * dcnt[1] + D[1][2] * dcnt[2] + D[1][3] * dcnt[3]) / (double)(vocabTotal * allUniCnt);

        pseudo[1] = new double[vectors[1].length];
        for (int j = 0; j < vectors[1].indices.length; j++) {
            int idx = vectors[1].indices[j];
            if (idx < 0)
                continue;

            int cnt = countVectors[1].Get(idx);
            double Dcnt = cnt > 3 ? D[1][3] : D[1][cnt];

            pseudo[1][idx] = 1.0 * (cnt - Dcnt) / (double)allUniCnt + unkProb;
        }

        for (int i = 2; i <= order; i++) {
            int[] sum = new int[vectors[i - 1].length];
//            double[] wsum = new double[vectors[i - 1].length];
            int[][] ddcnt = new int[4][vectors[i - 1].length];
            for (int j = 0; j < vectors[i].indices.length; j++) {
                int idx = vectors[i].indices[j];
                if (idx < 0)
                    continue;

                int cnt = countVectors[i].Get(idx);
                int histIdx = vectors[i].hists[idx];

                sum[histIdx] += cnt;
                if (cnt < 3)
                    ddcnt[cnt][histIdx] += 1;
                else
                    ddcnt[3][histIdx] += 1;
//                if (cnt <= 3)
//                    wsum[histIdx] += D[i][cnt] * cnt;
//                else
//                    wsum[histIdx] += D[i][3] * cnt;
            }

            backoff[i - 1] = new double[vectors[i - 1].length];
            for (int j = 0; j < vectors[i - 1].length; j++) {
                backoff[i - 1][j] = (D[i][1] * ddcnt[1][j] + D[i][2] * ddcnt[2][j] + D[i][3] * ddcnt[3][j]) / (double)sum[j];
//                backoff[i - 1][j] = wsum[j] / (double)sum[j];
            }

            pseudo[i] = new double[vectors[i].length];
            for (int j = 0; j < vectors[i].indices.length; j++) {
                int idx = vectors[i].indices[j];

                if (idx < 0)
                    continue;

                int cnt = countVectors[i].Get(idx);
                double Dcnt = cnt > 3 ? D[i][3] : D[i][cnt];
                int histIdx = vectors[i].hists[idx];

                pseudo[i][idx] = 1.0 * (cnt - Dcnt) / (double)sum[histIdx] + backoff[i - 1][histIdx] * pseudo[i - 1][histIdx];
            }
        }
    }
}
