package ca.uqac.phoenixxie.ltl.console;

import ca.uqac.phoenixxie.ltl.analyze.Formula;
import ca.uqac.phoenixxie.ltl.analyze.FormulaParser;
import ca.uqac.phoenixxie.ltl.analyze.State;
import ca.uqac.phoenixxie.ltl.analyze.StateParser;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;

import java.io.*;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RunAll {

    private static void gc() {
        System.out.println("Forced garbage collection, starting...");
        System.gc();
        System.out.println("Forced garbage collection, end");
    }

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

        Stat stat = new Stat(linecnt - 1, states.size(), formulas.size());

        gc();

        for (LTLBitmap.Type type : LTLBitmap.Type.values()) {
            StringBuilder sb = new StringBuilder();

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
            stat.setStateUsedTime(type, end - start);

            for (int i = 0; i < bitmaps.length; ++i) {
                printStat(states.get(i).getExpr(), bitmaps[i]);
                System.out.println();

                stat.setState(i, bitmaps[i].cardinality(), type, bitmaps[i].sizeInRealBytes());
            }

            System.out.println("Forced garbage collection, starting...");
            System.gc();
            System.out.println("Forced garbage collection, end");
            System.out.println();

            LTLBitmap[] results = new LTLBitmap[formulas.size()];

            for (int i = 0; i < results.length; ++i) {
                start = System.currentTimeMillis();
                results[i] = formulas.get(i).getLtlExpr().getResult(bitmaps);
                end = System.currentTimeMillis();
                printStat(formulas.get(i).getExpr(), results[i]);
                System.out.printf("Used %.4f seconds\n", (float) (end - start) / 1000f);
                System.out.println();
                gc();
                System.out.println();

                stat.setFormula(i, results[i].cardinality(), type, end - start, results[i].sizeInRealBytes());
            }

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
        }


        String datafile = System.getProperty("user.dir") + File.separator + "data-"
                + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".csv";

        try {
            BufferedWriter dataWriter = new BufferedWriter(new FileWriter(datafile, true));
            dataWriter.write(stat.toCSV());
            dataWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printStat(String title, LTLBitmap bm) {
        int bit = bm.sizeInBits();
        int card = bm.cardinality();
        int byten = bm.sizeInRealBytes();
        System.out.println("Bitmap: " + title);
        System.out.println("Bit count: " + bit + ", cardinality: " + card);
        System.out.println("Compressed byte count: " + byten);
        System.out.printf("Compression ratio: %.2f%%\n", ((float) byten * 8f * 100f / (float) bit));
    }

    static class Stat {
        int eventsNum;
        HashMap<LTLBitmap.Type, Long> stateUsedTime = new HashMap<>();
        State[] states;
        Formula[] formulas;

        public Stat(int eventsNum, int statesNum, int formulaNum) {
            this.eventsNum = eventsNum;
            this.states = new State[statesNum];
            for (int i = 0; i < statesNum; ++i) {
                this.states[i] = new State();
            }
            this.formulas = new Formula[formulaNum];
            for (int i = 0; i < formulaNum; ++i) {
                this.formulas[i] = new Formula();
            }
        }

        public void setStateUsedTime(LTLBitmap.Type type, long usedTime) {
            stateUsedTime.put(type, usedTime);
        }

        public void setState(int idx, int card, LTLBitmap.Type type, int bytes) {
            State st = this.states[idx];
            st.cardinality = card;
            st.bytes.put(type, bytes);
        }

        public void setFormula(int idx, int card, LTLBitmap.Type type, long usedTime, int bytes) {
            Formula fm = this.formulas[idx];
            fm.cardinality = card;
            fm.bytes.put(type, bytes);
            fm.usedTime.put(type, usedTime);
        }

        public String toCSV() {
            StringBuilder sb = new StringBuilder();
            sb.append(eventsNum).append(",")
                    .append(states.length).append(",")
                    .append(formulas.length).append(",");
            for (LTLBitmap.Type t : LTLBitmap.Type.values()) {
                sb.append(stateUsedTime.get(t)).append(",");
            }
            for (State st : states) {
                sb.append(st.cardinality).append(",");
                for (LTLBitmap.Type t : LTLBitmap.Type.values()) {
                    sb.append(st.bytes.get(t)).append(",");
                }
            }
            for (Formula fm : formulas) {
                sb.append(fm.cardinality).append(",");
                for (LTLBitmap.Type t : LTLBitmap.Type.values()) {
                    sb.append(fm.usedTime.get(t)).append(",");
                    sb.append(fm.bytes.get(t)).append(",");
                }
            }

            String str = sb.toString();
            return str.substring(0, str.length() - 1) + "\n";
        }

        public static Stat fromCSV(String input) {
            String[] parts = input.trim().split(",");
            int eventsNum = Integer.parseInt(parts[0]);
            int statesNum = Integer.parseInt(parts[1]);
            int formulasNum = Integer.parseInt(parts[2]);
            Stat stat = new Stat(eventsNum, statesNum, formulasNum);

            int idx = 3;
            for (LTLBitmap.Type t : LTLBitmap.Type.values()) {
                stat.stateUsedTime.put(t, Long.parseLong(parts[idx++]));
            }

            for (int i = 0; i < statesNum; ++i) {
                int card = Integer.parseInt(parts[idx++]);
                for (LTLBitmap.Type t : LTLBitmap.Type.values()) {
                    stat.setState(i, card, t, Integer.parseInt(parts[idx++]));
                }
            }

            for (int i = 0; i < formulasNum; ++i) {
                int card = Integer.parseInt(parts[idx++]);
                for (LTLBitmap.Type t : LTLBitmap.Type.values()) {
                    stat.setFormula(i, card, t, Long.parseLong(parts[idx++]), Integer.parseInt(parts[idx++]));
                }
            }

            return stat;
        }

        public static String csvToLatex(String line) {
            Stat stat = fromCSV(line);
            return stat.toLatex();
        }

        public String toLatex() {
            StringBuilder sb = new StringBuilder();

            sb.append("\\begin{table}[h]\n");
            sb.append("\\centering\n");
            sb.append("\\begin{tabular}{|l|l|");
            for (State state : states) {
                sb.append("c|");
            }
            sb.append("}\n");

            sb.append("\\hline\n");
            sb.append("\\multicolumn{2}{|c|}{} ");
            for (int i = 0; i < states.length; ++i) {
                sb.append("& \\states(").append(i + 1).append(") ");
            }
            sb.append("\\\\\n");

            sb.append("\\hline\n");
            sb.append("\\multicolumn{2}{|c|}{Number of bits} ");
            for (State state1 : states) {
                sb.append("& ").append(eventsNum).append(" ");
            }
            sb.append("\\\\\n");

            sb.append("\\hline\n");
            sb.append("\\multicolumn{2}{|c|}{Cardinality} ");
            for (State state : states) {
                sb.append("& ").append(state.cardinality).append(" ");
            }
            sb.append("\\\\\n");

            for (LTLBitmap.Type t : LTLBitmap.Type.values()) {
                sb.append("\\hline\n");
                sb.append("\\multirow{3}{*}{").append(t.toString()).append("} & Bytes ");
                for (State state : states) {
                    sb.append("& ").append(state.bytes.get(t)).append(" ");
                }
                sb.append("\\\\\n");

                sb.append("\\cline{2-").append(2 + states.length).append("}\n");
                sb.append("& Ratio ");
                for (State state : states) {
                    sb.append("& ")
                      .append(String.format("%.2f", (double) state.bytes.get(t) * 8f * 100f / (double) eventsNum)).append("\\% ");
                }
                sb.append("\\\\\n");

                sb.append("\\cline{2-").append(2 + states.length).append("}\n");
                sb.append("& Time (seconds) ");
                sb.append("& \\multicolumn{").append(states.length).append("}{|c|}{")
                  .append(String.format("%.2f", (double) stateUsedTime.get(t) / 1000f)).append("} ");
                sb.append("\\\\\n");
            }

            sb.append("\\hline\n");
            sb.append("\\end{tabular}\n");
            sb.append("\\caption{Statements}\n");
            sb.append("\\label{table:statements}\n");
            sb.append("\\end{table}\n\n");

            for (int i = 0; i < formulas.length; ++i) {
                Formula f = formulas[i];
                sb.append("\\begin{table}[h]\n");
                sb.append("\\centering\n");
                sb.append("\\begin{tabular}{|l|l|c|}\n");

                sb.append("\\hline\n");
                sb.append("\\multicolumn{3}{|c|}{\\formulas(").append(i + 1).append(")}");
                sb.append("\\\\\n");

                sb.append("\\hline\n");
                sb.append("\\multicolumn{2}{|c|}{Cardinality} ");
                sb.append(" & ").append(f.cardinality).append(" ");
                sb.append("\\\\\n");

                for (LTLBitmap.Type t : LTLBitmap.Type.values()) {
                    sb.append("\\hline\n");
                    sb.append("\\multirow{3}{*}{").append(t.toString()).append("} & Bytes ");
                    sb.append(" & ").append(f.bytes.get(t)).append(" ");
                    sb.append("\\\\\n");

                    sb.append("\\cline{2-3}\n");
                    sb.append("& Ratio ");
                    sb.append(" & ").append(String.format("%.2f", (double) f.bytes.get(t) * 8f * 100f / (double) eventsNum)).append("\\% ");
                    sb.append("\\\\\n");

                    sb.append("\\cline{2-3}\n");
                    sb.append("& Time (seconds) ");
                    sb.append(" & ").append(String.format("%.2f", (double) f.usedTime.get(t) / 1000f)).append(" ");
                    sb.append("\\\\\n");
                }

                sb.append("\\hline\n");
                sb.append("\\end{tabular}\n");
                sb.append("\\caption[]{\\formulas(").append(i + 1).append(")}\n");
                sb.append("\\label{table:formula").append(i + 1).append("}\n");
                sb.append("\\end{table}\n\n");
            }

            return sb.toString();
        }

        class State {
            int cardinality;
            HashMap<LTLBitmap.Type, Integer> bytes = new HashMap<>();
        }

        class Formula {
            int cardinality;
            HashMap<LTLBitmap.Type, Integer> bytes = new HashMap<>();
            HashMap<LTLBitmap.Type, Long> usedTime = new HashMap<>();
        }
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
