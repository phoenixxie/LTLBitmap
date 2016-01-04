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

    public interface BitmapAdapter extends Cloneable {
        void add(boolean bit);

        void addMany(boolean bit, int count);

        int getCapacity();

        int size();

        boolean firstBit();

        boolean lastBit();

        void clear(boolean bit);

        BitmapAdapter opNot();

        BitmapAdapter opAnd(BitmapAdapter bm);

        BitmapAdapter opOr(BitmapAdapter bm);

        BitmapAdapter opXor(BitmapAdapter bm);

        BitmapAdapter removeFirstBit();

        BitmapAdapter removeFromEnd(int len);

        BitmapAdapter clone();

        BitmapIterator begin();

        BitmapIterator end();

        String toString();
    }

    private final Type type;
    private final BitmapAdapter bitmap;

    public LTLBitmap(Type type) {
        this.type = type;
        this.bitmap = createAdapter(type);
    }

    private LTLBitmap(Type type, BitmapAdapter bm) {
        this.type = type;
        this.bitmap = bm;
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

    public int getCapacity() {
        return bitmap.getCapacity();
    }

    public void clear(boolean bit) {
        bitmap.clear(bit);
    }

    public LTLBitmap opNot() {
        return new LTLBitmap(type, bitmap.opNot());
    }

    public LTLBitmap opAnd(LTLBitmap bm) {
        BitmapAdapter left = this.bitmap;
        BitmapAdapter right = bm.bitmap;
        int diff = left.size() - right.size();
        if (diff > 0) {
           left = left.removeFromEnd(diff);
        } else if (diff < 0) {
            right = right.removeFromEnd(-diff);
        }

        return new LTLBitmap(type, left.opAnd(right));
    }

    public LTLBitmap opOr(LTLBitmap bm) {
        BitmapAdapter left = this.bitmap;
        BitmapAdapter right = bm.bitmap;
        int diff = left.size() - right.size();
        if (diff > 0) {
            left = left.removeFromEnd(diff);
        } else if (diff < 0) {
            right = right.removeFromEnd(-diff);
        }

        return new LTLBitmap(type, left.opOr(right));
    }

    public LTLBitmap opThen(LTLBitmap bm) {
        BitmapAdapter left = this.bitmap;
        BitmapAdapter right = bm.bitmap;
        int diff = left.size() - right.size();
        if (diff > 0) {
            left = left.removeFromEnd(diff);
        } else if (diff < 0) {
            right = right.removeFromEnd(-diff);
        }
        left = left.opNot();
        return new LTLBitmap(type, left.opOr(right));
    }

    public LTLBitmap opNext() {
        return new LTLBitmap(type, bitmap.removeFirstBit());
    }

    public LTLBitmap opGlobal() {
        BitmapIterator itor = bitmap.end();
        BitmapIterator itNext = itor.rfind0();
        if (itNext == null) {
            return new LTLBitmap(type, bitmap.clone());
        }

        BitmapAdapter newBm = createAdapter(type);
        newBm.addMany(false, itNext.index() + 1);
        newBm.addMany(true, bitmap.size() - itNext.index() - 1);
        return new LTLBitmap(type, newBm);
    }

    public LTLBitmap opFuture() {
        BitmapIterator itor = bitmap.end();
        BitmapIterator pos = itor.rfind1();
        if (pos == null) {
            return new LTLBitmap(type, bitmap.clone());
        }

        BitmapAdapter newBm = createAdapter(type);
        newBm.addMany(true, pos.index() + 1);
        newBm.addMany(false, bitmap.size() - pos.index() - 1);
        return new LTLBitmap(type, newBm);
    }

    public LTLBitmap opUntil(LTLBitmap rightBm) {
        if (type != rightBm.type) {
            throw new InvalidParameterException();
        }
        BitmapAdapter left = this.bitmap;
        BitmapAdapter right = rightBm.bitmap;
        int diff = left.size() - right.size();
        if (diff > 0) {
            left = left.removeFromEnd(diff);
        } else if (diff < 0) {
            right = right.removeFromEnd(-diff);
        }

        BitmapIterator ita = left.begin();
        BitmapIterator itb = right.begin();
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
                ita0 = left.end();
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
                itb0 = right.end();
            }

            off = itb0.index() - itb.index();
            newBm.addMany(true, off);
            ita.moveForward(off);
            itb = itb0;
        }

        if (!ita.isEnd()) {
            newBm.addMany(false, left.size() - ita.index());
        }

        assert (newBm.size() == left.size());
        return new LTLBitmap(type, newBm);
    }

    public LTLBitmap opWeakUntil(LTLBitmap rightBm) {
        if (type != rightBm.type) {
            throw new InvalidParameterException();
        }
        BitmapAdapter left = this.bitmap;
        BitmapAdapter right = rightBm.bitmap;
        int diff = left.size() - right.size();
        if (diff > 0) {
            left = left.removeFromEnd(diff);
        } else if (diff < 0) {
            right = right.removeFromEnd(-diff);
        }

        BitmapIterator ita = left.begin();
        BitmapIterator itb = right.begin();
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
                break;
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
                itb0 = right.end();
            }

            off = itb0.index() - itb.index();
            newBm.addMany(true, off);
            ita.moveForward(off);
            itb = itb0;
        }

        if (ita.isEnd()) {
            return new LTLBitmap(type, newBm);
        }

        BitmapIterator it0 = left.end().rfind0();
        if (it0 == null || it0.index() < ita.index()) {
            newBm.addMany(true, left.size() - ita.index());
        } else {
            newBm.addMany(false, it0.index() - ita.index() + 1);
            newBm.addMany(true, left.size() - newBm.size());
        }

        assert (newBm.size() == left.size());
        return new LTLBitmap(type, newBm);
    }


    public LTLBitmap opRelease(LTLBitmap rightBm) {
        if (type != rightBm.type) {
            throw new InvalidParameterException();
        }
        BitmapAdapter left = this.bitmap;
        BitmapAdapter right = rightBm.bitmap;
        int diff = left.size() - right.size();
        if (diff > 0) {
            left = left.removeFromEnd(diff);
        } else if (diff < 0) {
            right = right.removeFromEnd(-diff);
        }

        BitmapIterator ita = left.begin();
        BitmapIterator itb = right.begin();
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
                itb0 = right.end();
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
                ita0 = left.end();
            }

            off = ita0.index() - ita.index();
            newBm.addMany(true, off - 1);
            ita = ita0;
            itb.moveForward(off - 1);
        }

        if (!ita.isEnd()) {
            newBm.addMany(false, left.size() - ita.index());
        }

        assert (newBm.size() == left.size());
        return new LTLBitmap(type, newBm);
    }
}
