package utils.ui;

import javafx.scene.control.TableView;

public class GeneralUtils {

    // Scrolls the table so that the given row index will be centered
    public static void scrollToCenter(TableView<?> table, int rowIndex) {
        if (rowIndex < 0 || rowIndex >= table.getItems().size()) {
            return; // invalid index
        }

        double tableHeight = table.getHeight();
        double rowHeight = table.getFixedCellSize() > 0
                ? table.getFixedCellSize()
                : 24; // default row height if not set
        int visibleRows = (int) (tableHeight / rowHeight);

        int targetIndex = Math.max(0, rowIndex - visibleRows / 2);
        table.scrollTo(targetIndex);
    }
}
