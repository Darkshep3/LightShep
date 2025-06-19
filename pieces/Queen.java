package pieces;

public class Queen {
    public static long getQueenMoves(int square, long allies, long enemies) {
        return Rook.getRookMoves(square, allies, enemies) | Bishop.getBishopMoves(square, allies, enemies);
    }
}

