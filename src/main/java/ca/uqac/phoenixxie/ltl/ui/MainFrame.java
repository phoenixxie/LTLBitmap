package ca.uqac.phoenixxie.ltl.ui;

import ca.uqac.phoenixxie.ltl.parser.LTLParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
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

    private ArrayList<LTLParser.StateResult> listStates = new ArrayList();
    private DefaultListModel<String> listModelState = new DefaultListModel();

    private ArrayList<LTLParser.PathResult> listPath = new ArrayList();
    private DefaultListModel<String> listModelPath = new DefaultListModel();

    public MainFrame() {
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
                    public void onResult(LTLParser.StateResult result) {
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

        btnFormulaAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FormulaAddDialog dlg = new FormulaAddDialog();
                dlg.setResultListener(new FormulaAddDialog.OnResultListener() {
                    public void onResult(LTLParser.PathResult result) {
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
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainFrame");
        frame.setContentPane(new MainFrame().MainFrame);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
