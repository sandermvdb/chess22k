# chess22k

A chessengine build in Java that uses the UCI protocol to communicate with graphical interfaces.
Score is about 2300 elo.

Uses the following techniques:
- (magic) bitboards
- transposition table
- (internal) iterative-deepening
- killer-moves and history-heuristics for move ordering
- principal variation search
- null-move pruning
- late-move-reductions
- static exchange evaluation
- aspiration window
- has NO mobility evaluation because the performance impact was too high

TODO:
- futility-pruning
- improved insufficient material calculation
- improved evaluation function, especially mobility scores
- ...
