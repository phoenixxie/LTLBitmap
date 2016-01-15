package ca.uqac.phoenixxie.ltl.ui;

import ca.uqac.phoenixxie.ltl.parser.LTLParser;
import ca.uqac.phoenixxie.ltl.parser.LTLParser.PathResult;
import ca.uqac.phoenixxie.ltl.parser.LTLParser.StateResult;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {

    private JList jlistStates;
    private JPanel MainFrame;
    private JList jlistFormulas;
    private JTextField textEventCount;
    private JTextField textEventMinVal;
    private JTextField textEventMaxVal;
    private JButton btnGenerate;
    private JTextArea textAreaConsole;
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

    private ArrayList<StateResult> listStates = new ArrayList<StateResult>();
    private DefaultListModel<String> listModelState = new DefaultListModel();

    private ArrayList<PathResult> listPath = new ArrayList<PathResult>();
    private DefaultListModel<String> listModelPath = new DefaultListModel();

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

        jlistFormulas.setModel(listModelPath);
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
                    public void onResult(StateResult result) {
                        listStates.add(result);
                        listModelState.addElement(result.getExpr());
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
                    for (StateResult r : listStates) {
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

                            StateResult result = LTLParser.parseState(line);
                            if (result.isSuccess()) {
                                listStates.add(result);
                                listModelState.addElement(result.getExpr());
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

        btnFormulaAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FormulaAddDialog dlg = new FormulaAddDialog();
                dlg.setResultListener(new FormulaAddDialog.OnResultListener() {
                    public void onResult(PathResult result) {
                        listPath.add(result);
                        listModelPath.addElement(result.getExpr());
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
                listPath.remove(index);
                listModelPath.remove(index);
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
                    for (PathResult p : listPath) {
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

                            PathResult result = LTLParser.parsePath(line);
                            if (result.isSuccess()) {
                                listPath.add(result);
                                listModelPath.addElement(result.getExpr());
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
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
