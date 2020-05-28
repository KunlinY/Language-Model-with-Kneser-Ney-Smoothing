package edu.berkeley.nlp.assignments.assign1.student;


public class BitPackVector {
    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;

    private long[] storage;

    int UnitSize = 19;
    int UnitMask = (1 << UnitSize) - 1;
    static final int InitSize = 1 << 19;
    int size = 0;
    boolean fixed = false;

    public BitPackVector() {
        storage = new long[InitSize * UnitSize / BITS_PER_WORD];
    }

    public BitPackVector(int unitSize) {
        UnitSize = unitSize;
        UnitMask = (1 << UnitSize) - 1;
        storage = new long[InitSize * UnitSize / BITS_PER_WORD];
    }

    public BitPackVector(int unitSize, int initSize) {
        UnitSize = unitSize;
        UnitMask = (1 << UnitSize) - 1;
        storage = new long[Math.min(initSize, InitSize) * UnitSize / BITS_PER_WORD];
    }

    void EnsureCapacity(int value, int index) {
        if (fixed)
            return;

        if (index > size) {
            size = index;
        }

        if (((value >> UnitSize) & 0x7fffffff) > 0) {
            System.out.println("Grow unit");
            GrowUnit();
            System.out.println(storage.length * BITS_PER_WORD + " " + size + " " + UnitSize);
        }
        if (size >= storage.length * BITS_PER_WORD / UnitSize - 1) {
            System.out.println("Grow vector");
            GrowVector();
            System.out.println(storage.length * BITS_PER_WORD + " " + size + " " + UnitSize);
        }
    }

    int Get(int index) {
        EnsureCapacity(0, index);
        int bitIndex = index * UnitSize;
        int from = wordIndex(bitIndex);
        int to = wordIndex(bitIndex + UnitSize);
        int longIdx = bitIndex % BITS_PER_WORD;
        int intIdx = (BITS_PER_WORD - longIdx > UnitSize) ? 0 : UnitSize - BITS_PER_WORD + longIdx;
        int value = (int)(storage[from] >>> longIdx) & (UnitMask >>> (intIdx));;

        if (from != to && to < storage.length) {
            value |= (((int)(storage[to]) & UnitMask >>> (BITS_PER_WORD - longIdx)) << (BITS_PER_WORD - longIdx));
        }

        return value;
    }

    void Add(int value) {
        Set(value, size);
        size++;
    }

    void Set(int value, int index) {
        EnsureCapacity(value, index);
        int bitIndex = index * UnitSize;
        int from = wordIndex(bitIndex);
        int to = wordIndex(bitIndex + UnitSize);
        int longIdx = bitIndex % BITS_PER_WORD;
        storage[from] &= ~((long) UnitMask << longIdx);
        storage[from] |= (long) value << longIdx;

        if (from != to) {
            storage[to] &= ~((long) UnitMask >>> (BITS_PER_WORD - longIdx));
            storage[to] |= (long) value >>> (BITS_PER_WORD - longIdx);
        }
    }

    void PrintBit(long value) {
        String str = Long.toBinaryString(value);
        for (int i = str.length(); i < BITS_PER_WORD; i++) {
            System.out.print("0");
        }
        System.out.println(Long.toBinaryString(value));
    }

    private static int wordIndex(int bitIndex) {
        return bitIndex >>> ADDRESS_BITS_PER_WORD;
    }

    void Trim() {
        int bitInUse = (int)Math.ceil(1.0 * (size + 1) * UnitSize / BITS_PER_WORD);
        long[] newStorage = new long[bitInUse];
        System.arraycopy(storage, 0, newStorage, 0, bitInUse);
        storage = newStorage;
        fixed = true;
    }

    void GrowVector() {
        int newSize = storage.length * 2;
        long[] newStorage = new long[newSize];
        System.arraycopy(storage, 0, newStorage, 0, storage.length);
        storage = newStorage;
    }

    void GrowUnit() {
        int newUnitSize = UnitSize + 1;
        BitPackVector vec = new BitPackVector(newUnitSize, size);

        int bitIndex = 0;
        for (int i = 0; i < size; i++, bitIndex += newUnitSize) {
            int value = Get(i);

            vec.Set(value, i);
        }

        UnitSize = newUnitSize;
        UnitMask = (1 << UnitSize) - 1;

        storage = vec.storage;
    }
}
