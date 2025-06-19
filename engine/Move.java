package engine;

import java.util.Objects;

public class Move {
    private final int fromSquare, toSquare;
    private final MoveType type;
    private final char promotion;

    public Move(int fromSquare, int toSquare) {
        this(fromSquare, toSquare, MoveType.NORMAL, '\0');
    }

    public Move(int fromSquare, int toSquare, char promotion) {
        this(fromSquare, toSquare, getPromotionMoveType(promotion), promotion);
    }

    private static MoveType getPromotionMoveType(char promotion) {
        return switch (Character.toLowerCase(promotion)) {
            case 'q' -> MoveType.PROMOTION_QUEEN;
            case 'r' -> MoveType.PROMOTION_ROOK;
            case 'b' -> MoveType.PROMOTION_BISHOP;
            case 'n' -> MoveType.PROMOTION_KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece: " + promotion);
        };
    }

    public Move(int fromSquare, int toSquare, MoveType type) {
        this(fromSquare, toSquare, type, '\0');
    }

    public Move(int fromSquare, int toSquare, MoveType type, char promotion) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.type = type;
        this.promotion = promotion;
    }

    public int getFromSquare() {
        return fromSquare;
    }

    public int getToSquare() {
        return toSquare;
    }

    public MoveType getMoveType() {
        return type;
    }

    public char getPromotion() {
        return promotion;
    }

    public static String squareToAlgebraic(int sq) {
        if (sq < 0 || sq > 63) {
            return "-";
        }
        char file = (char) ('a' + (sq % 8));
        char rank = (char) ('1' + (sq / 8));
        return "" + file + rank;
    }

    public static int algebraicToSquare(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            return -1;
        }
        char fileChar = Character.toLowerCase(algebraic.charAt(0));
        char rankChar = algebraic.charAt(1);

        int file = fileChar - 'a';
        int rank = rankChar - '1';

        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            return -1;
        }
        return rank * 8 + file;
    }

    public String toUCI() {
        String uci = squareToAlgebraic(fromSquare) + squareToAlgebraic(toSquare);
        if (isPromotion()) {
            uci += Character.toLowerCase(promotion);
        }
        return uci;
    }

    public static Move fromUCI(String uciStr) {
        if (uciStr == null || (uciStr.length() != 4 && uciStr.length() != 5)) {
            throw new IllegalArgumentException("Invalid UCI move string length: " + uciStr);
        }

        int fromSq = algebraicToSquare(uciStr.substring(0, 2));
        int toSq = algebraicToSquare(uciStr.substring(2, 4));

        if (fromSq == -1 || toSq == -1) {
            throw new IllegalArgumentException("Invalid UCI move string (invalid squares): " + uciStr);
        }

        if (uciStr.length() == 5) {
            char promo = uciStr.charAt(4);
            return new Move(fromSq, toSq, promo);
        } else {
            return new Move(fromSq, toSq);
        }
    }


    public boolean isPromotion() {
        return type == MoveType.PROMOTION_QUEEN ||
                type == MoveType.PROMOTION_ROOK ||
                type == MoveType.PROMOTION_BISHOP ||
                type == MoveType.PROMOTION_KNIGHT;
    }

    public String toString() {
        return toUCI();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move m)) return false;
        return fromSquare == m.fromSquare
                && toSquare == m.toSquare
                && type == m.type
                && promotion == m.promotion;
    }

    public int hashCode() {
        return Objects.hash(fromSquare, toSquare, type, promotion);
    }
}