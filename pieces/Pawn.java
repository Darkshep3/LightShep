package pieces;

import utils.BitboardUtils;

public class Pawn {
    public static long getWhiteSingleStep(int square, long empty) {
        long bitboard = 1L << square;
        return (bitboard << 8) & empty;
    }

    public static long getWhiteDoubleStep(int square, long empty) {
        long bitboard = 1L << square;
        long single = (bitboard << 8) & empty;
        //BitboardUtils.printBitboard((single << 8) & empty & BitboardUtils.RANK_4);
        return (single << 8) & empty & BitboardUtils.RANK_4;
    }

    public static long getWhiteLeftAttack(int square, long enemy) {
        long bitboard = 1L << square;
        return (bitboard & ~BitboardUtils.FILE_A) << 7 & enemy;
    }

    public static long getWhiteRightAttack(int square, long enemy) {
        long bitboard = 1L << square;
        return (bitboard & ~BitboardUtils.FILE_H) << 9 & enemy;
    }

    public static long getBlackSingleStep(int square, long empty) {
        long bitboard = 1L << square;
        return (bitboard >>> 8) & empty;
    }

    public static long getBlackDoubleStep(int square, long empty) {
        long bitboard = 1L << square;
        long single = (bitboard >>> 8) & empty;
        return (single >>> 8) & empty & BitboardUtils.RANK_5;
    }

    public static long getBlackLeftAttack(int square, long enemy ) {
        long bitboard = 1L << square;
        return (bitboard & ~BitboardUtils.FILE_A) >>> 9 & enemy;
    }

    public static long getBlackRightAttack(int square, long enemy) {
        long bitboard = 1L << square;
        return (bitboard & ~BitboardUtils.FILE_H) >>> 7 & enemy;
    }

}