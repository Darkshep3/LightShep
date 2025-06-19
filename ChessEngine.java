import board.Bitboard;
import movegeneration.*;
import game.*;
import java.util.List;
import java.util.Scanner;
import engine.*;

public class ChessEngine {
    private GameState gameState;
    private static final int SEARCH_DEPTH = 6;

    public static void main(String[] args){
        ChessEngine chessEngine = new ChessEngine("5k2/8/4Q3/3B4/8/8/K7/8 w - - 2 2");
        //ChessEngine chessEngine = new ChessEngine();
        chessEngine.play(true, false);
        //chessEngine.play();
    }
    public ChessEngine(){
        this.gameState = new GameState();
    }
    public ChessEngine(String FEN){
        gameState = GameState.fromFEN(FEN);
    }
    public void play(){
        Scanner scanner = new Scanner(System.in);
        while (!gameState.isGameOver()) {
            Bitboard board = new Bitboard(gameState.getBoard());
            List<Move> allMoves = MoveGenerator.generateLegalMoves(gameState);
            gameState.board.printBoard();
            System.out.println("Enter your move (ex. e2e4), or 'quit' to exit:");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit")){
                System.out.println("fin.");
                break;
            }
            Move chosenMove = parseMove(input, allMoves);
            if (chosenMove == null) {
                System.out.println("Invalid move. Please try again.");
                continue;
            }
            gameState.makeMove(chosenMove);
        }

        scanner.close();
        System.out.println("Chess Engine Shutting Down...");
    }
    public void play(boolean isEngine, boolean isEngineWhite){
        Scanner scanner = new Scanner(System.in);

        Search search = new Search(20000);
        while (!gameState.isGameOver()) {
            Bitboard board = gameState.getBoard();
            board.printBoard();
            if (gameState.isWhiteToMove() && isEngineWhite || (!gameState.isWhiteToMove() && !isEngineWhite)){
                System.out.println("Engine is thinking...");
                Move topEngine = search.findBestMove(gameState, SEARCH_DEPTH);
                gameState.makeMove(topEngine);
            }
            else {
                List<Move> allMoves = MoveGenerator.generateLegalMoves(gameState);
                System.out.println("Enter your move (ex. e2e4), or 'quit' to exit:");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("quit")) {
                    System.out.println("fin.");
                    break;
                }
                Move move = parseMove(input, allMoves);
                if (move == null) {
                    System.out.println("Invalid move. Please try again.");
                    continue;
                }
                gameState.makeMove(move);
            }
        }

        scanner.close();
        System.out.println("Chess Engine Shutting Down...");
    }
    private Move parseMove(String input, List<Move> legalMoves){
        if (input.length() < 4) return null;
        int fromFile = input.charAt(0) - 'a';
        int fromRank = input.charAt(1) - '1';
        int toFile = input.charAt(2) - 'a';
        int toRank = input.charAt(3) - '1';
        if (fromFile < 0 || fromFile > 7 || toFile < 0 || toFile > 7 ||
                fromRank < 0 || fromRank > 7 || toRank < 0 || toRank > 7) {
            return null;
        }
        int fromSquare = fromRank * 8 + fromFile;
        int toSquare = toRank * 8 + toFile;
        for (Move move : legalMoves){
            if (input.length() == 5 && move.getFromSquare() == fromSquare
                    && move.getToSquare() == toSquare && input.charAt(4) != ' '){
                return move;}
            else if (input.length() == 4 && move.getFromSquare() == fromSquare
                    && move.getToSquare() == toSquare){
                return move;
            }
        }
        return null;
    }

    public static void printAllMoves(List<Move> allMoves){
        for (int i = 0; i < allMoves.size(); i++) {
            Move move = allMoves.get(i);
            System.out.println((i + 1) + ". " + move.toString());
        }
    }

}
