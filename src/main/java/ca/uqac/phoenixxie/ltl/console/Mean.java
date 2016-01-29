package ca.uqac.phoenixxie.ltl.console;

import java.io.*;

public class Mean {
    public static void main(String[] args) throws IOException {
        long[] sums = null;

        int cnt = 0;
        for (String arg : args) {
            File f = new File(arg);
            if (!f.exists() || !f.canRead()) {
                System.err.println("Unable to read " + arg);
                System.exit(1);
                return;
            }

            FileReader reader = new FileReader(f);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while (null != (line = br.readLine())) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (sums == null) {
                    sums = new long[parts.length];
                    for (int i = 0; i < sums.length; ++i) {
                        sums[i] = 0;
                    }
                }

                for (int i = 0; i < parts.length; ++i) {
                    String part = parts[i].trim();
                    sums[i] += Long.parseLong(part);
                }
                ++cnt;
            }
            reader.close();
        }

        if (cnt == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sums.length; ++i) {
            long mean = sums[i] / cnt;
            if (i == 0) {
                System.out.print(mean);
                sb.append(mean);
            } else {
                System.out.print("," + mean);
                sb.append(",").append(mean);
            }
        }
        System.out.println();

        System.out.println(RunAll.Stat.csvToLatex(sb.toString()));
    }
}
