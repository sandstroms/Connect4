/**
 * This is a naive-AI for the Connect4 game. It is built on a principle that it
 * should first prevent a win, then find an open spot that is two spaces away
 * from where the player last placed their token, and last on top of it. If no spot is open
 * based on this logic it finds the first open column and drops a token there.
 *
 * @author Sandstrom
 * @version 1.0
 */

package core;

public class Connect4ComputerPlayerOld {
    /**
     * This first calls the preventWin() function to try to prevent a win, and if it does
     * not need to prevent a win, the computer chooses a spot that is either two spots to
     * left or right of where player X last placed their token, or on top of it. Last, the
     * computer places a token in any open spot. If a -1 is returned, there is an unexpected
     * error.
     *
     * @param coord the coordinates of the last played token (by player X)
     * @param gameBoard is a 2D char array representing the current state of the game board
     * @param gameLogic is a Connect4 object used to call methods to determine move validity
     * @return an int representing the column where the computer will play its token
     */
    public int determineMove(int[] coord, char[][] gameBoard, Connect4Old gameLogic) {
        // first check if a win should be prevented
        if(preventWin(gameBoard, gameLogic) == -1) {
            int row = coord[0];
            int col = coord[1];

            // shift by two principle behind this naive-AI
            int newCol = col - 2;
            if (gameLogic.isValidMove(row, newCol))
                return newCol;
            newCol = col + 2;
            if (gameLogic.isValidMove(row, newCol))
                return newCol;
            //if cannot shift by two, place on top of last-played column
            if (gameLogic.isValidMove(row - 1, col))
                return col;
            //otherwise, find the first open spot
            for (int iterRow = 0; iterRow < gameBoard.length; iterRow++) {
                for (int iterCol = 0; iterCol < gameBoard[iterRow].length; iterCol++) {
                    if (gameBoard[iterRow][iterCol] == ' ')
                        return iterCol;
                }
            }

            // indicates something unexpected went wrong
            return -1;
        } else {
            return preventWin(gameBoard, gameLogic);
        }
    }

    /**
     * This checks the board to see if the human player is about to win (if they have
     * three-in-a-row anywhere with a four-in-a-row option possible. It will then block the
     * player from winning. If -1 is returned, there is no win to prevent.
     *
     * @param gameBoard is a 2D char array representing the current state of the board
     * @param gameLogic is a Connect4 object used for some of its methods
     * @return an int which is the column where the computer will place its token
     */
    public int preventWin(char[][] gameBoard, Connect4Old gameLogic) {
        int inARow;

        for(int row = 0; row < gameBoard.length; row++) {
            for(int col = 0; col < gameBoard[row].length; col++) {
                // down
                if (gameLogic.isValidMove(row + 3, col)) {
                    inARow = 0;
                    for (int i = 2; i >= 0; i--) {
                        if (gameBoard[row + i][col] == gameLogic.getCurrentToken())
                            inARow++;
                    }
                    if (inARow == 3)
                        return col;
                }
                // up
                if (gameLogic.isValidMove(row - 3, col)) {
                    inARow = 0;
                    for (int i = 2; i >= 0; i--) {
                        if (gameBoard[row - i][col] == gameLogic.getCurrentToken())
                            inARow++;
                    }
                    if (inARow == 3)
                        return col;
                }
                // diagonal up-left
                if (gameLogic.isValidMove(row - 3, col - 3)) {
                    inARow = 0;
                    for (int i = 2; i >= 0; i--) {
                        if (gameBoard[row - i][col - i] == gameLogic.getCurrentToken())
                            inARow++;
                    }
                    if (inARow == 3)
                        return col - 3;
                }
                // diagonal up-right
                if (gameLogic.isValidMove(row - 3, col + 3)) {
                    inARow = 0;
                    for (int i = 2; i >= 0; i--) {
                        if (gameBoard[row - i][col + i] == gameLogic.getCurrentToken())
                            inARow++;
                    }
                    if (inARow == 3)
                        return col + 3;
                }
                // right
                if (gameLogic.isValidMove(row, col + 3)) {
                    inARow = 0;
                    for (int i = 2; i >= 0; i--) {
                        if (gameBoard[row][col + i] == gameLogic.getCurrentToken())
                            inARow++;
                    }
                    if (inARow == 3)
                        return col + 3;
                }
                // left
                if (gameLogic.isValidMove(row, col - 3)) {
                    inARow = 0;
                    for (int i = 2; i >= 0; i--) {
                        if (gameBoard[row][col - i] == gameLogic.getCurrentToken())
                            inARow++;
                    }
                    if (inARow == 3)
                        return col - 3;
                }
                // diagonal down-right
                if (gameLogic.isValidMove(row + 3, col + 3)) {
                    inARow = 0;
                    for (int i = 2; i >= 0; i--) {
                        if (gameBoard[row + i][col + i] == gameLogic.getCurrentToken())
                            inARow++;
                    }
                    if (inARow == 3)
                        return col + 3;
                }
                //diagonal down-left
                if (gameLogic.isValidMove(row + 3, col - 3)) {
                    inARow = 0;
                    for(int i = 2; i >= 0; i--) {
                        if(gameBoard[row + i][col - i] == gameLogic.getCurrentToken())
                            inARow++;
                    }
                    if(inARow == 3)
                        return col - 3;
                }
            }
        }
        // this indicates that there is no win to prevent
        return -1;
    }
}
