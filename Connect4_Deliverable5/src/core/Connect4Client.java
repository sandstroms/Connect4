/**
 * This class is the client to connect to the server. Moves are passed and received from
 * the server via this class.
 *
 * @author Sandstrom
 * @version 1.0
 */

package core;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connect4Client extends Application implements Connect4Constants {
    private DataOutputStream toServer;
    private DataInputStream fromServer;

    private char myToken = ' ';
    private char otherToken  = ' ';

    private boolean myTurn = false;
    private boolean continuePlay = true;
    private boolean waiting = true;

    private Spot[][] spots = new Spot[ROWS][COLUMNS];
    private ChooseSpot[] chooseSpots = new ChooseSpot[COLUMNS];

    private Label errorMsgLbl = new Label();
    private Label whoseTurnLbl = new Label();
    private Label displayWinnerLbl = new Label();
    private Stage endGameStg = new Stage();

    private int moveRow = -1;
    private int moveCol = -1;

    /**
     * This is the start method for the GUI. It asks the user whether they would like to play against a computer or
     * another opponent. It also connects the player to the server. If an exception occurs, the socket will be closed.
     *
     * @param primaryStage a stage which will hold the game board and messages
     */
    @Override
    public void start(Stage primaryStage) {
        Label askPlayer = new Label("Select your opponent.");
        askPlayer.setFont(Font.font("Courier", FontWeight.MEDIUM, FontPosture.REGULAR, 32));
        askPlayer.setPadding(new Insets(25, 0, 0, 25));

        Stage chooseOppntStg = new Stage();

        HBox playerStageHBox = new HBox(25);
        playerStageHBox.setAlignment(Pos.CENTER);
        Button compButton = new Button("computer");
        Button playerButton = new Button("player");

        compButton.setOnAction(e -> {
            createComputerThread();
            Platform.runLater(() -> chooseOppntStg.close());
        });
        playerButton.setOnAction(e -> {
            createPlayerThread();
            Platform.runLater(() -> chooseOppntStg.close());
        });

        playerStageHBox.getChildren().addAll(compButton, playerButton);
        BorderPane playerPane = new BorderPane();
        playerPane.setTop(askPlayer);
        playerPane.setCenter(playerStageHBox);

        VBox boardScreen = new VBox(40);
        VBox messages = new VBox(25);
        GridPane gameBoard = new GridPane();
        whoseTurnLbl.setPadding(new Insets(0, 0, 20, 20));
        whoseTurnLbl.setFont(Font.font("Courier", FontWeight.MEDIUM, FontPosture.REGULAR, 32));
        errorMsgLbl.setPadding(new Insets(0, 0, 20, 20));
        errorMsgLbl.setFont(Font.font("Courier", FontWeight.MEDIUM, FontPosture.REGULAR, 32));
        messages.getChildren().addAll(whoseTurnLbl, errorMsgLbl);

        for (int col = 0; col < COLUMNS; col++) {
            chooseSpots[col] = new ChooseSpot(col);
            gameBoard.add(chooseSpots[col], col, 0);
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                spots[row][col] = new Spot();
                //shifted down one row because of ChooseSpots on the top row
                gameBoard.add(spots[row][col], col, row + 1);
            }
        }

        gameBoard.setPadding(new Insets(20, 20, 20, 20));
        boardScreen.getChildren().addAll(gameBoard, messages);

        Scene scene2 = new Scene(boardScreen, 920, 920);
        primaryStage.setTitle("Connect4");
        primaryStage.setScene(scene2);
        primaryStage.show();

        Scene scene1 = new Scene(playerPane, 400, 250);
        chooseOppntStg.setTitle("Choose opponent");
        chooseOppntStg.setScene(scene1);
        chooseOppntStg.initOwner(primaryStage);
        chooseOppntStg.initModality(Modality.WINDOW_MODAL);
        chooseOppntStg.show();

        endGameStg.setTitle("Game over"); // prepares endGame screen, but doesn't display it yet.

        displayWinnerLbl.setPadding(new Insets(50, 50, 50, 50));
        displayWinnerLbl.setFont(Font.font("Courier", FontWeight.MEDIUM, FontPosture.REGULAR, 42));

        Scene scene3 = new Scene(displayWinnerLbl, 550, 250);
        endGameStg.setScene(scene3);
        endGameStg.initOwner(primaryStage);
        endGameStg.initModality(Modality.WINDOW_MODAL);

        connectToServer();
    }

    /**
     * A class that represents the spots in the game board.
     */
    class Spot extends Pane {
        private Line line1;
        private Line line2;
        private Ellipse ellipse;

        /**
         * The constructor for the Spot, gives the Spot a border and sets its pref size.
         */
        public Spot() {
            setStyle("-fx-border-color: black");
            this.setPrefSize(2000, 2000);
        }

        /**
         * Marks a spot based on which player it is (based on their token).
         *
         * @param playerToken a char representing the player's token
         */
        public void markSpot(char playerToken) {
            if (playerToken == XPIECE) {
                line1 = new Line();
                line2 = new Line();

                //Token should fill up most of the Pane
                line1.setStartX(this.getWidth() / 6.0);
                line1.setStartY(this.getHeight() / 6.0);
                line1.setEndX(5.0 * this.getWidth() / 6.0);
                line1.setEndY(5.0 * this.getHeight() / 6.0);
                line1.startXProperty().bind(this.widthProperty().divide(6.0));
                line1.startYProperty().bind(this.heightProperty().divide(6.0));
                line1.endXProperty().bind(this.widthProperty().multiply(5.0).divide(6.0));
                line1.endYProperty().bind(this.heightProperty().multiply(5.0).divide(6.0));

                line2.setStartX(5.0 * this.getWidth() / 6.0);
                line2.setStartY(this.getHeight() / 6.0);
                line2.setEndX(this.getWidth() / 6.0);
                line2.setEndY(5.0 * this.getHeight() / 6.0);
                line2.startXProperty().bind(this.widthProperty().multiply(5.0).divide(6.0));
                line2.startYProperty().bind(this.heightProperty().divide(6.0));
                line2.endXProperty().bind(this.widthProperty().divide(6.0));
                line2.endYProperty().bind(this.heightProperty().multiply(5.0).divide(6.0));

                line1.setFill(Color.RED);
                line1.setStroke(Color.RED);
                line2.setFill(Color.RED);
                line2.setStroke(Color.RED);

                getChildren().addAll(line1, line2);
            }
            else {
                //Token should fill up most of the space
                ellipse = new Ellipse(this.getWidth() / 2.0, this.getHeight() / 2.0, this.getWidth() / 3.0, this.getHeight() / 3.0);
                ellipse.centerXProperty().bind(this.widthProperty().divide(2.0));
                ellipse.centerYProperty().bind(this.heightProperty().divide(2.0));
                ellipse.radiusXProperty().bind(this.widthProperty().divide(3.0));
                ellipse.radiusYProperty().bind(this.heightProperty().divide(3.0));

                ellipse.setStroke(Color.BLACK);
                ellipse.setFill(Color.BLACK);

                getChildren().add(ellipse);
            }
        }
    }

    /**
     * The class which acts like buttons on the top of the game board that the player can use to
     * choose where they want to place their token on the game board.
     */
    class ChooseSpot extends Pane {
        Ellipse ellipse;
        Line line1;
        Line line2;

        /**
         * The constructor for ChooseSpot; gives the object a blue border and importantly
         * adds listeners so a player can choose a column where they want to drop a token.
         * If an exception occurs, the sockets toServer and fromServer will be closed.
         */
        public ChooseSpot(int chooseSpotCol) {
            this.setPrefSize(2000, 2000);
            this.setOnMouseClicked(e -> chooseSpotAction(chooseSpotCol));
        }

        /**
         * Sets the tokens on the top of the board to the player's token.
         */
        public void setToken() {
            if (myToken == XPIECE) {
                line1 = new Line();
                line2 = new Line();
                // tokens should fill up most of the space
                line1.setStartX(this.getWidth() / 6.0);
                line1.setStartY(this.getHeight() / 6.0);
                line1.setEndX(5.0 * this.getWidth() / 6.0);
                line1.setEndY(5.0 * this.getHeight() / 6.0);
                line1.startXProperty().bind(this.widthProperty().divide(6.0));
                line1.startYProperty().bind(this.heightProperty().divide(6.0));
                line1.endXProperty().bind(this.widthProperty().multiply(5.0).divide(6.0));
                line1.endYProperty().bind(this.heightProperty().multiply(5.0).divide(6.0));

                line2.setStartX(5.0 * this.getWidth() / 6.0);
                line2.setStartY(this.getHeight() / 6.0);
                line2.setEndX(this.getWidth() / 6.0);
                line2.setEndY(5.0 * this.getHeight() / 6.0);
                line2.startXProperty().bind(this.widthProperty().multiply(5.0).divide(6.0));
                line2.startYProperty().bind(this.heightProperty().divide(6.0));
                line2.endXProperty().bind(this.widthProperty().divide(6.0));
                line2.endYProperty().bind(this.heightProperty().multiply(5.0).divide(6.0));

                line1.setFill(Color.RED);
                line1.setStroke(Color.RED);
                line2.setFill(Color.RED);
                line2.setStroke(Color.RED);

                getChildren().addAll(line1, line2);
            } else if (myToken == OPIECE) {
                //Token should fill up most of the space
                ellipse = new Ellipse(this.getWidth() / 2.0, this.getHeight() / 2.0, this.getWidth() / 3.0, this.getHeight() / 3.0);
                ellipse.centerXProperty().bind(this.widthProperty().divide(2.0));
                ellipse.centerYProperty().bind(this.heightProperty().divide(2.0));
                ellipse.radiusXProperty().bind(this.widthProperty().divide(3.0));
                ellipse.radiusYProperty().bind(this.heightProperty().divide(3.0));

                ellipse.setStroke(Color.BLACK);
                ellipse.setFill(Color.BLACK);

                getChildren().add(ellipse);
            }
        }
    }

    /**
     * This method connects a client to the server, including creating data streams.
     * If an exception occurs, the socket will be closed.
     */
    public void connectToServer() {
        String hostName = "localhost";
        int port = 8000;
        Socket socket = null;
        try {
            socket = new Socket(hostName, port);
            toServer = new DataOutputStream(socket.getOutputStream());
            fromServer = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.err.println(ex);
            try {
                socket.close();
            } catch(IOException ex2) {
                System.err.println(ex2);
            }
        }
    }

    /**
     * Checks if a player has won the game, or if there is no winner and the game should continue.
     * It also gets the last move played.
     *
     * @throws IOException an exception that is thrown by the readInt() method if input is in an invalid format
     */
    public void checkFromServerPvP() throws IOException {
        int anyWin = fromServer.readInt();

        if (anyWin == PLAYERXWON) {
            continuePlay = false;
            if (myToken == XPIECE) {
                Platform.runLater(() -> displayWinnerLbl.setText("Game over - You won!"));
            }
            else if (myToken == OPIECE) {
                Platform.runLater(() -> displayWinnerLbl.setText("Game over - You lost"));
                getMove();
            }
            Platform.runLater(() -> endGameStg.show());
        }
        else if (anyWin == PLAYEROWON) {
            continuePlay = false;
            if (myToken == XPIECE) {
                Platform.runLater(() -> displayWinnerLbl.setText("Game over - You lost"));
                getMove();
            }
            else if (myToken == OPIECE) {
                Platform.runLater(() -> displayWinnerLbl.setText("Game over - You won!"));
            }
            Platform.runLater(() -> endGameStg.show());
        }
        else if (anyWin == TIE) {
            continuePlay = false;
            Platform.runLater(() -> displayWinnerLbl.setText("Game over - Tie"));
            // player O would be the last one to make a move for a tie
            if (myToken == XPIECE)
                getMove();
            Platform.runLater(() -> endGameStg.show());
        }
        else if (anyWin == NOWIN) {
            getMove();
            Platform.runLater(() -> whoseTurnLbl.setText("Your turn"));
            myTurn = true;
        }
    }

    /**
     * Checks if a player has won the game, or if there is no winner and the game should continue.
     * It also gets the last move played.
     *
     * @throws IOException an exception that is thrown by the readInt() method if input is in an invalid format
     */
    public void checkFromServerComp() throws IOException {
        int anyWin = fromServer.readInt();
        if (anyWin == PLAYERXWON) {
            continuePlay = false;
            Platform.runLater(() -> {
                displayWinnerLbl.setText("Game over - You won!");
                endGameStg.show();
            });
        }
        else if (anyWin == PLAYEROWON) {
            continuePlay = false;
            getMove(); // this user is player X
            Platform.runLater(() -> {
                displayWinnerLbl.setText("Game over - You lost");
                endGameStg.show();
            });
        }
        else if (anyWin == TIE) {
            continuePlay = false;
            getMove();
            Platform.runLater(() -> {
                displayWinnerLbl.setText("Game over - Tie");
                endGameStg.show();
            });
        }
        else if (anyWin == NOWIN) {
            getMove();
            Platform.runLater(() -> whoseTurnLbl.setText("Your turn"));
            myTurn = true;
        }
    }

    /**
     * Makes a player wait to take a turn.
     *
     * @throws InterruptedException an exception thrown by Thread.sleep() if the thread is interrupted while waiting
     *                              sleeping, or occupied.
     */
    public void waitForMove() throws InterruptedException {
        while (waiting) {
            Thread.sleep(1000);
        }

        waiting = true;
    }

    /**
     * Retrieves a move from the server and sets the spot on the game board.
     *
     * @throws IOException an exception that is thrown by the readInt() method if input is in an invalid format
     */
    public void getMove() throws IOException {
        int row = fromServer.readInt();
        int col = fromServer.readInt();
        Platform.runLater(() -> spots[row][col].markSpot(otherToken));
    }

    /**
     * When the player decides they want to play against the computer, this starts a game session thread
     * to start play against it.
     */
    public void createComputerThread() {
        new Thread(() -> {
            try {
                toServer.writeInt(COMPUTEROPPONENT);

                myToken = XPIECE;
                otherToken = OPIECE;

                Platform.runLater(() -> {
                    for (int col = 0; col < COLUMNS; col++) {
                        chooseSpots[col].setToken();
                    }
                });

                Platform.runLater(() -> whoseTurnLbl.setText("Starting game against computer. You are Player X and go first."));

                myTurn = true;
                while (continuePlay) {
                    waitForMove();
                    checkFromServerComp();
                }
            }
            catch (InterruptedException ex) {
                System.err.println(ex);
            }
            catch (IOException ex) {
                System.err.println(ex);
                try {
                    fromServer.close();
                }
                catch (IOException ex2) {
                    System.err.println(ex2);
                }
            }
        }).start();
    }

    /**
     * When a player decides to play a player opponent, this creates a thread to run a two-player game session.
     */
    public void createPlayerThread() {
        new Thread(() -> {
            try {
                toServer.writeInt(PLAYEROPPONENT);

                // find out which player you are from server
                int player = fromServer.readInt();

                if (player == PLAYERX) {
                    myToken = XPIECE;
                    otherToken = OPIECE;
                    Platform.runLater(() -> {
                        whoseTurnLbl.setText("You are player X - waiting for another player to join.");
                        for (int col = 0; col < COLUMNS; col++) {
                            chooseSpots[col].setToken();
                        }
                    });

                    // signals that another player has joined the session
                    fromServer.readInt();

                    Platform.runLater(() -> whoseTurnLbl.setText("Another player has joined - you start first."));
                    myTurn = true;
                }
                else if (player == PLAYERO) {
                    myToken = OPIECE;
                    otherToken = XPIECE;

                    Platform.runLater(() -> {
                        for (int col = 0; col < COLUMNS; col++) {
                            chooseSpots[col].setToken();
                        }
                        whoseTurnLbl.setText("You are player O - waiting for player X to make a move.");
                    });
                }

                while (continuePlay) {
                    if (player == PLAYERX) {
                        waitForMove();
                        checkFromServerPvP();
                    }
                    else if (player == PLAYERO) {
                        checkFromServerPvP();
                        waitForMove();
                    }
                }
            }
            catch(InterruptedException ex) {
                System.err.println(ex);
            }
            catch (IOException ex) {
                System.err.println(ex);
                try {
                    toServer.close();
                } catch(IOException ex2) {
                    System.err.println(ex2);
                }
            }
        }).start();
    }

    /**
     * This method is an extension of the listener object that is waiting for a user to click on a ChooseSpot.
     * Once the spot is chosen, a token is "dropped" and the move is reported to the server.
     *
     * @param chooseSpotCol an int representing which column the ChooseSpot is for
     */
    public void chooseSpotAction(int chooseSpotCol) {
        if (myTurn) {
            try {
                errorMsgLbl.setText(""); // clear any previous error messages
                toServer.writeInt(chooseSpotCol);
                int moveValid = fromServer.readInt();
                if (moveValid == VALID) {
                    moveRow = fromServer.readInt();
                    moveCol = fromServer.readInt();
                    Platform.runLater(() -> spots[moveRow][moveCol].markSpot(myToken));
                    Platform.runLater(() -> whoseTurnLbl.setText("Waiting for other player to make a move"));
                    myTurn = false;
                    waiting = false;
                }
                else if (moveValid == INVALID) {
                    Platform.runLater(() -> {
                        errorMsgLbl.setTextFill(Color.RED);
                        errorMsgLbl.setText("That column is full, choose another column");
                    });
                }
            } catch (IOException ex) {
                System.err.println(ex);
                try {
                    toServer.close();
                    fromServer.close();
                } catch (IOException ex2) {
                    System.err.println(ex2);
                }
            }
        }
    }

    /**
     * For machines that have limited JavaFX support.
     *
     * @param args an array of String arguments passed in, not used
     */
    public static void main(String[] args) { launch(args); }
}


