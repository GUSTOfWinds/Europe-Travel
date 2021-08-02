import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class TwoTextInputDialog extends Alert {
    private TextField nameField = new TextField();
    private TextField timeField = new TextField();

    public TwoTextInputDialog(boolean disabled, boolean disableTextOnly, String nameText, int timeAmount, String place1Name, String place2Name) {
        super(AlertType.CONFIRMATION);
        GridPane grid = new GridPane();
        grid.addRow(0, new Label("Name: "), nameField);
        grid.addRow(1, new Label("Time"), timeField);
        setTitle("Connection");
        setHeaderText("Connection from " + place1Name + " to " + place2Name);

        getDialogPane().setContent(grid);
        nameField.setDisable(disabled);
        if(!disableTextOnly){
            timeField.setDisable(disabled);
        }
        if(disabled){
            nameField.setText(nameText);
            if(!disableTextOnly){
                timeField.setText(Integer.toString(timeAmount));
            }
        }
    }
    public String getName() {
        return nameField.getText();
    }
    public int getTime() {
        return Integer.parseInt(timeField.getText());
    }
}