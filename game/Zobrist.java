package game;

import java.util.Random;

public class Zobrist {
    private static long[][] pieceSquareKeys = new long [12][64];
    private static long[] enPassantKeys = new long[9];
    private static long[] castlingKeys = new long [16];
    private static long whiteToMoveKey;

    static {
        Random rng = new Random(123456789);
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 64; j++) {
                pieceSquareKeys[i][j] = rng.nextLong();
            }
        }
        for (int i = 0; i < 16; i++){
            castlingKeys[i] = rng.nextLong();
        }
        for (int i = 0; i < 9; i++){
            enPassantKeys[i] = rng.nextLong();
        }
        whiteToMoveKey = rng.nextLong();
    }
}
