package edu.berkeley.nlp.assignments.assign1.student;

import java.util.Arrays;

public class NGramVector {

    static final int InitSize = 1 << 20;
    static public final int Invalid = -1;

    int length = 0;
    int hashMask = InitSize - 1;
    int capacity = 0;

//    BitPackVector words = new BitPackVector();
//    BitPackVector hists = new BitPackVector();
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

    public void Trim() {
//        words.Trim();
//        hists.Trim();
        int[] newArr = new int[length];
        System.arraycopy(words, 0, newArr, 0, length);
        words = newArr;

        newArr = new int[length];
        System.arraycopy(hists, 0, newArr, 0, length);
        hists = newArr;
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
