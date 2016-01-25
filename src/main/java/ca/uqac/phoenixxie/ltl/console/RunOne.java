package ca.uqac.phoenixxie.ltl.console;

import ca.uqac.phoenixxie.ltl.analyze.Formula;
import ca.uqac.phoenixxie.ltl.analyze.FormulaParser;
import ca.uqac.phoenixxie.ltl.analyze.State;
import ca.uqac.phoenixxie.ltl.analyze.StateParser;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;
import ca.uqac.phoenixxie.ltl.bitmap.RoaringBitmap;
import com.googlecode.javaewah.EWAHCompressedBitmap;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;

public class RunOne {

    public static void main(String[] args) {
        Option option;
        try {
            option = Option.parse(args);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        } catch (Exception e) {
            Option.usage();
            e.printStackTrace();

            System.exit(1);
            return;
        }

        System.out.println("State file: " + option.states.getPath());
        System.out.println("Formula file: " + option.formulas.getPath());
        System.out.println("Event file: " + option.events.getPath());
        System.out.println("Bitmap type: " + option.bmtype.toString());

        ArrayList<State> states = new ArrayList<>();
        HashSet<String> variables = new HashSet<>();

        try {
            FileInputStream fstream = new FileInputStream(option.states);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            while ((strLine = br.readLine()) != null) {
                State state = StateParser.parse(strLine);
                if (!state.isSuccess()) {
                    System.err.println("Parse state error: " + strLine);
                    System.err.println(state.getErrorMsg());
                    System.exit(1);
                }
                variables.addAll(state.getVariables().keySet());
                states.add(state);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Got " + states.size() + " states including " + variables.size() + " variables");

        ArrayList<Formula> formulas = new ArrayList<>();
        int maxStateID = -1;
        try {
            FileInputStream fstream = new FileInputStream(option.formulas);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            while ((strLine = br.readLine()) != null) {
                Formula formula = FormulaParser.parse(strLine);
                if (!formula.isSuccess()) {
                    System.err.println("Parse formula error: " + strLine);
                    System.err.println(formula.getErrorMsg());
                    System.exit(1);
                }
                maxStateID = (maxStateID < formula.getMaxStateID() ? formula.getMaxStateID() : maxStateID);
                formulas.add(formula);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (maxStateID >= states.size()) {
            System.err.println("Unmatched numbers of states in file and states in formulas: " + maxStateID + ">=" + states.size());
            System.exit(1);
        }
        System.out.println("Got " + formulas.size() + " formulas");

        HashMap<String, Integer> vars = new HashMap<>();
        org.roaringbitmap.RoaringBitmap[] bms = new org.roaringbitmap.RoaringBitmap[states.size()];
        LTLBitmap[] bitmaps = new LTLBitmap[states.size()];
        for (int i = 0; i < bitmaps.length; ++i) {
            bitmaps[i] = new LTLBitmap(option.bmtype);
            bms[i] = new org.roaringbitmap.RoaringBitmap();
        }

        int linecnt = 0;
        try {
            FileInputStream fstream = new FileInputStream(option.events);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            boolean firstLine = true;
            String[] varNames = new String[0];
            while ((strLine = br.readLine()) != null) {
                ++linecnt;
                strLine = strLine.trim();
                if (strLine.isEmpty()) {
                    continue;
                }

                String[] parts = strLine.split(",");
                if (firstLine) {
                    varNames = parts;
                    firstLine = false;
                    continue;
                }

                if (parts.length != varNames.length) {
                    throw new InvalidParameterException("#" + linecnt + ":" + strLine);
                }

                for (int i = 0; i < parts.length; ++i) {
                    vars.put(varNames[i], Integer.parseInt(parts[i]));
                }

                for (int i = 0; i < states.size(); ++i) {
                    boolean v = states.get(i).getStateExpr().getResult(vars);
                    bitmaps[i].add(v);
                    if (v) {
                        bms[i].add(linecnt - 2);
                    } else {
                    }
                }
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (!vars.keySet().containsAll(variables)) {
            System.err.println("The variables in the states should be the subset of the ones from the event file");
            System.exit(1);
        }

        System.out.println("Read " + (linecnt - 1) + " lines of events");
        printStat(bitmaps);

        int bits = 0;
        int bytes = 0;
        for (int i = 0; i < bms.length; ++i) {
            bits += bitmaps[i].sizeInBits();
            bytes += bms[i].getSizeInBytes();
        }
        System.out.println("Bit count: " + bits);
        System.out.println("Used byte count: " + bytes);
        System.out.printf("Compression ratio: %.2f%%\n", ((float)bytes * 8f * 100f / (float)bits));

        LTLBitmap[] results = new LTLBitmap[formulas.size()];

        long start = System.currentTimeMillis();
        for (int i = 0; i < results.length; ++i) {
            results[i] = formulas.get(i).getLtlExpr().getResult(bitmaps);
        }
        long end = System.currentTimeMillis();

        System.out.printf("Got the result, used %.4f seconds\n", (float)(end - start) / 1000f);
        printStat(results);
    }

    private static void printStat(LTLBitmap[] bms) {
        int bits = 0;
        int bytes = 0;
        int cards = 0;

        int i = 0;
        for (LTLBitmap bm : bms) {
            int bit = bm.sizeInBits();
            int card = bm.cardinality();
            int byten = bm.sizeInRealBytes();
            bits += bit;
            bytes += byten;
            cards += card;
            System.out.println("Bitmap #" + i);
            System.out.println("Bit count: " + bit + ", cardinality: " + card);
            System.out.println("Compressed byte count: " + byten);
            System.out.printf("Compression ratio: %.2f%%\n", ((float) byten * 8f * 100f / (float) bit));
            ++i;
        }

        System.out.println("Total bit count: " + bits + ", cardinality: " + cards);
        System.out.println("Total compressed byte count: " + bytes);
        System.out.printf("Total compression ratio: %.2f%%\n", ((float) bytes * 8f * 100f / (float) bits));
    }

    private static class Option {
        private File states = null;
        private File formulas = null;
        private File events = null;
        private LTLBitmap.Type bmtype = null;

        public static void usage() {
            System.out.println("Options:");
            System.out.println(" -state <states_file>");
            System.out.println(" -formula <formulas_file>");
            System.out.println(" -event <events_file>");
            System.out.println(" -bmtype <bmtype>");
            System.out.println("Bitmap Type:");
            System.out.println(" raw");
            System.out.println(" wah");
            System.out.println(" concise");
            System.out.println(" ewah64");
            System.out.println(" ewah32");
            System.out.println(" roaring");
        }

        public static Option parse(String[] args) throws FileNotFoundException {
            Option op = new Option();

            for (int i = 0; i < args.length; ++i) {
                String arg = args[i];
                if (arg.startsWith("-")) {
                    arg = arg.substring(1);
                } else {
                    continue;
                }

                if (i + 1 >= args.length) {
                    throw new IndexOutOfBoundsException();
                }
                ++i;
                String para = args[i];

                switch (arg) {
                    case "state":
                        File fs = new File(para);
                        if (!fs.exists() || !fs.canRead()) {
                            throw new FileNotFoundException(para);
                        }
                        op.states = fs;
                        break;
                    case "formula":
                        File ff = new File(para);
                        if (!ff.exists() || !ff.canRead()) {
                            throw new FileNotFoundException(para);
                        }
                        op.formulas = ff;
                        break;
                    case "event":
                        File fe = new File(para);
                        if (!fe.exists() || !fe.canRead()) {
                            throw new FileNotFoundException(para);
                        }
                        op.events = fe;
                        break;
                    case "bmtype": {
                        switch (para.toLowerCase()) {
                            case "raw":
                                op.bmtype = LTLBitmap.Type.RAW;
                                break;
                            case "wah":
                                op.bmtype = LTLBitmap.Type.WAHCONCISE;
                                break;
                            case "ewah64":
                                op.bmtype = LTLBitmap.Type.EWAH;
                                break;
                            case "ewah32":
                                op.bmtype = LTLBitmap.Type.EWAH32;
                                break;
                            case "concise":
                                op.bmtype = LTLBitmap.Type.CONCISE;
                                break;
                            case "roaring":
                                op.bmtype = LTLBitmap.Type.ROARING;
                                break;
                            default:
                                throw new InvalidParameterException();
                        }
                        break;
                    }
                }
            }

            if (op.events != null && op.states != null && op.formulas != null && op.bmtype != null) {
                return op;
            } else {
                throw new InvalidParameterException();
            }
        }

        public File getStates() {
            return states;
        }

        public File getFormulas() {
            return formulas;
        }

        public File getEvents() {
            return events;
        }
    }
}
