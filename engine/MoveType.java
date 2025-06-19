package engine;

public enum MoveType {
    NORMAL,
    EN_PASSANT,
    CASTLING,
    PROMOTION_QUEEN,
    PROMOTION_ROOK,
    PROMOTION_BISHOP,
    PROMOTION_KNIGHT,
    CAPTURES;

    public boolean isPromotion() {
        return this.ordinal() >= PROMOTION_QUEEN.ordinal();
    }
}