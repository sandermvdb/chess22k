# chess22k

A chessengine build in Java that uses the UCI protocol to communicate with graphical interfaces.
Should be used with a 64 bit JRE. Score is about 2500 elo.

###Features
- (magic) bitboards
- transposition tables
- (internal) iterative-deepening
- killer-moves and history-heuristics for move ordering
- principal variation search
- (static) null move pruning
- razoring
- late move reductions
- static exchange evaluation
- aspiration window
- no openingbook or endgame tablebases
- no pondering

###Future
- fix certain node-explosions in endgames
- futility pruning
- tapered eval
- tweak values
- ...



_"Simplicity is the soul of efficiency"_       - Austin Freeman -
	