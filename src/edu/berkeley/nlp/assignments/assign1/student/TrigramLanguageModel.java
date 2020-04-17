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

    long total = 0;

    long[] wordCounter = new long[10];

    public TrigramLanguageModel(Iterable<List<String>> sentenceCollection) {
        System.out.println("Building TrigramLanguageModel . . .");
        int sent = 0;
        for (List<String> sentence : sentenceCollection) {
            sent++;
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(0, NgramLanguageModel.START);
            stoppedSentence.add(STOP);
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
                if (index >= wordCounter.length) wordCounter = CollectionUtils.copyOf(wordCounter, wordCounter.length * 2);
                wordCounter[index]++;
            }
        }
        System.out.println("Done building EmpiricalUnigramLanguageModel.");
        wordCounter = CollectionUtils.copyOf(wordCounter, EnglishWordIndexer.getIndexer().size());
        total = CollectionUtils.sum(wordCounter);
    }

    public int getOrder() {
        return 3;
    }

    public double getNgramLogProbability(int[] ngram, int from, int to) {
        if (to - from != 1) {
            System.out.println("WARNING: to - from > 1 for EmpiricalUnigramLanguageModel");
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
