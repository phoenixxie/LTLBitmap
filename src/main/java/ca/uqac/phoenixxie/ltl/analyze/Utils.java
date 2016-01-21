package ca.uqac.phoenixxie.ltl.analyze;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static int generateRandomWithinRange(int from, int to) {
        return ThreadLocalRandom.current().nextInt(to - from) + from;
    }

    public static boolean generateRandom(String filepath, Set<String> vars, int count, int min, int max) {
        ArrayList<String> listVars = new ArrayList<String>(vars);

        FileWriter writer = null;
        try {
            writer = new FileWriter(filepath);
            for (int j = 0; j < listVars.size(); ++j) {
                if (j == 0) {
                    writer.write(listVars.get(j));
                } else {
                    writer.write("," + listVars.get(j));
                }
            }
            writer.write('\n');

            for (int i = 0; i < count; ++i) {
                for (int j = 0; j < listVars.size(); ++j) {
                    if (j == 0) {
                        writer.write("" + generateRandomWithinRange(min, max));
                    } else {
                        writer.write("," + generateRandomWithinRange(min, max));
                    }
                }
                writer.write('\n');
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
