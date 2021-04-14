package com.mitrakoff.tomstman.view;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;

public class View {
    public void start(Controller controller) {
        try {
            final Terminal term = new DefaultTerminalFactory().createTerminal();
            final Screen screen = new TerminalScreen(term);
            final WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
            final MainWindow mainWindow = new MainWindow("Tomstman", controller);

            screen.startScreen();
            gui.addWindow(mainWindow);
            mainWindow.waitUntilClosed();
            screen.stopScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
