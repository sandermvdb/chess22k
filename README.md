# chess22k

A chessengine build in Java that uses the UCI protocol to communicate with graphical interfaces.
Should be used with a 64 bit JRE for optimal performance.
The binary is build using Java 14. Also a Java 11 binary is supplied.
Score is about 3100 elo (CCRL 40/4).

## Features
- (magic) bitboards
- transposition tables
- killer/counter-moves and history-heuristics for move ordering
- principal variation search
- (static) null move pruning
- razoring
- late move reductions and pruning
- futility pruning
- static exchange evaluation pruning
- aspiration window
- evaluation parameters tuned using the Texel's tuning method
- tapered eval
- lazy SMP
- pondering
- no openingbook or endgame tablebases


## Future
- improved SMP
- singular extensions
- syzygy
- improved king safety
- ...


_"Simplicity is the soul of efficiency"_       - Austin Freeman -
	