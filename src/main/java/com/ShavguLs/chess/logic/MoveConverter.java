package com.ShavguLs.chess.logic;

public class MoveConverter {
    public static String convertMoveToNotation(Piece piece, int srcRow, int srcCol,
                                               int destRow, int destCol, Board board,
                                               boolean wasCapture, boolean causesCheck,
                                               boolean causesCheckmate, boolean isCastleMove,
                                               Piece promotionPiece) {

        if (isCastleMove) {
            return handleCastlingMove(srcCol, destCol);
        }

        StringBuilder moveText = new StringBuilder();
        String pieceLetter = getPieceLetter(piece);

        // Add piece letter (except for pawns)
        if (!pieceLetter.equals("")) {
            moveText.append(pieceLetter);
        }

        // Add disambiguation if needed (for pieces other than pawns)
        if (!(piece instanceof Pawn)) {
            String extraInfo = findDisambiguationInfo(piece, srcRow, srcCol, destRow, destCol, board);
            moveText.append(extraInfo);
        }

        // For pawn captures, add the source file
        if (piece instanceof Pawn && wasCapture) {
            moveText.append(getFileLetterFromNumber(srcCol));
        }

        // Add capture notation
        if (wasCapture) {
            moveText.append("x");
        }

        // Add destination square
        moveText.append(getSquareName(destRow, destCol));

        // Add promotion notation
        if (promotionPiece != null) {
            moveText.append("=").append(getPieceLetter(promotionPiece));
        }

        // Add check/checkmate notation
        if (causesCheckmate) {
            moveText.append("#");
        } else if (causesCheck) {
            moveText.append("+");
        }

        return moveText.toString();
    }

    private static String handleCastlingMove(int srcCol, int destCol) {
        if (destCol > srcCol) {
            return "O-O";  // Kingside castling
        } else {
            return "O-O-O";  // Queenside castling
        }
    }

    private static String getPieceLetter(Piece piece) {
        if (piece instanceof King) {
            return "K";
        } else if (piece instanceof Queen) {
            return "Q";
        } else if (piece instanceof Rook) {
            return "R";
        } else if (piece instanceof Bishop) {
            return "B";
        } else if (piece instanceof Knight) {
            return "N";
        } else if (piece instanceof Pawn) {
            return "";
        }
        return "";
    }

    private static String findDisambiguationInfo(Piece piece, int srcRow, int srcCol,
                                                 int destRow, int destCol, Board board) {
        boolean needsExtraInfo = false;
        boolean sameFileAsOther = false;
        boolean sameRankAsOther = false;

        // Check if any other piece of the same type can move to the same destination
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece otherPiece = board.getPieceAt(r, c);
                if (otherPiece == null || otherPiece == piece) {
                    continue;
                }

                // Same piece type and color?
                if (otherPiece.getClass().equals(piece.getClass()) &&
                        otherPiece.isWhite() == piece.isWhite()) {

                    // Can this other piece also move to the destination?
                    if (otherPiece.isValidMove(r, c, destRow, destCol, board.getBoardArray())) {
                        needsExtraInfo = true;
                        if (c == srcCol) {
                            sameFileAsOther = true;
                        }
                        if (r == srcRow) {
                            sameRankAsOther = true;
                        }
                    }
                }
            }
        }

        if (!needsExtraInfo) {
            return "";
        }

        // If pieces are on different files, use file letter
        if (!sameFileAsOther) {
            return getFileLetterFromNumber(srcCol);
        }
        // If pieces are on different ranks, use rank number
        else if (!sameRankAsOther) {
            return getRankNumberFromPosition(srcRow);
        }
        // If pieces are on same file and rank (shouldn't happen), use full square
        else {
            return getSquareName(srcRow, srcCol);
        }
    }

    private static String getFileLetterFromNumber(int fileNumber) {
        char fileLetter = (char)('a' + fileNumber);
        return String.valueOf(fileLetter);
    }

    private static String getRankNumberFromPosition(int rankNumber) {
        int chessRank = 8 - rankNumber;  // Convert array index to chess rank
        return String.valueOf(chessRank);
    }

    private static String getSquareName(int row, int col) {
        String file = getFileLetterFromNumber(col);
        String rank = getRankNumberFromPosition(row);
        return file + rank;
    }

    public static boolean moveCausesCheck(Board board, boolean isWhiteKing) {
        return board.isKingInCheck(isWhiteKing);
    }

    public static boolean moveCausesCheckmate(Board board, boolean isWhiteKing) {
        return board.isKingInCheck(isWhiteKing) && !board.hasLegalMoves(isWhiteKing);
    }
}