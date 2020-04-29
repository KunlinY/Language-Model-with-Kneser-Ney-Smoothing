package edu.berkeley.nlp.assignments.assign1.student;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;

import java.util.Arrays;

public class Trie {
    static final int RootSize = 1 << 20;
    static final int InitSize = 1 << 4;
    static int Size = 0;

    int size = 0;
    int wordIndex = -1;
    int count = 0;
    Trie parent = null;
//    int adjCount = 0;
//    double probability = 0.0;
//    double backoff = 0.0;

    Trie[] children;

    public Trie() {
    }

    public Trie(int size) {
        children = new Trie[size];
    }

    public Trie Add(int[] ngram, int from, int to) {
        int index = FindPos(ngram[to]);

        if (children[index] == null || children[index].wordIndex != ngram[to]) {
            if (size == children.length) {
                Grow();
            }

            Trie child = new Trie(InitSize);
            child.wordIndex = ngram[to];
            System.arraycopy(children, index, children, index + 1, size - index);
            child.parent = this;
            children[index] = child;
            size++;
            Size++;
        }

        if (from == to) {
            children[index].count++;
            return children[index];
        }
        else
            return children[index].Add(ngram, from, to - 1);
    }

    public Trie Find(int[] ngram, int from, int to) {
        int index = FindPos(ngram[to]);

        if (children[index].wordIndex != ngram[to])
            return new Trie();
        else if (from == to)
            return children[index];
        else
            return children[index].Find(ngram, from, to - 1);
    }

    public int FindPos(int index) {
        return FindPos(index, 0, size - 1);
    }

    public int FindPos(int index, int low, int high) {
        while (low < high) {
            int mid = (low + high) >>> 1;
            int midVal = children[mid].wordIndex;

            if (midVal < index)
                low = mid + 1;
            else if (midVal > index)
                high = mid;
            else
                return mid;
        }
        return low;
    }

    public void Trim() {
        if (size == children.length)
            return;

        Trie[] newChildren = new Trie[size];
        System.arraycopy(children, 0, newChildren,0, size);
        children = newChildren;
    }

    public void Grow() {
        if (children.length > 2 << 12) {
            System.out.println(children.length * 2);
        }

        int newSize = children.length * 2;
        Trie[] newChildren = new Trie[newSize];
        System.arraycopy(children, 0, newChildren, 0, children.length);
        children = newChildren;
    }

    public String Words() {
        StringBuilder s = new StringBuilder();
        Trie t = this;
        while (t.parent != null) {
            s.append(EnglishWordIndexer.getIndexer().get(t.wordIndex));
            t = t.parent;
        }
        return s.toString();
    }
}
