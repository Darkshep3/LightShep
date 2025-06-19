package pieces;

public class Rook {
    public static long getRookMoves(int square, long allies, long enemies) {
        long moves = 0L;
        int file = square % 8;
        for (int r = square - 1; r >= square - file; r--) {
            long target = 1L << r;
            if ((target & allies) != 0) break;
            moves |= target;
            if ((target & enemies) != 0) break;
        }

        for (int r = square + 1; r <= square - file + 7; r++) {
            long target = 1L << r;
            if ((target & allies) != 0) break;
            moves |= target;
            if ((target & enemies) != 0) break;
        }

        for (int f = square + 8; f <= 63; f += 8) {
            long target = 1L << f;
            if ((target & allies) != 0) break;
            moves |= target;
            if ((target & enemies) != 0) break;
        }

        for (int f = square - 8; f >= 0; f -= 8) {
            long target = 1L << f;
            if ((target & allies) != 0) break;
            moves |= target;
            if ((target & enemies) != 0) break;
        }
        return moves;
    }
}
