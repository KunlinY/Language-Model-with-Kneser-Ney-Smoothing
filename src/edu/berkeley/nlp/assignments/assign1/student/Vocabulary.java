package edu.berkeley.nlp.assignments.assign1.student;

import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class Vocabulary {

    static final int Invalid = -1;
    static final int unkIndex = Invalid;
    static final int EndOfSentence = 0;
    static final int InitSize = 1 << 20;

    int length = 0;
    int hashMask = InitSize - 1;
    int capacity = InitSize;

    ArrayList<Pair<Integer, Integer>> offsetLens = new ArrayList<>(InitSize);
    ArrayList<Integer> indices = new ArrayList<>(InitSize);
    StringBuilder buffer = new StringBuilder(InitSize * 4);

    public Vocabulary() {
        for (int i = 0; i <  InitSize; i++) {
            indices.add(Invalid);
        }
    }

    public int Find(String word) {
        int pos = FindPos(word);
        int index = indices.get(pos);
        return (index == Invalid) ? unkIndex : index;
    }

    public int FindPos(String word) {
        int len = word.length();
        int skip = 0;
        int pos = WordHash(word);
        int index;
        while (true) {
            index = indices.get(pos);
            if (index == Invalid) break;

            Pair<Integer, Integer> o = offsetLens.get(index);
            if (o.getSecond() == len
                    && word.equals(buffer.substring(o.getFirst(), o.getFirst() + o.getSecond()))) break;

            pos = (pos + (++skip)) & hashMask;

//            System.out.format("FindPos hash conflict: %s %d %d\n", word, pos, skip);
        }

        return pos;
    }

    public int Add(String word) {
        int pos = FindPos(word);
        if (indices.get(pos) == Invalid) {
            if (length >= capacity - 1) {
                Grow();
                pos = FindPos(word);
            }
            indices.set(pos, length++);
            offsetLens.add(new Pair<>(buffer.length(), word.length()));
            buffer.append(word);
        }

        return indices.get(pos);
    }

    public String Get(int n) {
        Pair<Integer, Integer> o = offsetLens.get(n);
        return buffer.substring(o.getFirst(), o.getSecond());
    }

    public void Grow() {
        int newCapacity = capacity * 2;
        indices.ensureCapacity(newCapacity);

        for (int i = capacity; i < newCapacity; i++) {
            indices.add(-1);
        }

        System.out.format("Grow capacity from %d to %d\n", capacity, newCapacity);

        capacity = newCapacity;
        offsetLens.ensureCapacity(capacity);
        hashMask = capacity - 1;

        Iterator<Pair<Integer, Integer>> j = offsetLens.iterator();
        for (int i = 0; i < length; i++) {
            int skip = 0;
            Pair<Integer, Integer> o = j.next();
            int pos = WordHash(o.getFirst(), o.getSecond());

            while(indices.get(pos) != Invalid) {
                pos = (pos + (++skip)) & hashMask;
                System.out.format("Grow hash conflict: %d %d\n", pos, skip);
            }

            indices.set(pos, i);
        }
    }

    public ArrayList<Integer> Sort() {
        ArrayList<Integer> sortMap = new ArrayList<>(length);
        ArrayList<Integer> sortIndices = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            sortIndices.add(i);
        }

        int numFixedWords = (unkIndex == Invalid) ? 1 : 2;

        sortIndices.subList(numFixedWords, length).sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {
                return Get(i).compareTo(Get(j));
            }
        });

        StringBuilder newBuffer = new StringBuilder(length);
        ArrayList<Pair<Integer, Integer>> newOffsetLens = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            Pair<Integer, Integer> o = offsetLens.get(sortIndices.get(i));
            newOffsetLens.add(new Pair<>(newBuffer.length(), o.getSecond()));
            sortMap.set(sortIndices.get(i), i);
        }

        buffer = newBuffer;
        offsetLens = newOffsetLens;

        ArrayList<Integer> input = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            input.add(sortMap.get(indices.get(i)));
        }

        for (int i = 0; i < indices.size(); i++) {
            if (indices.get(i) != Invalid) {
                indices.set(i, input.get(i));
            }
        }

        return sortMap;
    }

    public int WordHash(int start, int len) {
        int h = 0;
        int end = start + len;

        for (int i = start; i < end; i++) {
            h += (h << 3) + buffer.charAt(i);
        }

        h *= 1103515245;
        h &= hashMask;

        return h;
    }

    public int WordHash(String word) {
        int h = 0;

        for (char c: word.toCharArray()) {
            h += (h << 3) + c;
        }

        h *= 1103515245;
        h &= hashMask;

        return h;
    }
}
