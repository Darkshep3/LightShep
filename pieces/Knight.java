package pieces;

public class Knight {
    private static final long [] knightMoves = new long [64];
    static {
        for (int square = 0; square < 64; square++){
            long bitboard = 1L << square;
            long moves = 0L;
            if (square % 8 > 0 && square > 15) moves |= bitboard >> 17;
            if (square % 8 > 1 && square > 7) moves |= bitboard >>> 10;
            if (square % 8 > 1 && square < 56) moves |= bitboard << 6;
            if (square % 8 > 0 && square < 48) moves |= bitboard << 15;
            if (square % 8 < 7 && square < 48) moves |= bitboard << 17;
            if (square % 8 < 6 && square < 56) moves |= bitboard << 10;
            if (square % 8 < 6 && square > 7) moves |= bitboard >>> 6;
            if (square % 8 < 7 && square > 15) moves |= bitboard >>> 15;
            knightMoves[square] = moves;
        }
    }
    public static long getKnightMoves(int square, long allies) {
        return knightMoves[square] & ~allies;
    }

}
