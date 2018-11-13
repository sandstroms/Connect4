/**
 * This performs all of the game logic for the Connect4 game, including dropping the token
 * into the appropriate place, seeing if a move is valid, and checking for a winner.
 *
 * @author Sandstrom
 * @version 1.2
 */

package core;

public class Connect4 implements Connect4Constants {
    private char spots[][];
    private int lastSpot[];
    private char currentToken;
    private boolean isTie;
    private boolean playerXWin;
    private boolean playerOWin;
    private int whoseTurn;

    /**
     * Instantiates a 2D array which will store all tokens placed. This constructor
     * make each spot empty, and starts the game with player X's turn.
     */
    public Connect4() {
        spots = new char[ROWS][COLUMNS];
        for(int row = 0; row < ROWS; row++) {
            for(int col = 0; col < COLUMNS; col++) {
                spots[row][col] = ' ';
            }
        }
        lastSpot = new int[2]; // row, column
        currentToken = ' ';
        isTie = false;
        playerXWin = false;
        playerOWin = false;
        whoseTurn = PLAYERX;
    }

    /**
     * Sets the current token based on which player's turn it is, then
     * drops the token into the appropriate coordinate. Finally, it
     * returns that coordinate. If the move is not valid, [-1, -1] is returned.
     *
     * @param col the column where the player wants to drop their token
     * @return an int array representing the coordinates of where the player's token landed (row, col)
     */
    public int[] makeMove(int col) {
        setCurrentToken();
        int[] coord = dropToken(col);
        lastSpot = coord;
        return coord;
    }

    /**
     * Checks if a move is within the game board's limits and if
     * there is not already a token at that coordinate. If both conditions
     * are true, this returns true. Otherwise, it returns false.
     *
     * @param row is an int that represents the row to be checked
     * @param col is an int that represents the column to be checked
     * @return a boolean that indicates if the token can be placed at the specified coordinate (row, col)
     */
    public boolean isValidMove(int row, int col) {
        if(isValidSpot(row, col)) {
            if (spots[row][col] == ' ')
                return true;
            else
                return false;
        }
        return false;
    }

    /**
     * Checks the whole board to see if the current player won by checking for four-in-a-row
     * in all directions starting at the top-left. It traverses through the whole board.
     * If it does not find a winner, it checks if board is full which means there is a tie.
     */
    public void checkBoard() {
        for(int row = 0; row < ROWS; row++) {
            for(int col = 0; col < COLUMNS; col++) {
                // used for checking if there are four-in-a-row
                int inARow;
                // down
                if (isValidSpot(row + 3, col)) {
                    inARow = 0;
                    for (int i = 3; i >= 0; i--) {
                        if (spots[row + i][col] == currentToken)
                            inARow++;
                    }
                    if (inARow == 4) {
                        if (whoseTurn == PLAYERX)
                            playerXWin = true;
                        else
                            playerOWin = true;
                    }
                }
                // diagonal up-left
                if (isValidSpot(row - 3, col - 3)) {
                    inARow = 0;
                    for (int i = 3; i >= 0; i--) {
                        if (spots[row - i][col - i] == currentToken)
                            inARow++;
                    }
                    if (inARow == 4) {
                        if (whoseTurn == PLAYERX)
                            playerXWin = true;
                        else
                            playerOWin = true;
                    }
                }
                // diagonal up-right
                if (isValidSpot(row - 3, col + 3)) {
                    inARow = 0;
                    for (int i = 3; i >= 0; i--) {
                        if (spots[row - i][col + i] == currentToken)
                            inARow++;
                    }
                    if (inARow == 4) {
                        if (whoseTurn == PLAYERX)
                            playerXWin = true;
                        else
                            playerOWin = true;
                    }
                }
                // right
                if (isValidSpot(row, col + 3)) {
                    inARow = 0;
                    for (int i = 3; i >= 0; i--) {
                        if (spots[row][col + i] == currentToken)
                            inARow++;
                    }
                    if (inARow == 4) {
                        if (whoseTurn == PLAYERX)
                            playerXWin = true;
                        else
                            playerOWin = true;
                    }
                }
                // left
                if (isValidSpot(row, col - 3)) {
                    inARow = 0;
                    for (int i = 3; i >= 0; i--) {
                        if (spots[row][col - i] == currentToken)
                            inARow++;
                    }
                    if (inARow == 4) {
                        if (whoseTurn == PLAYERX)
                            playerXWin = true;
                        else
                            playerOWin = true;
                    }
                }
                // diagonal down-left
                if (isValidSpot(row + 3, col - 3)) {
                    inARow = 0;
                    for (int i = 3; i >= 0; i--) {
                        if (spots[row + i][col - i] == currentToken)
                            inARow++;
                    }
                    if (inARow == 4) {
                        if (whoseTurn == PLAYERX)
                            playerXWin = true;
                        else
                            playerOWin = true;
                    }
                }
                // diagonal down-right
                if (isValidSpot(row + 3, col + 3)) {
                    inARow = 0;
                    for (int i = 3; i >= 0; i--) {
                        if (spots[row + i][col + i] == currentToken)
                            inARow++;
                    }
                    if (inARow == 4) {
                        if (whoseTurn == PLAYERX)
                            playerXWin = true;
                        else
                            playerOWin = true;
                    }
                }
            }
        }

        if(!(playerXWin || playerOWin)) {
            // assume its a tie, until an empty spot proves otherwise
            isTie = true;

            for (int row = 0; row < ROWS; row++) {
                for(int col = 0; col < COLUMNS; col++) {
                    if (spots[row][col] == ' ')
                        isTie = false;
                }
            }
        }
    }

    /**
     * This private method places a token at the bottom of the column requested by the player as long as the
     * move is valid (calls isValidMove method). If the move is valid, it sets the bottom-most spot to the
     * current player's token. If a move is not possible, this method returns [-1, -1]
     *
     * @param col the column in which the player has requested the token to be dropped in.
     * @return the coordinates of where the token was placed (row, col)
     */
    private int[] dropToken(int col) {
        int[] coord = new int[2];

        for(int row = ROWS - 1; row >= 0; row--) {
            if (isValidMove(row, col)) {
                coord[0] = row;
                coord[1] = col;
                setSpot(coord);
                return coord;
            }
        }
        // represents a move is invalid
        coord[0] = -1;
        coord[1] = -1;
        return coord;
    }

    /**
     * Sets the specified coordinate of the spots
     * array to the current player's token.
     *
     * @param coord an int array in which the first index is the row and the second index is the column
     */
    public void setSpot(int[] coord){
        int row = coord[0];
        int col = coord[1];
        spots[row][col] = currentToken;
    }

    /**
     * Checks if a spot is valid. Specifically, it checks if a spot is within the board,
     * not if a column is full or not. Returns true if the spot is valid, and false otherwise.
     * One final note is this was not passed in an int[] because it makes the checkBoard method
     * more readable to have a separated row and column.
     *
     * @param row an int which is the row to check to see if the spot is valid
     * @param col an int which is the column to check to see if the spot is valid
     * @return a boolean that indicates if the spot is valid or not
     */
    public boolean isValidSpot(int row, int col) {
        if((row >= 0 && row <= ROWS - 1) && (col >= 0 && col <= COLUMNS - 1))
            return true;
        else
            return false;
    }

    /**
     * Checks the win-state instance variables to see if there is a tie or a specific player won, else
     * there is no winner. This method works closely with the checkBoard method because
     * it relies on that to run before the win-state instance variables can change. Returns the appropriate
     * message depending on what the outcome is.
     *
     * @return a String bearing the appropriate message depending on the outcome (tie, playerX wins, playerO wins, no-win)
     */
    public String checkForWinner() {
        if(isTie)
            return "TIE";
        else if(playerXWin)
            return "PLAYERX";
        else if(playerOWin)
            return "PLAYERO";
        else
            return "NO_WIN";
    }

    /**
     * Sets the current token to the current player's token
     */
    public void setCurrentToken() {
        if(whoseTurn == PLAYERX) {
            currentToken = XPIECE;
        }
        else
            currentToken = OPIECE;
    }

    public void changePlayerTurn() {
        if(whoseTurn == PLAYERX)
            whoseTurn = PLAYERO;
        else
            whoseTurn = PLAYERX;
    }

    /**
     * Gets the current token in play (i.e. based on which player's turn it is).
     *
     * @return a char representing the current token in play
     */
    public char getCurrentToken() { return currentToken; }

    /**
     * Gets the 2D array representing the board
     *
     * @return a 2D char array of the board
     */
    public char[][] getSpots() { return spots; }

    /**
     * Sets the last played spot on the game board.
     *
     * @param coord an int[] that indicates the last played spot
     */
    public void setLastSpot(int[] coord) { lastSpot = coord; }

    /**
     * Retrieves the last played spot on the game board.
     *
     * @return the last played spot on the game board
     */
    public int[] getLastSpot() { return lastSpot; }
}
