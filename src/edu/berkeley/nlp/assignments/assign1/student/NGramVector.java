package edu.berkeley.nlp.assignments.assign1.student;

import java.util.ArrayList;
import java.util.Comparator;

public class NGramVector {

    static final int InitSize = 1 << 20;
    static public final int Invalid = -1;

    int length = 0;
    int hashMask = InitSize - 1;
    int capacity = 0;

    ArrayList<Integer> words = new ArrayList<>(InitSize);
    ArrayList<Integer> hists = new ArrayList<>(InitSize);
    ArrayList<Integer> indices = new ArrayList<>(InitSize);

    public NGramVector() {
        ReIndex(InitSize);
    }

    public int Find(int h, int w) {
        int pos = FindPos(h, w);
        return indices.get(pos);
    }

    public int Add(int h, int w) {
        int pos = FindPos(h, w);

        if (indices.get(pos) == Invalid) {
            if (length >= words.size()) {
                Grow();
                pos = FindPos(h, w);
            }
            indices.set(pos, length);
            words.add(w);
            hists.add(h);
            length++;
        }

        return indices.get(pos);
    }

    public void Grow() {
        int newCapacity = capacity * 2;
        ReIndex(newCapacity);
        words.ensureCapacity(newCapacity);
        hists.ensureCapacity(newCapacity);
        capacity = newCapacity;
    }

    public ArrayList<Integer> Sort(ArrayList<Integer> vocabMap, ArrayList<Integer> boNGramMap) {
        for (int i = 0; i < length; i++) {
            words.set(i, vocabMap.get(words.get(i)));
            hists.set(i, boNGramMap.get(hists.get(i)));
        }

        ArrayList<Integer> sortIndices = new ArrayList<>(length);
        ArrayList<Integer> nGramMap = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            sortIndices.add(i);
            nGramMap.add(Invalid);
        }
        sortIndices.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {
                return hists.get(i).equals(hists.get(j))
                       ? words.get(i).compareTo(words.get(j)) : hists.get(i).compareTo(hists.get(j));
            }
        });

        boolean sorted = true;
        for (int i = 1; i < length; i++) {
            if (sortIndices.get(i - 1) < sortIndices.get(i)) {
                sorted = false;
            }
        }
        if (sorted)
            return new ArrayList<>();

        ArrayList<Integer> newWords = new ArrayList<>(words.size());
        ArrayList<Integer> newHists = new ArrayList<>(hists.size());
        nGramMap.ensureCapacity(length);
        for (int i = 0; i < length; i++) {
            newWords.add(words.get(sortIndices.get(i)));
            newHists.add(words.get(sortIndices.get(i)));
            nGramMap.set(sortIndices.get(i), i);
        }
        words = newWords;
        hists = newHists;

        ReIndex(indices.size());

        return nGramMap;
    }

    public int capacity() {
        return indices.size();
    }

    public int FindPos(int h, int w) {
        int skip = 0;
        int pos = IndexHash(h, w);
        int index;

        while ((index = indices.get(pos)) != Invalid
                && !(words.get(index) == w && hists.get(index) == h)) {
            pos = (pos + (++skip)) & hashMask;
        }

        return pos;
    }

    public void ReIndex(int size) {
        indices.ensureCapacity(size);

        for (int i = capacity; i < size; i++) {
            indices.add(Invalid);
        }

        if (size > capacity) {
            capacity = size;
        }
        hashMask = capacity - 1;

        for (int i = 0; i < length; i++) {
            int skip = 0;
            int pos = IndexHash(hists.get(i), words.get(i));
            while (indices.get(pos) != Invalid) {
                pos = (pos + (++skip)) & hashMask;
            }
            indices.set(pos, i);
        }
    }

    public int IndexHash(int key1, int key2) {
        int hash = 0, tmp;

        hash += (key1 >> 16);
        tmp = ((key1 & 0xFFFF) << 11) ^ hash;
        hash = (hash << 16) ^ tmp;
        hash += hash >> 11;

        hash += (key2 >> 16);
        tmp = ((key2 & 0xFFFF) << 11) ^ hash;
        hash = (hash << 16) ^ tmp;
        hash += hash >> 11;

        hash ^= hash << 3;
        hash += hash >> 5;
        hash ^= hash << 4;
        hash += hash >> 17;
        hash ^= hash << 25;
        hash += hash >> 6;
        hash &= hashMask;

        return hash;
    }


}
