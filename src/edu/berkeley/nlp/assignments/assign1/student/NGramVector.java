package edu.berkeley.nlp.assignments.assign1.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class NGramVector {

    static final int InitSize = 1 << 20;
    static public final int Invalid = -1;

    int length = 0;
    int hashMask = InitSize - 1;
    int capacity = 0;

    int[] words = new int[InitSize];
    int[] hists = new int[InitSize];
    int[] indices;

    public NGramVector() {
        ReIndex(InitSize);
    }

    public int Find(int h, int w) {
        int pos = FindPos(h, w);
        if (pos >= 0) {
            return indices[pos];
        } else {
            return Invalid;
        }
    }

    public int Add(int h, int w) {
        int pos = FindPos(h, w);

        if (indices[pos] == Invalid) {
            if (length >= capacity - 1) {
                Grow();
                pos = FindPos(h, w);
            }
            indices[pos] = length;
            words[length] = w;
            hists[length] = h;
            length++;
        }

        return indices[pos];
    }

//    public ArrayList<Integer> Sort(ArrayList<Integer> vocabMap, ArrayList<Integer> boNGramMap) {
//        for (int i = 0; i < length; i++) {
//            words.set(i, vocabMap.get(words.get(i)));
//            hists.set(i, boNGramMap.get(hists.get(i)));
//        }
//
//        ArrayList<Integer> sortIndices = new ArrayList<>(length);
//        ArrayList<Integer> nGramMap = new ArrayList<>(length);
//        for (int i = 0; i < length; i++) {
//            sortIndices.add(i);
//            nGramMap.add(Invalid);
//        }
//        sortIndices.sort(new Comparator<Integer>() {
//            @Override
//            public int compare(Integer i, Integer j) {
//                return hists.get(i).equals(hists.get(j))
//                       ? words.get(i).compareTo(words.get(j)) : hists.get(i).compareTo(hists.get(j));
//            }
//        });
//
//        boolean sorted = true;
//        for (int i = 1; i < length; i++) {
//            if (sortIndices.get(i - 1) < sortIndices.get(i)) {
//                sorted = false;
//            }
//        }
//        if (sorted)
//            return new ArrayList<>();
//
//        ArrayList<Integer> newWords = new ArrayList<>(words.size());
//        ArrayList<Integer> newHists = new ArrayList<>(hists.size());
//        nGramMap.ensureCapacity(length);
//        for (int i = 0; i < length; i++) {
//            newWords.add(words.get(sortIndices.get(i)));
//            newHists.add(words.get(sortIndices.get(i)));
//            nGramMap.set(sortIndices.get(i), i);
//        }
//        words = newWords;
//        hists = newHists;
//
//        ReIndex(indices.size());
//
//        return nGramMap;
//    }

    public int FindPos(int h, int w) {
        int skip = 0;
        int pos = IndexHash(h, w);
        int index;

        while ((index = indices[pos]) != Invalid
                && !(words[index] == w && hists[index] == h)) {
            pos = (pos + (++skip)) & hashMask;
        }

        return pos;
    }

    public void Grow() {
        int newCapacity = capacity * 2;

        int[] newArr = new int[newCapacity];
        System.arraycopy(words, 0, newArr, 0, capacity);
        words = newArr;

        newArr = new int[newCapacity];
        System.arraycopy(hists, 0, newArr, 0, capacity);
        hists = newArr;

        ReIndex(newCapacity);

        System.out.println("Grow " + capacity
                + " Current Memory Usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    }

    public void ReIndex(int size) {
        if (size > capacity) {
            capacity = size;
        }
        hashMask = capacity - 1;

        indices = new int[capacity];
        Arrays.fill(indices, -1);

        for (int i = 0; i < length; i++) {
            int skip = 0;
            int pos = IndexHash(hists[i], words[i]);
            while (indices[pos] != Invalid) {
                pos = (pos + (++skip)) & hashMask;
            }
            indices[pos] = i;
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
