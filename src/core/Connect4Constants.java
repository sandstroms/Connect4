/**
 * This interface has constants that describe the number of rows and columns this version of Connect4 has and indicates
 * what char the X and O pieces are. It includes constants for indicating whose turn it is, and who won the game among
 * other constants.
 *
 * @author Sandstrom
 * @version 1.0
 */

package core;

public interface Connect4Constants {
    char XPIECE = 'X';
    char OPIECE = 'O';
    int PLAYERXTURN = 1;
    int PLAYERXWON = 3;
    int PLAYEROWON = 4;
    int TIE = 5;
    int ROWS = 6;
    int COLUMNS = 7;
    int COMPUTEROPPONENT = 8;
    int PLAYEROPPONENT = 9;
    int PLAYERX = 10;
    int PLAYERO = 11;
    int NOWIN = 12;
    int VALID = 13;
    int INVALID = 14;
}
