package ca.uqac.phoenixxie.ltl.ui;

import ca.uqac.phoenixxie.ltl.analyze.State;
import ca.uqac.phoenixxie.ltl.analyze.StateParser;

import javax.swing.*;
import java.awt.event.*;

public class StateAddDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldState;
    private JLabel labelMsg;

    public interface OnResultListener {
        void onResult(State state);
    }

    private OnResultListener listener = null;

    public StateAddDialog() {
        setContentPane(contentPane);
        setTitle("Add a new state...");
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

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
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
        String text = textFieldState.getText();
        State state = StateParser.parse(text);
        if (state.isSuccess()) {
            if (this.listener != null) {
                this.listener.onResult(state);
            }
            dispose();
        } else {
            String msg = state.getErrorMsg();
            labelMsg.setText("Error: " + msg);
        }
    }

    private void onCancel() {
        dispose();
    }

}
