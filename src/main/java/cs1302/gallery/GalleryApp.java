package cs1302.gallery;

import java.net.http.HttpClient;
import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represents an iTunes Gallery App.
 */
public class GalleryApp extends Application {

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private static final Image DEFAULT_IMAGE = new Image("file:resources/default.png");

    private VBox vbox;
    private Stage stage;
    private Scene scene;
    private HBox root;
    private App zzz;

    /**
     * Constructs a {@code GalleryApp} object}.
     */
    public GalleryApp() {
        this.stage = null;
        this.scene = null;
        this.root = new HBox();
        zzz = new App();

    } // GalleryApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        zzz.changeImageBox(App.DEFAULT);
        root.getChildren().add(zzz);


        System.out.println("init() called");
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(this.root);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("GalleryApp!");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    /** {@inheritDoc} */
    @Override
    public void stop() {
        // feel free to modify this method
        System.out.println("stop() called");
    } // stop
/*
    private void changeImageBox(Image...images) {

        if (images.length == 1) {
            for (int i = 0; i < imageRows.length; i++) {
                imageRows[i] = new HBox();
            } //for

            for (int r = 0; r < imageBox.length; r++) {
                for (int c = 0; c < imageBox[0].length; c++) {
                    imageBox[r][c] = new ImageView(DEFAULT_IMAGE);
                } //for
            } //for

        } else {
            int counter = 0;
            for (int r = 0; r < imageBox.length; r++) {
                for (int c = 0; c < imageBox[0].length; c++) {
                    imageBox[r][c].setImage(images[counter]);
                    counter++;
                } //for
            } //for
        } //else

        for (int i = 0; i < imageRows.length; i++) {
            imageRows[i].getChildren().addAll(imageBox[i]);
        }
        for (HBox elem : imageRows) {
            imageCols.getChildren().addAll(elem);
        }
    } //changeImageBox

*/

} // GalleryApp