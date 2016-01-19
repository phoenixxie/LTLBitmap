package ca.uqac.phoenixxie.ltl.analyze;

import ca.uqac.phoenixxie.ltl.analyze.StateParser;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class StateAnalyzer {
    public static int generateRandomWithinRange(int from, int to) {
        return ThreadLocalRandom.current().nextInt(to - from) + from;
    }

    public static void splitRange(State state, int min, int max) {
        String[] keys = state.variables.keySet().toArray(new String[state.variables.size()]);

        HashMap<String, Integer> input = new HashMap<>();
        Stack<Integer> stack = new Stack<>();
        int[][][] ranges = new int[keys.length][][];

        ArrayList<HashMap<String, int[]>> trueGroup = new ArrayList<>();
        ArrayList<HashMap<String, int[]>> falseGroup = new ArrayList<>();

        for (int i = 0; i < keys.length; ++i) {
            Integer[] range = state.variables.get(keys[i]);
            ranges[i] = new int[range.length + 1][2];

            for (int j = 0; j < range.length; ++j) {
                if (j == 0) {
                    ranges[i][j][0] = min;
                    ranges[i][j][1] = range[j];
                } else {
                    ranges[i][j][0] = range[j - 1];
                    ranges[i][j][1] = range[j];
                }
            }
            ranges[i][range.length][0] = range[range.length - 1];
            ranges[i][range.length][1] = max;
        }

        while (true) {
            int s = stack.size();

            if (s < keys.length) {
                for (int i = s; i < keys.length; ++i) {
                    stack.push(0);
                    input.put(keys[i], generateRandomWithinRange(ranges[i][0][0], ranges[i][0][1]));
                }
            }

            boolean ret = state.stateExpr.getResult(input);
            ArrayList<HashMap<String, int[]>> list = ret ? trueGroup : falseGroup;
            HashMap<String, int[]> item = new HashMap<>();
            for (int i = 0; i < keys.length; ++i) {
                item.put(keys[i], ranges[i][stack.get(i)]);
            }
            list.add(item);

            int idx = keys.length - 1;
            while (!stack.isEmpty()) {
                int last = stack.pop() + 1;
                int size = ranges[idx].length;

                if (last < size) {
                    input.put(keys[idx], generateRandomWithinRange(ranges[idx][last][0], ranges[idx][last][1]));
                    stack.push(last);
                    break;
                } else {
                    --idx;
                }
            }

            if (stack.isEmpty()) {
                break;
            }
        }

        state.trueGroup = trueGroup;
        state.falseGroup = falseGroup;

//        System.out.println("True:");
//        for (HashMap<String, int[]> h : trueGroup) {
//            for(Map.Entry<String, int[]> entry : h.entrySet()) {
//                System.out.println(entry.getKey() + ":" + entry.getValue()[0] + "-" + entry.getValue()[1] + "  ");
//            }
//            System.out.println();
//        }
//
//        System.out.println("False:");
//        for (HashMap<String, int[]> h : falseGroup) {
//            for(Map.Entry<String, int[]> entry : h.entrySet()) {
//                System.out.println(entry.getKey() + ":" + entry.getValue()[0] + "-" + entry.getValue()[1] + "  ");
//            }
//            System.out.println();
//        }
    }
}