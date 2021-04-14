package com.mitrakoff.tomstman.view;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.input.KeyStroke;

public class MainWindow extends BasicWindow {
    private final Controller controller;
    private final ActionListBox collectionListbox;
    private final TextBox urlTextBox;
    private final ComboBox<String> methodCombobox;
    private final TextBox bodyTextbox;
    private final TextBox responseTextbox;

    public MainWindow(String title, Controller controller) {
        super(title);
        this.controller = controller;
        urlTextBox = new TextBox(new TerminalSize(200, 1), "https://");
        methodCombobox = new ComboBox<>("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");
        bodyTextbox = new TextBox(new TerminalSize(200, 10), "{\n  \n}", TextBox.Style.MULTI_LINE);
        responseTextbox = new TextBox(new TerminalSize(200, 30), "", TextBox.Style.MULTI_LINE).setReadOnly(true);
        collectionListbox = new ActionListBox(new TerminalSize(32, 999));
        refreshRequestListbox();

        // panel with Method and URL
        final Panel methodUrlPanel = new Panel(new BorderLayout());
        methodUrlPanel.addComponent(methodCombobox.withBorder(Borders.singleLine(" Method ")), BorderLayout.Location.LEFT);
        methodUrlPanel.addComponent(urlTextBox.withBorder(Borders.singleLine(" URL ")), BorderLayout.Location.CENTER);

        // request panel (main area)
        final Panel requestPanel = new Panel(new BorderLayout());
        requestPanel.addComponent(methodUrlPanel, BorderLayout.Location.TOP);
        requestPanel.addComponent(bodyTextbox.withBorder(Borders.singleLine(" Json Body ")), BorderLayout.Location.CENTER);
        requestPanel.addComponent(responseTextbox.withBorder(Borders.singleLine(" Response ")), BorderLayout.Location.BOTTOM);

        // shortcuts panel
        final Panel shortcutsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(8));
        shortcutsPanel.addComponent(new Label(""));
        shortcutsPanel.addComponent(new Label("<F2>  Save request"));
        shortcutsPanel.addComponent(new Label("<F5>  Send request"));
        shortcutsPanel.addComponent(new Label("<F8>  Remove request"));
        shortcutsPanel.addComponent(new Label("<F10> Exit"));

        // content panel
        final Panel contentPanel = new Panel(new BorderLayout());
        contentPanel.addComponent(collectionListbox.withBorder(Borders.singleLine(" Request Collection ")), BorderLayout.Location.LEFT);
        contentPanel.addComponent(requestPanel.withBorder(Borders.singleLine(" Request ")), BorderLayout.Location.CENTER);

        // main panel
        final Panel mainPanel = new Panel(new BorderLayout());
        mainPanel.addComponent(shortcutsPanel.withBorder(Borders.singleLine(" T O M S T M A N ")), BorderLayout.Location.TOP);
        mainPanel.addComponent(contentPanel, BorderLayout.Location.CENTER);

        // this
        this.setTheme(SimpleTheme.makeTheme(
                true,
                TextColor.ANSI.WHITE,
                TextColor.ANSI.BLACK,
                TextColor.ANSI.YELLOW,
                TextColor.ANSI.BLACK,
                TextColor.ANSI.YELLOW,
                TextColor.ANSI.BLUE,
                TextColor.ANSI.RED
        ));
        this.addWindowListener(new WindowListenerAdapter() {
            public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
                handleHotkeys(keyStroke);
            }
        });
        this.setHints(Arrays.asList(Hint.FULL_SCREEN, Hint.NO_DECORATIONS));
        this.setComponent(mainPanel);
    }

    private void refreshRequestListbox() {
        collectionListbox.clearItems();
        controller.getRequests().forEach(request -> collectionListbox.addItem(request.toString(), () -> this.setDataToComponents(request)));
    }

    private void setDataToComponents(RequestData data) {
        urlTextBox.setText(data.url);
        methodCombobox.setSelectedItem(data.method);
        bodyTextbox.setText(data.jsonBody);
    }

    private void handleHotkeys(KeyStroke keyStroke) {
        final int selectedIdx = collectionListbox.getSelectedIndex();
        final String name = selectedIdx >= 0 ? controller.getRequests().get(selectedIdx).name : "";
        final String url = urlTextBox.getText();
        final String method = methodCombobox.getText();
        final String body = bodyTextbox.getText();

        switch (keyStroke.getKeyType()) {
            case F2:
                final String newName = TextInputDialog.showDialog(getTextGUI(), "New request", "Input the request name", url);
                final boolean isOkPressed = newName != null;
                if (isOkPressed) {
                    if (!newName.isEmpty()) {
                        controller.saveRequest(newName, url, method, body);
                        refreshRequestListbox();
                    } else MessageDialog.showMessageDialog(getTextGUI(), "Error", "Name must not be empty");
                }
                break;
            case F8:
                final MessageDialogButton btn = MessageDialog.showMessageDialog(getTextGUI(), "", "Delete request?", MessageDialogButton.Yes, MessageDialogButton.No);
                if (btn == MessageDialogButton.Yes) {
                    controller.removeRequest(name);
                    refreshRequestListbox();
                }
                break;
            case F5:
                final TerminalSize size = getTextGUI().getScreen().getTerminalSize();
                final WaitingDialog dialog = WaitingDialog.showDialog(getTextGUI(),"", "Sending request");
                dialog.setPosition(new TerminalPosition(size.getColumns()/2-10, size.getRows()/2));
                new Thread(() -> { // dialog is modal, so we have to close() it from another thread
                    final String[] result = controller.sendRequest(url, method, body);
                    responseTextbox.setText(String.format("Status: %s\n\n%s", result[1], result[0]));
                    dialog.close();
                }).start();
                break;
            case F10:
                close();
                break;
        }
    }
}
