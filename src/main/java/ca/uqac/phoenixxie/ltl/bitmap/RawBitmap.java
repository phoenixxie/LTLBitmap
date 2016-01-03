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

    public void addMany(boolean bit, int count) {
        for (int i = 0; i < count; ++i) {
            add(bit);
        }
    }

    public int size() {
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

    public Bitmap.BitmapIterator begin() {
        return new Iterator();
    }

    public Bitmap.BitmapIterator end() {
        return new Iterator(size);
    }

    class Iterator implements Bitmap.BitmapIterator {
        private int index;

        public Iterator() {
            this(0);
        }

        public Iterator(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }

        public void moveForward(int offset) {
            assert(index + offset <= size);
            index += offset;
        }

        public Bitmap.BitmapIterator find0() {
            for (int i = index; i < size; ++i) {
                if (bitset.get(i) == false) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        public Bitmap.BitmapIterator rfind0() {
            for (int i = index - 1; i >= 0; --i) {
                if (bitset.get(i) == false) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        public Bitmap.BitmapIterator find1() {
            for (int i = index; i < size; ++i) {
                if (bitset.get(i) == true) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        public Bitmap.BitmapIterator rfind1() {
            for (int i = index - 1; i >= 0; --i) {
                if (bitset.get(i) == true) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        public boolean currentBit() {
            return bitset.get(index);
        }

        public boolean isEnd() {
            return index == size;
        }
    }
}
