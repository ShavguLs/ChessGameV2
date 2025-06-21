package com.ShavguLs.chess.logic;

import com.ShavguLs.chess.view.PromotionHandler;

public class Board {
    private static final int BOARD_SIZE = 8;

    // for en passant
    private int lastMoveStartRow = -1;
    private int lastMoveStartCol = -1;
    private int lastMoveEndRow = -1;
    private int lastMoveEndCol = -1;

    Piece[][] board = new Piece[8][8];

    public Board(){
        //setUpInitialPosition();
    }

    public void setPiece(int row, int col, Piece piece){
        board[row][col] = piece;
    }

    public Piece getPieceAt(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            return null; // Out of bounds
        }
        return board[row][col];
    }

    public Piece[][] getBoardArray() {
        return this.board;
    }

    public boolean attemptMove(int srcRow, int srcCol, int destRow, int destCol, boolean isWhiteTurn) {
        // A standard move is just an attempt with no specific promotion piece.
        // The more detailed method will handle asking the user if needed.
        return attemptMove(srcRow, srcCol, destRow, destCol, isWhiteTurn, null);
    }

    public boolean attemptMove(int srcRow, int srcCol, int destRow, int destCol, boolean isWhiteTurn, Piece promotionPiece) {
        Piece pieceToMove = getPieceAt(srcRow, srcCol);

        // 1. Basic validation
        if (pieceToMove == null || pieceToMove.isWhite() != isWhiteTurn) {
            return false;
        }

        // 2. Handle special case: En Passant
        if (pieceToMove instanceof Pawn && isEnPassantMove(srcRow, srcCol, destRow, destCol, isWhiteTurn)) {
            return handleEnPassant(srcRow, srcCol, destRow, destCol, isWhiteTurn);
        }

        // 3. Handle special case: Castling
        if (pieceToMove instanceof King && Math.abs(destCol - srcCol) == 2) {
            return handleCastling(srcRow, srcCol, destRow, destCol, isWhiteTurn);
        }

        // 4. Validate the move according to piece rules
        if (!pieceToMove.isValidMove(srcRow, srcCol, destRow, destCol, this.board)) {
            return false;
        }

        // 5. King-specific safety check
        if (pieceToMove instanceof King) {
            if (isSquareAttacked(destRow, destCol, !isWhiteTurn)) {
                return false;
            }
        }

        // 6. Check if move would leave own king in check
        Board boardAfterMove = this.copy();
        boardAfterMove.movePiece(srcRow, srcCol, destRow, destCol);
        if (boardAfterMove.isKingInCheck(isWhiteTurn)) {
            return false;
        }

        // 7. All validations passed - execute the move
        this.movePiece(srcRow, srcCol, destRow, destCol);
        pieceToMove.markMove();

        // 8. Handle promotion - FIXED: Check promotion BEFORE showing dialog
        if (pieceToMove instanceof Pawn) {
            boolean shouldPromote = false;

            // FIXED: More explicit promotion conditions
            if (pieceToMove.isWhite() && destRow == 0) {
                // White pawn reached the 8th rank (top of board, row 0)
                shouldPromote = true;
            } else if (!pieceToMove.isWhite() && destRow == 7) {
                // Black pawn reached the 1st rank (bottom of board, row 7)
                shouldPromote = true;
            }

            System.out.println("DEBUG (Promotion Check): Pawn at (" + srcRow + "," + srcCol + ") moved to (" + destRow + "," + destCol + "). IsWhite: " + pieceToMove.isWhite() + ". ShouldPromote: " + shouldPromote);

            // DEBUG: Add logging to help identify the issue
            if (pieceToMove instanceof Pawn) {
                System.out.println("DEBUG: Pawn move from (" + srcRow + "," + srcCol + ") to (" + destRow + "," + destCol + ")");
                System.out.println("DEBUG: Pawn is white: " + pieceToMove.isWhite());
                System.out.println("DEBUG: Should promote: " + shouldPromote);
            }

            if (shouldPromote) {
                if (promotionPiece != null) {
                    // Use the provided promotion piece (for validation/PGN parsing)
                    setPiece(destRow, destCol, promotionPiece);
                } else {
                    // FIXED: Only ask user for promotion choice AFTER move is fully validated
                    // This ensures the dialog only appears for legal promotion moves
                    Piece userChosenPiece = PromotionHandler.getPromotionChoice(pieceToMove.isWhite());
                    setPiece(destRow, destCol, userChosenPiece);
                }
            }
        }

        // 9. Update move tracking
        updateLastMove(srcRow, srcCol, destRow, destCol);


        this.isWhiteTurn = !this.isWhiteTurn;

        return true;
    }

    public void setupStandardBoard() {
        clearBoard(); // Start with a completely empty board

        // Black pieces (top of the board, rows 0 and 1)
        setPiece(0, 0, new Rook(false));
        setPiece(0, 1, new Knight(false));
        setPiece(0, 2, new Bishop(false));
        setPiece(0, 3, new Queen(false));
        setPiece(0, 4, new King(false));
        setPiece(0, 5, new Bishop(false));
        setPiece(0, 6, new Knight(false));
        setPiece(0, 7, new Rook(false));
        for (int i = 0; i < 8; i++) {
            setPiece(1, i, new Pawn(false));
        }

        // White pieces (bottom of the board, rows 6 and 7)
        for (int i = 0; i < 8; i++) {
            setPiece(6, i, new Pawn(true));
        }
        setPiece(7, 0, new Rook(true));
        setPiece(7, 1, new Knight(true));
        setPiece(7, 2, new Bishop(true));
        setPiece(7, 3, new Queen(true));
        setPiece(7, 4, new King(true));
        setPiece(7, 5, new Bishop(true));
        setPiece(7, 6, new Knight(true));
        setPiece(7, 7, new Rook(true));
    }

    void movePiece(int srcRow, int srcCol, int destRow, int destCol){
        Piece p = board[srcRow][srcCol];
        board[destRow][destCol] = p;
        board[srcRow][srcCol] = null;
    }

    public boolean isKingInCheck(boolean isWhiteKing) {
        int kingRow = -1, kingCol = -1;

        // Find the king
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = getPieceAt(r, c);
                if (piece instanceof King && piece.isWhite() == isWhiteKing) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
            if (kingRow != -1) break;
        }

        if (kingRow == -1) {
            return false; // Should not happen
        }

        // Check if that square is attacked by the OPPONENT
        boolean isAttacked = isSquareAttacked(kingRow, kingCol, !isWhiteKing);
        return isAttacked;
    }

    private boolean handleCastling(int srcRow, int srcCol, int destRow, int destCol, boolean isWhiteTurn) {
        if (isKingInCheck(isWhiteTurn)) {
            return false;
        }

        int direction = destCol - srcCol;
        int rookCol = (direction > 0) ? 7 : 0;
        Piece rook = getPieceAt(srcRow, rookCol);

        if (!(rook instanceof Rook) || rook.hasMoved()) {
            return false;
        }

        // Check path is clear
        int step = (direction > 0) ? 1 : -1;
        for (int c = srcCol + step; c != rookCol; c += step) {
            if (getPieceAt(srcRow, c) != null) {
                return false;
            }
        }

        // Check king doesn't pass through check
        if (isSquareAttacked(srcRow, srcCol + step, !isWhiteTurn)) {
            return false;
        }

        // Check king doesn't end in check
        Board boardAfterMove = this.copy();
        boardAfterMove.movePiece(srcRow, srcCol, destRow, destCol);
        if(boardAfterMove.isKingInCheck(isWhiteTurn)){
            return false;
        }

        // All checks passed, perform the castling
        movePiece(srcRow, srcCol, destRow, destCol); // Move King
        getPieceAt(destRow, destCol).markMove();
        movePiece(srcRow, rookCol, srcRow, srcCol + step); // Move Rook
        getPieceAt(srcRow, srcCol + step).markMove();

        updateLastMove(srcRow, srcCol, destRow, destCol);
        this.isWhiteTurn = !this.isWhiteTurn;
        return true;
    }

    public boolean isSquareAttacked(int row, int col, boolean byWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece attacker = board[r][c];
                if (attacker != null && attacker.isWhite() == byWhite) {
                    if (attacker.isAttackingSquare(r, c, row, col, board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Board copy() {
        Board newBoard = new Board();
        newBoard.clearBoard();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    newBoard.setPiece(row, col, piece.clone());
                }
            }
        }

        newBoard.lastMoveStartRow = this.lastMoveStartRow;
        newBoard.lastMoveStartCol = this.lastMoveStartCol;
        newBoard.lastMoveEndRow = this.lastMoveEndRow;
        newBoard.lastMoveEndCol = this.lastMoveEndCol;

        return newBoard;
    }

    public void clearBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = null;
            }
        }
        lastMoveStartRow = lastMoveStartCol = lastMoveEndRow = lastMoveEndCol = -1;
    }

    public boolean hasLegalMoves(boolean isWhiteSide) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = getPieceAt(r, c);
                if (piece != null && piece.isWhite() == isWhiteSide) {
                    for (int destR = 0; destR < 8; destR++) {
                        for (int destC = 0; destC < 8; destC++) {
                            Board tempBoard = this.copy();
                            if (tempBoard.attemptMove(r, c, destR, destC, isWhiteSide)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isEnPassantMove(int srcRow, int srcCol, int destRow, int destCol, boolean isWhiteTurn) {
        Piece movingPawn = getPieceAt(srcRow, srcCol);
        if (!(movingPawn instanceof Pawn)) {
            return false;
        }

        if (Math.abs(srcCol - destCol) == 1 && getPieceAt(destRow, destCol) == null) {
            int direction = isWhiteTurn ? -1 : 1;
            if (destRow == srcRow + direction) {
                Piece adjacentPiece = getPieceAt(srcRow, destCol);
                if (adjacentPiece instanceof Pawn && adjacentPiece.isWhite() != isWhiteTurn) {
                    return lastMoveEndRow == srcRow && lastMoveEndCol == destCol &&
                            lastMoveStartRow == srcRow + (isWhiteTurn ? -2 : 2) &&
                            lastMoveStartCol == destCol;
                }
            }
        }
        return false;
    }

    public boolean handleEnPassant(int srcRow, int srcCol, int destRow, int destCol, boolean isWhiteTurn) {
        // Simulate the move to check if it leaves king in check
        Board boardAfterMove = this.copy();
        boardAfterMove.movePiece(srcRow, srcCol, destRow, destCol);
        boardAfterMove.setPiece(srcRow, destCol, null); // Remove captured pawn

        if (boardAfterMove.isKingInCheck(isWhiteTurn)) {
            return false;
        }

        // Perform the actual en passant capture
        this.movePiece(srcRow, srcCol, destRow, destCol);
        this.setPiece(srcRow, destCol, null); // Remove the captured pawn

        updateLastMove(srcRow, srcCol, destRow, destCol);
        getPieceAt(destRow, destCol).markMove();
        this.isWhiteTurn = !this.isWhiteTurn;
        return true;
    }

    private void updateLastMove(int srcRow, int srcCol, int destRow, int destCol) {
        this.lastMoveStartRow = srcRow;
        this.lastMoveStartCol = srcCol;
        this.lastMoveEndRow = destRow;
        this.lastMoveEndCol = destCol;
    }


    // In: src/main/com/ShavguLs/chess/logic/Board.java
// Add these new public methods to the class.

    private boolean isWhiteTurn = true; // Add this field to the top of your Board class

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

// You already have a great attemptMove, we just need to make sure it flips the turn.
// Find your attemptMove method and add this one line at the end, right before 'return true;':
// this.isWhiteTurn = !this.isWhiteTurn;

    public boolean isGameOver() {
        // The game is over if the current player has no legal moves.
        return !hasLegalMoves(isWhiteTurn);
    }

    public String getGameResult() {
        if (!isGameOver()) {
            return "*"; // Game is still in progress
        }

        if (isKingInCheck(isWhiteTurn)) {
            // The current player is in checkmate. The other player won.
            return isWhiteTurn ? "0-1 (Black wins)" : "1-0 (White wins)";
        } else {
            // The current player has no moves but is not in check.
            return "1/2-1/2 (Stalemate)";
        }
    }

    // This is the FEN generator from before, which is essential for sending state updates.
    public String generateFen() {
        StringBuilder fen = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            int emptySquareCount = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = getPieceAt(row, col);
                if (piece == null) {
                    emptySquareCount++;
                } else {
                    if (emptySquareCount > 0) {
                        fen.append(emptySquareCount);
                        emptySquareCount = 0;
                    }
                    char symbol = ' ';
                    if (piece instanceof Pawn)   { symbol = 'p'; }
                    else if (piece instanceof Knight) { symbol = 'n'; }
                    else if (piece instanceof Bishop) { symbol = 'b'; }
                    else if (piece instanceof Rook)   { symbol = 'r'; }
                    else if (piece instanceof Queen)  { symbol = 'q'; }
                    else if (piece instanceof King)   { symbol = 'k'; }
                    fen.append(piece.isWhite() ? Character.toUpperCase(symbol) : symbol);
                }
            }
            if (emptySquareCount > 0) {
                fen.append(emptySquareCount);
            }
            if (row < 7) {
                fen.append('/');
            }
        }
        // Add the turn to the FEN string
        fen.append(isWhiteTurn ? " w" : " b");
        // A full FEN would also include castling rights, en passant target, etc.
        // fen.append(" KQkq - 0 1");
        return fen.toString();
    }


    public String generatePositionString() {
        StringBuilder sb = new StringBuilder();
        Piece[][] boardArray = this.getBoardArray(); // Use the board's own array

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = boardArray[row][col];
                if (piece == null) {
                    sb.append('.');
                } else {
                    char c = piece.getFenChar();
                    sb.append(c);
                }
            }
        }

        // Add turn indicator
        sb.append(this.isWhiteTurn ? 'W' : 'B'); // Use the board's own turn state

        return sb.toString();
    }



    // Add this new method to Board.java
    public void loadFen(String fen) {
        this.clearBoard();
        String[] parts = fen.split(" ");
        String boardState = parts[0];

        int row = 0;
        int col = 0;

        for (char c : boardState.toCharArray()) {
            if (c == '/') {
                row++;
                col = 0;
            } else if (Character.isDigit(c)) {
                col += Character.getNumericValue(c);
            } else {
                boolean isWhite = Character.isUpperCase(c);
                Piece piece = null;
                char lowerC = Character.toLowerCase(c);

                if (lowerC == 'k') piece = new King(isWhite);
                else if (lowerC == 'q') piece = new Queen(isWhite);
                else if (lowerC == 'r') piece = new Rook(isWhite);
                else if (lowerC == 'b') piece = new Bishop(isWhite);
                else if (lowerC == 'n') piece = new Knight(isWhite);
                else if (lowerC == 'p') piece = new Pawn(isWhite);

                if (piece != null) {
                    this.setPiece(row, col, piece);
                    col++;
                }
            }
        }

        if (parts.length > 1) {
            this.isWhiteTurn = parts[1].equals("w");
        }

        // A full FEN parser would also handle castling rights, en passant, etc.
        // This is sufficient for our needs right now.
    }
}