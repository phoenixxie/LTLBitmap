package ca.uqac.phoenixxie.ltl.bitmap;

import it.uniroma3.mat.extendedset.intset.ConciseSet;
import it.uniroma3.mat.extendedset.intset.IntSet;

public class WAHConciseBitmap implements LTLBitmap.BitmapAdapter {
    private ConciseSet bitmap;
    private int size;

    public WAHConciseBitmap() {
        bitmap = new ConciseSet(true);
        size = 0;
    }

    private WAHConciseBitmap(ConciseSet bm, int size) {
        this.bitmap = bm;
        this.size = size;
    }

    @Override
    public void add(boolean bit) {
        if (bit) {
            bitmap.add(size);
        }
        ++size;
    }

    @Override
    public void addMany(boolean bit, int count) {
        if (count <= 0) {
            return;
        }

        if (bit) {
            bitmap.fill(size, size + count - 1);
        }
        size += count;
    }

    @Override
    public boolean get(int position) {
        return bitmap.contains(position);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int getRealSize() {
        return bitmap.getRealSize();
    }

    @Override
    public String toString() {
        StringBuilder answer = new StringBuilder();
        IntSet.IntIterator i = bitmap.iterator();
        int lastpos = 0;
        while (i.hasNext()) {
            int pos = i.next();
            for (int j = lastpos; j < pos; ++j) {
                answer.append("0");
            }
            answer.append("1");
            lastpos = pos + 1;
        }
        for (int j = lastpos; j < size(); ++j) {
            answer.append("0");
        }
        return answer.toString();
    }

    @Override
    public boolean firstBit() {
        return get(0);
    }

    @Override
    public int last0() {
        if (size == 0) {
            return -1;
        }
        if (last1() != size - 1) {
            return size - 1;
        }
        IntSet.IntIterator it = bitmap.descendingIterator();
        int lastpos = size - 1;
        while (it.hasNext()) {
            int pos = it.next();
            if (pos != lastpos) {
                break;
            }
            --lastpos;
        }
        return lastpos;
    }

    @Override
    public int last1() {
        if (bitmap.isEmpty()) {
            return -1;
        }
        return bitmap.last();
    }

    @Override
    public LTLBitmap.BitmapAdapter opNot() {
        WAHConciseBitmap bm = new WAHConciseBitmap(bitmap.complemented(), size);

        if (bitmap.last() < size - 1) {
            bm.bitmap.fill(bitmap.last() + 1, size - 1);
        }

        return bm;
    }

    @Override
    public LTLBitmap.BitmapAdapter opAnd(LTLBitmap.BitmapAdapter bm) {
        return new WAHConciseBitmap(bitmap.intersection(((WAHConciseBitmap)bm).bitmap),
                Math.max(size, bm.size())
                );
    }

    @Override
    public LTLBitmap.BitmapAdapter opOr(LTLBitmap.BitmapAdapter bm) {
        return new WAHConciseBitmap(bitmap.union(((WAHConciseBitmap)bm).bitmap),
                Math.max(size, bm.size())
        );
    }

    @Override
    public LTLBitmap.BitmapAdapter opXor(LTLBitmap.BitmapAdapter bm) {
        return new WAHConciseBitmap(bitmap.symmetricDifference(((WAHConciseBitmap)bm).bitmap),
                Math.max(size, bm.size())
        );
    }

    @Override
    public LTLBitmap.BitmapAdapter removeFirstBit() {
        return new WAHConciseBitmap(bitmap.shiftLeft1Bit(), size - 1);
    }

    @Override
    public LTLBitmap.BitmapAdapter clone() {
        return new WAHConciseBitmap(bitmap.clone(), size);
    }

    @Override
    public LTLBitmap.BitmapIterator begin() {
        return new Iterator();
    }

    @Override
    public LTLBitmap.BitmapIterator end() {
        return new Iterator(null, size, -1);
    }

    private class Iterator implements LTLBitmap.BitmapIterator {
        private int index;
        private IntSet.IntIterator itor;
        private boolean isEnd = false;
        private int next1Pos;
        private boolean currentBit;

        public Iterator() {
            if (size == 0) {
                isEnd = true;
                index = 0;
                return;
            }
            index = 0;
            if (bitmap.isEmpty()) {
                next1Pos = -1;
                itor = null;
                return;
            }

            itor = bitmap.iterator();
            next1();
            if (next1Pos == 0) {
                currentBit = true;
            } else {
                currentBit = false;
            }
        }

        private Iterator(IntSet.IntIterator it, int index, int next1Pos) {
            this.itor = it;
            this.index = index;
            if (index == size) {
                isEnd = true;
                return;
            }
            this.next1Pos = next1Pos;
            if (index == next1Pos) {
                currentBit = true;
            } else {
                currentBit = false;
            }
        }

        private void next1() {
            if (itor.hasNext()) {
                next1Pos = itor.next();
            } else {
                next1Pos = -1;
            }
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public void moveForward(int offset) {
            if (index + offset > size) {
                throw new IndexOutOfBoundsException();
            }
            if (index + offset == size) {
                isEnd = true;
                index = size;
                return;
            }
            int dest = index + offset;
            while (next1Pos != -1 && dest > next1Pos) {
                next1();
            }

            if (dest == next1Pos) {
                currentBit = true;
            } else {
                currentBit = false;
            }
            index = dest;
        }

        @Override
        public LTLBitmap.BitmapIterator find0() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (next1Pos == -1) {
                return new Iterator(null, index, -1);
            }

            Iterator newit = new Iterator(itor.clone(), index, next1Pos);
            if (newit.next1Pos == -1 || newit.index < newit.next1Pos) {
                return newit;
            }

            while (newit.next1Pos != -1 && newit.index == newit.next1Pos) {
                newit.next1();
                ++newit.index;
            }
            if (newit.index == size) {
                return null;
            }
            newit.currentBit = false;

            return newit;
        }

        @Override
        public LTLBitmap.BitmapIterator find1() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (next1Pos == -1) {
                return null;
            }

            if (index < next1Pos) {
                return new Iterator(itor.clone(), next1Pos, next1Pos);
            }

            Iterator newit = new Iterator(itor.clone(), index, next1Pos);
            if (newit.index == newit.next1Pos) {
                return newit;
            }

            newit.next1();
            if (newit.next1Pos == -1) {
                return null;
            }
            newit.index = newit.next1Pos;
            newit.currentBit = true;

            return newit;
        }

        @Override
        public boolean currentBit() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }
            return currentBit;
        }

        @Override
        public boolean isEnd() {
            return isEnd;
        }
    }
}
