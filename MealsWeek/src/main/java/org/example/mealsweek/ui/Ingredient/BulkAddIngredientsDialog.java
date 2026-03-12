package org.example.mealsweek.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.*;
import java.util.stream.Collectors;

public class BulkAddIngredientsDialog extends Dialog<List<String>> {

    private final TableView<Row> table = new TableView<>();
    private final ObservableList<Row> rows = FXCollections.observableArrayList();

    private final Label duplicatesLabel = new Label("");
    private final SimpleBooleanProperty hasDuplicates = new SimpleBooleanProperty(false);

    private Node addBtn;
    private ButtonType addBtnType;

    public BulkAddIngredientsDialog() {
        setTitle("Добавить несколько ингредиентов");
        setHeaderText("Заполняй названия. Enter — следующая строка. Ctrl+V — можно вставить много строк.");

        addBtnType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addBtnType, ButtonType.CANCEL);

        addBtn = getDialogPane().lookupButton(addBtnType);
        addBtn.setDisable(true);

        configureWarnings();
        configureTable();

        // старт: 1 пустая строка
        rows.add(new Row(""));
        table.setItems(rows);

        // подписки на изменения строк
        rows.forEach(r -> r.valueProperty().addListener((obs, o, n) -> refreshState()));
        rows.addListener((ListChangeListener<Row>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Row r : change.getAddedSubList()) {
                        r.valueProperty().addListener((obs, o, n) -> refreshState());
                    }
                }
            }
            refreshState();
        });

        VBox root = new VBox(10, table, duplicatesLabel, buildHint());
        root.setPadding(new Insets(10));
        root.setPrefSize(700, 460);
        getDialogPane().setContent(root);

        // Результат
        setResultConverter(bt -> {
            if (bt != addBtnType) return null;

            refreshState(); // на всякий случай
            if (hasDuplicates.get()) return null;

            List<String> result = collectUniqueNames();
            return result.isEmpty() ? null : result;
        });

        Platform.runLater(() -> {
            table.requestFocus();
            table.getSelectionModel().select(0);
            table.edit(0, table.getColumns().get(0));
        });
    }

    private void configureTable() {
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle("-fx-font-size: 16px;");

        TableColumn<Row, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(cd -> cd.getValue().valueProperty());

        nameCol.setCellFactory(col -> new TextFieldTableCell<Row, String>(new StringConverter<>() {
            @Override public String toString(String object) { return object == null ? "" : object; }
            @Override public String fromString(String string) { return string; }
        }) {
            private TextField textField;

            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    // TextField создаётся внутри TextFieldTableCell только во время редактирования.
                    // Берём его как графику, когда редактирование началось.
                    Node g = getGraphic();
                    if (g instanceof TextField tf) {
                        textField = tf;
                    }
                }
            }

            {
                // ловим Enter только когда ячейка в режиме редактирования
                addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ENTER && isEditing()) {
                        e.consume();

                        String value = (textField != null) ? textField.getText() : getItem();
                        commitEdit(value);        // ✅ сохраняем введённое
                        ensureTrailingEmptyRow(); // ✅ если надо — добавим пустую строку
                        refreshState();           // ✅ обновим дубли/кнопку

                        int rowIndex = getIndex();
                        goNextRow(rowIndex);      // ✅ сразу в следующую строку
                    }
                });
            }
        });

        nameCol.setOnEditCommit(ev -> {
            Row row = ev.getRowValue();
            row.setValue(ev.getNewValue());
            ensureTrailingEmptyRow();
            refreshState();
        });

        table.getColumns().add(nameCol);
        table.setItems(rows);

        // Ctrl+V много строк — ловим на таблице (надёжнее, чем на cell)
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.V && e.isControlDown()) {
                Clipboard clip = Clipboard.getSystemClipboard();
                if (clip.hasString()) {
                    String txt = clip.getString();
                    if (txt != null && txt.contains("\n")) {
                        e.consume();
                        int start = Math.max(0, table.getSelectionModel().getSelectedIndex());
                        pasteMultiline(start, txt);
                    }
                }
            }
        });

        // подсветка дублей
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Row item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                    return;
                }

                if (item.duplicateProperty().get()) {
                    setStyle("-fx-background-color: rgba(239,68,68,0.18);");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configureWarnings() {
        duplicatesLabel.setWrapText(true);
        duplicatesLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 800; -fx-font-size: 14px;");
        duplicatesLabel.setVisible(false);
        duplicatesLabel.setManaged(false);
    }

    private Node buildHint() {
        Label hint = new Label("Подсказка: можно вставлять сразу много строк (Ctrl+V) — каждая строка станет отдельной записью.");
        hint.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
        return hint;
    }

    private void refreshState() {
        ensureTrailingEmptyRow();
        validateDuplicates();

        boolean hasAtLeastOne = rows.stream().anyMatch(r -> normalize(r.getValue()) != null);
        addBtn.setDisable(!hasAtLeastOne || hasDuplicates.get());
    }

    private void validateDuplicates() {
        rows.forEach(r -> r.duplicateProperty().set(false));
        hasDuplicates.set(false);

        Map<String, List<Row>> grouped = rows.stream()
                .filter(r -> normalize(r.getValue()) != null)
                .collect(Collectors.groupingBy(r -> normalize(r.getValue()).toLowerCase(Locale.ROOT)));

        List<String> dupNames = new ArrayList<>();

        for (var e : grouped.entrySet()) {
            if (e.getValue().size() > 1) {
                hasDuplicates.set(true);
                dupNames.add(e.getValue().get(0).getValue().trim());
                e.getValue().forEach(r -> r.duplicateProperty().set(true));
            }
        }

        if (hasDuplicates.get()) {
            duplicatesLabel.setText("Есть дубликаты (удали или переименуй): " + String.join(", ", dupNames));
            duplicatesLabel.setVisible(true);
            duplicatesLabel.setManaged(true);
        } else {
            duplicatesLabel.setText("");
            duplicatesLabel.setVisible(false);
            duplicatesLabel.setManaged(false);
        }

        table.refresh();
    }

    private void ensureTrailingEmptyRow() {
        if (rows.isEmpty()) {
            rows.add(new Row(""));
            return;
        }

        Row last = rows.get(rows.size() - 1);
        if (normalize(last.getValue()) != null) {
            rows.add(new Row(""));
        }

        // оставить только одну пустую в конце
        while (rows.size() >= 2) {
            Row a = rows.get(rows.size() - 1);
            Row b = rows.get(rows.size() - 2);
            if (normalize(a.getValue()) == null && normalize(b.getValue()) == null) {
                rows.remove(rows.size() - 1);
            } else break;
        }
    }

    private void goNextRow(int currentRowIndex) {
        ensureTrailingEmptyRow();

        int next = Math.min(currentRowIndex + 1, rows.size() - 1);

        Platform.runLater(() -> {
            table.requestFocus();
            table.getSelectionModel().clearAndSelect(next);
            table.scrollTo(next);
            table.layout(); // помогает “оживить” таблицу после коммита/добавления строки
            table.edit(next, table.getColumns().get(0));
        });
    }

    private void pasteMultiline(int startIndex, String txt) {
        List<String> lines = Arrays.stream(txt.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        if (lines.isEmpty()) return;

        ensureTrailingEmptyRow();

        int idx = Math.max(0, startIndex);
        for (String line : lines) {
            if (idx >= rows.size()) rows.add(new Row(""));
            rows.get(idx).setValue(line);
            idx++;
        }

        ensureTrailingEmptyRow();
        refreshState();

        int next = Math.min(idx, rows.size() - 1);
        table.getSelectionModel().select(next);
        table.scrollTo(next);
        Platform.runLater(() -> table.edit(next, table.getColumns().get(0)));
    }

    private List<String> collectUniqueNames() {
        var seen = new HashSet<String>();
        List<String> result = new ArrayList<>();

        for (Row r : rows) {
            String name = normalize(r.getValue());
            if (name == null) continue;

            String key = name.toLowerCase(Locale.ROOT);
            if (seen.add(key)) result.add(name);
        }
        return result;
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    public static class Row {
        private final SimpleStringProperty value = new SimpleStringProperty("");
        private final SimpleBooleanProperty duplicate = new SimpleBooleanProperty(false);

        public Row(String v) { value.set(v); }

        public SimpleStringProperty valueProperty() { return value; }
        public String getValue() { return value.get(); }
        public void setValue(String v) { value.set(v == null ? "" : v); }

        public SimpleBooleanProperty duplicateProperty() { return duplicate; }
    }
}