package ca.uqac.phoenixxie.ltl.ui;

import ca.uqac.phoenixxie.ltl.analyze.Formula;
import ca.uqac.phoenixxie.ltl.analyze.FormulaParser;

import javax.swing.*;
import java.awt.event.*;

public class FormulaAddDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldFormula;
    private JLabel labelMsg;

    public interface OnResultListener {
        void onResult(Formula result);
    }

    private OnResultListener listener = null;


    public FormulaAddDialog() {
        setTitle("Add a LTL formulae...");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public void setResultListener(OnResultListener listener) {
        this.listener = listener;
    }

    private void onOK() {
        String text = textFieldFormula.getText();
        Formula result = FormulaParser.parse(text);
        if (result.isSuccess()) {
            if (this.listener != null) {
                this.listener.onResult(result);
            }
            dispose();
        } else {
            String msg = result.getErrorMsg();
            labelMsg.setText("Error: " + msg);
        }
    }


    private void onCancel() {
        dispose();
    }
}
