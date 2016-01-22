package ca.uqac.phoenixxie.ltl.analyze;

import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

public class LTLVerification {

    private static class Option {
        private File states = null;
        private File formulas = null;
        private File events = null;
        private LTLBitmap.Type bmtype = null;

        public File getStates() {
            return states;
        }

        public File getFormulas() {
            return formulas;
        }

        public File getEvents() {
            return events;
        }

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
                arg = args[i];

                switch (arg) {
                    case "state":
                        File fs = new File(arg);
                        if (!fs.exists() || !fs.canRead()) {
                            throw new FileNotFoundException(arg);
                        }
                        op.states = fs;
                        break;
                    case "formula":
                        File ff = new File(arg);
                        if (!ff.exists() || !ff.canRead()) {
                            throw new FileNotFoundException(arg);
                        }
                        op.formulas = ff;
                        break;
                    case "event":
                        File fe = new File(arg);
                        if (!fe.exists() || !fe.canRead()) {
                            throw new FileNotFoundException(arg);
                        }
                        op.events = fe;
                        break;
                    case "bmtype":
                    {
                        switch (arg.toLowerCase()) {
                            case "raw": op.bmtype = LTLBitmap.Type.RAW; break;
                            case "wah": op.bmtype = LTLBitmap.Type.WAHCONCISE; break;
                            case "ewah64": op.bmtype = LTLBitmap.Type.EWAH; break;
                            case "ewah32": op.bmtype = LTLBitmap.Type.EWAH32; break;
                            case "concise": op.bmtype = LTLBitmap.Type.CONCISE; break;
                            case "roaring": op.bmtype = LTLBitmap.Type.ROARING; break;
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
            System.exit(1);
            return;
        }

        ArrayList<State> states = new ArrayList<>();
        ArrayList<Formula> formulas = new ArrayList<>();

        try {
            FileInputStream fstream = new FileInputStream(option.states);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            while ((strLine = br.readLine()) != null) {
                states.add(StateParser.parse(strLine));
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            FileInputStream fstream = new FileInputStream(option.formulas);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            while ((strLine = br.readLine()) != null) {
                formulas.add(FormulaParser.parse(strLine));
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, Integer> vars = new HashMap<>();
        LTLBitmap[] bitmaps = new LTLBitmap[states.size()];
        for (int i = 0; i < bitmaps.length; ++i) {
            bitmaps[i] = new LTLBitmap(option.bmtype);
        }

        try {
            FileInputStream fstream = new FileInputStream(option.events);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            boolean firstLine = true;
            String[] varNames = new String[0];
            int n = 0;
            while ((strLine = br.readLine()) != null) {
                ++n;
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
                    throw new InvalidParameterException("#" + n + ":" + strLine);
                }

                for (int i = 0; i < parts.length; ++i) {
                    vars.put(varNames[i], Integer.parseInt(parts[i]));
                }

                for (int i = 0; i < states.size(); ++i) {
                    bitmaps[i].add(states.get(i).getStateExpr().getResult(vars));
                }
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        LTLBitmap[] results = new LTLBitmap[formulas.size()];
        for (int i = 0; i < results.length; ++i) {
            results[i] = formulas.get(i).getLtlExpr().getResult(bitmaps);
        }
    }
}
