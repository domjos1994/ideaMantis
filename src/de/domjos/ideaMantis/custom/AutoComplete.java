package de.domjos.ideaMantis.custom;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public class AutoComplete implements DocumentListener {

    private static enum Mode {
        INSERT,
        COMPLETION
    };

    private JTextField textField;
    private final List<String> keywords;
    private Mode mode = Mode.INSERT;

    public AutoComplete(JTextField textField, List<String> keywords) {
        this.textField = textField;
        this.keywords = keywords;
        Collections.sort(keywords);
    }

    @Override
    public void changedUpdate(DocumentEvent ev) { }

    @Override
    public void removeUpdate(DocumentEvent ev) { }

    @Override
    public void insertUpdate(DocumentEvent ev) {
        if (ev.getLength() != 1)
            return;

        int pos = ev.getOffset();
        int startPos = 0;
        String content = null;
        try {
            String[] array = textField.getText(0, pos + 1).split(",");
            if(textField.getText(0, pos + 1).contains(",")) {
                startPos = textField.getText().lastIndexOf(",");
            }
            content = array[array.length-1].trim();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Find where the word starts
        int w;
        for (w = pos; w >= startPos; w--) {
            if (!Character.isLetter(textField.getText().charAt(w))) {
                break;
            }
        }

        // Too few chars
        if (pos - w < 2)
            return;

        String prefix = textField.getText().substring(w + 1).toLowerCase();
        int n = Collections.binarySearch(keywords, prefix);
        if (n < 0 && -n <= keywords.size()) {
            String match = keywords.get(-n - 1);
            if (match.startsWith(prefix)) {
                // A completion is found
                String completion = match.substring(pos - w);
                // We cannot modify Document from within notification,
                // so we submit a task that does the change later
                SwingUtilities.invokeLater(new CompletionTask(completion, pos + 1));
            }
        } else {
            // Nothing found
            mode = Mode.INSERT;
        }
    }

    private class CompletionTask implements Runnable {
        private String completion;
        private int position;

        CompletionTask(String completion, int position) {
            this.completion = completion;
            this.position = position;
        }

        public void run() {
            StringBuffer sb = new StringBuffer(textField.getText());
            sb.insert(position, completion);
            textField.setText(sb.toString());
            textField.setCaretPosition(position + completion.length());
            textField.moveCaretPosition(position);
            mode = Mode.COMPLETION;
        }
    }

}