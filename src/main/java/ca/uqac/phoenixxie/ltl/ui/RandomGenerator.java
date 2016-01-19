package ca.uqac.phoenixxie.ltl.ui;

import ca.uqac.phoenixxie.ltl.analyze.State;
import ca.uqac.phoenixxie.ltl.analyze.StateParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.*;
import java.util.*;

public class RandomGenerator extends JFrame {

    private JList jlistStates;
    private JPanel RandomGenerator;
    private JTextField textEventCount;
    private JTextField textEventMinVal;
    private JTextField textEventMaxVal;
    private JButton btnGenerate;
    private JButton btnStateAdd;
    private JButton btnStateRemove;
    private JScrollPane jscrollPaneStates;
    private JButton btnStateLoad;
    private JButton btnStateSave;
    private JTextField textOutputFile;
    private JButton btnOutputFile;
    private JTextPane textPanelVars;

    private ArrayList<State> listStates = new ArrayList<>();
    private DefaultListModel<String> listModelState = new DefaultListModel<>();
    private HashMap<String, HashSet<Integer>> variables = new HashMap<>();

    public RandomGenerator() {
        setContentPane(RandomGenerator);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        textEventMinVal.setText("" + State.MIN);
        textEventMaxVal.setText("" + State.MAX);

        jlistStates.setModel(listModelState);
        jlistStates.setCellRenderer(new ListCellRenderer<String>() {
            public Component getListCellRendererComponent(JList list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel("s" + index + ": " + value);
                label.setOpaque(true);

                if (isSelected) {
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                } else {
                    label.setBackground(list.getBackground());
                    label.setForeground(list.getForeground());
                }

                return label;
            }
        });

        jscrollPaneStates.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });

        btnStateAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StateAddDialog dlg = new StateAddDialog();
                dlg.setResultListener(new StateAddDialog.OnResultListener() {
                    public void onResult(State state) {
                        listStates.add(state);
                        listModelState.addElement(state.getExpr());
                        updateVariables(state.getVariables());
                    }
                });
                dlg.pack();
                dlg.setLocationRelativeTo(RandomGenerator.this);
                dlg.setLocation(300, 300);
                dlg.setVisible(true);
            }
        });

        btnStateRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = jlistStates.getSelectedIndex();
                if (index == -1) {
                    return;
                }
                listStates.remove(index);
                listModelState.remove(index);
            }
        });

        btnStateSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save the states...");
                int ret = fc.showSaveDialog(RandomGenerator.this);
                if (ret != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File file = fc.getSelectedFile();
                try {
                    FileWriter writer = new FileWriter(file);
                    for (State r : listStates) {
                        writer.write(r.getExpr() + "\n");
                    }
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        btnStateLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setMultiSelectionEnabled(true);
                int ret = fc.showOpenDialog(RandomGenerator.this);
                if (ret != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File[] files = fc.getSelectedFiles();
                for (File f : files) {
                    try {
                        FileReader reader = new FileReader(f);
                        BufferedReader br = new BufferedReader(reader);
                        String line;
                        while ((line = br.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) {
                                continue;
                            }

                            State state = StateParser.parse(line);
                            if (state.isSuccess()) {
                                listStates.add(state);
                                listModelState.addElement(state.getExpr());
                            }
                        }
                        br.close();
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        btnOutputFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int ret = fc.showSaveDialog(RandomGenerator.this);
                if (ret != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                textOutputFile.setText(fc.getSelectedFile().getPath());
            }
        });

        btnGenerate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filepath = textOutputFile.getText();
                if (filepath.isEmpty()) {
                    btnOutputFile.doClick();
                    return;
                }
            }
        });
    }

    private void updateVariables(HashMap<String, Integer[]> vars) {
        for (Map.Entry<String, Integer[]> var : vars.entrySet()) {
            if (variables.containsKey(var.getKey())) {
                variables.get(var.getKey()).addAll(Arrays.asList(var.getValue()));
            } else {
                variables.put(var.getKey(), new HashSet<Integer>(Arrays.asList(var.getValue())));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, HashSet<Integer>> var : variables.entrySet()) {
            sb.append(var.getKey()).append(": ");

            Integer[] arr = var.getValue().toArray(new Integer[var.getValue().size()]);
            Arrays.sort(arr);

            for (int i = 0; i < arr.length; ++i) {
                if (i == 0) {
                    sb.append("(").append(State.MIN).append(", ").append(arr[i]).append(")");
                } else {
                    sb.append(", [").append(arr[i - 1]).append(", ").append(arr[i]).append(")");
                }
            }
            sb.append(", [").append(arr[arr.length - 1]).append(", ").append(State.MAX).append(")");

            sb.append("\n");
        }
        textPanelVars.setText(sb.toString());
    }

    public static void main(String[] args) {
        RandomGenerator frame = new RandomGenerator();
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
