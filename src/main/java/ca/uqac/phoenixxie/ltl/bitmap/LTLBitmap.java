package ca.uqac.phoenixxie.ltl.bitmap;

import java.security.InvalidParameterException;

public class LTLBitmap {
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

    public LTLBitmap opNot() {
        return new LTLBitmap(type, bitmap.opNot());
    }

    public LTLBitmap opAnd(LTLBitmap bm) {
        return new LTLBitmap(type, bitmap.opAnd(bm.bitmap));
    }

    public LTLBitmap opOr(LTLBitmap bm) {
        return new LTLBitmap(type, bitmap.opOr(bm.bitmap));
    }

    public LTLBitmap opThen(LTLBitmap bm) {
        BitmapAdapter left = bitmap.opNot();
        return new LTLBitmap(type, left.opOr(bm.bitmap));
    }

    public LTLBitmap opNext() {
        return new LTLBitmap(type, bitmap.removeFirstBit());
    }

    public LTLBitmap opGlobal() {
        int last0 = bitmap.last0();
        if (last0 == -1) {
            return new LTLBitmap(type, bitmap.clone());
        }

        BitmapAdapter newBm = createAdapter(type);
        newBm.addMany(false, last0 + 1);
        newBm.addMany(true, bitmap.size() - last0 - 1);
        return new LTLBitmap(type, newBm);
    }

    public LTLBitmap opFuture() {
        int last1 = bitmap.last1();
        if (last1 == -1) {
            return new LTLBitmap(type, bitmap.clone());
        }

        BitmapAdapter newBm = createAdapter(type);
        newBm.addMany(true, last1 + 1);
        newBm.addMany(false, bitmap.size() - last1 - 1);
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

        int last0 = left.last0();
        if (last0 == -1 || last0 < ita.index()) {
            newBm.addMany(true, left.size() - ita.index());
        } else {
            newBm.addMany(false, last0 - ita.index() + 1);
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
                itb = itb1;
                ita.moveForward(off);
            }

            BitmapIterator itb0 = itb.find0();
            if (itb0 == null) {
                break;
            }

            BitmapIterator ita1 = ita.find1();
            if (ita1 == null) {
                break;
            }

            if (ita1.index() >= itb0.index()) {
                off = itb0.index() - itb.index();
                newBm.addMany(false, off);
                itb = itb0;
                ita.moveForward(off);
                continue;
            }

            BitmapIterator ita0 = ita1.find0();
            if (ita0 == null) {
                ita0 = left.end();
            }

            off = ita0.index() - ita.index();
            newBm.addMany(true, off);
            itb.moveForward(off);
            ita = ita0;
        }

        if (itb.isEnd()) {
            return new LTLBitmap(type, newBm);
        }

        int last0 = right.last0();
        if (last0 == -1 || last0 < itb.index()) {
            newBm.addMany(true, right.size() - itb.index());
        } else {
            newBm.addMany(false, last0 - itb.index() + 1);
            newBm.addMany(true, right.size() - newBm.size());
        }

        assert (newBm.size() == right.size());
        return new LTLBitmap(type, newBm);
    }

    enum Type {
        RAW,
        JAVAEWAH
    }

    public interface BitmapIterator {
        int index();

        void moveForward(int offset);

        BitmapIterator find0();

        BitmapIterator find1();

        boolean currentBit();

        boolean isEnd();
    }


    public interface BitmapAdapter extends Cloneable {
        void add(boolean bit);

        void addMany(boolean bit, int count);

        int size();

        boolean firstBit();

        int last0();

        int last1();

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
}
