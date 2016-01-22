package ca.uqac.phoenixxie.ltl.ui;

import ca.uqac.phoenixxie.ltl.analyze.FormulaParser;
import ca.uqac.phoenixxie.ltl.analyze.State;
import ca.uqac.phoenixxie.ltl.analyze.StateParser;
import ca.uqac.phoenixxie.ltl.analyze.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.*;
import java.util.*;

public class MainFrame extends JFrame {

    private JList jlistStates;
    private JPanel MainFrame;
    private JList jlistFormulas;
    private JTextField textEventCount;
    private JTextField textEventMinVal;
    private JTextField textEventMaxVal;
    private JButton btnGenerate;
    private JButton btnStateAdd;
    private JButton btnStateRemove;
    private JButton btnFormulaAdd;
    private JButton btnFormulaRemove;
    private JScrollPane jscrollPaneStates;
    private JScrollPane jscrollPaneFormulaes;
    private JButton btnStateLoad;
    private JButton btnFormulaLoad;
    private JButton btnStateSave;
    private JButton btnFormulaSave;
    private JTextField textOutputFile;
    private JButton btnOutputFile;
    private JTextPane textPanelVars;

    private ArrayList<State> listStates = new ArrayList<State>();
    private DefaultListModel<String> listModelState = new DefaultListModel();
    private HashMap<String, HashSet<Integer>> variables = new HashMap<>();

    private ArrayList<FormulaParser.Result> listFormulas = new ArrayList<FormulaParser.Result>();
    private DefaultListModel<String> listModelFormula = new DefaultListModel();

    public MainFrame() {
        setContentPane(MainFrame);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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

        jlistFormulas.setModel(listModelFormula);
        jlistFormulas.setCellRenderer(new ListCellRenderer<String>() {
            public Component getListCellRendererComponent(JList list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel("f" + index + ": " + value);
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
        jscrollPaneFormulaes.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
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
                        updateVariables();
                    }
                });
                dlg.pack();
                dlg.setLocationRelativeTo(MainFrame.this);
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
                updateVariables();
            }
        });

        btnStateSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int ret = fc.showSaveDialog(MainFrame.this);
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
                int ret = fc.showOpenDialog(MainFrame.this);
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
                updateVariables();
            }
        });

        btnFormulaAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FormulaAddDialog dlg = new FormulaAddDialog();
                dlg.setResultListener(new FormulaAddDialog.OnResultListener() {
                    public void onResult(FormulaParser.Result result) {
                        listFormulas.add(result);
                        listModelFormula.addElement(result.getExpr());
                    }
                });
                dlg.pack();
                dlg.setLocationRelativeTo(MainFrame.this);
                dlg.setLocation(300, 300);
                dlg.setVisible(true);
            }
        });

        btnFormulaRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = jlistFormulas.getSelectedIndex();
                if (index == -1) {
                    return;
                }
                listFormulas.remove(index);
                listModelFormula.remove(index);
            }
        });

        btnFormulaSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int ret = fc.showSaveDialog(MainFrame.this);
                if (ret != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File file = fc.getSelectedFile();
                try {
                    FileWriter writer = new FileWriter(file);
                    for (FormulaParser.Result p : listFormulas) {
                        writer.write(p.getExpr() + "\n");
                    }
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        btnFormulaLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setMultiSelectionEnabled(true);
                int ret = fc.showOpenDialog(MainFrame.this);
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

                            FormulaParser.Result result = FormulaParser.parse(line);
                            if (result.isSuccess()) {
                                listFormulas.add(result);
                                listModelFormula.addElement(result.getExpr());
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
                int ret = fc.showSaveDialog(MainFrame.this);
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

                if (variables.isEmpty()) {
                    return;
                }

                int count = Integer.parseInt(textEventCount.getText());
                if (count <= 0) {
                    return;
                }

                int min = Integer.parseInt(textEventMinVal.getText());
                int max = Integer.parseInt(textEventMaxVal.getText());
                if (min > max) {
                    textEventMaxVal.setText("");
                    textEventMinVal.setText("");
                    return;
                }

                Utils.generateRandom(filepath, variables.keySet(), count, min, max);
            }
        });
    }

    private void updateVariables() {
        variables.clear();
        for (State state : listStates) {
            for (Map.Entry<String, Integer[]> var : state.getVariables().entrySet()) {
                if (variables.containsKey(var.getKey())) {
                    variables.get(var.getKey()).addAll(Arrays.asList(var.getValue()));
                } else {
                    variables.put(var.getKey(), new HashSet<Integer>(Arrays.asList(var.getValue())));
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, HashSet<Integer>> var : variables.entrySet()) {
            sb.append(var.getKey()).append(": ");

            Integer[] arr = var.getValue().toArray(new Integer[var.getValue().size()]);
            Arrays.sort(arr);

            for (int i = 0; i < arr.length; ++i) {
                if (i == 0) {
                    sb.append("(min").append(", ").append(arr[i]).append(")");
                } else {
                    sb.append(", [").append(arr[i - 1]).append(", ").append(arr[i]).append(")");
                }
            }
            sb.append(", [").append(arr[arr.length - 1]).append(", ").append("max)");

            sb.append("\n");
        }
        textPanelVars.setText(sb.toString());
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
