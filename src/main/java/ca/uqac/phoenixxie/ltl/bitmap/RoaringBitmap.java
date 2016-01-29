package ca.uqac.phoenixxie.ltl.bitmap;

import org.roaringbitmap.IntIterator;

public class RoaringBitmap implements LTLBitmap.BitmapAdapter {
    private org.roaringbitmap.RoaringBitmap bitmap;
    private int size;

    public RoaringBitmap() {
        bitmap = new org.roaringbitmap.RoaringBitmap();
        size = 0;
    }

    private RoaringBitmap(org.roaringbitmap.RoaringBitmap bm, int size) {
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
        if (bit) {
            for (int i = size; i < size + count; ++i) {
                bitmap.add(i);
            }
        }
        size += count;
    }

    @Override
    public boolean get(int position) {
        return bitmap.contains(position);
    }

    @Override
    public String toString() {
        StringBuilder answer = new StringBuilder();
        IntIterator i = bitmap.getIntIterator();
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
    public int size() {
        return size;
    }

    @Override
    public int getRealSize() {
        return bitmap.getSizeInBytes();
    }

    @Override
    public boolean firstBit() {
        return get(0);
    }

    @Override
    public int cardinality() {
        return bitmap.getCardinality();
    }

    @Override
    public int last0() {
        IntIterator i = bitmap.getReverseIntIterator();
        int lastpos = size - 1;
        while (i.hasNext()) {
            int pos = i.next();
            if (pos != lastpos) {
                break;
            }
            --lastpos;
        }
        return lastpos;
    }

    @Override
    public int last1() {
        IntIterator i = bitmap.getReverseIntIterator();
        if (i.hasNext()) {
            return i.next();
        } else {
            return -1;
        }
    }

    @Override
    public LTLBitmap.BitmapAdapter opNot() {
        org.roaringbitmap.RoaringBitmap bm = bitmap.clone();
        bm.flip(0, size);
        return new RoaringBitmap(bm, size);
    }

    @Override
    public LTLBitmap.BitmapAdapter opAnd(LTLBitmap.BitmapAdapter bm) {
        org.roaringbitmap.RoaringBitmap left = bitmap.clone();
        left.and(((RoaringBitmap) bm).bitmap);
        return new RoaringBitmap(left, Math.max(size, bm.size()));
    }

    @Override
    public LTLBitmap.BitmapAdapter opOr(LTLBitmap.BitmapAdapter bm) {
        org.roaringbitmap.RoaringBitmap r = org.roaringbitmap.RoaringBitmap.or(bitmap,
                ((RoaringBitmap) bm).bitmap);
        return new RoaringBitmap(r, Math.max(size, bm.size()));
    }

    @Override
    public LTLBitmap.BitmapAdapter opXor(LTLBitmap.BitmapAdapter bm) {
        org.roaringbitmap.RoaringBitmap r = org.roaringbitmap.RoaringBitmap.xor(bitmap,
                ((RoaringBitmap) bm).bitmap);
        return new RoaringBitmap(r, Math.max(size, bm.size()));
    }

    @Override
    public LTLBitmap.BitmapAdapter removeFirstBit() {
        org.roaringbitmap.RoaringBitmap bm = new org.roaringbitmap.RoaringBitmap();
        if (size == 0) {
            return new RoaringBitmap(bm, 0);
        }

        IntIterator i = bitmap.getIntIterator();
        while (i.hasNext()) {
            int pos = i.next();
            if (pos == 0) {
                continue;
            }
            bm.add(pos - 1);
        }

        return new RoaringBitmap(bm, size - 1);
    }

    @Override
    public LTLBitmap.BitmapAdapter clone() {
        org.roaringbitmap.RoaringBitmap bm = bitmap.clone();
        return new RoaringBitmap(bm, size);
    }

    @Override
    public LTLBitmap.BitmapIterator begin() {
        return new Iterator();
    }

    @Override
    public LTLBitmap.BitmapIterator end() {
        return new Iterator(size);
    }

    public class Iterator implements LTLBitmap.BitmapIterator {
        private int index = 0;
        private boolean isEnd = false;

        public Iterator() {
            if (size == 0) {
                isEnd = true;
            }
        }

        public Iterator(int index) {
            this.index = index;
            if (index == size) {
                isEnd = true;
            }
            if (index > size) {
                throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public void moveForward(int offset) {
            if (offset <= 0) {
                return;
            }

            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (index + offset > size) {
                throw new IndexOutOfBoundsException();
            }

            if (index + offset == size) {
                index = size;
                isEnd = true;
                return;
            }
            index += offset;
        }

        @Override
        public LTLBitmap.BitmapIterator find0() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }
            for (int i = index; i < size; ++i) {
                if (get(i) == false) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        @Override
        public LTLBitmap.BitmapIterator find1() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }
            for (int i = index; i < size; ++i) {
                if (get(i) == true) {
                    return new Iterator(i);
                }
            }
            return null;        }

        @Override
        public boolean currentBit() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }
            return get(index);
        }

        @Override
        public boolean isEnd() {
            return isEnd;
        }
    }
}
