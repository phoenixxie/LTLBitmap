package ca.uqac.phoenixxie.ltl.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {

    private JList listStates;
    private JPanel MainFrame;
    private JList listFormulas;
    private JTextField textEventCount;
    private JTextField textEventMinVal;
    private JTextField textEventMaxVal;
    private JButton btnGenerate;
    private JTextArea textAreaConsole;
    private JButton btnStateAdd;
    private JButton btnStateRemove;
    private JButton btnFormulaAdd;
    private JButton btnFormulaRemove;

    public MainFrame() {
        btnStateAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StateAddDialog dlg = new StateAddDialog();
                dlg.pack();
                dlg.setLocationRelativeTo(MainFrame.this);
                dlg.setLocation(300, 300);
                dlg.setVisible(true);
            }
        });
        btnFormulaAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FormulaAddDialog dlg = new FormulaAddDialog();
                dlg.pack();
                dlg.setLocationRelativeTo(MainFrame.this);
                dlg.setLocation(300, 300);
                dlg.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainFrame");
        frame.setContentPane(new MainFrame().MainFrame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
