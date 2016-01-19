package ca.uqac.phoenixxie.ltl.analyze;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static int generateRandomWithinRange(int from, int to) {
        return ThreadLocalRandom.current().nextInt(to - from) + from;
    }

    public static void generateRandom(Writer writer, int varCount, int count) throws IOException {

        for (int i = 0; i < count; ++i) {
            for (int j = 0; j < varCount; ++j) {
                if (j == 0) {
                    writer.write("" + generateRandomWithinRange(State.MIN, State.MAX));
                } else {
                    writer.write("," + generateRandomWithinRange(State.MIN, State.MAX));
                }
            }
            writer.write('\n');
        }
    }
}
