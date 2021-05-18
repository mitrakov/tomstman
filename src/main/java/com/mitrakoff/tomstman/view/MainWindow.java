package com.mitrakoff.tomstman.view;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
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
    private final Panel headersPanel;

    public MainWindow(String title, Controller controller) {
        super(title);
        this.controller = controller;
        urlTextBox = new TextBox(new TerminalSize(200, 1), "https://");
        methodCombobox = new ComboBox<>("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");
        bodyTextbox = new TextBox(new TerminalSize(200, 10), "{\n  \n}", TextBox.Style.MULTI_LINE);
        responseTextbox = new TextBox(new TerminalSize(200, 30), "", TextBox.Style.MULTI_LINE).setReadOnly(true);
        collectionListbox = new ActionListBox(new TerminalSize(32, 999));
        refreshCollectionListbox();

        // panel with Method and URL
        final Panel methodUrlPanel = new Panel(new BorderLayout());
        methodUrlPanel.addComponent(methodCombobox.withBorder(Borders.singleLine(" Method ")), BorderLayout.Location.LEFT);
        methodUrlPanel.addComponent(urlTextBox.withBorder(Borders.singleLine(" URL ")), BorderLayout.Location.CENTER);

        // panel with Headers
        headersPanel = new Panel(new GridLayout(2));

        // panel with Method/URL and headers
        final Panel methodUrlHeadersPanel = new Panel(new LinearLayout());
        methodUrlHeadersPanel.addComponent(methodUrlPanel);
        methodUrlHeadersPanel.addComponent(headersPanel, LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));

        // request panel (main area)
        final Panel requestPanel = new Panel(new BorderLayout());
        requestPanel.addComponent(methodUrlHeadersPanel, BorderLayout.Location.TOP);
        requestPanel.addComponent(bodyTextbox.withBorder(Borders.singleLine(" Json Body ")), BorderLayout.Location.CENTER);
        requestPanel.addComponent(responseTextbox.withBorder(Borders.singleLine(" Response ")), BorderLayout.Location.BOTTOM);

        // shortcuts panel
        final Panel shortcutsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(8));
        shortcutsPanel.addComponent(new Label(""));
        shortcutsPanel.addComponent(new Label("<F2>  Save request"));
        shortcutsPanel.addComponent(new Label("<F3>  Add header"));
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

    private void refreshCollectionListbox() {
        collectionListbox.clearItems();
        controller.getRequests().forEach(request -> collectionListbox.addItem(request.toString(), () -> this.setDataToComponents(request)));
    }

    private void setDataToComponents(RequestData data) {
        urlTextBox.setText(data.url);
        methodCombobox.setSelectedItem(data.method);
        bodyTextbox.setText(data.jsonBody);
        headersPanel.removeAllComponents();
        for (Map.Entry<String, String> entry : data.headers.entrySet()) {
            addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Shortcut events processor
     */
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
                        controller.saveRequest(newName, url, method, body, getHeaders());
                        refreshCollectionListbox();
                    } else MessageDialog.showMessageDialog(getTextGUI(), "Error", "Name must not be empty");
                }
                break;
            case F3:
                addHeader("", "");
                break;
            case F8:
                final MessageDialogButton btn = MessageDialog.showMessageDialog(getTextGUI(), "", "Delete request?", MessageDialogButton.Yes, MessageDialogButton.No);
                if (btn == MessageDialogButton.Yes) {
                    controller.removeRequest(name);
                    refreshCollectionListbox();
                }
                break;
            case F5:
                final TerminalSize size = getTextGUI().getScreen().getTerminalSize();
                final WaitingDialog dialog = WaitingDialog.showDialog(getTextGUI(),"", "Sending request");
                dialog.setPosition(new TerminalPosition(size.getColumns()/2-10, size.getRows()/2));
                new Thread(() -> { // dialog is modal, so we have to close() it from another thread
                    final ResponseData response = controller.sendRequest(url, method, body, getHeaders());
                    final String status = response.status > 0 ? String.valueOf(response.status) : "ERROR";
                    responseTextbox.setText(String.format("Status: %s    Elapsed time: %d msec\n\n%s", status, response.elapsedTimeMsec, response.response));
                    dialog.close();
                }).start();
                break;
            case F10:
                close();
                break;
        }
    }

    /**
     * Adds a new Header-Key and Header-Value textboxes into HeaderPanel
     */
    private void addHeader(String key, String value) {
        headersPanel.addComponent(new TextBox(new TerminalSize(16, 1), key).withBorder(Borders.singleLine(" Header name ")));
        headersPanel.addComponent(new TextBox(value).withBorder(Borders.singleLine(" Header value ")), GridLayout.createHorizontallyFilledLayoutData());
    }

    /**
     * @return headers from GUI expressed as Map(key -> value)
     */
    private Map<String, String> getHeaders() {
        final List<String> headerList = headersPanel
                .getChildrenList()
                .stream()
                .filter(c -> c instanceof Border)
                .map(b -> ((Border) b).getComponent())
                .filter(b -> b instanceof TextBox)
                .map(t -> ((TextBox) t).getText())
                .collect(Collectors.toList());
        return listToMap(headerList);
    }

    /**
     * Converts List to Map by unzipping list values. List must contain even count of elements.
     * Example: ["1", "a", "2", "b"] => {"1"->"a", "2"->"b"}
     */
    private <T> Map<T, T> listToMap(List<T> list) {
        final int length = list.size();
        if (length % 2 == 1) throw new IllegalArgumentException("List size must be even");

        final Map<T, T> result = new HashMap<>(length /2);
        for (int i = 0; i < length; i+=2) {
            result.put(list.get(i), list.get(i+1));
        }
        return result;
    }
}
