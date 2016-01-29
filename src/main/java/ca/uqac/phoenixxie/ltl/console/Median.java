package ca.uqac.phoenixxie.ltl.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Median {
    public static void main(String[] args) throws IOException {
        ArrayList<Long>[] lists = null;

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
                if (lists == null) {
                    lists = new ArrayList[parts.length];
                    for (int i = 0; i < lists.length; ++i) {
                        lists[i] = new ArrayList<Long>();
                    }
                }

                for (int i = 0; i < parts.length; ++i) {
                    String part = parts[i].trim();
                    lists[i].add(Long.parseLong(part));
                }
                ++cnt;
            }
            reader.close();
        }

        if (cnt == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lists.length; ++i) {
            ArrayList<Long> l = lists[i];
            Collections.sort(l);
            long n;
            if (cnt % 2 == 0) {
                n = (l.get(cnt / 2 - 1) + l.get(cnt / 2)) / 2;
            } else {
                n = l.get(cnt / 2);
            }
            if (i == 0) {
                System.out.print(n);
                sb.append(n);
            } else {
                System.out.print("," + n);
                sb.append(",").append(n);
            }
        }
        System.out.println();

        System.out.println(RunAll.Stat.csvToLatex(sb.toString()));

    }
}
