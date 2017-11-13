# chess22k

A chessengine build in Java that uses the UCI protocol to communicate with graphical interfaces.
Should be used with a 64 bit JRE for optimal performance.
The binaries are build using Java 9 and are not compatible with older Java versions.
Score is about 2800 elo.

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


_"Simplicity is the soul of efficiency"_       - Austin Freeman -
	