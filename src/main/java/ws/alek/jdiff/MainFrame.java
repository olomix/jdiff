package ws.alek.jdiff;

import name.fraser.neil.plaintext.diff_match_patch;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.LinkedList;

public class MainFrame extends JFrame {

    private final static String REGULAR_STYLE = "regular";
    private final static String ADD_STYLE = "add";
    private final static String DEL_STYLE = "del";

    private final StyledDocument rightDoc;
    private final StyledDocument leftDoc;
    private final diff_match_patch dmp = new diff_match_patch();

    public MainFrame() {
        setTitle("Really simple diff tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextPane leftPane = new JTextPane();
        leftDoc = leftPane.getStyledDocument();
        addStyleToDocument(leftDoc);
        addDocumentListeners(leftDoc);
        JScrollPane leftScrollPane = new JScrollPane(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPane.getViewport().add(leftPane);
        leftScrollPane.setRowHeaderView(new TextLineNumber(leftPane));

        JTextPane rightPane = new JTextPane();
        rightDoc = rightPane.getStyledDocument();
        addStyleToDocument(rightDoc);
        addDocumentListeners(rightDoc);
        JScrollPane rightScrollPane = new JScrollPane(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightScrollPane.getViewport().add(rightPane);
        rightScrollPane.setRowHeaderView(new TextLineNumber(rightPane));

        JPanel diffPanel = new JPanel(new GridLayout(1, 2));
        diffPanel.setPreferredSize(new Dimension(1000, 800));
        diffPanel.add(leftScrollPane);
        diffPanel.add(rightScrollPane);
        getContentPane().add(diffPanel, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }

    private void addDocumentListeners(StyledDocument doc) {
        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                repaintDiff(e.getDocument());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                repaintDiff(e.getDocument());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void repaintDiff(Document doc) {
        String textLeft = "";
        String textRight = "";
        try {
            textLeft = leftDoc.getText(0, leftDoc.getLength());
            textRight = rightDoc.getText(0, rightDoc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(textLeft, textRight);
        dmp.diff_cleanupSemantic(diffs);
        int leftDocPos = 0;
        int rightDocPos = 0;
        for (diff_match_patch.Diff d : diffs) {
            if (d.operation == diff_match_patch.Operation.EQUAL) {
                SwingUtilities.invokeLater(new AttributeChanger(leftDoc, leftDocPos, d.text.length(), REGULAR_STYLE));
                leftDocPos += d.text.length();
                SwingUtilities.invokeLater(new AttributeChanger(rightDoc, rightDocPos, d.text.length(), REGULAR_STYLE));
                rightDocPos += d.text.length();
            } else if (d.operation == diff_match_patch.Operation.DELETE) {
                SwingUtilities.invokeLater(new AttributeChanger(leftDoc, leftDocPos, d.text.length(), DEL_STYLE));
                leftDocPos += d.text.length();
            } else if (d.operation == diff_match_patch.Operation.INSERT) {
                SwingUtilities.invokeLater(new AttributeChanger(rightDoc, rightDocPos, d.text.length(), ADD_STYLE));
                rightDocPos += d.text.length();
            } else {
                throw new IllegalStateException("Unknown operation");
            }
        }
    }

    private void addStyleToDocument(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext()
                .getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(def, "Monospaced");
        StyleConstants.setFontSize(def, 14);

        Style regular =  doc.addStyle(REGULAR_STYLE, def);

        Style del = doc.addStyle(DEL_STYLE, regular);
        StyleConstants.setBackground(del, new Color(255, 221, 221));

        Style add = doc.addStyle(ADD_STYLE, regular);
        StyleConstants.setBackground(add, new Color(221, 255, 221));
    }

}


class AttributeChanger implements Runnable {
    final StyledDocument doc;
    private final int idx;
    private final int length;
    private final String style;

    public AttributeChanger(StyledDocument doc, int idx, int length, String style) {
        this.doc = doc;
        this.idx = idx;
        this.style = style;
        this.length = length;
    }

    @Override
    public void run() {
        doc.setCharacterAttributes(idx, length, doc.getStyle(style), true);
    }
}
