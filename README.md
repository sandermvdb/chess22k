# chess22k

A chessengine build in Java that uses the UCI protocol to communicate with graphical interfaces.
Should be used with a 64 bit JRE for optimal performance.
Score is about 2700 elo.

## Features
- (magic) bitboards
- transposition tables
- (internal) iterative-deepening
- killer-moves and history-heuristics for move ordering
- principal variation search
- (static) null move pruning
- razoring
- late move reductions
- futility pruning
- static exchange evaluation for move ordering and pruning
- aspiration window
- evaluation scores tuned using the Texel's tuning method
- tapered eval
- no openingbook or endgame tablebases
- no pondering

## Future
- I am running out of ideas! :(


_"Simplicity is the soul of efficiency"_       - Austin Freeman -
	