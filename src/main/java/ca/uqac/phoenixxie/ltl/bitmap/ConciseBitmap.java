package ca.uqac.phoenixxie.ltl.bitmap;

import it.uniroma3.mat.extendedset.intset.ConciseSet;

public class ConciseBitmap implements LTLBitmap.BitmapAdapter {
    private ConciseSet bitmap;

    public ConciseBitmap() {
        bitmap = new ConciseSet();
    }

    @Override
    public void add(boolean bit) {

    }

    @Override
    public void addMany(boolean bit, int count) {

    }

    @Override
    public boolean get(int position) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean firstBit() {
        return false;
    }

    @Override
    public int last0() {
        return 0;
    }

    @Override
    public int last1() {
        return 0;
    }

    @Override
    public LTLBitmap.BitmapAdapter opNot() {
        return null;
    }

    @Override
    public LTLBitmap.BitmapAdapter opAnd(LTLBitmap.BitmapAdapter bm) {
        return null;
    }

    @Override
    public LTLBitmap.BitmapAdapter opOr(LTLBitmap.BitmapAdapter bm) {
        return null;
    }

    @Override
    public LTLBitmap.BitmapAdapter opXor(LTLBitmap.BitmapAdapter bm) {
        return null;
    }

    @Override
    public LTLBitmap.BitmapAdapter removeFirstBit() {
        return null;
    }

    @Override
    public LTLBitmap.BitmapAdapter clone() {
        return null;
    }

    @Override
    public LTLBitmap.BitmapIterator begin() {
        return null;
    }

    @Override
    public LTLBitmap.BitmapIterator end() {
        return null;
    }
}
