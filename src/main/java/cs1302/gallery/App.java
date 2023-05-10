package cs1302.gallery;

import java.util.Random;
import java.io.IOException;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.image.ImageView;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
*App class is that is a Vbox child.
*/
public class App extends VBox {
    private static final HttpClient HTTP_CLIENT = GalleryApp.HTTP_CLIENT;
    private static final Gson GSON = GalleryApp.GSON;
    private static final int LIM = 200;
    private static final String ITUNES_API = "https://itunes.apple.com/search";
    public static final Image DEFAULT = new Image("file:resources/default.png");
    private static final String[] OPTIONS = {
        "movie",
        "podcast",
        "music",
        "musicVideo",
        "audiobook",
        "shortfilm",
        "tvShow",
        "software",
        "ebook",
        "all"
    };
//-----------------instance variables--------
    private Timeline timeline;
    private KeyFrame keyframe;
    private boolean success;
    private boolean playing;
    private ItunesResponse jason;
    private Image[] curImages;
//-----------------upper stuff -----------------
    private Button play;
    private Button get;
    private ComboBox<String> dropdown;
    private TextField searchBar;
    private Label label;
    private HBox upper;
//-----------------middle stuff---------------------------
    private Label status;
    private HBox[] imageRows;
    private VBox imageCols;
    private ImageView[][] imageBox;
//-----------------lower stuff-----------------------------
    private ProgressBar progress;
    private Label apiLabel;
    private HBox lower;
//---------------------------------------

    /**
     *Constructor.
     *constructs a Vbox object with entire app preloaded
     */
    public App() {
        super(5.0);
        success = false;
        playing = false;
        keyframe = new KeyFrame(Duration.seconds(2), (e) -> slideShow());
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyframe);

//--------------------------------------
        play = new Button("Play");
        play.setDisable(true);
        play.setOnAction((e) -> run( () -> playAction()));

        label = new Label("Search: ");
        searchBar = new TextField("Taylor Swift");

        dropdown = new ComboBox<>();
        dropdown.getItems().addAll(OPTIONS);
        dropdown.setValue("music");

        get = new Button("Get Images");
        get.setOnAction((e) -> run(() ->  getAction()));
        upper = new HBox(5.0);

        upper.getChildren().addAll(play, label, searchBar, dropdown, get);
//----------------------------------------------------------------------------
        status = new Label("Type in a term, select a media type, then click the button.");
        imageRows = new HBox[4];
        imageCols = new VBox();
        imageBox = new ImageView[4][5];
//----------------------------------------------------------------------------
        progress = new ProgressBar(0.0);
        progress.setPrefWidth(220.0);
        apiLabel = new Label("Images provided by iTunes Search API");
        lower = new HBox(5.0);
        lower.getChildren().addAll(progress, apiLabel);



        this.getChildren().addAll(upper, status, imageCols, lower);
    } //constructor

    /**
     *Method that is called when play/pause button pressed.
     *play - starts slideshow method, button set to pause
     *pause - stops slideshow method, button set to play
     */
    private void playAction() {
        if (!playing) {
            playing = true;
            Platform.runLater( () -> play.setText("Pause"));
            timeline.play();

        } else {
            playing = false;
            Platform.runLater( () -> play.setText("Play"));
            timeline.pause();
        }

    } //playAction

    /**
     *Method that is called when get button is pressed.
     *takes input from search bar and drop down menu
     *uses input to generate api url
     *receives information from api url
     *parses data using Gson into java object
     *used data to download images and update what is show in image views
     */
    private void getAction() {
        Platform.runLater(() -> get.setDisable(true));  //disables buttons to prevent
        Platform.runLater(() -> play.setDisable(true)); //running mulitple instances
        if (playing) {
            playAction();
        }
        String term = encode(getSearch());  //search bar data
        String media = encode(dropdown.getValue());  //dropdown data
        String limit = encode("" + LIM);  //limit, 200
        //query string with all data inputed
        String url = String.format("?term=%s&media=%s&limit=%s", term, media, limit);
        HttpRequest request = HttpRequest.newBuilder()  //build request url
            .uri(URI.create(ITUNES_API + url))
            .build();

        try {
            //receives data from request url in string form
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

            if (response.statusCode() != 200) {  //makes sure a valid response was received
                throw new IOException(response.toString());
            } else {
                jason = GSON.fromJson(response.body(), ItunesResponse.class);  //parses json data
                if (jason.resultCount < 21) {
                    throw new IllegalArgumentException("0 distinct results found," +
                    " but 21 or more are needed");
                } //if
                curImages = getImages();  //if everything valid
                changeImageBox(curImages); //downloads and displays first 20 images

            } //else
            //if something goes wrong, displays error message with details
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            Platform.runLater( () -> {
                System.err.println(e);
                success = false;
                Alert alert = new Alert(AlertType.ERROR, e.toString());
                alert.showAndWait();
            });
        } finally {  //if success enables buttons and displays url in status
            if  (success) {
                Platform.runLater( () -> status.setText(ITUNES_API + url));
                play.setDisable(false);
            } else {
                Platform.runLater( () -> status.setText("Last attempt to get images failed..."));
            }

            get.setDisable(false);

        }


    } //getAction

    /**
     *Encodes given string to be used in an url.
     *@param string - string that is encoded
     *@return - return encoded string
     */
    private String encode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    /**
     *Gets the input in search bar in string form.
     *@return - returns a string of input
     */
    public String getSearch() {
        return searchBar.getCharacters().toString();
    } //getSearch

    /**
     *Creates a new thread and runs it.
     *@param runner - runnable object that is used to create new thread
     */
    private void run(Runnable runner) {
        Thread newThread = new Thread(runner);
        newThread.setDaemon(true);
        newThread.start();
    }

    /**
     *Takes in a var args of images.
     *initallizing, takes in one default image to display for all image view objects
     *changing, takes in an array of images
     *chooses the first twenty to display
     *@param images - array of downloaded images
     */
    public void changeImageBox(Image...images) {

        if (images.length == 1) { //"constructor"
            for (int i = 0; i < imageRows.length; i++) {
                imageRows[i] = new HBox();
            } //for

            for (int r = 0; r < imageBox.length; r++) {
                for (int c = 0; c < imageBox[0].length; c++) {
                    imageBox[r][c] = new ImageView(DEFAULT);
                } //for
            } //for

        } else {  //adds desired images to "queue"
            int counter = 0;
            for (int r = 0; r < imageBox.length; r++) {
                for (int c = 0; c < imageBox[0].length; c++) {
                    imageBox[r][c].setImage(images[counter]);
                    counter++;
                } //for
            } //for
        } //else

        //displays images from "queue"
        for (int i = 0; i < imageRows.length; i++) {
            imageRows[i].getChildren().clear();    //clears hbox so no duplicate error
            imageRows[i].getChildren().addAll(imageBox[i]);
        }
        imageCols.getChildren().clear();  //clears vbox so no duplicate error
        for (HBox elem : imageRows) {
            imageCols.getChildren().addAll(elem);
        }
    } //changeImageBox

    /**
     *Downloads all images from results.
     *@return - returns downloaded images in an array
     */
    private Image[] getImages() {
        success = false;
        Platform.runLater(() -> status.setText("Getting images...")); //status
        Double num = new Double(0.0);
        int size = jason.results.length;
        Image[] images = new Image[size];
        double prog = 0.0;

        //downloads images from result json
        for (int i = 0; i < size; i++) {
            images[i] = new Image(jason.results[i].artworkUrl100);
            prog = num.valueOf(i) / num.valueOf(size);
            progress.setProgress(prog);  //updates progress real time
        }

        success = true;
        return images;
    } //getImages

    /**
     *Changes a currently displayed image with a randomly choosen one.
     *choosen image is one that has been pre downloaded
     */
    private void slideShow() {
        Image[] curView = new Image[20];
        Random random = new Random();
        int ranValue = random.nextInt(curImages.length);
        int counter = 0;

        //gets current view as image array form to be used by image updater method
        for (int r = 0; r < imageBox.length; r++) {
            for (int c = 0; c < imageBox[0].length; c++) {
                curView[counter] = imageBox[r][c].getImage();
                counter++;
            } //for
        } //for

        //chooses random view box sets a random downloaded image into it
        curView[random.nextInt(21)] = curImages[random.nextInt(curImages.length)];
        Platform.runLater( () -> changeImageBox(curView)); //updates current view

    }

} //app
