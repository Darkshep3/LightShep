package movegeneration;
import board.*;
import game.Delta;
import game.GameState;
import pieces.*;
import engine.*;
import utils.BitboardUtils;
import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    public static List<Move> generateLegalMoves(GameState gamestate) {
        List<Move> legalMoves = new ArrayList<>();
        List<Move> pseudoMoves = generateMoves(gamestate);
        for (Move move : pseudoMoves) {
            Delta delta = gamestate.deltaMove(move);
            boolean inCheck = isInCheck(gamestate.getBoard(), !gamestate.isWhiteToMove());
            gamestate.unmakeMove(delta);
            if (!inCheck) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    public static List<Move> generateMoves(GameState gamestate) {
        boolean isWhiteToMove = gamestate.isWhiteToMove();
        Bitboard board = gamestate.getBoard();
        long allies = isWhiteToMove ? board.getWhitePieces() : board.getBlackPieces();
        long enemy = isWhiteToMove ? board.getBlackPieces() : board.getWhitePieces();
        long empty = board.getEmpty();

        boolean castleKing = isWhiteToMove ? gamestate.isWhiteKingCastle() : gamestate.isBlackKingCastle();
        boolean castleQueen = isWhiteToMove ? gamestate.isWhiteQueenCastle() : gamestate.isBlackQueenCastle();

        int enPassant = gamestate.getEnPassant();

        List<Move> allMoves = new ArrayList<>();
        allMoves.addAll(generatePawnMoves(board, enemy, empty, enPassant, isWhiteToMove));
        allMoves.addAll(generateKnightMoves(board, allies, isWhiteToMove));
        allMoves.addAll(generateBishopMoves(board, allies, enemy, isWhiteToMove));
        allMoves.addAll(generateRookMoves(board, allies, enemy, isWhiteToMove));
        allMoves.addAll(generateQueenMoves(board, allies, enemy, isWhiteToMove));
        allMoves.addAll(generateKingMoves(board, allies, empty, isWhiteToMove, castleKing, castleQueen));
        return allMoves;
    }

    public static List<Move> generatePawnMoves(Bitboard board, long enemy, long empty, int enPassant, boolean isWhiteToMove) {
        List<Move> pawnMoves = new ArrayList<>();
        long pawns = isWhiteToMove ? board.wpawns : board.bpawns;

        while (pawns != 0) {
            int fromSq = Long.numberOfTrailingZeros(pawns);
            int toSq;
            pawns &= pawns - 1;
            if (isWhiteToMove) {
                long whitePush1 = Pawn.getWhiteSingleStep(fromSq, empty);
                if (whitePush1 != 0) {
                    toSq = Long.numberOfTrailingZeros(whitePush1);
                    if ((1L << toSq & BitboardUtils.RANK_8) != 0) {
                        addPromotions(pawnMoves, fromSq, toSq);
                    } else {
                        pawnMoves.add(new Move(fromSq, toSq));
                    }
                }

                long whitePush2 = Pawn.getWhiteDoubleStep(fromSq, empty);
                if (whitePush2 != 0) {
                    toSq = Long.numberOfTrailingZeros(whitePush2);
                    pawnMoves.add(new Move(fromSq, toSq));
                }
                long whiteLeftAttack = Pawn.getWhiteLeftAttack(fromSq, enemy);
                if (whiteLeftAttack != 0) {
                    toSq = Long.numberOfTrailingZeros(whiteLeftAttack);
                    if ((1L << toSq & BitboardUtils.RANK_8) != 0) {
                        addPromotions(pawnMoves, fromSq, toSq);
                    } else {
                        pawnMoves.add(new Move(fromSq, toSq));
                    }
                }
                long whiteRightAttack = Pawn.getWhiteRightAttack(fromSq, enemy);
                if (whiteRightAttack != 0) {
                    toSq = Long.numberOfTrailingZeros(whiteRightAttack);
                    if ((1L << toSq & BitboardUtils.RANK_8) != 0) {
                        addPromotions(pawnMoves, fromSq, toSq);
                    } else {
                        pawnMoves.add(new Move(fromSq, toSq));
                    }                }

            } else {
                long blackPush1 = Pawn.getBlackSingleStep(fromSq, empty);
                if (blackPush1 != 0) {
                    toSq = Long.numberOfTrailingZeros(blackPush1);
                    if ((1L << toSq & BitboardUtils.RANK_1) != 0) {
                        addPromotions(pawnMoves, fromSq, toSq);
                    } else {
                        pawnMoves.add(new Move(fromSq, toSq));
                    }
                }

                long blackPush2 = Pawn.getBlackDoubleStep(fromSq, empty);
                if (blackPush2 != 0) {
                    toSq = Long.numberOfTrailingZeros(blackPush2);
                    pawnMoves.add(new Move(fromSq, toSq));
                }
                long blackLeftAttack = Pawn.getBlackLeftAttack(fromSq, enemy);
                if (blackLeftAttack != 0) {
                    toSq = Long.numberOfTrailingZeros(blackLeftAttack);
                    if ((1L << toSq & BitboardUtils.RANK_1) != 0) {
                        addPromotions(pawnMoves, fromSq, toSq);
                    } else {
                        pawnMoves.add(new Move(fromSq, toSq));
                    }
                }
                long blackRightAttack = Pawn.getBlackRightAttack(fromSq, enemy);
                if (blackRightAttack != 0) {
                    toSq = Long.numberOfTrailingZeros(blackRightAttack);
                    if ((1L << toSq & BitboardUtils.RANK_1) != 0) {
                        addPromotions(pawnMoves, fromSq, toSq);
                    } else {
                        pawnMoves.add(new Move(fromSq, toSq));
                    }
                }
            }
            if (enPassant != -1) {
                long enPassantBB = 1L << enPassant;
                if (isWhiteToMove) {
                    if ((Pawn.getWhiteLeftAttack(fromSq, enPassantBB)) != 0) {
                        pawnMoves.add(new Move(fromSq, enPassant, MoveType.EN_PASSANT));
                    }
                    if ((Pawn.getWhiteRightAttack(fromSq, enPassantBB)) != 0) {
                        pawnMoves.add(new Move(fromSq, enPassant, MoveType.EN_PASSANT));
                    }
                }
                else {
                    if ((Pawn.getBlackLeftAttack(fromSq, enPassantBB)) != 0) {
                        pawnMoves.add(new Move(fromSq, enPassant, MoveType.EN_PASSANT));
                    }
                    if ((Pawn.getBlackRightAttack(fromSq, enPassantBB)) != 0) {
                        pawnMoves.add(new Move(fromSq, enPassant, MoveType.EN_PASSANT));
                    }
                }
            }

        }

        return pawnMoves;
    }

    public static List<Move> generateKnightMoves(Bitboard board, long allies, boolean isWhiteToMove) {
        List<Move> knightMoves = new ArrayList<>();
        long knights = isWhiteToMove ? board.wknights : board.bknights;
        while (knights != 0) {
            int fromSq = Long.numberOfTrailingZeros(knights);
            long temp = Knight.getKnightMoves(fromSq, allies);
            while (temp != 0) {
                knightMoves.add(new Move(fromSq, Long.numberOfTrailingZeros(temp)));
                temp &= temp -1;
            }
            knights &= knights - 1;
        }
        return knightMoves;
    }
    public static List<Move> generateBishopMoves(Bitboard board, long allies, long enemy, boolean isWhiteToMove){
        List<Move> bishopMoves = new ArrayList<>();
        long bishops = isWhiteToMove ? board.wbishops : board.bbishops;
        while (bishops != 0){
            int fromSq = Long.numberOfTrailingZeros(bishops);
            long temp = Bishop.getBishopMoves(fromSq, allies, enemy);
            while (temp != 0){
                bishopMoves.add(new Move(fromSq, Long.numberOfTrailingZeros(temp)));
                temp &= temp -1;
            }
            bishops &= bishops -1;
        }

        return bishopMoves;
    }
    public static List<Move> generateRookMoves(Bitboard board, long allies, long enemy, boolean isWhiteToMove){
        List<Move> rookMoves = new ArrayList<>();
        long rooks = isWhiteToMove ? board.wrooks : board.brooks;
        while (rooks != 0){
            int fromSq = Long.numberOfTrailingZeros(rooks);
            long temp = Rook.getRookMoves(fromSq, allies, enemy);
            while (temp != 0){
                rookMoves.add(new Move(fromSq, Long.numberOfTrailingZeros(temp)));
                temp &= temp -1;
            }
            rooks &= rooks -1;
        }

        return rookMoves;
    }
    public static List<Move> generateQueenMoves(Bitboard board, long allies, long enemy, boolean isWhiteToMove){
        List<Move> queenMoves = new ArrayList<>();
        long queens = isWhiteToMove ? board.wqueens : board.bqueens;
        while (queens != 0){
            int fromSq = Long.numberOfTrailingZeros(queens);
            long temp = Queen.getQueenMoves(fromSq, allies, enemy);
            while (temp != 0){
                queenMoves.add(new Move(fromSq, Long.numberOfTrailingZeros(temp)));
                temp &= temp -1;
            }
            queens &= queens -1;
        }

        return queenMoves;
    }

    public static List<Move> generateKingMoves(Bitboard board, long allies, long empty, boolean isWhiteToMove, boolean kingCastle, boolean queenCastle){
        List<Move> kingMoves = new ArrayList<>();
        long king = isWhiteToMove ? board.wking : board.bking;
        if (king == 0L) return kingMoves;
        int fromSq = Long.numberOfTrailingZeros(king);
        long temp = King.getKingMoves(fromSq, allies);
        while (temp != 0){
            kingMoves.add(new Move(fromSq, Long.numberOfTrailingZeros(temp)));
            temp &= temp -1;
        }
        if (isAttackedSquare(board, fromSq, !isWhiteToMove)) {
            return kingMoves;
        }
        if (kingCastle) {
            int sq = isWhiteToMove? 5: 61;
            int sq2 = isWhiteToMove? 6: 62;
            boolean f = (((1L << (sq)) & empty) != 0) && !(isAttackedSquare(board, sq, !isWhiteToMove));
            boolean g = ((1L << (sq2)) & empty) != 0 && !(isAttackedSquare(board, sq2, !isWhiteToMove));
            if (f && g) {
                int toSq = isWhiteToMove? 6: 62;
                kingMoves.add(new Move(fromSq, toSq, MoveType.CASTLING));
            }
        }
        if (queenCastle) {
            int sq = isWhiteToMove? 1: 57;
            int sq2 = isWhiteToMove? 2: 58;
            int sq3 = isWhiteToMove? 3: 59;
            boolean b = ((1L << (sq)) & empty) != 0;
            boolean c = ((1L << sq2) & empty) != 0 && !(isAttackedSquare(board, sq2, !isWhiteToMove));
            boolean d = ((1L << sq3) & empty) != 0 && !(isAttackedSquare(board, sq3, !isWhiteToMove));

            if (b && c && d){
                int toSq = isWhiteToMove ? 2 : 58;
                kingMoves.add(new Move(fromSq, toSq, MoveType.CASTLING));
            }
        }
        return kingMoves;
    }
    public static long attackedSquares(Bitboard board, boolean isWhite) {
        long attacks = 0L;
        long pawns = isWhite? board.wpawns : board.bpawns;
        long knights = isWhite ? board.wknights : board.bknights;
        long bishops = isWhite? board.wbishops : board.bbishops;
        long rooks = isWhite ? board.wrooks : board.brooks;
        long queens = isWhite ? board.wqueens : board.bqueens;
        long king = isWhite ? board.wking : board.bking;
        long allies = isWhite? board.getWhitePieces(): board.getBlackPieces();
        long enemies = isWhite? board.getBlackPieces(): board.getWhitePieces();
        if (isWhite) {
            attacks |= (pawns << 7) & ~BitboardUtils.FILE_H;
            attacks |= (pawns << 9) & ~BitboardUtils.FILE_A;
        } else {
            attacks |= (pawns >>> 9) & ~BitboardUtils.FILE_H;
            attacks |= (pawns >>> 7) & ~BitboardUtils.FILE_A;
        }
        if (king != 0L) {
            attacks |= King.getKingAttacks(Long.numberOfTrailingZeros(king));
        }
        attacks |= generateKnightAttacks(knights, allies);
        attacks |= generateBishopAttacks(bishops, allies, enemies);
        attacks |= generateRookAttacks(rooks, allies, enemies);
        attacks |= generateQueenAttacks(queens, allies, enemies);
        return attacks;
    }
    public static boolean isInCheck(Bitboard board, boolean isWhiteMove) {
        long king = isWhiteMove ? board.wking : board.bking;
        int square = Long.numberOfTrailingZeros(king);
        return (isAttackedSquare(board, square, !isWhiteMove));
    }
    public static boolean isAttackedSquare(Bitboard bb, int square, boolean isWhite) {
        long attacks = attackedSquares(bb, isWhite);
        return ((attacks >>> square) & 1) != 0;
    }

    public static long generateKnightAttacks(long knight, long allies) {
        long attacks = 0L;
        while (knight != 0) {
            int square = Long.numberOfTrailingZeros(knight);
            attacks |= Knight.getKnightMoves(square, allies);
            knight &= knight - 1;
        }
        return attacks;
    }
    public static long generateBishopAttacks(long bishops, long allies, long enemies) {
        long attacks = 0L;
        while (bishops != 0) {
            int square = Long.numberOfTrailingZeros(bishops);
            attacks |= Bishop.getBishopMoves(square, allies, enemies);
            bishops &= bishops -1;
        }
        return attacks;
    }

    public static long generateRookAttacks(long rooks, long allies, long enemies) {
        long attacks = 0L;
        while (rooks != 0) {
            int square = Long.numberOfTrailingZeros(rooks);
            attacks |= Rook.getRookMoves(square, allies, enemies);
            rooks &= rooks -1;
        }
        return attacks;
    }
    public static long generateQueenAttacks(long queens, long allies, long enemies) {
        return generateBishopAttacks(queens, allies, enemies) | generateRookAttacks(queens, allies, enemies);
    }
    private static void addPromotions(List<Move> moves, int fromSq, int toSq) {
        moves.add(new Move(fromSq, toSq, 'q'));
        moves.add(new Move(fromSq, toSq, 'r'));
        moves.add(new Move(fromSq, toSq, 'b'));
        moves.add(new Move(fromSq, toSq, 'n'));
    }

    public static List<Move> generateCaptures(GameState gameState) {
        List<Move> captures = new ArrayList<>();
        List<Move> pseudoMoves = generateMoves(gameState);
        for (Move m : pseudoMoves) {
            char target = gameState.board.getPieceAt(m.getToSquare());
            if (target != ' ' || m.getMoveType() == MoveType.EN_PASSANT || m.getMoveType().name().startsWith("PROMOTION")) {
                Delta delta = gameState.deltaMove(m);
                if (!gameState.isInCheck()) {
                    captures.add(m);
                }
                gameState.unmakeMove(delta);
            }
        }

        return captures;
    }

}