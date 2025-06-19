package game;

import engine.MoveType;

public class Delta {
    public int from, to;
    public char movedPiece, capturedPiece, promotionPiece;
    public boolean whiteToMove;
    public boolean wKCastle, wQCastle, bKCastle, bQCastle;
    public int enPassant;
    public int halfCount, fullCount;
    public MoveType movetype;

    public Delta(int from, int to, char movedPiece, char capturedPiece, char promotionPiece,
                 boolean whiteToMove, boolean wKCastle, boolean wQCastle, boolean bKCastle, boolean bQCastle,
                 int enPassant, int halfCount, int fullCount, MoveType movetype){
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.promotionPiece = promotionPiece;
        this.whiteToMove = whiteToMove;
        this.wKCastle = wKCastle;
        this.wQCastle = wQCastle;
        this.bKCastle = bKCastle;
        this.bQCastle = bQCastle;
        this.enPassant = enPassant;
        this.halfCount = halfCount;
        this.fullCount = fullCount;
        this.movetype = movetype;
    }

}
