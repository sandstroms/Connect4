/**
 * This includes the functionality to play on a GUI.
 *
 * @author Sandstrom
 * @version 1.0
 */

package ui;
import core.Connect4Old;
import core.Connect4ComputerPlayerOld;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;

import java.util.Scanner;

public class Connect4GUI extends Application {
       private Connect4Old gameLogic = new Connect4Old();
       private Spot[][] spots;
       private Label errorMsg;

       /**
        * This is the entry point for the GUI. If something goes wrong with the computer player, an error window will
        * pop-up and the game will end. This is unexpected.
        *
        * @param primaryStage a Stage which will hold the game board
        */
       @Override
       public void start(Stage primaryStage) {
              Scanner scan = new Scanner(System.in);

              System.out.println("Welcome to the Connect 4 simulator. Enter 'G' if you want to play with a graphic interface, and 'T' if you want to play with a text interface");

              String input = scan.nextLine().toUpperCase().trim();
              while (!(input.toUpperCase().equals("G") || input.toUpperCase().equals("T"))) {
                     System.out.println("That is not a valid option. Please enter just the letter 'G' or 'T'");
                     input = scan.nextLine().toUpperCase().trim();
              }
              if(input.equals("T")) {
                     Connect4Old gameLogic = new Connect4Old(); // to get the number of rows and columns
                     Connect4TextConsole textConsole = new Connect4TextConsole(gameLogic.getRows(), gameLogic.getCols());
                     textConsole.playTextConsole();
              }
              else if(input.equals("G")) {
                     Stage chooseOpponentStg = new Stage();

                     Label askPlayer = new Label("Select your opponent.");
                     askPlayer.setFont(Font.font("Courier", FontWeight.MEDIUM, FontPosture.REGULAR, 32));
                     askPlayer.setPadding(new Insets(25, 0, 0, 25));
                     HBox playerStageHBox = new HBox(25);
                     playerStageHBox.setAlignment(Pos.CENTER);
                     Button compButton = new Button("computer");
                     Button playerButton = new Button("player");

                     compButton.setOnAction(e -> {
                            gameLogic.setIsComputerPlayer();
                            chooseOpponentStg.close();
                     });
                     playerButton.setOnAction(e -> {
                            gameLogic.setIsTwoPlayer();
                            chooseOpponentStg.close();
                     });
                     playerStageHBox.getChildren().addAll(compButton, playerButton);

                     BorderPane playerPane = new BorderPane();
                     playerPane.setTop(askPlayer);
                     playerPane.setCenter(playerStageHBox);

                     VBox boardScreen = new VBox(40);
                     VBox messages = new VBox(25);
                     GridPane gameBoard = new GridPane();
                     //initially player X's turn
                     Label whoseTurn = new Label("Player X");
                     whoseTurn.setPadding(new Insets(0, 0, 20, 20));
                     errorMsg = new Label();
                     whoseTurn.setFont(Font.font("Courier", FontWeight.MEDIUM, FontPosture.REGULAR, 42));
                     errorMsg.setPadding(new Insets(50, 50, 50, 50));
                     errorMsg.setFont(Font.font("Courier", FontWeight.MEDIUM, FontPosture.REGULAR, 42));

                     ChooseSpot[] chooseSpots = new ChooseSpot[gameLogic.getCols()];
                     spots = new Spot[gameLogic.getRows()][gameLogic.getCols()];
                     for (int col = 0; col < gameLogic.getCols(); col++) {
                            chooseSpots[col] = new ChooseSpot(col);
                            gameBoard.add(chooseSpots[col], col, 0);
                     }

                     for (int row = 0; row < gameLogic.getRows(); row++) {
                            for (int col = 0; col < gameLogic.getCols(); col++) {
                                   spots[row][col] = new Spot();
                                   //shifted because of ChooseSpots
                                   gameBoard.add(spots[row][col], col, row + 1);
                            }
                     }

                     gameLogic.getIsComputerPlayer().addListener(ov -> {
                            Connect4ComputerPlayerOld computerPlayer = new Connect4ComputerPlayerOld();
                            gameLogic.getIsPlayerXTurn().addListener(ov2 -> {
                                   for (int col = 0; col < gameLogic.getCols(); col++) {
                                          chooseSpots[col].changeToken(gameLogic.getIsPlayerXTurn().getValue());
                                   }
                                   if (gameLogic.getIsPlayerXTurn().getValue()) {
                                          whoseTurn.setText("Your turn");
                                          // clear any error message
                                          errorMsg.setText("");
                                   } else if (!gameLogic.getIsPlayerXTurn().getValue()) {
                                          whoseTurn.setText("Computer's turn");
                                          errorMsg.setText("");
                                          int col = computerPlayer.determineMove(gameLogic.getLastSpot(), gameLogic.getSpots(), gameLogic);
                                          //this is not expected to run
                                          if (col == -1) {
                                                 gameLogic.setUnexpectedErr();
                                          }
                                          int[] coord = gameLogic.makeMove(col);
                                          //The following lines are here because the computer does not "click" on any ChooseSpot
                                          spots[coord[0]][coord[1]].markSpot(gameLogic.getIsPlayerXTurn().getValue());
                                          gameLogic.checkBoard();
                                          gameLogic.checkForWinner();

                                          gameLogic.changePlayerTurn();
                                   }
                            });
                     });

                     gameLogic.getIsTwoPlayer().addListener(ov -> {
                            gameLogic.getIsPlayerXTurn().addListener(ov2 -> {
                                   for (int col = 0; col < gameLogic.getCols(); col++) {
                                          chooseSpots[col].changeToken(gameLogic.getIsPlayerXTurn().getValue());
                                   }
                                   if (gameLogic.getIsPlayerXTurn().getValue()) {
                                          whoseTurn.setText("Player X's turn");
                                          errorMsg.setText("");
                                   } else if (!gameLogic.getIsPlayerXTurn().getValue()) {
                                          whoseTurn.setText("Player O's turn");
                                          errorMsg.setText("");
                                   }
                            });
                     });

                     gameLogic.getGameOver().addListener(ov -> {
                            Stage endGame = new Stage();
                            endGame.setTitle("Game over");

                            Label displayWinner = new Label();
                            displayWinner.setPadding(new Insets(50, 50, 50, 50));
                            displayWinner.setFont(Font.font("Courier", FontWeight.MEDIUM, FontPosture.REGULAR, 42));

                            if (gameLogic.getIsPlayerOWin()) {
                                   displayWinner.setText("Player O won");
                            } else if (gameLogic.getIsPlayerXWin()) {
                                   displayWinner.setText("Player X won");
                            } else if (gameLogic.getIsTie()) {
                                   displayWinner.setText("Tie");
                            }

                            Scene scene3 = new Scene(displayWinner, 400, 250);
                            endGame.setScene(scene3);
                            endGame.initOwner(primaryStage);
                            endGame.initModality(Modality.WINDOW_MODAL);
                            endGame.show();
                     });

                     gameLogic.getUnexpectedErr().addListener(ov -> {
                            Stage unexpectedErrStg = new Stage();
                            unexpectedErrStg.setTitle("Error");

                            Label errorMsg2 = new Label();
                            errorMsg.setText("There was an unexpected error with the program");
                            Scene scene4 = new Scene(errorMsg2, 400, 250);
                            unexpectedErrStg.setScene(scene4);
                            unexpectedErrStg.initOwner(primaryStage);
                            unexpectedErrStg.initModality(Modality.WINDOW_MODAL);
                            unexpectedErrStg.show();
                     });

                     gameBoard.setPadding(new Insets(20, 20, 20, 20));
                     messages.getChildren().addAll(whoseTurn, errorMsg);
                     boardScreen.getChildren().addAll(gameBoard, messages);

                     Scene scene2 = new Scene(boardScreen, 1200, 800);
                     primaryStage.setTitle("Connect4");
                     primaryStage.setScene(scene2);
                     primaryStage.show();

                     Scene scene1 = new Scene(playerPane, 400, 250);
                     chooseOpponentStg.setTitle("Choose opponent");
                     chooseOpponentStg.setScene(scene1);
                     chooseOpponentStg.initOwner(primaryStage);
                     chooseOpponentStg.initModality(Modality.WINDOW_MODAL);
                     chooseOpponentStg.show();
              }
       }

       /**
        * A class that represents the spots in the game board.
        */
       class Spot extends Pane {
              private Line line1;
              private Line line2;
              private Ellipse ellipse;

              /**
               * The constructor for the Spot, gives the Spot a border.
               */
              public Spot() {
                     setStyle("-fx-border-color: black");
                     this.setPrefSize(2000, 2000);
              }

              public void markSpot(boolean isPlayerXTurn) {
                     if(isPlayerXTurn) {
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
              private int[] coord = new int[2];
              Ellipse ellipse;
              Line line1;
              Line line2;

              /**
               * The constructor for ChooseSpot; gives the object a blue border and importantly
               * adds listeners so a player can choose a column where they want to drop a token.
               *
               * @param col an int representing the col that the ChooseSpot belongs to
               */
              public ChooseSpot(int col) {
                     //Start by displaying X's on the top row (to indicate it is player X's turn).
                     markX();
                     this.setPrefSize(2000, 2000);
                     this.setOnMouseClicked(e -> {
                            coord = gameLogic.makeMove(col);
                            if(gameLogic.isValidSpot(coord[0], coord[1])) {
                                   spots[coord[0]][coord[1]].markSpot(gameLogic.getIsPlayerXTurn().getValue());
                                   gameLogic.setLastSpot(coord);
                                   gameLogic.checkBoard();
                                   gameLogic.checkForWinner();
                                   gameLogic.changePlayerTurn();
                            }
                            else {
                                   errorMsg.setTextFill(Color.RED);
                                   errorMsg.setText("That column is full, choose another column");
                            }
                     });
              }

              /**
               * Changes the shape of the token depending on which player's turn it is. This provides
               * visual cues about whose turn it is (along with the text-based display).
               *
               * @param isPlayerXTurn a boolean representing whose player turn it is
               */
              public void changeToken(boolean isPlayerXTurn) {
                     getChildren().clear();
                     if (isPlayerXTurn) {
                            markX();
                     } else {
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

              /**
               * Makes an 'X' mark. Since it is needed in two places a method seemed appropriate, whereas an ellipse is
               * only drawn once.
               */
              public void markX() {
                     line1 = new Line();
                     line2 = new Line();

                     // tokens should fill up most of the Pane space
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
       }
}











