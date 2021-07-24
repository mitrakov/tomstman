package com.mitrakoff.tomstman.view;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.input.KeyStroke;
import static java.lang.Math.*;

@SuppressWarnings({"WeakerAccess", "SameParameterValue", "OptionalUsedAsFieldOrParameterType"})
public class MainWindow extends BasicWindow {
    static private final LayoutData FILL = LinearLayout.createLayoutData(LinearLayout.Alignment.Fill);
    static private final LayoutData FILL_GROW = LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow);
    static private final int BODY_LINES = 4;
    static private final String INITIAL_JSON = "{}";
    static private final String ADD_HEADER = "<F3> Add header";
    static private final String REMOVE_HEADER = "<F3> Del header";
    static private final RequestData EMPTY_REQUEST = new RequestData("", "https://", "GET", INITIAL_JSON, "", Collections.emptyMap());

    private final Controller controller;
    private final ActionListBox collectionListbox;
    private final TextBox urlTextBox;
    private final ComboBox<String> methodCombobox;
    private final TextBox bodyTextbox;
    private final TextBox jmesTextbox;
    private final Label statusLabel;
    private final TextBox responseTextbox;
    private final Panel headersPanel;
    private final Label addHeaderLabel;

    private RequestData currentRequest = EMPTY_REQUEST;
    private Optional<String> currentHeader = Optional.empty();

    public MainWindow(String title, Controller controller) {
        super(title);
        this.controller = controller;
        urlTextBox = new TextBox("https://");
        methodCombobox = new ComboBox<>("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH");
        bodyTextbox = new TextBox(new TerminalSize(0, BODY_LINES), INITIAL_JSON, TextBox.Style.MULTI_LINE);
        jmesTextbox = new TextBox();
        statusLabel = new Label("").setForegroundColor(TextColor.ANSI.RED_BRIGHT);
        responseTextbox = new TextBox("", TextBox.Style.MULTI_LINE).setReadOnly(true);
        collectionListbox = new ActionListBox();
        addHeaderLabel = new Label(ADD_HEADER);
        refreshCollectionListbox();

        // panel with Method and URL
        final Panel methodUrlPanel = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(0));
        methodUrlPanel.addComponent(methodCombobox.withBorder(Borders.singleLine(" Method ")), FILL);
        methodUrlPanel.addComponent(urlTextBox.withBorder(Borders.singleLine(" URL ")), FILL_GROW);

        // panel with Headers
        headersPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        // panel with Method/URL and headers
        final Panel methodUrlHeadersPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        methodUrlHeadersPanel.addComponent(methodUrlPanel, FILL);
        methodUrlHeadersPanel.addComponent(headersPanel, FILL_GROW);

        // request panel (main area)
        final Panel requestPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        requestPanel.addComponent(methodUrlHeadersPanel, FILL);
        requestPanel.addComponent(bodyTextbox.withBorder(Borders.singleLine(" Json Body ")), FILL);
        requestPanel.addComponent(jmesTextbox.withBorder(Borders.singleLine(" Jmes Path ")), FILL);
        requestPanel.addComponent(responseTextbox.withBorder(Borders.singleLine(" Response ")), FILL_GROW);
        requestPanel.addComponent(statusLabel, FILL);

        // shortcuts panel
        final Panel shortcutsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(3));
        shortcutsPanel.addComponent(new Label(" <F2> Save request").setForegroundColor(TextColor.ANSI.CYAN_BRIGHT));
        shortcutsPanel.addComponent(addHeaderLabel.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT));
        shortcutsPanel.addComponent(new Label("<F4> New request").setForegroundColor(TextColor.ANSI.CYAN_BRIGHT));
        shortcutsPanel.addComponent(new Label("<F5> Send request").setForegroundColor(TextColor.ANSI.CYAN_BRIGHT));
        shortcutsPanel.addComponent(new Label("<F6> Copy to clipboard").setForegroundColor(TextColor.ANSI.CYAN_BRIGHT));
        shortcutsPanel.addComponent(new Label("<F8> Remove request").setForegroundColor(TextColor.ANSI.CYAN_BRIGHT));
        shortcutsPanel.addComponent(new Label("<F10> Exit").setForegroundColor(TextColor.ANSI.CYAN_BRIGHT));
        shortcutsPanel.addComponent(new Label("<F11> Up").setForegroundColor(TextColor.ANSI.CYAN_BRIGHT));
        shortcutsPanel.addComponent(new Label("<F12> Down").setForegroundColor(TextColor.ANSI.CYAN_BRIGHT));

        // shortcuts + version panel
        final Panel shortcutsVersionPanel = new Panel(new BorderLayout());
        shortcutsVersionPanel.addComponent(shortcutsPanel, BorderLayout.Location.LEFT);
        shortcutsVersionPanel.addComponent(buildVersionComponent(), BorderLayout.Location.RIGHT);

        // content panel
        final Panel contentPanel = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(0));
        contentPanel.addComponent(collectionListbox.withBorder(Borders.singleLine(" Request Collection ")), FILL);
        contentPanel.addComponent(requestPanel.withBorder(Borders.singleLine(" Request ")), FILL_GROW);

        // main panel
        final Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.addComponent(shortcutsVersionPanel.withBorder(Borders.singleLine(" T O M S T M A N ")), FILL);
        mainPanel.addComponent(contentPanel, FILL_GROW);

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

    private void setDataToComponents(RequestData request) {
        saveRequestIfNeeded();
        currentRequest = request;

        urlTextBox.setText(request.url);
        methodCombobox.setSelectedItem(request.method);
        bodyTextbox.setText(request.jsonBody);
        jmesTextbox.setText(request.jmesPath);
        responseTextbox.setText("");
        updateHeaders(request.headers);
    }

    private void updateHeaders(Map<String, String> headers) {
        headersPanel.removeAllComponents();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            addHeader(entry.getKey(), entry.getValue());
        }
        // this code is to avoid a bug when the focus was inside "headersPanel" and we removed all headers 4 lines above
        if (!collectionListbox.isFocused())
            urlTextBox.takeFocus();
    }

    private void saveRequestIfNeeded() {
        final RequestData updatedRequest = makeRequest(currentRequest.name);
        if (!currentRequest.equals(updatedRequest)) {
            if (currentRequest.equals(EMPTY_REQUEST)) {
                saveEmptyRequest(updatedRequest, "Save new request?");
            } else {
                final MessageDialogButton btn = MessageDialog.showMessageDialog(getTextGUI(), "", String.format("Save request '%s'?", currentRequest.name), MessageDialogButton.Yes, MessageDialogButton.No);
                if (btn == MessageDialogButton.Yes)
                    saveRequest(updatedRequest);
            }
        }
    }

    private void saveEmptyRequest(RequestData request, String title) {
        final String newName = TextInputDialog.showDialog(getTextGUI(), String.format(" %s ", title), "Input the request name", suggestName(request.url));
        final boolean isOkPressed = newName != null;
        if (isOkPressed) {
            if (!newName.isEmpty()) saveRequest(makeRequest(newName));
            else MessageDialog.showMessageDialog(getTextGUI(), "Error", "Name must not be empty");
        }
    }

    private void saveRequest(RequestData request) {
        controller.saveRequest(request);
        refreshCollectionListbox();
        currentRequest = request;
    }

    /**
     * Shortcut events processor
     */
    private void handleHotkeys(KeyStroke keyStroke) {
        final RequestData request = makeRequest(currentRequest.name);
        switch (keyStroke.getKeyType()) {
            case F2: {
                if (currentRequest.equals(EMPTY_REQUEST)) {
                    saveEmptyRequest(request, "New request");
                } else if (!currentRequest.equals(request)) {
                    final DialogWindow dialog = buildDialog("", String.format("Saving request '%s'", request.name));
                    new Timer().schedule(new TimerTask() {public void run() { dialog.close();}}, 1000);
                    saveRequest(request);
                }
                break;
            }
            case F3: {
                if (currentHeader.isPresent()) {
                    removeHeader(currentHeader.get());
                } else addHeader("", "");
                break;
            }
            case F4: {
                setDataToComponents(EMPTY_REQUEST);
                break;
            }
            case F5: {
                final DialogWindow sendDialog = buildDialog("", "Sending request");
                new Thread(() -> { // dialog is modal, so we have to close() it from another thread
                    final ResponseData response = controller.sendRequest(request);
                    final String status = response.status > 0 ? String.valueOf(response.status) : "ERROR";
                    statusLabel.setText(String.format("Status: %s    Elapsed time: %d msec", status, response.elapsedTimeMsec));
                    responseTextbox.setText(response.response);
                    sendDialog.close();
                }).start();
                break;
            }
            case F6: {
                final String response = responseTextbox.getText();
                final DialogWindow clipboardDialog = buildDialog("", "Copying to clipboard");
                new Timer().schedule(new TimerTask() {public void run() { clipboardDialog.close();}}, 1000);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(response), null);
                break;
            }
            case F8: {
                final int selectedIdx = collectionListbox.getSelectedIndex();
                if (selectedIdx >= 0) {
                    final String name = controller.getRequests().get(selectedIdx).name;
                    final MessageDialogButton btn = MessageDialog.showMessageDialog(getTextGUI(), "", String.format("Delete request '%s'?", name), MessageDialogButton.Yes, MessageDialogButton.No);
                    if (btn == MessageDialogButton.Yes) {
                        controller.removeRequest(name);
                        refreshCollectionListbox();
                        if (name.equals(currentRequest.name))
                            currentRequest = EMPTY_REQUEST;
                    }
                }
                break;
            }
            case F10: {
                saveRequestIfNeeded();
                close();
                break;
            }
            case F11: {
                final int selectedIdx = collectionListbox.getSelectedIndex();
                if (selectedIdx >= 1) {
                    controller.moveRequestUp(selectedIdx);
                    refreshCollectionListbox();
                }
                break;
            }
            case F12: {
                final int selectedIdx = collectionListbox.getSelectedIndex();
                if (0 <= selectedIdx && selectedIdx < collectionListbox.getItemCount() - 1) {
                    controller.moveRequestDown(selectedIdx);
                    refreshCollectionListbox();
                }
                break;
            }
        }
    }

    private RequestData makeRequest(String name) {
        final String url = urlTextBox.getText();
        final String method = methodCombobox.getText();
        final String body = bodyTextbox.getText();
        final String jmesPath = jmesTextbox.getText();
        return new RequestData(name, url, method, body, jmesPath, getHeaders());
    }

    /**
     * Adds a new Header-Key and Header-Value textboxes into HeaderPanel
     */
    private void addHeader(String key, String value) {
        final TextBox headerKey = new TextBox(new TerminalSize(16, 1), key) {
            @Override
            protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                super.afterEnterFocus(direction, previouslyInFocus);
                currentHeader = Optional.of(this.getText());
                addHeaderLabel.setText(REMOVE_HEADER);
            }

            @Override
            protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
                super.afterLeaveFocus(direction, nextInFocus);
                currentHeader = Optional.empty();
                addHeaderLabel.setText(ADD_HEADER);
            }
        };
        final TextBox headerValue = new TextBox(value) {
            @Override
            protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                super.afterEnterFocus(direction, previouslyInFocus);
                currentHeader = Optional.of(headerKey.getText());
                addHeaderLabel.setText(REMOVE_HEADER);
            }

            @Override
            protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
                super.afterLeaveFocus(direction, nextInFocus);
                currentHeader = Optional.empty();
                addHeaderLabel.setText(ADD_HEADER);
            }
        };

        final Panel row = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(0));
        row.addComponent(headerKey.withBorder(Borders.singleLine(" Header name ")), FILL);
        row.addComponent(headerValue.withBorder(Borders.singleLine(" Header value ")), FILL_GROW);
        headersPanel.addComponent(row, FILL_GROW);
    }

    private void removeHeader(String headerKey) {
        final Map<String, String> headers = getHeaders();
        headers.remove(headerKey);
        updateHeaders(headers);
    }

    /**
     * @return headers from GUI expressed as Map(key -> value)
     */
    private Map<String, String> getHeaders() {
        final List<String> headerList = getChildrenDeep(headersPanel)
                .stream()
                .filter(c -> c instanceof TextBox)
                .map(t -> ((TextBox)t).getText())
                .collect(Collectors.toList());
        return listToMap(headerList);
    }

    /**
     * @return all children of a given container recursively
     */
    private Collection<Component> getChildrenDeep(Container root) {
        final Collection<Component> result = root.getChildren();
        for (Component e : root.getChildren()) {
            if (e instanceof Container) {
                result.addAll(getChildrenDeep((Container) e));
            }
        }
        return result;
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

    /**
     * @return a label with current app version
     */
    private Component buildVersionComponent() {
        String version = getClass().getPackage() != null
            ? getClass().getPackage().getImplementationVersion() != null
                ? "v" + getClass().getPackage().getImplementationVersion()
                : "-"
            : "-";
        final Label label = new Label(version).setForegroundColor(TextColor.ANSI.GREEN);
        label.setPreferredSize(new TerminalSize(label.getText().length()+1, 1));
        return label;
    }

    /**
     * Suggests a request name by the URI
     */
    private String suggestName(String url) {
        // very basic implementation; need to revise later
        final String urlNoProtocol = url.replace("http://", "").replace("https://", "");
        final String shortUrl = urlNoProtocol.substring(0, min(urlNoProtocol.length(), 32));
        final String[] p = shortUrl.split("/");
        return p.length > 0 ? p[0] : shortUrl;
    }

    private WaitingDialog buildDialog(String title, String message) {
        final TerminalSize size = getTextGUI().getScreen().getTerminalSize();
        final WaitingDialog dialog = WaitingDialog.showDialog(getTextGUI(), title, String.format("%s ", message));
        dialog.setPosition(new TerminalPosition(size.getColumns()/2-message.length()/2, size.getRows()/2));
        return dialog;
    }
}
