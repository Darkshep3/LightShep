package engine;

import evaluation.*;
import game.*;
import movegeneration.MoveGenerator;

import java.util.Comparator;
import java.util.List;

public class Search {
    private static final int MAX_QUIESCENCE_DEPTH = 5;
    private long startTime;
    private long timeLimit = 10000;
    private boolean timeUp = false;

    public final int CHECKMATE_SCORE = 100000;
    public final int DRAW_SCORE = 0;

    private int searchDepth = 5;
    //private long nodeCount = 0;


    public Search() {
    }

    public Search(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }

    public int getSearchDepth() {
        return this.searchDepth;
    }

    public Move findBestMove(GameState gameState, int maxDepth) {
        return findBestMoveIterative(gameState, maxDepth);
    }

    private Move findBestMoveIterative(GameState gameState, int maxDepth) {
        startTime = System.currentTimeMillis();
        timeUp = false;

        Move bestMove = null;
        for (int depth = 1; depth <= maxDepth; depth++) {
            Move currentBestMove = alphaBetaRoot(gameState, depth);
            //System.out.println("Current Best Move:" + currentBestMove);
            //System.out.println("\n-----------depth: " + depth);
            if (!timeUp) bestMove = currentBestMove;
            else break;
        }
        //System.out.println("Nodes searched: " + nodeCount);
        return bestMove;
    }

    private Move alphaBetaRoot(GameState gameState, int depth) {
        List<Move> legalMoves = MoveGenerator.generateLegalMoves(gameState);
        orderMoves(gameState, legalMoves);

        Move bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        for (Move move : legalMoves) {
            if (timeExceeded()) {
                break;
            }

            Delta delta = gameState.deltaMove(move);
            double score = -alphaBeta(gameState, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1);
            gameState.unmakeMove(delta);
            //System.out.println("Score: " + score + "Move: " + move);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
                //System.out.println("switch");
            }
        }

        System.out.println("Eval: " + (gameState.isWhiteToMove()? bestScore: -bestScore));
        return bestMove;
    }

    private double alphaBeta(GameState gameState, int depth, double alpha, double beta, int maxDepth) {
        //nodeCount++;
        if (timeExceeded()){
            return quiescenceSearch(gameState, alpha, beta, 0, maxDepth);
        }

        if (depth == 0 || gameState.isGameOver()) {
            return quiescenceSearch(gameState, alpha, beta, 0, maxDepth);
        }

        List<Move> moves = MoveGenerator.generateLegalMoves(gameState);
        orderMoves(gameState, moves);

        if (moves.isEmpty()) {
            if (gameState.isInCheck()) {
                return -CHECKMATE_SCORE + (maxDepth);
            } else {
                return DRAW_SCORE;
            }
        }

        double value = Double.NEGATIVE_INFINITY;

        for (Move move : moves) {
            Delta delta = gameState.deltaMove(move);
            double score = -alphaBeta(gameState, depth - 1, -beta, -alpha, maxDepth+1);
            gameState.unmakeMove(delta);
            if (score > value) value = score;
            if (value > alpha) alpha = value;
            if (alpha >= beta) break;
        }
        return value;
    }

    private double quiescenceSearch(GameState gameState, double alpha, double beta, int currentDepth, int maxDepth) {
        //nodeCount++;
        double standPat = Evaluation.evaluate(gameState.board, gameState.isWhiteToMove());
        if (timeExceeded()) {
            return standPat;
        }
        if (gameState.isGameOver()) {
            if (gameState.isInCheck()) return (-CHECKMATE_SCORE + (maxDepth));
            else return DRAW_SCORE;
        }
        if (currentDepth >= MAX_QUIESCENCE_DEPTH) {
            return standPat;
        }

        if (standPat >= beta) return beta;
        if (alpha < standPat) alpha = standPat;

        List<Move> captures = MoveGenerator.generateCaptures(gameState);

        for (Move move : captures) {
            Delta delta = gameState.deltaMove(move);
            double score = -quiescenceSearch(gameState, -beta, -alpha, currentDepth + 1, maxDepth);
            gameState.unmakeMove(delta);

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }

        return alpha;
    }

    private void orderMoves(GameState gameState, List<Move> moves) {
        moves.sort((m1, m2) -> Integer.compare(getMovePriority(m2), getMovePriority(m1)));
    }

    private int getMovePriority(Move move) {
        MoveType moveType = move.getMoveType();
        if (moveType.isPromotion()) return 3;
        else if (moveType == MoveType.CAPTURES) return 2;
        else if (moveType == MoveType.EN_PASSANT) return 1;
        else return 0;
    }

    private boolean timeExceeded() {
        if (timeUp) return true;
        if (System.currentTimeMillis() - startTime > timeLimit) {
            timeUp = true;
            return true;
        }
        return false;
    }
}
