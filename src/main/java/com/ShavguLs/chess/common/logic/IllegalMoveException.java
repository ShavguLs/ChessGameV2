package com.ShavguLs.chess.common.logic;

public class IllegalMoveException extends RuntimeException{

    public IllegalMoveException(String message){
        super(message);
    }

}
