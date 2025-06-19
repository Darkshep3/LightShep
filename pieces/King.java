package pieces;

import static utils.BitboardUtils.printBitboard;

public class King{
    private static final long [] kingMoves = new long [64];
    static {
        for (int square = 0; square < 64; square++){
            long bitboard = 1L << square;
            long moves = 0L;
            if (square % 8 > 0 && square > 7) moves |= bitboard >>> 9;
            if (square % 8 > 0) moves |= bitboard >>> 1;
            if (square % 8 > 0 && square < 56) moves |= bitboard << 7;
            if (square < 56) moves |= bitboard << 8;
            if (square % 8 < 7 && square < 56) moves |= bitboard << 9;
            if (square % 8 < 7) moves |= bitboard << 1;
            if (square % 8 < 7 && square > 7) moves |= bitboard >>> 7;
            if (square > 7) moves |= bitboard >>> 8;
            kingMoves[square] = moves;
        }
    }
    public static long getKingMoves(int square, long allies) {
        //printBitboard(King.getKingAttacks(square));
        return kingMoves[square] & ~allies;
    }
    public static long getKingAttacks(int square) {
        return kingMoves[square];
    }

}
