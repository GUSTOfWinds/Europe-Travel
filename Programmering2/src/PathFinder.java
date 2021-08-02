import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PathFinder extends Application{

    //Static Finals To Avoid Magic Number Usage
    private static final double PADHEIGHT = 90;
    private static final double PADWIDTH = 16;
    private static final double PADNAME = 7;

    //Base object of ListGraph, for all the graphing functionality
    private ListGraph<Place> graph = new ListGraph<>();

    //Base layout for the scene itself
    private Stage primaryStage = new Stage();
    private BorderPane root = new BorderPane();
    private Scene scene = new Scene(root);

    //Additional Scene Layout - Menus
    private VBox menuBox = new VBox();
    private MenuBar fileMenuBar = new MenuBar();
    private Menu fileMenu = new Menu("File");
    private MenuItem newMapItem = new MenuItem("New Map");
    private MenuItem openItem = new MenuItem("Open");
    private MenuItem saveItem = new MenuItem("Save");
    private MenuItem saveImageItem = new MenuItem("Save Image");
    private MenuItem exitItem = new MenuItem("Exit");

    //Additional Scene Layout - Buttons
    private Pane buttonPane = new FlowPane();
    private Button findPathButton = new Button("Find Path");
    private Button showConnectionButton = new Button("Show Connection");
    private Button newPlaceButton = new Button("New Place");
    private Button newConnectionButton = new Button("New Connection");
    private Button changeConnectionButton = new Button("Change Connection");

    //Additional Scene Layout - Map
    private Pane imagePane = new Pane();
    private ImageView mapView = new ImageView();
    private Image mapImage = new Image("europa.gif");

    //Information Holding - Lists, objects and similar to hold information for the methods & handlers to use
    private Place place1;
    private Place place2;
    private boolean changesSaved = true;
    private ArrayList<Place> placeList = new ArrayList<>();
    private ArrayList<Connection> connectionList = new ArrayList<>();

    //This is executed when "main" begins, and is how the program starts
    @Override
    public void start(Stage primaryStage) {

        //Sets the stage as well as its starting size
        this.primaryStage = primaryStage;
        primaryStage.setWidth(mapImage.getWidth() + PADWIDTH);

        //Sets all the ID's of the respective components to the assignment specified ID's
        fileMenuBar.setId("menu");
        fileMenu.setId("menuFile");
        newMapItem.setId("menuNewMap");
        openItem.setId("menuOpenFile");
        saveItem.setId("menuSaveFile");
        saveImageItem.setId("menuSaveImage");
        exitItem.setId("menuExit");
        findPathButton.setId("btnFindPath");
        showConnectionButton.setId("btnShowConnection");
        newPlaceButton.setId("btnNewPlace");
        changeConnectionButton.setId("btnChangeConnection");
        newConnectionButton.setId("btnNewConnection");
        imagePane.setId("outputArea");
        primaryStage.setTitle("PathFinder");

        //Sets all the menu functionality in the top of the root BorderPane, including the menus themselves & their handlers
        root.setTop(menuBox);
        menuBox.getChildren().add(fileMenuBar);
        fileMenuBar.getMenus().add(fileMenu);
        fileMenu.getItems().add(newMapItem);
        newMapItem.setOnAction(new NewMapHandler());
        fileMenu.getItems().add(openItem);
        openItem.setOnAction(new OpenHandler());
        fileMenu.getItems().add(saveItem);
        saveItem.setOnAction(new SaveHandler());
        fileMenu.getItems().add(saveImageItem);
        saveImageItem.setOnAction(new SaveImageHandler());
        fileMenu.getItems().add(exitItem);
        exitItem.setOnAction(new CloseHandler());

        //Sets all the button functionality in the center of the root BorderPane, including the buttons themselves, & their handlers
        root.setCenter(buttonPane);
        buttonPane.getChildren().add(findPathButton);
        findPathButton.setOnAction(new FindPathHandler());
        buttonPane.getChildren().add(showConnectionButton);
        showConnectionButton.setOnAction(new ShowConnectionHandler());
        buttonPane.getChildren().add(newPlaceButton);
        newPlaceButton.setOnAction(new NewPlaceHandler());
        buttonPane.getChildren().add(newConnectionButton);
        newConnectionButton.setOnAction(new NewConnectionHandler());
        buttonPane.getChildren().add(changeConnectionButton);
        changeConnectionButton.setOnAction(new ChangeConnectionHandler());

        //Disables all the buttons, as they should not be clickable until a Map is loaded
        findPathButton.setDisable(true);
        showConnectionButton.setDisable(true);
        newPlaceButton.setDisable(true);
        newConnectionButton.setDisable(true);
        changeConnectionButton.setDisable(true);

        //Sets the imagePane as the bottom of the BorderPane, in preparation to display the map there later
        root.setBottom(imagePane);

        //Sets the scene, sets a closing handler, and then shows everything to the user
        primaryStage.setOnCloseRequest(new ExitHandler());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //This Launches The Program
    public static void main(String[] args) {
        launch(args);
    }

    //Two-Parter Handler. Primarily, it gives us a new map, but it should also reset/clear the current map to make way for said new one
    class NewMapHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {

            //Check: If User Wants To Proceed Without Saving
            if(!changesSaved){
                if(!continueWithoutSaving()){
                    return;
                }
            }

            //This part of the handler resets the map pane, as well as the graph's hashmap
            imagePane.getChildren().clear();
            graph.resetGraph();
            connectionList.clear();
            placeList.clear();
            place1 = null;
            place2 = null;

            //This part of the handler puts in a new map
            imagePane.getChildren().add(mapView);
            mapView.setImage(mapImage);
            primaryStage.setHeight(mapImage.getHeight() + PADHEIGHT);
            primaryStage.centerOnScreen();

            //This enables all the buttons, which are disabled by default when the program first begins
            findPathButton.setDisable(false);
            showConnectionButton.setDisable(false);
            newPlaceButton.setDisable(false);
            newConnectionButton.setDisable(false);
            changeConnectionButton.setDisable(false);

            //New Map means the user has unsaved changes, or atleast according to the VPL
            changesSaved = false;
        }
    }

    //This begins the process in a 2-Handler process, of preparing the map to expect clicks from the user
    class NewPlaceHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {

            //First, we disable the button & change the cursor
            newPlaceButton.setDisable(true);
            imagePane.setCursor(Cursor.CROSSHAIR);

            //Then, we prepare the imagePane to expect a click from the user with a new Handler
            imagePane.setOnMouseClicked(new MapClickHandler());
        }
    }

    //This Handler is called from NewPlaceHandler, and handles the actual click the user makes to put down a Place
    class MapClickHandler implements EventHandler<MouseEvent>{
        @Override
        public void handle(MouseEvent mouseEvent) {

            //This gets the position the user clicks, and stores it for later
            double x = mouseEvent.getX();
            double y = mouseEvent.getY();

            //Then we create a textInput for the user to select a name
            TextInputDialog placeName = new TextInputDialog();
            placeName.setTitle("Name");
            placeName.setHeaderText("Please Enter The Following");
            placeName.setContentText("Name of place:");
            var result = placeName.showAndWait();

            //We collect the result. If a result is present, we then go through with the actual placement itself
            if(result.isPresent()) {
                //First, we get the name the user entered, and create a Place using it & earlier position data
                String name = placeName.getEditor().getText();
                Place newPlace = new Place(name, x, y);

                //Then, we add the Place to graph's "nodes" SetList & to the map itself, and set it's ID
                graph.add(newPlace);
                placeList.add(newPlace);
                imagePane.getChildren().add(newPlace);
                newPlace.setId(name);

                //Afterwards, we create a label out of the name, and add it just underneath the place's position
                Label labelPlace = new Label(name);
                labelPlace.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
                labelPlace.setTextFill(Color.BLACK);
                labelPlace.setLayoutX(x);
                labelPlace.setLayoutY(y + PADNAME);
                imagePane.getChildren().add(labelPlace);


                //Now that we know the user has added a place, we update the changesSaved boolean accordingly
                changesSaved = false;

                //Finally, we disable the label to avoid click issues, and give the Place a ClickHandler so the user can select them
                labelPlace.setDisable(true);
                newPlace.setOnMouseClicked(new PlaceClickHandler());

            }
            //Regardless if the user went through with the placement, we want to reset the imagePane & button back to its original state
            newPlaceButton.setDisable(false);
            imagePane.setCursor(Cursor.DEFAULT);
            imagePane.setOnMouseClicked(null);
        }
    }

    //This handler lets the user select up to 2 places, and stores those places for other Handlers to check & use
    class PlaceClickHandler implements EventHandler<MouseEvent>{
        @Override
        public void handle(MouseEvent mouseEvent) {

            //First, this gets the place that the user clicked
            Place place = (Place) mouseEvent.getSource();

            //Then this if/else chain either unselects or selects with a setSelected call of true or false
            //ALT: It doesn't allow the selection of a place if 2 places are already selected
            if(place.isSelected()){
                place.setSelected(false);
                if(place == place1){
                    place1 = null;
                }else{
                    place2 = null;
                }
            }else{
                if(place1 == null){
                    place1 = place;
                    place.setSelected(true);
                }else if(place2 == null){
                    place2 = place;
                    place.setSelected(true);
                }
            }
        }
    }

    class NewConnectionHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {

            //Error Checks, to see if the operation is legal.
            if(place1 == null || place2 == null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An Error Occured");
                alert.setContentText("Two places must be selected!");
                alert.showAndWait();
            }else if(graph.getEdgeBetween(place1, place2) != null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An Error Occured");
                alert.setContentText("Connection already exists");
                alert.showAndWait();
                //If the operation is legal, then we run the following
            }else{

                //First, create two variables to store the data for the try/catch function
                String name;
                int time;

                try{
                    //We create a Input Dialog with two text boxes, given the specifications of:
                    //No disables boxes, no default text, and showing text stating a Connection from place1 to place 2
                    TwoTextInputDialog input = new TwoTextInputDialog(false, false, "Null", 0, place1.getName(), place2.getName());
                    input.showAndWait();

                    //We get the user's inputs
                    name = input.getName();
                    time = input.getTime();

                    //If the user inputs incorrectly, then we catch the exception, create an error message, then end the handler operation with return;
                } catch (NumberFormatException e){
                    Alert a = new Alert(Alert.AlertType.ERROR, "Fel Imatning!");
                    a.showAndWait();
                    return;
                }

                //Using the inputs gotten, we connect the two places with the appropriate name and weight(time)
                graph.connect(place1, place2, name, time);
                Connection newLine = new Connection(place1.getName(), place2.getName(), name, time);
                connectionList.add(newLine);

                //In addition to the graph connection, we also create a line between the two places that we add onto the map
                Line connectionLine = new Line(place1.getX(), place1.getY(), place2.getX(), place2.getY());
                imagePane.getChildren().add(connectionLine);
                connectionLine.setDisable(true);

                //Now that a new connection exists, the user has unsaved changes. So, we update the boolean
                changesSaved = false;
            }
        }
    }

    class ShowConnectionHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {

            //We check if the operation is legal, eg: Has the user selected two places, and do they have a connection?
            if(place1 == null || place2 == null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An Error Occured");
                alert.setContentText("Two places must be selected!");
                alert.showAndWait();
            }else if(graph.getEdgeBetween(place1, place2) == null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An Error Occured");
                alert.setContentText("Connection doesn't exist");
                alert.showAndWait();
            }else{

                //We collect the name & time using getEdgeBetween
                String name = graph.getEdgeBetween(place1, place2).getName();
                int time = graph.getEdgeBetween(place1, place2).getWeight();

                //We create an input Dialog with the specifications of:
                //Both boxes are disables, and have default values displayed to the user. The title text reads "From place1 to place2"
                TwoTextInputDialog display = new TwoTextInputDialog(true, false, name, time, place1.getName(), place2.getName());
                display.showAndWait();
            }
        }
    }

    class ChangeConnectionHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {

            //We check if the operation is legal. If not; Error Alert. If it is, we continue with the code
            if(place1 == null || place2 == null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An Error Occured");
                alert.setContentText("Two places must be selected!");
                alert.showAndWait();
            }else if(graph.getEdgeBetween(place1, place2) == null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An Error Occured");
                alert.setContentText("Connection doesn't exist");
                alert.showAndWait();
            }else{

                //We get the Edge's name, and it's time value
                String name = graph.getEdgeBetween(place1, place2).getName();
                int time = graph.getEdgeBetween(place1, place2).getWeight();
                int newWeight = 0;

                //We create a dialog with two text boxes with the specifications
                //Only the textbox is disabled, the name is already input, the time is empty, and the header says "From place1 to place2"
                TwoTextInputDialog input = new TwoTextInputDialog(true, true, name, time, place1.getName(), place2.getName());

                //We use a try-catch to ensure that any FormatException gets snapped up, and gets a proper error message before ending the operation
                try{
                    input.showAndWait();

                    //We get the user's input, and...
                    newWeight = input.getTime();
                } catch (NumberFormatException e){
                    Alert a = new Alert(Alert.AlertType.ERROR, "Fel Imatning!");
                    a.showAndWait();
                    return;
                }

                //...Use it in a setConnectionWeight method call. The method ensures both edges reflect this updated value.
                graph.setConnectionWeight(place1, place2, newWeight);

                //Now that changes have been made, they haven't been saved. Therefore:
                changesSaved = false;
            }
        }
    }

    class FindPathHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {

            //Errors checks. If the operation isn't legal, it isn't carried through.
            if (place1 == null || place2 == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An Error Occured");
                alert.setContentText("Two places must be selected!");
                alert.showAndWait();
            }else if (!graph.pathExists(place1, place2)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message");
                alert.setHeaderText("No Path Exists");
                alert.setContentText("There is no path between the two selected places");
                alert.showAndWait();
            }else{

                //We create a List of edges, detailing the path between place 1 & 2. The getPath method ensures they are in the correct order
                List<Edge<Place>> pathList = graph.getPath(place1, place2);

                //We create a new alert, and set it's header text & title. We also define variables to use in the upcoming for-loop.
                Alert path = new Alert(Alert.AlertType.INFORMATION);
                path.setTitle("Message");
                path.setHeaderText("The Path from " + place1.getName() + " to " + place2.getName() + ":");
                String content = "";
                int totalWeight = 0;

                //We go through each edge, and add it's relevant information to a new line to the content String. We also increment the total integer.
                for(Edge<Place> currentEdge : pathList){
                    String destination = currentEdge.getDestination().getName();
                    String edgeName = currentEdge.getName();
                    int edgeWeight = currentEdge.getWeight();
                    content += "to " + destination + " by " + edgeName + " takes " + edgeWeight + "\n";
                    totalWeight += edgeWeight;
                }





                //Finally, we add the total integer to the final line, before updating the content text, and displaying the alert to the user.
                content += "Total " + totalWeight;
                TextArea textArea = new TextArea(content);
                path.getDialogPane().setContent(textArea);
                textArea.setEditable(false);
                path.showAndWait();
            }
        }
    }

    //This is a method that returns true or false depending on if the user input ok or cancel in a confirmation dialog, respectivley. Used to check if the user
    //wants to continue without saving, in order to avoid needing to re-write code
    private boolean continueWithoutSaving(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning!");
        alert.setHeaderText("Please Confirm");
        alert.setContentText("Unsaved changes, continue anyway?");
        Optional<ButtonType> bt = alert.showAndWait();
        if(!(bt.get() == ButtonType.OK)){
            return false;
        }
        return true;
    }
    //This method exits/ends the program.
    class CloseHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {

            //This "fires" an event, or rather a window event, to close the current window, eg. the program.
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }
    //Same as CloseHandler, but this handles a WindowEvent(Eg. the X) instead of an ActionEvent (The menu option)
    class ExitHandler implements  EventHandler<WindowEvent>{
        @Override
        public void handle(WindowEvent windowEvent) {

            //Duplicate of NewMap Check - Check: If User Wants To Proceed Without Saving
            if(!changesSaved){
                if(!continueWithoutSaving()){
                    windowEvent.consume();
                }
            }
        }
    }
    //Writes down information into a europa.graph textfile, with the information about places and their connections
    class SaveHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {

            //First we define the file name, and the writers for the writing process
            String fileName = "europa.graph";
            try {
                FileWriter writer = new FileWriter(fileName);
                PrintWriter out = new PrintWriter(writer);

                //We begin by writing the title (eg the image path) & define a new line for the Place information to go, along with a string to append

                //String imageName = mapImage.getUrl();
                //out.println("file:" + imageName);

                out.println("file:europa.gif");

                String placeInformation = "";
                for(Place place: graph.getNodes()){
                    //We define all the variables, then add them to the placeInformation string.
                    //Afterwards, we write out the entire placeInformation string to the next line
                    String name = place.getName();
                    double x = place.getX();
                    double y = place.getY();

                    placeInformation += name + ";" + x + ";" + y + ";";
                }
                out.println(placeInformation);

                //Next, we write out all of the remaining lines one by one for the edges
                //We use a seperate list to store the connections, and write them out twice, so that we get a proper representation of both existing edges
                for(Connection connection: connectionList){
                    out.println(connection.getFrom() + ";" + connection.getTo() + ";" + connection.getName() + ";" + connection.getWeight());
                    out.println(connection.getTo() + ";" + connection.getFrom() + ";" + connection.getName() + ";" + connection.getWeight());
                }

                out.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //If the try/catch worked, that means we now have a saved file! Therefore:
            changesSaved = true;
        }
    }
    //Saves an image of the map
    class SaveImageHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                //We define the "image" to be written out as the bottom of the borderpane, eg. where we store our map
                WritableImage image = root.getBottom().snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

                //We write it out in "png" format, and give it an appropriate name. We don't give a location, so it just defaults to the project map
                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            }catch (IOException e){
                Alert alert = new Alert(Alert.AlertType.ERROR, "IO-Fel " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    //This method goes through a private list of all the places added, and returns a place based on the input name
    private Place placeFinder(String nameOfPlace){
        for(Place place: placeList){
            if(place.getName().equalsIgnoreCase(nameOfPlace)){
                return place;
            }
        }
        return null;
    }

    class OpenHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {

            //Checks if user wants to open, eg. continue without saving any changes
            if(!changesSaved){
                if(!continueWithoutSaving()){
                    return;
                }
            }

            //Before loading anything, the scene is reset, same as with NewMap.
            //However we do not put in a new image yet, as we need information from the fileReader to do that. So this just removes the image for now.
            imagePane.getChildren().clear();
            graph.resetGraph();
            connectionList.clear();
            placeList.clear();
            findPathButton.setDisable(false);
            showConnectionButton.setDisable(false);
            newPlaceButton.setDisable(false);
            newConnectionButton.setDisable(false);
            changeConnectionButton.setDisable(false);
            place1 = null;
            place2 = null;

            //This on it's own counts as unsaved changes, so we can define it already here before importing anything
            changesSaved = false;

            //Starts up the reader to grab the correct file, and defines a basic String line to work with
            try {
                FileReader reader = new FileReader("europa.graph");
                BufferedReader in = new BufferedReader(reader);
                String line = in.readLine();

                //Now that line is the first line, we use this to read the information necessary to know which file to set the image as
                String[] urlArray = line.split(":");
                Image newMap = new Image(urlArray[1]);
                mapView.setImage(newMap);
                imagePane.getChildren().add(mapView);
                //mapView.setImage(mapImage);
                primaryStage.setHeight(mapImage.getHeight() + PADHEIGHT);
                primaryStage.centerOnScreen();



                //We call read once more to move the current line from the "file:europa.gif" line, to the line containing all the node information
                line = in.readLine();


                //Next, we define an array to split up the information into a list, as well as integers to use for a for-loop
                String[] placeArray = line.split(";");
                int placeCount = placeArray.length / 3;
                int currentArrayPos = 0;

                //Next, we run a for-loop equal to the amount of places in the first read line, which we define using placeCount
                for(int i = 0; i < placeCount; i++){

                    //Since the information Goes Name, X-Cord, Y-Cord, we grab, then increment currentArrayPos, in that specific order each time we loop

                    String placeName = placeArray[currentArrayPos];
                    currentArrayPos += 1;
                    double x = Double.parseDouble(placeArray[currentArrayPos]);
                    currentArrayPos += 1;
                    double y = Double.parseDouble(placeArray[currentArrayPos]);

                    //Then we add the place in the exact same way we do when we add a New Place with the button "NewPlace"
                    Place place = new Place(placeName, x, y);
                    graph.add(place);
                    placeList.add(place);
                    imagePane.getChildren().add(place);
                    place.setId(placeName);
                    Label labelPlace = new Label(placeName);
                    labelPlace.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
                    labelPlace.setTextFill(Color.BLACK);
                    labelPlace.setLayoutX(x);
                    labelPlace.setLayoutY(y + PADNAME);
                    imagePane.getChildren().add(labelPlace);
                    labelPlace.setDisable(true);
                    place.setOnMouseClicked(new PlaceClickHandler());

                    //Finally, we increment currentPos again to prepare for the next loop. This raises the Pos to 3 over its starting value of the current loop
                    //meaning it goes to the next place in the array, then reads it's name, x and then y coordinate.
                    currentArrayPos +=1;
                }

                //After adding the places, we move on to add each individual connection
                //This happens on seperate lines, so we can use a while-loop here to keep it running until we run out of file to read

                while((line = in.readLine()) != null){

                    //We have 4 things to read, so first we define the array & it's regex, then extract information from each set point in the array
                    //The order is always from, to, name, weight, so we can be static when defining the array position
                    String[] connections = line.split(";");
                    String from = connections[0];
                    String to = connections[1];
                    String name = connections[2];
                    int weight = Integer.parseInt(connections[3]);

                    //However, we dont have the actual places, only their names.
                    //Therefore, we create two places using a search method with the from/to as parameters
                    Place fromPlace = placeFinder(from);
                    Place toPlace = placeFinder(to);

                    //After we have the information, we want to test that any potential connection doesn't already exist, to avoid illegal inputs
                    //
                    if(graph.getEdgeBetween(fromPlace, toPlace) == null){

                        //Now that we have the information, we input the connection, same as with the NewConnection button
                        graph.connect(fromPlace, toPlace, name, weight);
                        Connection newLine = new Connection(from, to, name, weight);
                        connectionList.add(newLine);
                        Line connectionLine = new Line(fromPlace.getX(), fromPlace.getY(), toPlace.getX(), toPlace.getY());
                        imagePane.getChildren().add(connectionLine);
                        connectionLine.setDisable(true);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //After everything is said and done, there are unsaved changes. So we:
            changesSaved = false;
        }
    }
}