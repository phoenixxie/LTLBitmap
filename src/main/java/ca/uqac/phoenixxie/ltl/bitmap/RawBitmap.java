package ca.uqac.phoenixxie.ltl.bitmap;

import java.util.BitSet;

public class RawBitmap implements Bitmap.BitmapAdapter {
    private BitSet bitset = new BitSet();
    private int size = 0;

    public int getCapacity() {
        return bitset.size();
    }

    public boolean firstBit() {
        return bitset.get(0);
    }

    public boolean lastBit() {
        return bitset.get(size - 1);
    }

    public void add(boolean bit) {
        if (bit) {
            bitset.set(size);
        } else {
            bitset.clear(size);
        }
        ++size;
    }

    public int getSize() {
        return size;
    }

    public void clear(boolean bit) {
        if (bit) {
            bitset.set(0, size);
        } else {
            bitset.clear(0, size);
        }
    }

    public void opNot() {
        bitset.flip(0, size);
    }

    public void opAnd(Bitmap.BitmapAdapter bm) {
        RawBitmap right = (RawBitmap) bm;
        bitset.and(right.bitset);
        size = Math.max(size, right.size);
    }

    public void opOr(Bitmap.BitmapAdapter bm) {
        RawBitmap right = (RawBitmap) bm;
        bitset.or(right.bitset);
        size = Math.max(size, right.size);
    }

    public void opXor(Bitmap.BitmapAdapter bm) {
        RawBitmap right = (RawBitmap) bm;
        bitset.xor(right.bitset);
        size = Math.max(size, right.size);
    }

    public void opShiftLeft1Bit() {
        bitset = bitset.get(1, size);
        --size;
    }

    public Bitmap.BitmapIterator fromStart() {
        return null;
    }

    public Bitmap.BitmapIterator fromEnd() {
        return null;
    }
}
