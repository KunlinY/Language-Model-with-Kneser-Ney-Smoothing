package edu.berkeley.nlp.assignments.assign1.student;

import java.util.ArrayList;

public class Vocabulary {
    ArrayList<Integer> offsetLens;
    ArrayList<Integer> indices;
    String buffer;

    int length = 0;
    int hashMast;
    boolean fixedVocab = false;

    static final int Invalid = -1;
    static final int unkIndex = Invalid;
    static final int EndOfSentence = 0;
    static final int InitSize = 1 << 16;

    public Vocabulary() {

    }
}
