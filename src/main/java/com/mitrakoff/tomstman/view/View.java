package com.mitrakoff.tomstman.view;

import java.util.function.Consumer;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;

public class View {
    protected /*final*/ Consumer<Throwable> exceptionHandler;

    public void start(Controller controller) {
        try {
            final Terminal term = new DefaultTerminalFactory().createTerminal();
            final Screen screen = new TerminalScreen(term);
            final WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
            final MainWindow mainWindow = new MainWindow("Tomstman", controller);
            exceptionHandler = e -> MessageDialog.showMessageDialog(gui, "Error", e.getMessage(), MessageDialogButton.OK);

            screen.startScreen();
            gui.addWindow(mainWindow);
            mainWindow.waitUntilClosed();
            screen.stopScreen();
        } catch (Throwable e) {
            if (exceptionHandler != null)
                exceptionHandler.accept(e);
            else e.printStackTrace();
        }
    }

    public Consumer<Throwable> getExceptionHandler() {
        return exceptionHandler;
    }
}
