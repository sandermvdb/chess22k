# chess22k

A chessengine build in Java that uses the UCI protocol to communicate with graphical interfaces.
Should be used with a 64 bit JRE. Score is about 2400 elo.
Uses Ed Schroder's king safety idea but this has been disabled for the moment.

Features
- (magic) bitboards
- transposition tables
- (internal) iterative-deepening
- killer-moves and history-heuristics for move ordering
- principal variation search
- null-move pruning
- late move reductions
- static exchange evaluation
- aspiration window
- no openingbook or endgame tablebases
- no pondering

Future
- NEVER loses on time
- fix certain node-explosions in endgames
- futility pruning
- improve evaluation function (king-safety, ...)
- tapered eval
- ...



"Simplicity is the soul of efficiency"
- Austin Freeman
	