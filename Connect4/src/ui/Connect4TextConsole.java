/**
 * This displays the game board to the user and acts
 * as the console by which the user can play the game.
 *
 * @author Sandstrom
 * @version 1.2
 */

package ui;
import java.util.Scanner;
import core.Connect4Old;
import core.Connect4ComputerPlayerOld;

public class Connect4TextConsole {
    private char[][] gameBoard;
    Connect4Old gameLogic = new Connect4Old();
    Scanner scan = new Scanner(System.in);
    int col;
    int[] coord = new int[2];
    int coordRow;
    int coordCol;

    /**
     * Instantiates a scanner and the game board with the number of
     * rows and columns. Alternates bars and empty spaces.
     *
     * @param rows the number of rows the game board has
     * @param cols the number of columns the game board has (will be translated)
     */
    public Connect4TextConsole(int rows, int cols) {
        // need to translate number of rows from Connect4
        int newCols = translateCol(cols);

        gameBoard = new char[rows][newCols];
        for(int iterRow = 0; iterRow < rows; iterRow++) {
            for (int iterCol = 0; iterCol < newCols; iterCol++) {
                if (iterCol % 2 == 0)
                    gameBoard[iterRow][iterCol] = '|';
                else
                    gameBoard[iterRow][iterCol] = ' ';
            }
        }
    }

    /**
     * This prints the board out on the console.
     *
     * @param rows the number of rows the game board has
     * @param cols the number of cols the game board has
     */
    public void displayBoard(int rows, int cols) {
        int newCols = translateCol(cols);
        for(int iterRow = 0; iterRow < rows; iterRow++) {
            for (int iterCol = 0; iterCol < newCols; iterCol++) {
                System.out.print(gameBoard[iterRow][iterCol]);
            }
            System.out.println();
        }
    }

    /**
     * This sets a spot to the current player's token.
     *
     * @param coord the first index is the row and the second index is the column
     * @param isPlayerXTurn the player whose turn it is
     */
    public void setSpot(int[] coord, boolean isPlayerXTurn) {
        Connect4Old gameLogic = new Connect4Old(); // to get X and O pieces
        int row = coord[0];
        int col = coord[1];
        int newCol = translateCol(col);
        char setChar;
        if(isPlayerXTurn)
            setChar = gameLogic.getXPIECE();
        else
            setChar = gameLogic.getOPIECE();

        gameBoard[row][newCol] = setChar;
    }

    /**
     * Translates the column number from the Connect4 class
     * to this class to account for the bars in the game board.
     *
     * @param col the col to be translated
     * @return an int that is the result of the operation
     */
    public int translateCol(int col) {
        return col * 2 + 1;
    }

    /**
     * Prints out the appropriate winner (player X or O). Is called from the checkForWinner method.
     *
     * @param whoWon the player that won the game
     * @return a message with the player that won the game
     */
    public String printWinner(String whoWon) {
        if(whoWon.equals("PLAYERX"))
            return "Player X Won the Game";
        else if(whoWon.equals("PLAYERO"))
            return "Player O Won the Game";
        else
            return "Tie";
    }

    /**
     * Checks to see if input is valid. If it, it continues to seek input until the user gives valid input, which is
     * 1 - 7.
     *
     * @param input a String that is the user's original input
     * @return a String that is the user's resulting output (may be the same if there are no errors)
     */
    public String testInput(String input) {
        String result = input;
        do {
            try {
                col = Integer.parseInt(result);
                if(col < 1 || col > 7) {
                    System.out.println("That column number is not valid; enter a column between 1 - 7");
                    result = scan.nextLine().trim();
                }
            } catch(NumberFormatException ex) {
                System.out.println("That is not a valid input; enter a whole number between 1 - 7");
                result = scan.nextLine().trim();
            }
        } while(col < 1 || col > 7);
        //accounting for shifting from a 1 to a 0-based index
        col--;
        coord = gameLogic.makeMove(col);
        coordRow = coord[0];
        coordCol = coord[1];
        return result;
    }

    /**
     * This method is called from main to provide the functionality
     * of the text-console based Connect4 game. If a column is full, it asks
     * the user to choose another column. If something goes wrong with the computer,
     * a message comes on the screen and the program terminates (unexpected).
     */
    public void playTextConsole() {
        String winner = ""; // needs to be initialized
        String input;

        System.out.println("Playing text interface.");
        displayBoard(gameLogic.getRows(), gameLogic.getCols());
        System.out.println("Enter 'P' if you want to play against another player; enter 'C' to play against the computer");

        input = scan.nextLine().toUpperCase().trim();
        while (!(input.equals("P") || input.equals("C"))) {
            System.out.println("That is not a valid input. Please enter 'P' or 'C'");
            input = scan.nextLine().toUpperCase().trim();
        }

        if (input.equals("P")) {
            do {
                // needs to be assigned a value
                col = 0;
                if(gameLogic.getIsPlayerXTurn().getValue())
                    System.out.println("Player X - your turn. Enter a column number between 1 - 7");
                else
                    System.out.println("Player O - your turn. Enter a column number between 1 - 7");
                input = scan.nextLine().trim();
                testInput(input);

                // if a column is full, ask the user for another column.
                while (!gameLogic.isValidSpot(coordRow, coordCol)) {
                    System.out.println("That column is already full; please choose another column");
                    input = scan.nextLine().trim();
                    testInput(input);
                }
                System.out.println("row: " + coord[0] + " col: " + coord[1]);

                setSpot(coord, gameLogic.getIsPlayerXTurn().getValue());
                displayBoard(gameLogic.getRows(), gameLogic.getCols());

                gameLogic.checkBoard();
                winner = gameLogic.checkForWinner();

                gameLogic.changePlayerTurn();
            } while (winner.equals("NO_WIN"));
        } else if (input.equals("C")) {
            do {
                Connect4ComputerPlayerOld computerPlayer = new Connect4ComputerPlayerOld();
                // needs to be assigned a value
                col = 0;
                if(gameLogic.getIsPlayerXTurn().getValue()) {
                    System.out.println("Your turn - Enter a column number between 1 - 7.");
                    input = scan.nextLine().trim();
                    testInput(input);

                    // if a spot is not valid, ask the user for another column.
                    while (!gameLogic.isValidSpot(coordRow, coordCol)) {
                        System.out.println("That column is already full; please choose another column");
                        input = scan.nextLine().trim();
                        testInput(input);
                    }
                }
                else {
                    System.out.println("Computer's turn");
                    // gets the computer player's move
                    col = computerPlayer.determineMove(coord, gameLogic.getSpots(), gameLogic);
                    if(col != -1) {
                        // computer's move - overwrites the coordinates of the last played token
                        coord = gameLogic.makeMove(col);
                    }
                    else { // note: this should never run and is an unexpected error
                        System.out.println("Sorry, something went wrong with the computer player.");
                        return;
                    }
                }

                setSpot(coord, gameLogic.getIsPlayerXTurn().getValue());
                displayBoard(gameLogic.getRows(), gameLogic.getCols());

                gameLogic.checkBoard();
                winner = gameLogic.checkForWinner();

                gameLogic.changePlayerTurn();
            } while (winner.equals("NO_WIN"));
        }
        System.out.println(printWinner(winner));
    }
}


