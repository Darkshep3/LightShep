package pieces;

public class Bishop {
    public static long getBishopMoves(int square, long allies, long enemies) {
        long moves = 0L;

        for (int r = square - 9; r >= 0 && r % 8 != 7; r -= 9) {
            long target = 1L << r;
            if ((target & allies) != 0) break;
            moves |= target;
            if ((target & enemies) != 0) break;
        }
        for (int r = square + 9; r <= 63 && r % 8 != 0; r += 9) {
            long target = 1L << r;
            if ((target & allies) != 0) break;
            moves |= target;
            if ((target & enemies) != 0) break;
        }
        for (int r = square - 7; r >= 0 && r % 8 != 0; r -= 7) {
            long target = 1L << r;
            if ((target & allies) != 0) break;
            moves |= target;
            if ((target & enemies) != 0) break;
        }

        for (int r = square + 7; r <= 63 && r % 8 != 7; r += 7) {
            long target = 1L << r;
            if ((target & allies) != 0) break;
            moves |= target;
            if ((target & enemies) != 0) break;
        }
        return moves;
    }
}
