# chess22k

A chessengine build in Java that uses the UCI protocol to communicate with graphical interfaces.
Should be used with a 64 bit JRE. Score is about 2400 elo.
Uses Ed Schroder's king safety idea but this has been disabled for the moment.

Features
- (magic) bitboards
- transposition table
- (internal) iterative-deepening
- killer-moves and history-heuristics for move ordering
- principal variation search
- null-move pruning
- late move reductions
- static exchange evaluation
- aspiration window

Future
- improved time-management
- futility pruning
- improve evaluation function (king-safety, ...)
- 2 transposition tables: always replace, depth replacement scheme
- ...

If you want to build this project yourself, you have to set the artifact name in the pom.xml (to chess22k).
I have made this configurable so I can easily build an experimental version but maven does not seem to like this.



"Simplicity is the soul of efficiency"
- Austin Freeman
	