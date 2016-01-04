package ca.uqac.phoenixxie.ltl.bitmap;

import java.security.InvalidParameterException;
import java.util.BitSet;

public class RawBitmap implements LTLBitmap.BitmapAdapter {
    private BitSet bitset = new BitSet();

    private int size = 0;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; ++i) {
            sb.append(bitset.get(i) ? "1" : "0");
        }

        return sb.toString();
    }

    public int getCapacity() {
        return bitset.size();
    }

    public boolean firstBit() {
        return bitset.get(0);
    }

    public boolean lastBit() {
        return bitset.get(size - 1);
    }

    public boolean get(int index) {
        if (index >= size || index < 0) {
            throw new InvalidParameterException();
        }
        return bitset.get(index);
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

    public void opAnd(LTLBitmap.BitmapAdapter bm) {
        RawBitmap right = (RawBitmap) bm;
        bitset.and(right.bitset);
        size = Math.max(size, right.size);
    }

    public void opOr(LTLBitmap.BitmapAdapter bm) {
        RawBitmap right = (RawBitmap) bm;
        bitset.or(right.bitset);
        size = Math.max(size, right.size);
    }

    public void opXor(LTLBitmap.BitmapAdapter bm) {
        RawBitmap right = (RawBitmap) bm;
        bitset.xor(right.bitset);
        size = Math.max(size, right.size);
    }

    public void opShiftLeft1Bit() {
        bitset = bitset.get(1, size);
        --size;
    }

    public LTLBitmap.BitmapIterator begin() {
        return new Iterator();
    }

    public LTLBitmap.BitmapIterator end() {
        return new Iterator(size);
    }

    class Iterator implements LTLBitmap.BitmapIterator {
        private int index;

        public Iterator() {
            this(0);
        }

        public Iterator(int index) {
            if (index > size || index < 0) {
                throw new InvalidParameterException();
            }
            this.index = index;
        }

        public int index() {
            return index;
        }

        public void moveForward(int offset) {
            if (offset < 0) {
                throw new InvalidParameterException();
            }
            if (index + offset > size) {
                throw new InvalidParameterException();
            }
            index += offset;
        }

        public LTLBitmap.BitmapIterator find0() {
            for (int i = index; i < size; ++i) {
                if (bitset.get(i) == false) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        public LTLBitmap.BitmapIterator rfind0() {
            int i = index;
            if (i == size) {
                --i;
            }
            for (; i >= 0; --i) {
                if (bitset.get(i) == false) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        public LTLBitmap.BitmapIterator find1() {
            int i = index;
            if (i == size) {
                --i;
            }
            for (; i < size; ++i) {
                if (bitset.get(i) == true) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        public LTLBitmap.BitmapIterator rfind1() {
            for (int i = index - 1; i >= 0; --i) {
                if (bitset.get(i) == true) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        public boolean currentBit() {
            if (index == size) {
                throw new IndexOutOfBoundsException();
            }
            return bitset.get(index);
        }

        public boolean isEnd() {
            return index == size;
        }
    }
}
