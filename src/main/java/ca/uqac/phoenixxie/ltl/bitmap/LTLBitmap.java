package ca.uqac.phoenixxie.ltl.bitmap;

import java.security.InvalidParameterException;

public class LTLBitmap {
    enum Type {
        RAW,
        JAVAEWAH
    }

    public interface BitmapIterator {
        int index();

        void moveForward(int offset);

        BitmapIterator find0();
        BitmapIterator rfind0();

        BitmapIterator find1();
        BitmapIterator rfind1();

        boolean currentBit();

        boolean isEnd();
    }

    public interface BitmapAdapter {
        void add(boolean bit);
        void addMany(boolean bit, int count);
        int getCapacity();
        int size();
        boolean firstBit();
        boolean lastBit();
        void clear(boolean bit);
        void opNot();
        void opAnd(BitmapAdapter bm);
        void opOr(BitmapAdapter bm);
        void opXor(BitmapAdapter bm);
        void opShiftLeft1Bit();

        BitmapIterator begin();
        BitmapIterator end();
        String toString();
    }

    private Type type;
    private BitmapAdapter bitmap;

    public LTLBitmap(Type type) {
        this.type = type;
        this.bitmap = createAdapter(type);
    }

    private static BitmapAdapter createAdapter(Type type) {
        switch (type) {
            case RAW:
                return new RawBitmap();
            case JAVAEWAH:
                break;
        }
        throw new InvalidParameterException();
    }

    public void add(boolean bit) {
        bitmap.add(bit);
    }

    public void add(String in) {
        for (char c : in.toCharArray()) {
            if (c == '0') {
                add(false);
            } else if (c == '1') {
                add(true);
            }
        }
    }

    @Override
    public String toString() {
        return bitmap.toString();
    }

    public void clear() {
        this.bitmap = createAdapter(type);
    }

    public int getCapacity() {
        return bitmap.getCapacity();
    }

    public void clear(boolean bit) {
        bitmap.clear(bit);
    }

    private void makeSameSize(LTLBitmap rightBm) {
        int diff = bitmap.size() - rightBm.bitmap.size();
        if (diff > 0) {
            rightBm.bitmap.addMany(false, diff);
        } else if (diff < 0) {
            bitmap.addMany(false, -diff);
        }
        assert(bitmap.size() == rightBm.bitmap.size());
    }

    public void opNot() {
        bitmap.opNot();
    }

    public void opAnd(LTLBitmap bm) {
        makeSameSize(bm);
        bitmap.opAnd(bm.bitmap);
    }

    public void opOr(LTLBitmap bm) {
        makeSameSize(bm);
        bitmap.opOr(bm.bitmap);
    }

    public void opThen(LTLBitmap bm) {
        makeSameSize(bm);
        bitmap.opNot();
        bitmap.opOr(bm.bitmap);
    }

    public void opNext() {
        bitmap.opShiftLeft1Bit();
    }

    public void opGlobal() {
        BitmapIterator itor = bitmap.end();
        BitmapIterator itNext = itor.rfind0();
        if (itNext == null) {
            return;
        }

        BitmapAdapter newBm = createAdapter(type);
        newBm.addMany(false, itNext.index() + 1);
        newBm.addMany(true, bitmap.size() - itNext.index() - 1);
        bitmap = newBm;
    }

    public void opFuture() {
        BitmapIterator itor = bitmap.end();
        BitmapIterator pos = itor.rfind1();
        if (pos == null) {
            return;
        }

        BitmapAdapter newBm = createAdapter(type);
        newBm.addMany(true, pos.index() + 1);
        newBm.addMany(false, bitmap.size() - pos.index() - 1);
        bitmap = newBm;
    }

    public void opUntil(LTLBitmap rightBm) {
        if (type != rightBm.type) {
            throw new InvalidParameterException();
        }
        makeSameSize(rightBm);

        BitmapIterator ita = bitmap.begin();
        BitmapIterator itb = rightBm.bitmap.begin();
        BitmapAdapter newBm = createAdapter(type);

        while (!ita.isEnd() && !itb.isEnd()) {
            int off;
            BitmapIterator ita1 = ita.find1();
            if (ita1 == null) {
                break;
            }
            if (ita1.index() > ita.index()) {
                off = ita1.index() - ita.index();
                newBm.addMany(false, off);
                ita = ita1;
                itb.moveForward(off);
            }

            BitmapIterator ita0 = ita.find0();
            if (ita0 == null) {
                ita0 = bitmap.end();
            }

            BitmapIterator itb1 = itb.find1();
            if (itb1 == null) {
                break;
            }

            if (itb1.index() > ita0.index()) {
                off = ita0.index() - ita.index();
                newBm.addMany(false, off);
                ita = ita0;
                itb.moveForward(off);
                continue;
            }

            BitmapIterator itb0 = itb1.find0();
            if (itb0 == null) {
                itb0 = rightBm.bitmap.end();
            }

            off = itb0.index() - itb.index();
            newBm.addMany(true, off);
            ita.moveForward(off);
            itb = itb0;
        }

        if (!ita.isEnd()) {
            newBm.addMany(false, bitmap.size() - ita.index());
        }

        assert(newBm.size() == bitmap.size());

        bitmap = newBm;
    }

    public void opWeakUntil(LTLBitmap rightBm) {
        if (type != rightBm.type) {
            throw new InvalidParameterException();
        }
        makeSameSize(rightBm);

        BitmapIterator ita = bitmap.begin();
        BitmapIterator itb = rightBm.bitmap.begin();
        BitmapAdapter newBm = createAdapter(type);

        while (!ita.isEnd() && !itb.isEnd()) {
            int off;
            BitmapIterator ita1 = ita.find1();
            if (ita1 == null) {
                break;
            }
            if (ita1.index() > ita.index()) {
                off = ita1.index() - ita.index();
                newBm.addMany(false, off);
                ita = ita1;
                itb.moveForward(off);
            }

            BitmapIterator ita0 = ita.find0();
            if (ita0 == null) {
                ita0 = bitmap.end();
            }

            off = ita0.index() - ita.index();
            newBm.addMany(true, off);
            ita = ita0;
            itb.moveForward(off);
            if (ita.isEnd()) {
                break;
            }
            if (itb.currentBit() == false) {
                continue;
            }

            BitmapIterator itb0 = itb.find0();
            if (itb0 == null) {
                itb0 = rightBm.bitmap.end();
            }

            off = itb0.index() - itb.index();
            newBm.addMany(true, off);
            ita.moveForward(off);
            itb = itb0;
        }

        if (!ita.isEnd()) {
            newBm.addMany(false, bitmap.size() - ita.index());
        }

        assert(newBm.size() == bitmap.size());

        bitmap = newBm;
    }

    public void opRelease(LTLBitmap rightBm) {
        if (type != rightBm.type) {
            throw new InvalidParameterException();
        }
        makeSameSize(rightBm);

        BitmapIterator ita = bitmap.begin();
        BitmapIterator itb = rightBm.bitmap.begin();
        BitmapAdapter newBm = createAdapter(type);

        while (!ita.isEnd() && !itb.isEnd()) {
            int off;
            BitmapIterator itb1 = itb.find1();
            if (itb1 == null) {
                break;
            }
            if (itb1.index() > itb.index()) {
                off = itb1.index() - itb.index();
                newBm.addMany(false, off);
                ita.moveForward(off);
                itb = itb1;
            }

            BitmapIterator itb0 = itb.find0();
            if (itb0 == null) {
                itb0 = rightBm.bitmap.end();
            }

            off = itb0.index() - itb.index();
            newBm.addMany(true, off);
            ita.moveForward(off - 1);
            itb = itb0;
            if (itb.isEnd()) {
                ita.moveForward(1);
                break;
            }
            if (ita.currentBit() == false) {
                ita.moveForward(1);
                continue;
            }

            BitmapIterator ita0 = ita.find0();
            if (ita0 == null) {
                ita0 = bitmap.end();
            }

            off = ita0.index() - ita.index();
            newBm.addMany(true, off - 1);
            ita = ita0;
            itb.moveForward(off - 1);
        }

        if (!ita.isEnd()) {
            newBm.addMany(false, bitmap.size() - ita.index());
        }

        assert(newBm.size() == bitmap.size());

        bitmap = newBm;
    }
}
