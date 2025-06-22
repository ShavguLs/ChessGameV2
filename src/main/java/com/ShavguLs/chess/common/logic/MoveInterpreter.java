package com.ShavguLs.chess.common.logic;

public class MoveInterpreter {
    private final Board board;
    private boolean whiteToMove = true;

    public MoveInterpreter(Board board) {
        this.board = board;
    }

    // Enhanced interpretMove method that handles promotions without showing dialogs
    public void interpretMove(String move) throws IllegalMoveException {
        // Handle castling by delegating to the board
        if (move.equals("O-O") || move.equals("O-O-O")) {
            int row = whiteToMove ? 7 : 0;
            if (board.getPieceAt(row, 4) instanceof King) {
                int destCol = move.equals("O-O") ? 6 : 2;
                if (board.attemptMove(row, 4, row, destCol, whiteToMove)) {
                    whiteToMove = !whiteToMove;
                    return;
                }
            }
            throw new IllegalMoveException("Illegal castling move: " + move);
        }

        // Parse promotion notation first
        Piece promotionPiece = null;
        if (move.contains("=")) {
            int index = move.indexOf("=");
            char promotionChar = move.charAt(index + 1);
            move = move.substring(0, index);

            // Create the promotion piece based on the notation
            promotionPiece = switch (promotionChar) {
                case 'Q' -> new Queen(whiteToMove);
                case 'R' -> new Rook(whiteToMove);
                case 'N' -> new Knight(whiteToMove);
                case 'B' -> new Bishop(whiteToMove);
                default -> null;
            };
        }

        // Parse piece type
        char pieceChar = 'P';
        int startIndex = 0;
        if (Character.isUpperCase(move.charAt(0))) {
            pieceChar = move.charAt(0);
            startIndex = 1;
        }

        // Remove check/checkmate/capture symbols
        move = move.replaceAll("[+#x]", "");

        // Extract destination square
        String dest = move.substring(move.length() - 2);
        int destCol = dest.charAt(0) - 'a';
        int destRow = 7 - (Character.getNumericValue(dest.charAt(1)) - 1);

        // Extract disambiguation
        String disambiguation = move.substring(startIndex, move.length() - 2);

        // Find source square
        int[] source = findSourceSquare(pieceChar, destRow, destCol, disambiguation);

        if (source == null) {
            throw new IllegalMoveException("Illegal or ambiguous move: Could not find source for " + move);
        }

        int srcRow = source[0];
        int srcCol = source[1];

        // Use the enhanced attemptMove with promotion piece parameter
        if (board.attemptMove(srcRow, srcCol, destRow, destCol, whiteToMove, promotionPiece)) {
            whiteToMove = !whiteToMove;
        } else {
            throw new IllegalMoveException("Illegal move (rejected by Board): " + move);
        }
    }

    // Finds Source square for move
    int[] findSourceSquare(char pieceChar, int destRow, int destCol, String disambiguation) {
        System.out.printf("Looking for move to (%d, %d) = %c%d with disambiguation [%s]\n",
                destRow, destCol, (char) (destCol + 'a'), 8 - destRow, disambiguation);

        // --- SPECIAL CHECK FOR EN PASSANT ---
        // En passant only happens with pawns on an empty destination square.
        if (pieceChar == 'P' && board.getPieceAt(destRow, destCol) == null) {
            // The attacking pawn must be on the row behind the destination.
            int pawnSrcRow = destRow - (whiteToMove ? -1 : 1);
            // The source column is given by the disambiguation string (e.g., 'e' in exd6)
            if (!disambiguation.isEmpty() && Character.isLetter(disambiguation.charAt(0))) {
                int pawnSrcCol = disambiguation.charAt(0) - 'a';

                Piece potentialPawn = board.getPieceAt(pawnSrcRow, pawnSrcCol);
                if (potentialPawn instanceof Pawn && potentialPawn.isWhite() == whiteToMove) {
                    // We found a pawn in the right spot. Now, ask the BOARD if this is a valid en passant.
                    if (board.isEnPassantMove(pawnSrcRow, pawnSrcCol, destRow, destCol, whiteToMove)) {
                        // Check for pins before returning
                        Board simulated = board.copy();
                        simulated.handleEnPassant(pawnSrcRow, pawnSrcCol, destRow, destCol, whiteToMove);
                        if (!simulated.isKingInCheck(whiteToMove)) {
                            return new int[]{pawnSrcRow, pawnSrcCol};
                        }
                    }
                }
            }
        }
        // --- END OF SPECIAL CHECK ---



        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece == null || piece.isWhite() != whiteToMove || !matchPieceType(piece, pieceChar)) {
                    continue;
                }
                if (!piece.isValidMove(row, col, destRow, destCol, board.getBoardArray())) {
                    continue;
                }

                // Disambiguation Logic
                if (!disambiguation.isEmpty()) {
                    if (disambiguation.length() == 2) {
                        // ... (rest of your disambiguation logic is fine)
                    } else if (Character.isDigit(disambiguation.charAt(0))) {
                        char rankChar = disambiguation.charAt(0);
                        int rankRow = 7 - (Character.getNumericValue(rankChar) - 1);
                        if (row != rankRow) continue;
                    } else if (Character.isLetter(disambiguation.charAt(0))) {
                        char fileChar = disambiguation.charAt(0);
                        int fileCol = fileChar - 'a';
                        if (col != fileCol) continue;
                    }
                }

                // Check if move leaves king in check
                Board simulated = board.copy();
                simulated.movePiece(row, col, destRow, destCol);
                if (simulated.isKingInCheck(whiteToMove)) {
                    continue;
                }

                return new int[]{row, col};
            }
        }
        return null; // No legal source found
    }

    private boolean matchPieceType(Piece piece, char pieceChar) {
        return switch (pieceChar) {
            case 'N' -> piece instanceof Knight;
            case 'B' -> piece instanceof Bishop;
            case 'R' -> piece instanceof Rook;
            case 'Q' -> piece instanceof Queen;
            case 'K' -> piece instanceof King;
            case 'P' -> piece instanceof Pawn;
            default -> false;
        };
    }
}