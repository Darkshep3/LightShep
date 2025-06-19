package board;

public class Bitboard {
    public long wpawns, wknights, wbishops, wrooks, wqueens, wking;
    public long bpawns, bknights, bbishops, brooks, bqueens, bking;
    public long[] pieceBb;
    public final char[] pieceChars = {'P', 'N', 'B', 'R', 'Q', 'K', 'p', 'n', 'b', 'r', 'q', 'k'};

    public Bitboard() {
        this(false);
    }

    public Bitboard(boolean isNew) {
        if (isNew) initialize();
        else updatePieceBb();
    }

    public Bitboard(Bitboard other) {
        this.wpawns = other.wpawns;
        this.wknights = other.wknights;
        this.wbishops = other.wbishops;
        this.wrooks = other.wrooks;
        this.wqueens = other.wqueens;
        this.wking = other.wking;

        this.bpawns = other.bpawns;
        this.bknights = other.bknights;
        this.bbishops = other.bbishops;
        this.brooks = other.brooks;
        this.bqueens = other.bqueens;
        this.bking = other.bking;
        updatePieceBb();
    }

    public void initialize() {
        wpawns = 0x000000000000FF00L;
        wknights = 0x0000000000000042L;
        wbishops = 0x0000000000000024L;
        wrooks = 0x0000000000000081L;
        wqueens = 0x0000000000000008L;
        wking = 0x0000000000000010L;
        bpawns = 0x00FF000000000000L;
        bknights = 0x4200000000000000L;
        bbishops = 0x2400000000000000L;
        brooks = 0x8100000000000000L;
        bqueens = 0x0800000000000000L;
        bking = 0x1000000000000000L;

        updatePieceBb();
    }

    private void updatePieceBb() {
        pieceBb = new long[]{
                wpawns, wknights, wbishops, wrooks, wqueens, wking,
                bpawns, bknights, bbishops, brooks, bqueens, bking
        };
    }

    public long getOccupied() {
        return getWhitePieces() | getBlackPieces();
    }

    public long getEmpty() {
        return ~getOccupied();
    }

    public long getWhitePieces() {
        return wpawns | wknights | wbishops | wrooks | wqueens | wking;
    }

    public long getBlackPieces() {
        return bpawns | bknights | bbishops | brooks | bqueens | bking;
    }

    public void setBit(char piece, int index) {
        long bit = 1L << index;
        switch (piece) {
            case 'P' -> wpawns |= bit;
            case 'N' -> wknights |= bit;
            case 'B' -> wbishops |= bit;
            case 'R' -> wrooks |= bit;
            case 'Q' -> wqueens |= bit;
            case 'K' -> wking |= bit;
            case 'p' -> bpawns |= bit;
            case 'n' -> bknights |= bit;
            case 'b' -> bbishops |= bit;
            case 'r' -> brooks |= bit;
            case 'q' -> bqueens |= bit;
            case 'k' -> bking |= bit;
        }
        updatePieceBb();
    }

    public void clearSquare(int index) {
        long mask = ~(1L << index);
        wpawns &= mask;
        wknights &= mask;
        wbishops &= mask;
        wrooks &= mask;
        wqueens &= mask;
        wking &= mask;
        bpawns &= mask;
        bknights &= mask;
        bbishops &= mask;
        brooks &= mask;
        bqueens &= mask;
        bking &= mask;
        updatePieceBb();
    }

    public void movePiece(int from, int to, char piece) {
        if (piece == ' ') return;
        clearSquare(from);
        clearSquare(to);
        setBit(piece, to);
    }

    public char getPieceAt(int index) {
        long bit = 1L << index;
        for (int i = 0; i < 12; i++) {
            if ((pieceBb[i] & bit) != 0) {
                return pieceChars[i];
            }
        }
        return ' ';
    }

    public void printBoard() {
        for (int rank = 7; rank >= 0; rank--) {
            System.out.print(rank + 1 + " ");
            for (int file = 0; file < 8; file++) {
                int square = 8 * rank + file;
                String piece = "\u2003";
                if ((wpawns & (1L << square)) != 0) piece = "♙";
                else if ((wbishops & (1L << square)) != 0) piece = "♗";
                else if ((wknights & (1L << square)) != 0) piece = "♘";
                else if ((wrooks & (1L << square)) != 0) piece = "♖";
                else if ((wqueens & (1L << square)) != 0) piece = "♕";
                else if ((wking & (1L << square)) != 0) piece = "♔";
                else if ((bpawns & (1L << square)) != 0) piece = "♟";
                else if ((bbishops & (1L << square)) != 0) piece = "♝";
                else if ((bknights & (1L << square)) != 0) piece = "♞";
                else if ((brooks & (1L << square)) != 0) piece = "♜";
                else if ((bqueens & (1L << square)) != 0) piece = "♛";
                else if ((bking & (1L << square)) != 0) piece = "♚";
                System.out.print(piece + " ");
            }
            System.out.println();
        }
        System.out.println("  ａ ｂ ｃ ｄ ｅ ｆ ｇ ｈ");
    }
}
