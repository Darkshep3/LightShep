package game;
import board.*;
import engine.Move;
import engine.MoveType;
import movegeneration.MoveGenerator;
import java.util.List;
import java.util.Objects;

import static engine.Move.squareToAlgebraic;

public class GameState {
    public Bitboard board;
    public boolean whiteToMove;
    public boolean whiteKingCastle, whiteQueenCastle, blackKingCastle, blackQueenCastle;
    public int enPassant, fullCount, halfCount;

    public GameState() {
        this.board = new Bitboard(true);
        this.whiteToMove = true;
        this.whiteKingCastle = true;
        this.blackKingCastle = true;
        this.whiteQueenCastle = true;
        this.blackQueenCastle = true;
        this.enPassant = -1;
        this.fullCount = 0;
        this.halfCount = 0;
    }

    public GameState(Bitboard board, boolean whiteToMove, boolean whiteKingCastle,
                     boolean whiteQueenCastle, boolean blackKingCastle, boolean blackQueenCastle,
                     int enPassant, int halfCount, int fullCount) {
        this.board = board;
        this.whiteToMove = whiteToMove;
        this.whiteKingCastle = whiteKingCastle;
        this.whiteQueenCastle = whiteQueenCastle;
        this.blackKingCastle = blackKingCastle;
        this.blackQueenCastle = blackQueenCastle;
        this.enPassant = enPassant;
        this.fullCount = fullCount;
        this.halfCount = halfCount;
    }

    public GameState(GameState other) {
        this.board = new Bitboard(other.board);
        this.whiteToMove = other.whiteToMove;
        this.whiteKingCastle = other.whiteKingCastle;
        this.whiteQueenCastle = other.whiteQueenCastle;
        this.blackKingCastle = other.blackKingCastle;
        this.blackQueenCastle = other.blackQueenCastle;
        this.enPassant = other.enPassant;
        this.halfCount = other.halfCount;
        this.fullCount = other.fullCount;
    }

    public void makeMove(Move move) {
        int from = move.getFromSquare();
        int to = move.getToSquare();
        char piece = board.getPieceAt(from);
        char captured = board.getPieceAt(to);
        MoveType moveType = move.getMoveType();
        char promoPiece = move.getPromotion();

        enPassant = -1;

        if (Character.toLowerCase(piece) == 'p' || captured != ' ') {
            halfCount = 0;
        } else {
            halfCount++;
        }
        if (!whiteToMove) {
            fullCount++;
        }

        if (moveType == MoveType.EN_PASSANT) {
            int capSq = whiteToMove ? to - 8 : to + 8;
            board.clearSquare(capSq);
            board.movePiece(from, to, piece);

        }
        else if (moveType == MoveType.CASTLING) {
            board.movePiece(from, to, piece);
            if (to == 6) board.movePiece(7, 5, 'R');
            else if (to == 2) board.movePiece(0, 3, 'R');
            else if (to == 62) board.movePiece(63, 61, 'r');
            else if (to == 58) board.movePiece(56, 59, 'r');

        }
        else if (moveType.isPromotion()) {
            board.clearSquare(from);
            if (captured != ' ') board.clearSquare(to);
            if (whiteToMove) {
                promoPiece = Character.toUpperCase(promoPiece);
            }
            else {
                promoPiece = Character.toLowerCase(promoPiece);
            }
            board.setBit(promoPiece, to);

        }
        else {
            board.movePiece(from, to, piece);
        }
        if (piece == 'K') {
            whiteKingCastle = false;
            whiteQueenCastle = false;
        } else if (piece == 'k') {
            blackKingCastle = false;
            blackQueenCastle = false;
        }
        if (piece == 'R') {
            if (from == 0) whiteQueenCastle = false;
            else if (from == 7) whiteKingCastle = false;
        } else if (piece == 'r') {
            if (from == 56) blackQueenCastle = false;
            else if (from == 63) blackKingCastle = false;
        }
        if (captured == 'R') {
            if (to == 0) whiteQueenCastle = false;
            else if (to == 7) whiteKingCastle = false;
        } else if (captured == 'r') {
            if (to == 56) blackQueenCastle = false;
            else if (to == 63) blackKingCastle = false;
        }
        if (Character.toLowerCase(piece) == 'p' && Math.abs(from - to) == 16) {
            enPassant = (from + to) / 2;
        }
        whiteToMove = !whiteToMove;
    }
    public Delta deltaMove(Move move) {
        int from = move.getFromSquare();
        int to = move.getToSquare();
        char piece = board.getPieceAt(from);
        char captured;
        MoveType moveType = move.getMoveType();
        char promoPiece = move.getPromotion();

        if (moveType == MoveType.EN_PASSANT) {
            captured = whiteToMove ? 'p' : 'P';
        }
        else if (moveType.isPromotion()) {
            captured = board.getPieceAt(to);
        }
        else {
            captured = board.getPieceAt(to);
        }

        Delta delta = new Delta(from, to, piece, captured, promoPiece,
                this.whiteToMove, this.whiteKingCastle, this.whiteQueenCastle,
                this.blackKingCastle, this.blackQueenCastle,
                this.enPassant, this.halfCount, this.fullCount,
                moveType);

        makeMove(move);
        return delta;
    }

    public void unmakeMove(Delta delta) {
        whiteToMove = delta.whiteToMove;
        whiteKingCastle = delta.wKCastle;
        whiteQueenCastle = delta.wQCastle;
        blackKingCastle = delta.bKCastle;
        blackQueenCastle = delta.bQCastle;
        enPassant = delta.enPassant;
        halfCount = delta.halfCount;
        fullCount = delta.fullCount;

        MoveType moveType = delta.movetype;

        if (moveType == MoveType.CASTLING) {
            if (delta.to == 6) board.movePiece(5, 7, 'R');
            else if (delta.to == 2) board.movePiece(3, 0, 'R');
            else if (delta.to == 62) board.movePiece(61, 63, 'r');
            else if (delta.to == 58) board.movePiece(59, 56, 'r');
        }

        board.clearSquare(delta.to);

        if (moveType.isPromotion()) {
            char originalPawn = Character.isUpperCase(delta.movedPiece) ? 'P' : 'p';
            board.setBit(originalPawn, delta.from);
        }
        else {
            board.setBit(delta.movedPiece, delta.from);
        }

        if (delta.capturedPiece != ' ') {
            if (moveType == MoveType.EN_PASSANT) {
                int capturedPawnSq = delta.whiteToMove ? delta.to - 8 : delta.to + 8;
                board.setBit(delta.capturedPiece, capturedPawnSq);
            }
            else {
                board.setBit(delta.capturedPiece, delta.to);
            }
        }
    }

    public Bitboard getBoard() {
        return board;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public boolean isWhiteKingCastle() {
        return whiteKingCastle;
    }

    public boolean isWhiteQueenCastle() {
        return whiteQueenCastle;
    }

    public boolean isBlackKingCastle() {
        return blackKingCastle;
    }

    public boolean isBlackQueenCastle() {
        return blackQueenCastle;
    }

    public int getEnPassant() {
        return enPassant;
    }

    public int getFullCount() {
        return fullCount;
    }
    public int getHalfCount() {
        return halfCount;
    }

    public String toString() {
        return "GameState{" +
                "whiteToMove=" + whiteToMove +
                ", whiteKingCastle=" + whiteKingCastle +
                ", whiteQueenCastle=" + whiteQueenCastle +
                ", blackKingCastle=" + blackKingCastle +
                ", blackQueenCastle=" + blackQueenCastle +
                ", enPassant=" + enPassant +
                ", fullCount=" + fullCount +
                ", halfCount=" + halfCount +
                '}';
    }

    public String toFEN() {
        StringBuilder FEN = new StringBuilder();
        long occupy = board.getOccupied();
        long white = board.getWhitePieces();
        long black = board.getBlackPieces();
        int count = 0;
        for (int i = 0; i < 64; i++) {
            long target = 1L << i;
            if ((occupy & target) == 0) {
                count++;
            } else {
                if (count != 0)
                    FEN.append(Integer.toString(count));
                if ((white & target) != 0) {
                    if ((board.wpawns & target) != 0) FEN.append("P");
                    else if ((board.wknights & target) != 0) FEN.append("N");
                    else if ((board.wbishops & target) != 0) FEN.append("B");
                    else if ((board.wrooks & target) != 0) FEN.append("R");
                    else if ((board.wqueens & target) != 0) FEN.append("Q");
                    else FEN.append("K");
                } else {
                    if ((board.bpawns & target) != 0) FEN.append("p");
                    else if ((board.bknights & target) != 0) FEN.append("n");
                    else if ((board.bbishops & target) != 0) FEN.append("b");
                    else if ((board.brooks & target) != 0) FEN.append("r");
                    else if ((board.bqueens & target) != 0) FEN.append("q");
                    else FEN.append("k");
                }
                count = 0;

            }
            if (i % 8 == 7) {
                if (count != 0)
                    FEN.append(count);
                FEN.append("/");
                count = 0;
            }
        }
        StringBuilder store = new StringBuilder();
        int index = 0;
        while (FEN.indexOf("/", index) != -1) {
            int end = FEN.indexOf("/", index);
            if (end != -1) {
                store.insert(0, FEN.substring(index, end) + "/");
            }
            index = end + 1;
        }
        FEN = new StringBuilder(store.substring(0, store.length() - 1));
        FEN.append(whiteToMove ? " w " : " b ");
        String castling = "";
        if (whiteKingCastle) castling += "K";
        if (whiteQueenCastle) castling += "Q";
        if (blackKingCastle) castling += "k";
        if (blackQueenCastle) castling += "q";
        if (castling.isEmpty()) castling = "-";
        FEN.append(castling).append(" ");
        FEN.append(enPassant == -1 ? "-" : squareToAlgebraic(enPassant)).append(" ");
        int halfmoveClock = halfCount;
        int fullmoveNumber = fullCount;
        FEN.append(halfmoveClock).append(" ").append(fullmoveNumber);

        return FEN.toString();
    }
    public static GameState fromFEN(String FEN){
        //ex. rnbqkb1r/ppp2ppp/5n2/3Pp3/2p1P3/8/PP3PPP/RNBQKBNR w KQkq e6 0 5
        String[] parts = FEN.split("\\s+");
        if (parts.length != 6){
            throw new IllegalArgumentException("Invalid FEN");
        }

        Bitboard bb = new Bitboard();

        StringBuilder part1 = new StringBuilder();
        StringBuilder t = new StringBuilder();
        for (char c: parts[0].toCharArray()){
            if (c == '/'){
                part1.insert(0, t);
                t.setLength(0);
            }
            else if (Character.isDigit(c)) {
                int count = c - '0';
                t.append("e".repeat(count));
            }
            else{
                t.append(c);
            }
        }
        part1.insert(0, t);
        String b = part1.toString();

        for (int i = 0; i < 64; i++){
            if (b.charAt(i) != 'e'){
                bb.setBit(b.charAt(i), i);
            }
        }


        boolean whiteToMove = parts[1].equalsIgnoreCase("w");

        String castling = parts[2];
        boolean wK = castling.contains("K");
        boolean wQ = castling.contains("Q");
        boolean bK = castling.contains("k");
        boolean bQ = castling.contains("q");
        int enPassant = -1;
        if (!Objects.equals(parts[3], "-")) {
            char file = parts[3].charAt(0);
            char rank = parts[3].charAt(1);
            int f = (file - 'a');
            int r = (rank - '1');
            if (f < 0 || f > 7 || r < 0 || r > 7)
                throw new IllegalArgumentException("Invalid en passant square");
            enPassant = f + (r) * 8;
        }
        int halfmove = Integer.parseInt(parts[4]);
        int fullmove = Integer.parseInt(parts[5]);
        return new GameState(bb, whiteToMove, wK, wQ, bK, bQ, enPassant, halfmove, fullmove);
    }
    public boolean isGameOver() {
        List<Move> allMoves = MoveGenerator.generateLegalMoves(this);
        if (allMoves.isEmpty()) {
            return true;
        }
        if (isInsufficientMaterial(board))
            return true;

        if (halfCount >= 100)
            return true;
        return false;
    }
    public boolean isInCheck() {
        return MoveGenerator.isInCheck(board, whiteToMove);
    }
    public boolean isInsufficientMaterial(Bitboard bb) {
        if ((bb.wpawns | bb.bpawns | bb.wrooks | bb.brooks | bb.wqueens | bb.bqueens) != 0) {
            return false;
        }
        int whiteMinors = Long.bitCount(bb.wbishops) + Long.bitCount(bb.wknights);
        int blackMinors = Long.bitCount(bb.bbishops) + Long.bitCount(bb.bknights);
        if (whiteMinors > 1 || blackMinors > 1) return false;
        if (whiteMinors == 0 && blackMinors == 0) return true;
        return false;
    }
}