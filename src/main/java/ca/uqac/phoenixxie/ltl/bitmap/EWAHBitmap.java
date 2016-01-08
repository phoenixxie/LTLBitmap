package ca.uqac.phoenixxie.ltl.bitmap;

import com.googlecode.javaewah.Buffer;
import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;
import com.googlecode.javaewah.RunningLengthWord;

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
        if (count <= 0) {
            return;
        }
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
    public int last0() {
        return bitmap.findLast0();
    }

    @Override
    public int last1() {
        return bitmap.findLast1();
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
        return new EWAHBitmap(bitmap.removeFirstBit());
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
        return new Iterator();
    }

    @Override
    public LTLBitmap.BitmapIterator end() {
        return new Iterator('X');
    }

    public class Iterator implements LTLBitmap.BitmapIterator {
        private final Buffer buffer = bitmap.getBuffer();
        private final int sizeInWords = buffer.sizeInWords();

        private int index = 0;

        private boolean inClean = true;
        private int pointerInWords = 0;
        private int currMarkerStartPos = 0;
        private boolean isEnd = false;

        private long currLiteralWord;
        private int pointerInDirtyWord = 0;
        private int pointerDirtyWord = 0;
        private int pointerInRunningLength = 0;

        private long currRunningLength;
        private boolean currRunningBit;
        private int currNumberOfDirtyWords;
        private int currMarkerMaxBits;
        private int currRunningLengthMaxBits;
        private int currDirtyWordsMaxBits;

        public Iterator() {
            if (bitmap.sizeInBits() == 0) {
                isEnd = true;
                return;
            }

            updateMarkerInfo();
            updatePointerInThisMarker(0);
        }

        Iterator(char ignore) {
            isEnd = true;
            index = bitmap.sizeInBits();
        }

        private Iterator(int index, int pointerInWords, int currMarkerStartPos) {
            if (bitmap.sizeInBits() == 0) {
                isEnd = true;
                return;
            }

            this.index = index;
            this.pointerInWords = pointerInWords;
            this.currMarkerStartPos = currMarkerStartPos;

            updateMarkerInfo();
            updatePointerInThisMarker(this.index - currMarkerStartPos);
        }

        @Override
        public int index() {
            return index;
        }

        private void updateMarkerInfo() {
            currRunningLength = RunningLengthWord.getRunningLength(this.buffer, pointerInWords);
            currRunningBit = RunningLengthWord.getRunningBit(this.buffer, pointerInWords);
            currNumberOfDirtyWords = RunningLengthWord.getNumberOfLiteralWords(this.buffer, pointerInWords);
            currMarkerMaxBits = EWAHCompressedBitmap.WORD_IN_BITS * ((int) currRunningLength + currNumberOfDirtyWords);
            currRunningLengthMaxBits = EWAHCompressedBitmap.WORD_IN_BITS * (int) currRunningLength;
            currDirtyWordsMaxBits = EWAHCompressedBitmap.WORD_IN_BITS * currNumberOfDirtyWords;

            assert currRunningLength > 0 || currNumberOfDirtyWords > 0;

        }

        private void updatePointerInThisMarker(int pos) {
            if (pos >= currMarkerMaxBits) {
                throw new IndexOutOfBoundsException();
            }

            if (pos < currRunningLengthMaxBits) {
                inClean = true;
                pointerInRunningLength = pos;
            } else {
                pos -= currRunningLengthMaxBits;
                inClean = false;
                pointerDirtyWord = pos / EWAHCompressedBitmap.WORD_IN_BITS;
                currLiteralWord = buffer.getWord(pointerInWords + 1 + pointerDirtyWord);
                pointerInDirtyWord = pos % EWAHCompressedBitmap.WORD_IN_BITS;
            }
        }

        private void moveForwardOneMarker() {
            if (pointerInWords >= sizeInWords) {
                throw new IndexOutOfBoundsException();
            }

            pointerInWords += 1 + currNumberOfDirtyWords;
            currMarkerStartPos += currMarkerMaxBits;

            if (pointerInWords == sizeInWords) {
                isEnd = true;
                index = currMarkerStartPos;
                return;
            }

            updateMarkerInfo();
        }

        @Override
        public void moveForward(int offset) {
            if (offset <= 0) {
                return;
            }

            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (index + offset > bitmap.sizeInBits()) {
                throw new IndexOutOfBoundsException();
            }

            if (index + offset == bitmap.sizeInBits()) {
                index = bitmap.sizeInBits();
                isEnd = true;
                return;
            }

            index += offset;
            while (currMarkerStartPos + currMarkerMaxBits < index) {
                moveForwardOneMarker();
            }
            updatePointerInThisMarker(index - currMarkerStartPos);
        }

        @Override
        public LTLBitmap.BitmapIterator find0() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            final long full = ~0L;

            Iterator itor = new Iterator(index, pointerInWords, currMarkerStartPos);
            while (itor.pointerInWords < itor.sizeInWords) {
                if (itor.inClean) {
                    if (itor.currRunningBit == false) {
                        return itor;
                    }
                    itor.updatePointerInThisMarker(itor.currRunningLengthMaxBits);
                }

                int startbits = itor.currMarkerStartPos + itor.currRunningLengthMaxBits;
                for (int i = itor.pointerDirtyWord;
                     i < itor.currNumberOfDirtyWords;
                     ++i, itor.pointerInDirtyWord = 0, startbits += EWAHCompressedBitmap.WORD_IN_BITS
                        ) {

                    long w = itor.buffer.getWord(itor.pointerInWords + 1 + i);
                    int leftbits = Math.min(EWAHCompressedBitmap.WORD_IN_BITS, bitmap.sizeInBits() - startbits);
                    long mask = full;
                    if (leftbits != EWAHCompressedBitmap.WORD_IN_BITS) {
                        mask = (1L << leftbits) - 1;
                    }
                    mask = (mask >>> itor.pointerInDirtyWord) << itor.pointerInDirtyWord;
                    if ((w & mask) == mask) {
                        continue;
                    }

                    mask = 1L << itor.pointerInDirtyWord;
                    for (int j = itor.pointerInDirtyWord; j < leftbits; ++j) {
                        if ((w & mask) == 0) {
                            itor.index = startbits + j;
                            itor.updatePointerInThisMarker(itor.index - itor.currMarkerStartPos);
                            return itor;
                        }
                        mask <<= 1L;
                    }
                }
                itor.moveForwardOneMarker();
                if (itor.isEnd) {
                    return null;
                }
                itor.index = itor.currMarkerStartPos;
                itor.updatePointerInThisMarker(0);
            }

            return null;
        }

        @Override
        public LTLBitmap.BitmapIterator find1() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            Iterator itor = new Iterator(index, pointerInWords, currMarkerStartPos);
            while (itor.pointerInWords < itor.sizeInWords) {
                if (itor.inClean) {
                    if (itor.currRunningBit == true) {
                        return itor;
                    }
                    // switch to dirty words
                    if (itor.currNumberOfDirtyWords > 0) {
                        itor.updatePointerInThisMarker(itor.currRunningLengthMaxBits);
                    }
                }

                int startbits = itor.currMarkerStartPos + itor.currRunningLengthMaxBits;
                for (int i = itor.pointerDirtyWord;
                     i < itor.currNumberOfDirtyWords;
                     ++i, itor.pointerInDirtyWord = 0, startbits += EWAHCompressedBitmap.WORD_IN_BITS
                        ) {

                    long w = itor.buffer.getWord(itor.pointerInWords + 1 + i);
                    int leftbits = Math.min(EWAHCompressedBitmap.WORD_IN_BITS, bitmap.sizeInBits() - startbits);

                    long mask = 0L;
                    if (leftbits != EWAHCompressedBitmap.WORD_IN_BITS) {
                        mask = ~((1L << leftbits) - 1);
                    }
                    if (itor.pointerInDirtyWord > 0) {
                        mask |= (1 << itor.pointerInDirtyWord) - 1;
                    }
                    if ((w | mask) == mask) {
                        continue;
                    }

                    mask = 1L << itor.pointerInDirtyWord;
                    for (int j = pointerInDirtyWord; j < leftbits; ++j) {
                        if ((w & mask) == mask) {
                            itor.index = startbits + j;
                            itor.updatePointerInThisMarker(itor.index - itor.currMarkerStartPos);
                            return itor;
                        }
                        mask <<= 1L;
                    }
                }
                itor.moveForwardOneMarker();
                if (itor.isEnd) {
                    return null;
                }
                itor.index = itor.currMarkerStartPos;
                itor.updatePointerInThisMarker(0);
            }

            return null;
        }

        @Override
        public boolean currentBit() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (inClean) {
                return currRunningBit;
            } else {
                long mask = 1L << pointerInDirtyWord;
                return ((mask & currLiteralWord) == mask);
            }
        }

        @Override
        public boolean isEnd() {
            return isEnd;
        }
    }
}
