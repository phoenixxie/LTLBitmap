package ca.uqac.phoenixxie.ltl.console;

import ca.uqac.phoenixxie.ltl.analyze.Formula;
import ca.uqac.phoenixxie.ltl.analyze.FormulaParser;
import ca.uqac.phoenixxie.ltl.analyze.State;
import ca.uqac.phoenixxie.ltl.analyze.StateParser;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;

public class RunAll {

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
        System.out.println("Verify every result: " + (option.verifyResult ? "yes" : "no"));

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

        List<String[]> listLines = new ArrayList<>();
        List<String> varNames = new ArrayList<>();

        int linecnt = 0;
        try {
            FileInputStream fstream = new FileInputStream(option.events);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            boolean firstLine = true;
            while ((strLine = br.readLine()) != null) {
                ++linecnt;
                strLine = strLine.trim();
                if (strLine.isEmpty()) {
                    continue;
                }

                String[] parts = strLine.split(",");
                if (firstLine) {
                    varNames = Arrays.asList(parts);
                    firstLine = false;
                    continue;
                }

                if (parts.length != varNames.size()) {
                    throw new InvalidParameterException("#" + linecnt + ":" + strLine);
                }

                listLines.add(parts);
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (!varNames.containsAll(variables)) {
            System.err.println("The variables used in the states should be the subset of the ones from the event file");
            System.exit(1);
        }

        System.out.println("Read " + (linecnt - 1) + " lines of events");

        HashMap<String, Integer> vars = new HashMap<>();
        ArrayList<String> strResults = new ArrayList<>();

        System.out.println("Forced garbage collection, starting...");
        System.gc();
        System.out.println("Forced garbage collection, end");

        for (LTLBitmap.Type type : LTLBitmap.Type.values()) {
            LTLBitmap[] bitmaps = new LTLBitmap[states.size()];
            for (int i = 0; i < bitmaps.length; ++i) {
                bitmaps[i] = new LTLBitmap(type);
            }

            System.out.println();
            System.out.println("Using " + type.toString());

            long start = System.currentTimeMillis();
            for (String[] parts : listLines) {
                for (int i = 0; i < parts.length; ++i) {
                    vars.put(varNames.get(i), Integer.parseInt(parts[i]));
                }

                for (int i = 0; i < states.size(); ++i) {
                    bitmaps[i].add(states.get(i).getStateExpr().getResult(vars));
                }
            }
            long end = System.currentTimeMillis();
            System.out.printf("Executed the states, used %.4f seconds\n", (float) (end - start) / 1000f);
            printStat(bitmaps);

            System.out.println("Forced garbage collection, starting...");
            System.gc();
            System.out.println("Forced garbage collection, end");

            LTLBitmap[] results = new LTLBitmap[formulas.size()];

            start = System.currentTimeMillis();
            for (int i = 0; i < results.length; ++i) {
                results[i] = formulas.get(i).getLtlExpr().getResult(bitmaps);
            }
            end = System.currentTimeMillis();

            System.out.printf("Got the result, used %.4f seconds\n", (float) (end - start) / 1000f);
            printStat(results);

            if (option.verifyResult) {
                boolean first = strResults.isEmpty();
                for (int i = 0; i < results.length; ++i) {
                    if (first) {
                        strResults.add(results[i].toString());
                    } else {
                        String str = results[i].toString();
                        if (!str.equals(strResults.get(i))) {
                            System.err.println("Results don't match!" + i);
                            System.exit(1);
                        }
                    }
                }
            }
            System.out.println("Forced garbage collection, starting...");
            System.gc();
            System.out.println("Forced garbage collection, end");
        }
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
        private boolean verifyResult = false;

        public static void usage() {
            System.out.println("Options:");
            System.out.println(" -state <states_file>");
            System.out.println(" -formula <formulas_file>");
            System.out.println(" -event <events_file>");
            System.out.println(" [-verify <yes|no>]");
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
                    case "verify":
                        if (para.equals("yes")) {
                            op.verifyResult = true;
                        } else {
                            op.verifyResult = false;
                        }
                }
            }

            if (op.events != null && op.states != null && op.formulas != null) {
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
