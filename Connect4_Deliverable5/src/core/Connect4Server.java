/**
 * This class contains the multi-threaded server implementation which can handle multiple clients,
 * as well as different types of game sessions (player v. player or computer v. player)
 *
 * @author Sandstrom
 * @version 1.0
 */

package core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Connect4Server extends Application implements Connect4Constants {
    private int numSession = 1;

    /**
     * Displays a console that shows information about the server. Allows up to two clients to join a game session
     * and then begins a thread to start the session. If an exception occurs, the instantiated sockets will be closed.
     *
     * @param primaryStage a stage that will display what is going on in the server (e.g who joined and their IP address)
     */
    @Override
    public void start(Stage primaryStage) {
        TextArea textArea = new TextArea();
        Scene s1 = new Scene(new ScrollPane(textArea), 400, 250);
        primaryStage.setTitle("Connect4 Server");
        primaryStage.setScene(s1);
        primaryStage.show();

        new Thread(() -> {
            ServerSocket serverSocket = null;
            Socket player = null;
            Socket otherPlayer = null; // may or may not be player O, depends on if they play computer or player opponent

            try {
                int port = 8000;
                serverSocket = new ServerSocket(port);
                textArea.appendText("Connect4 server started.\n");

                while (true) {
                    player = serverSocket.accept();
                    textArea.appendText("A player joined the session.\n");

                    InetAddress inetAddress = player.getInetAddress();
                    textArea.appendText("Player's host name is " + inetAddress.getHostName() + '\n');
                    textArea.appendText("Player's IP address is " + inetAddress.getHostAddress() + '\n');

                    DataInputStream fromPlayer = new DataInputStream(player.getInputStream());
                    DataOutputStream toPlayer = new DataOutputStream(player.getOutputStream());
                    int whichOpponent = fromPlayer.readInt();
                    if (whichOpponent == COMPUTEROPPONENT) {
                        textArea.appendText("Starting a thread for session number " + numSession + '\n');
                        numSession++;

                        HandleCompPlayerSess compPlayerSess = new HandleCompPlayerSess(player);
                        Thread compPlayerThread = new Thread(compPlayerSess);
                        compPlayerThread.start();
                    }
                    else if (whichOpponent == PLAYEROPPONENT) {
                        toPlayer.writeInt(PLAYERX);

                        boolean otherOpponent = false;
                        while(!otherOpponent) {
                            otherPlayer = serverSocket.accept();
                            DataOutputStream toOtherPlayer = new DataOutputStream(otherPlayer.getOutputStream());
                            DataInputStream fromOtherPlayer = new DataInputStream(otherPlayer.getInputStream());

                            textArea.appendText("A player joined the session.\n");

                            inetAddress = otherPlayer.getInetAddress();
                            textArea.appendText("Player's host name is " + inetAddress.getHostName() + '\n');
                            textArea.appendText("Players's IP address is " + inetAddress.getHostAddress() + '\n');

                            whichOpponent = fromOtherPlayer.readInt();
                            if(whichOpponent == COMPUTEROPPONENT) {
                                textArea.appendText("Starting a thread for session number " + numSession + '\n');
                                numSession++;

                                HandleCompPlayerSess compPlayerSess = new HandleCompPlayerSess(otherPlayer);
                                Thread compPlayerThread = new Thread(compPlayerSess);
                                compPlayerThread.start();
                            }
                            else if(whichOpponent == PLAYEROPPONENT) {
                                toOtherPlayer.writeInt(PLAYERO);

                                textArea.appendText("Starting a thread for session number " + numSession + '\n');
                                numSession++;

                                otherOpponent = true;

                                HandleTwoPlayerSess task = new HandleTwoPlayerSess(player, otherPlayer);
                                Thread taskThread = new Thread(task);
                                taskThread.start();
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                System.err.print(ex);
                try {
                    serverSocket.close();
                    if(player != null) {
                        player.close();
                    }
                    if(otherPlayer != null) {
                        otherPlayer.close();
                    }
                } catch(IOException ex2) {
                    System.err.println(ex2);
                }
            }
        }).start();
    }

    /* This class handles a two-player game session. */
    class HandleTwoPlayerSess implements Runnable {
        private Connect4 gameLogic;
        private Socket player1;
        private Socket player2;

        private int row;
        private int col;

        /**
         * Constructor for a two-player session. Instantiates the Connect4 object and the instance variable sockets to
         * the player1 and player2 parameters. Also, assigns the row and col invalid values that will be overwritten.
         *
         * @param player1 a socket that is the connection to player1
         * @param player2 a socket that is the connection to player2
         */
        public HandleTwoPlayerSess(Socket player1, Socket player2) {
            gameLogic = new Connect4();
            this.player1 = player1;
            this.player2 = player2;
            row = -1;
            col = -1;
        }

        /**
         * When the thread with the task starts this will run. Implements the functionality
         * to handle a two-player game session. If an exception occurs, the sockets player1
         * and player2 will be closed.
         */
        public void run() {
            try {
                DataInputStream fromPlayerX = new DataInputStream(player1.getInputStream());
                DataOutputStream toPlayerX = new DataOutputStream(player1.getOutputStream());
                DataInputStream fromPlayerO = new DataInputStream(player2.getInputStream());
                DataOutputStream toPlayerO = new DataOutputStream(player2.getOutputStream());

                toPlayerX.writeInt(PLAYERXTURN); // notify to start game

                while(true) {
                    // player X
                    int[] coordX = makeMove(fromPlayerX, toPlayerX); // also sends the move to player X

                    gameLogic.setLastSpot(coordX);
                    gameLogic.checkBoard();
                    String anyWin = gameLogic.checkForWinner();
                    if(anyWin.equals("PLAYERX")) {
                        toPlayerX.writeInt(PLAYERXWON);
                        toPlayerO.writeInt(PLAYERXWON);
                        sendMove(toPlayerO, row, col);
                        break;
                    }
                    else if(anyWin.equals("TIE")) {
                        toPlayerX.writeInt(TIE);
                        toPlayerO.writeInt(TIE);
                        sendMove(toPlayerO, row, col);
                        break;
                    }
                    else if(anyWin.equals("NO_WIN")){
                        toPlayerO.writeInt(NOWIN);
                        sendMove(toPlayerO, row, col);
                        gameLogic.changePlayerTurn();
                    }

                    // player O
                    int[] coordO = makeMove(fromPlayerO, toPlayerO); //also sends the move to player O

                    gameLogic.setLastSpot(coordO);
                    gameLogic.checkBoard();
                    anyWin = gameLogic.checkForWinner();
                    if(anyWin.equals("PLAYERO")) {
                        toPlayerX.writeInt(PLAYEROWON);
                        toPlayerO.writeInt(PLAYEROWON);
                        sendMove(toPlayerX, row, col);
                        break;
                    }
                    else if(anyWin.equals("TIE")) {
                        toPlayerX.writeInt(TIE);
                        toPlayerO.writeInt(TIE);
                        sendMove(toPlayerX, row, col);
                        break;
                    }
                    else if(anyWin.equals("NO_WIN")){
                        toPlayerX.writeInt(NOWIN);
                        sendMove(toPlayerX, row, col);
                        gameLogic.changePlayerTurn();
                    }
                }
            } catch(IOException ex) {
                System.err.println(ex);
                try {
                    player1.close();
                    player2.close();
                } catch(IOException ex2) {
                    System.err.println(ex2);
                }
            }
        }

        /**
         * Reads in a player's move and determines if it is valid. If it is invalid, it will continue to ask
         * for a new column until the user enters a valid column. Sets coord to the valid column, and changes some
         * instance variables such as row and col to the appropriate value. Sends the move to the appropriate player
         * and returns the coordinates of the move.
         *
         * @param fromPlayer a DataInputStream from the player making a move
         * @param toPlayer a DataOutputStream to the player that is currently making a move
         * @return an int array that represents the coordinate of the move the player made
         * @throws IOException an exception that is thrown if there is a problem with the readInt method
         */
        public int[] makeMove(DataInputStream fromPlayer, DataOutputStream toPlayer) throws IOException {
            int[] coord;
            do {
                col = fromPlayer.readInt();
                coord = gameLogic.makeMove(col);
                row = coord[0];
                col = coord[1];
                if(!gameLogic.isValidSpot(row, col))
                    toPlayer.writeInt(INVALID);
            } while (!gameLogic.isValidSpot(row, col));

            toPlayer.writeInt(VALID);
            sendMove(toPlayer, row, col);
            return coord;
        }

        /**
         * Sends the move to a player so their game board can be marked.
         * If an exception occurs, the DataOutputStream will be closed.
         *
         * @param dos a DataOutputStream to the player that needs to see the move that was made
         * @param row the row where the token was placed
         * @param col the column where the token was placed
         */
        public void sendMove(DataOutputStream dos, int row, int col) {
            try {
                dos.writeInt(row);
                dos.writeInt(col);
            } catch(IOException ex) {
                System.err.println(ex);
                try {
                    dos.close();
                } catch(IOException ex2) {
                    System.err.println(ex2);
                }
            }
        }
    }

    /* This class is the task to handle a computer-player session */
    class HandleCompPlayerSess implements Runnable {
        private Connect4 gameLogic;

        private Socket player1;

        private int row;
        private int col;

        /**
         * This constructor instantiates the Connect4 object and socket instance variable to player1.
         * Also assigns row and col invalid values that will be overwritten.
         *
         * @param player1 a Socket that is the connection to player1
         */
        public HandleCompPlayerSess(Socket player1) {
            gameLogic = new Connect4();
            this.player1 = player1;
            row = -1;
            col = -1;
        }

        /**
         * This method will run when the thread with the task passed to it starts. It implements the functionality
         * to handle a computer-player game session. If an exception occurs, the socket will be closed.
         */
        public void run() {
            try {
                DataInputStream fromPlayerX = new DataInputStream(player1.getInputStream());
                DataOutputStream toPlayerX = new DataOutputStream(player1.getOutputStream());
                Connect4ComputerPlayer computerPlayer = new Connect4ComputerPlayer();

                while (true) {
                    // player X
                    int[] coordX = makeMove(fromPlayerX, toPlayerX); // also updates row and col instance variables

                    gameLogic.setLastSpot(coordX);
                    gameLogic.checkBoard();
                    String anyWin = gameLogic.checkForWinner();
                    if(anyWin.equals("PLAYERX")) {
                        toPlayerX.writeInt(PLAYERXWON);
                        break;
                    }
                    else if(anyWin.equals("TIE")) {
                        toPlayerX.writeInt(TIE);
                        break;
                    }
                    else if(anyWin.equals("NO_WIN")){
                        gameLogic.changePlayerTurn();
                    }

                    col = computerPlayer.determineMove(gameLogic.getLastSpot(), gameLogic.getSpots(), gameLogic);
                    int[] coordO = gameLogic.makeMove(col);
                    row = coordO[0];
                    col = coordO[1];
                    gameLogic.setLastSpot(coordO);
                    gameLogic.checkBoard();

                    anyWin = gameLogic.checkForWinner();
                    if (anyWin.equals("PLAYERO")) {
                        toPlayerX.writeInt(PLAYEROWON);
                        sendMove(toPlayerX, row, col);
                        break;
                    }
                    else if (anyWin.equals("TIE")) {
                        toPlayerX.writeInt(TIE);
                        sendMove(toPlayerX, row, col);
                        break;
                    }
                    else if (anyWin.equals("NO_WIN")) {
                        toPlayerX.writeInt(NOWIN);
                        sendMove(toPlayerX, row, col);
                        gameLogic.changePlayerTurn();
                    }
                }
            } catch(IOException ex) {
                System.err.println(ex);
                try {
                    player1.close();
                } catch(IOException ex2) {
                    System.err.println(ex2);
                }
            }
        }

        /**
         * Reads in a player's move and determines if it is valid. If it is invalid, it will send an invalid
         * move message to the player and continue to ask for a new column until the user enters a valid column.
         * The rows and col instance variables are overwritten, perhaps several times.
         * Once the move is valid, a valid message is written to the player and the move is sent to the player
         * and sets coord to the valid row and column.
         *
         * @param fromPlayer a DataInputStream from the player making a move
         * @param toPlayer a DataOutputStream to the player that is currently making a move
         * @return an int array that represents the coordinate of the move the player made
         * @throws IOException an exception that is thrown if there is a problem with the readInt method
         */
        public int[] makeMove(DataInputStream fromPlayer, DataOutputStream toPlayer) throws IOException {
            int[] coord;
            do {
                col = fromPlayer.readInt();
                coord = gameLogic.makeMove(col);
                row = coord[0];
                col = coord[1];
                if(!gameLogic.isValidSpot(row, col))
                    toPlayer.writeInt(INVALID);
            } while (!gameLogic.isValidSpot(row, col));

            toPlayer.writeInt(VALID);
            sendMove(toPlayer, row, col);
            return coord;
        }

        /**
         * This sends a move to a player so it can be marked on their game board.
         * If an exception occurs, the DataOuputStream will be closed.
         *
         * @param dos a DataOutputStream that will send information to a specified player
         * @param row the row where a player placed their token
         * @param col the column where a player placed their token
         */
        public void sendMove(DataOutputStream dos, int row, int col) {
            try {
                dos.writeInt(row);
                dos.writeInt(col);
            } catch(IOException ex) {
                System.err.println(ex);
                try {
                    dos.close();
                } catch(IOException ex2) {
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
