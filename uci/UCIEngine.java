//Thank you gemini for this <3
package uci;

import engine.Move;
import engine.Search;
import game.GameState;
import board.Bitboard;
import movegeneration.MoveGenerator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Main class for the UCI (Universal Chess Interface) engine.
 * Handles communication with a UCI-compatible chess GUI by reading commands
 * from stdin and responding to stdout.
 */
public class UCIEngine {

    private GameState currentGameState;
    private Search searchEngine;
    private ExecutorService searchExecutor; // Manages the search thread
    private Future<?> currentSearchTask; // Represents the ongoing search task

    /**
     * Constructor for the UCIEngine.
     * Initializes the game state, search engine, and a single-threaded executor
     * for running search tasks.
     */
    public UCIEngine() {
        // Initialize with the standard starting game state
        currentGameState = new GameState();
        searchEngine = new Search();
        // Use a single-threaded executor to ensure only one search runs at a time
        searchExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Main method to start the UCI engine.
     * Creates an instance of UCIEngine and calls its run method.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        UCIEngine uciEngine = new UCIEngine();
        uciEngine.run();
    }

    /**
     * Runs the main UCI loop.
     * Continuously reads commands from standard input and processes them.
     * Shuts down the executor service when the engine quits.
     */
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            // Loop indefinitely, reading commands until 'quit' is received or an error occurs
            while ((line = reader.readLine()) != null) {
                handleUciCommand(line);
            }
        } catch (Exception e) {
            // Log any errors that occur during the UCI loop
            System.err.println("Error in UCI loop: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure the executor service is shut down when the application exits
            if (searchExecutor != null && !searchExecutor.isShutdown()) {
                searchExecutor.shutdownNow(); // Attempt to shut down all running tasks
            }
        }
    }

    /**
     * Handles a single UCI command received from the GUI.
     * Parses the command and dispatches it to the appropriate handler method.
     * @param command The full UCI command string.
     */
    private void handleUciCommand(String command) {
        // Split the command into parts based on whitespace
        String[] parts = command.trim().split("\\s+");
        if (parts.length == 0) {
            return; // Ignore empty commands
        }
        String cmd = parts[0]; // The first part is the command name

        switch (cmd) {
            case "uci":
                // Respond with engine identification and options
                System.out.println("id name MyJavaChessEngine");
                System.out.println("id author YourName"); // Replace with your name
                // TODO: Add any specific UCI options your engine supports here.
                // Example: System.out.println("option name Hash type spin default 1 min 1 max 1024");
                System.out.println("uciok"); // Signal that UCI initialization is complete
                break;

            case "isready":
                // Indicate that the engine has finished initialization and is ready for commands
                System.out.println("readyok");
                break;

            case "ucinewgame":
                // Reset the game state for a new game
                currentGameState = new GameState();
                // Cancel any ongoing search from a previous game
                if (currentSearchTask != null && !currentSearchTask.isDone()) {
                    currentSearchTask.cancel(true); // Interrupt the search thread
                }
                break;

            case "position":
                // Set up the board position based on FEN or starting position and apply moves
                handlePositionCommand(parts);
                break;

            case "go":
                // Start searching for the best move based on provided parameters
                handleGoCommand(parts);
                break;

            case "stop":
                // Stop the current search immediately
                if (currentSearchTask != null && !currentSearchTask.isDone()) {
                    currentSearchTask.cancel(true); // Interrupt the search thread
                }
                break;

            case "quit":
                // Shut down the engine and exit the application
                if (currentSearchTask != null && !currentSearchTask.isDone()) {
                    currentSearchTask.cancel(true);
                }
                searchExecutor.shutdownNow(); // Forcefully shut down the executor
                System.exit(0); // Terminate the JVM
                break;

            case "d": // Custom debug command: print current board and FEN
                currentGameState.getBoard().printBoard();
                System.out.println("FEN: " + currentGameState.toFEN());
                break;

            default:
                // For any unknown commands, print an info message (optional)
                System.out.println("info unknown command: " + command);
                break;
        }
    }

    /**
     * Handles the "position" UCI command.
     * This command sets the current board state. It can be either "startpos"
     * or a FEN string, optionally followed by a list of moves.
     * @param parts The array of strings representing the command parts.
     */
    private void handlePositionCommand(String[] parts) {
        int movesIndex = -1; // Index where the "moves" keyword or actual moves start

        // Check if the command specifies "startpos"
        if (parts.length > 1 && parts[1].equals("startpos")) {
            currentGameState = new GameState(); // Initialize to the standard starting position
            movesIndex = 2; // Moves, if any, will start from the third part
        }
        // Check if the command specifies a FEN string
        else if (parts.length > 1 && parts[1].equals("fen")) {
            StringBuilder fenBuilder = new StringBuilder();
            int i = 2;
            // Reconstruct the FEN string, as it can contain spaces
            while (i < parts.length && !parts[i].equals("moves")) {
                fenBuilder.append(parts[i]).append(" ");
                i++;
            }
            try {
                currentGameState = GameState.fromFEN(fenBuilder.toString().trim()); // Parse the FEN string
            } catch (IllegalArgumentException e) {
                // If FEN parsing fails, log the error and fall back to startpos
                System.err.println("Error parsing FEN: " + e.getMessage());
                currentGameState = new GameState(); // Fallback to startpos
            }
            movesIndex = i + 1; // Moves, if any, will start after the "moves" keyword
        } else {
            // If the position command is malformed, log an error and return
            System.err.println("Invalid position command: " + Arrays.toString(parts));
            return;
        }

        // Apply moves if present in the command
        if (movesIndex != -1 && movesIndex < parts.length) {
            for (int i = movesIndex; i < parts.length; i++) {
                String uciMoveStr = parts[i];
                // Parse the UCI move string into a Move object
                Move move = parseUciMove(uciMoveStr);
                if (move != null) {
                    currentGameState.makeMove(move); // Apply the move to the game state
                } else {
                    System.err.println("Invalid move in position command: " + uciMoveStr);
                }
            }
        }
    }

    /**
     * Parses a UCI move string (e.g., "e2e4", "e7e8q") into a Move object.
     * This method attempts to find a matching legal move.
     * NOTE: This is a simplified parser. A robust engine would generate *all*
     * legal moves for the current state and then find the one that matches
     * the UCI string. This implementation relies on the `MoveGenerator`
     * providing the necessary moves.
     * @param uciMoveStr The UCI move string (e.g., "e2e4", "g1f3", "e7e8q").
     * @return A Move object if a valid move is found, otherwise null.
     */
    private Move parseUciMove(String uciMoveStr) {
        if (uciMoveStr.length() < 4 || uciMoveStr.length() > 5) {
            return null; // UCI moves are typically 4 or 5 characters long
        }

        try {
            // Extract from and to squares from the UCI string
            int fromSquare = Move.algebraicToSquare(uciMoveStr.substring(0, 2));
            int toSquare = Move.algebraicToSquare(uciMoveStr.substring(2, 4));
            char promotionChar = ' '; // Default to no promotion

            // Determine if it's a promotion move
            if (uciMoveStr.length() == 5) {
                promotionChar = uciMoveStr.charAt(4);
            }

            // Generate all legal moves for the current state
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(currentGameState);

            // Iterate through legal moves to find a match
            for (Move move : legalMoves) {
                // Check if from and to squares match
                if (move.getFromSquare() == fromSquare && move.getToSquare() == toSquare) {
                    // If it's a promotion move, check if the promotion piece also matches
                    if (uciMoveStr.length() == 5) {
                        if (Character.toLowerCase(move.getPromotion()) == Character.toLowerCase(promotionChar)) {
                            return move; // Found a matching promotion move
                        }
                    } else {
                        // For non-promotion moves, ensure it's not a promotion move in the legal moves list
                        if (!move.getMoveType().isPromotion()) {
                            return move; // Found a matching normal move
                        }
                    }
                }
            }
            return null; // No matching legal move found
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing UCI move string '" + uciMoveStr + "': " + e.getMessage());
            return null;
        }
    }


    /**
     * Handles the "go" UCI command.
     * This command initiates the search for the best move. It can specify
     * time limits (wtime, btime, movetime) or a search depth.
     * @param parts The array of strings representing the command parts.
     */
    private void handleGoCommand(String[] parts) {
        long wtime = -1, btime = -1, movetime = -1; // White time, Black time, time for this move
        int depth = -1; // Search depth

        // Parse the parameters of the "go" command
        for (int i = 1; i < parts.length; i++) {
            switch (parts[i]) {
                case "wtime":
                    wtime = Long.parseLong(parts[++i]); // White's remaining time in milliseconds
                    break;
                case "btime":
                    btime = Long.parseLong(parts[++i]); // Black's remaining time in milliseconds
                    break;
                case "movetime":
                    movetime = Long.parseLong(parts[++i]); // Time to spend on this move in milliseconds
                    break;
                case "depth":
                    depth = Integer.parseInt(parts[++i]); // Search to a specific depth
                    break;
                // TODO: Implement other 'go' command parameters like 'infinite', 'nodes', 'ponder', etc.
            }
        }

        // Determine the effective time limit for the current move
        long timeLimitForThisMove = -1;
        if (movetime != -1) {
            timeLimitForThisMove = movetime; // If movetime is specified, use it
        } else if (currentGameState.isWhiteToMove() && wtime != -1) {
            // Simple time allocation: use a fraction of remaining time
            timeLimitForThisMove = wtime / 30; // A common heuristic, adjust as needed
        } else if (!currentGameState.isWhiteToMove() && btime != -1) {
            timeLimitForThisMove = btime / 30;
        }

        // Set the search engine's time limit
        if (timeLimitForThisMove != -1) {
            searchEngine = new Search((int) timeLimitForThisMove);
        } else {
            searchEngine = new Search(); // Use the default time limit if none specified
        }

        // Set the search engine's depth
        if (depth != -1) {
            searchEngine.setSearchDepth(depth);
        } else {
            searchEngine.setSearchDepth(5); // Default search depth if no depth specified
        }

        // Cancel any previously running search task to avoid conflicts
        if (currentSearchTask != null && !currentSearchTask.isDone()) {
            currentSearchTask.cancel(true);
        }

        // Submit the search task to the executor service.
        // This runs the search in a separate thread, keeping the main thread free
        // to receive further UCI commands (like 'stop').
        currentSearchTask = searchExecutor.submit(() -> {
            try {
                // Call the search engine to find the best move
                Move bestMove = searchEngine.findBestMove(currentGameState, searchEngine.getSearchDepth());
                if (bestMove != null) {
                    // If a best move is found, report it to the GUI
                    System.out.println("bestmove " + bestMove.toUCI());
                } else {
                    // If no move is found (e.g., game over, no legal moves), report it
                    System.out.println("info no best move found");
                }
            } catch (Exception e) {
                // Log any exceptions that occur during the search process
                System.err.println("Error during search: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
