package ca.uqac.phoenixxie.ltl.console;

import ca.uqac.phoenixxie.ltl.analyze.*;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class EventGenerator {

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
        System.out.println("Event file: " + option.events.getPath());
        System.out.println("Event count: " + option.count);
        System.out.println("Min value: " + option.min);
        System.out.println("Max value: " + option.max);

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

        if (Utils.generateRandom(option.events.getPath(), variables, option.count, option.min, option.max)) {
            System.out.println("Events have been generated.");
        } else {
            System.err.println("Event generation failed.");
            System.exit(1);
        }
    }

    private static class Option {
        private File states = null;
        private File events = null;
        private int count = 0;
        private int min = -10000;
        private int max = 100000;

        public static void usage() {
            System.out.println("Options:");
            System.out.println(" -state <states_file>");
            System.out.println(" -count <events_count>");
            System.out.println(" -event <events_file>");
            System.out.println(" [ -min <min_value (default: -10000)> ]");
            System.out.println(" [ -max <max_value (default: 10000)> ]");
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
                    case "event":
                        File fe = new File(para);
                        if (fe.exists() && !fe.canWrite()) {
                            throw new FileNotFoundException(para);
                        }
                        op.events = fe;
                        break;
                    case "count":
                        op.count = Integer.parseInt(para);
                        if (op.count <= 0) {
                            throw new InvalidParameterException();
                        }
                        break;
                    case "min":
                        op.min = Integer.parseInt(para);
                        break;
                    case "max":
                        op.max = Integer.parseInt(para);
                        break;
                }
            }

            if (op.events != null && op.states != null && op.count > 0 && op.max > op.min) {
                return op;
            } else {
                throw new InvalidParameterException();
            }
        }

        public File getStates() {
            return states;
        }

        public File getEvents() {
            return events;
        }

        public int getCount() {
            return count;
        }
    }
}
