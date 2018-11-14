The Connect4GUI and Connect4TextConsole classes were not actually used in Deliverable 5. I added them simply becuase I had to for 
the deliverable. They are the same files from Deliverable 4. Since I refactored the Connect4 class and added the Connect4Constants (interface), 
the original Connect4 and Connect4ComputerPlayer classes needed to be added to allow Connect4TextConsole and Connect4GUI to compile. 
As such, the Connect4 and ComputerPlayer files are included and designated with the suffix "Old". In all, there are four class files that were not used in the deliverable.
The code in Connect4 Client specifically copies much of the GUI code. I would recommend just reviewing the "Connect4", "Connect4ComputerPlayer", "Connect4Server", and 
"Connect4Client" classes and "Connect4Constants" inteface since the others are not used.