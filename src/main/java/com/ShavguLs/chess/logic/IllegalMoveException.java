package com.ShavguLs.chess.logic;

public class IllegalMoveException extends RuntimeException{

    public IllegalMoveException(String message){
        super(message);
    }

}
