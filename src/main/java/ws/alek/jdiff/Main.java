package ws.alek.jdiff;

import ws.alek.jdiff.MainFrame;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                JFrame frame = new MainFrame();
            }
        });
    }
}
