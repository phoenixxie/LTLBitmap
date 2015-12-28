package ca.uqac.phoenixxie.ltl.bitmap;

public class Bitmap {
    public interface BitmapIterator {
        int getPosition();

        boolean gotoNext(boolean val);

        boolean gotoPrev(boolean val);
    }

    public interface BitmapAdapter {
        void add(boolean bit);
        int getCapacity();
        int getSize();
        boolean firstBit();
        boolean lastBit();
        void clear(boolean bit);
        void opNot();
        void opAnd(BitmapAdapter bm);
        void opOr(BitmapAdapter bm);
        void opXor(BitmapAdapter bm);
        void opShiftLeft1Bit();

        BitmapIterator fromStart();
        BitmapIterator fromEnd();
    }

    private BitmapAdapter adapter;

    public Bitmap(BitmapAdapter adapter) {
        this.adapter = adapter;
    }

    public void add(boolean bit) {
        adapter.add(bit);
    }

    public int getCapacity() {
        return adapter.getCapacity();
    }

    public void clear(boolean bit) {
        adapter.clear(bit);
    }

    public void opNot() {
        adapter.opNot();
    }

    public void opAnd(BitmapAdapter bm) {
        adapter.opAnd(bm);
    }

    public void opOr(BitmapAdapter bm) {
        adapter.opOr(bm);
    }

    public void opXor(BitmapAdapter bm) {
        adapter.opXor(bm);
    }

    public void opThen(BitmapAdapter bm) {
        adapter.opNot();
        adapter.opOr(bm);
    }

    // shift left 1 bit and add a bit whose value equals to the last bit
    void opNext() {
        adapter.opShiftLeft1Bit();
        adapter.add(adapter.lastBit());
    }

    void opGlobal() {

    }

    void opFuture() {

    }

    void opUntil() {

    }

    void opRelease() {

    }

    void opWeakUntil() {

    }
}
