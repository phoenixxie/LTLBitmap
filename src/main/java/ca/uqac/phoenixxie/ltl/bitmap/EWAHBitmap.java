package ca.uqac.phoenixxie.ltl.bitmap;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;

public class EWAHBitmap implements LTLBitmap.BitmapAdapter {
    private EWAHCompressedBitmap bitmap;

    public EWAHBitmap() {
        bitmap = new EWAHCompressedBitmap();
    }

    private EWAHBitmap(EWAHCompressedBitmap bm) {
        bitmap = bm;
    }

    @Override
    public void add(boolean bit) {
        if (bit) {
            bitmap.set(bitmap.sizeInBits());
        } else {
            bitmap.clear(bitmap.sizeInBits());
        }
    }

    @Override
    public void addMany(boolean bit, int count) {
        int fullwords = count / EWAHCompressedBitmap.WORD_IN_BITS;
        int left = count % EWAHCompressedBitmap.WORD_IN_BITS;
        bitmap.addStreamOfEmptyWords(bit, fullwords);
        for (int i = 0; i < left; ++i) {
            add(bit);
        }
    }

    @Override
    public int size() {
        return bitmap.sizeInBits();
    }

    @Override
    public boolean firstBit() {
        return bitmap.get(0);
    }

    @Override
    public boolean lastBit() {
        return bitmap.get(bitmap.sizeInBits() - 1);
    }

    public boolean get(int index) {
        if (index >= size() || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return bitmap.get(index);
    }

    @Override
    public LTLBitmap.BitmapAdapter opNot() {
        try {
            EWAHCompressedBitmap bm = bitmap.clone();
            bm.not();
            return new EWAHBitmap(bm);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public LTLBitmap.BitmapAdapter opAnd(LTLBitmap.BitmapAdapter bm) {
        return new EWAHBitmap(bitmap.and(((EWAHBitmap) bm).bitmap));
    }

    @Override
    public LTLBitmap.BitmapAdapter opOr(LTLBitmap.BitmapAdapter bm) {
        return new EWAHBitmap(bitmap.or(((EWAHBitmap) bm).bitmap));
    }

    @Override
    public LTLBitmap.BitmapAdapter opXor(LTLBitmap.BitmapAdapter bm) {
        return new EWAHBitmap(bitmap.xor(((EWAHBitmap) bm).bitmap));
    }

    @Override
    public LTLBitmap.BitmapAdapter removeFirstBit() {
        return null;
    }

    @Override
    public LTLBitmap.BitmapAdapter removeFromEnd(int len) {
        return null;
    }

    @Override
    public LTLBitmap.BitmapAdapter clone() {
        try {
            EWAHCompressedBitmap bm = bitmap.clone();
            return new EWAHBitmap(bm);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder answer = new StringBuilder();
        IntIterator i = bitmap.intIterator();
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
    public LTLBitmap.BitmapIterator begin() {
        return null;
    }

    @Override
    public LTLBitmap.BitmapIterator end() {
        return null;
    }

    class Iterator implements LTLBitmap.BitmapIterator {

        @Override
        public int index() {
            return 0;
        }

        @Override
        public void moveForward(int offset) {

        }

        @Override
        public LTLBitmap.BitmapIterator find0() {
            return null;
        }

        @Override
        public LTLBitmap.BitmapIterator rfind0() {
            return null;
        }

        @Override
        public LTLBitmap.BitmapIterator find1() {
            return null;
        }

        @Override
        public LTLBitmap.BitmapIterator rfind1() {
            return null;
        }

        @Override
        public boolean currentBit() {
            return false;
        }

        @Override
        public boolean isEnd() {
            return false;
        }
    }
}
