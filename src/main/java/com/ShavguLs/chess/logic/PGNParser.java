package com.ShavguLs.chess.logic;

import java.util.ArrayList;
import java.util.List;

public class PGNParser {
    public static List<String> parseMoves(String pgn){
        //This part of code removes metadata which pgn file contains. We do this by skipping lines that start with [
        StringBuilder movesOnly = new StringBuilder();
        String[] lines = pgn.split("\\R"); //Split by lines

        for(String line : lines){
            line = line.trim();
            if(!line.startsWith("[") && !line.isEmpty()){
                movesOnly.append(line).append(" ");
            }
        }


        String cleaned = movesOnly.toString().replaceAll("\\d+\\.(\\.\\.\\.)?", "").replaceAll("\\s+", " ").trim();


        // This part of code parses string of moves by removing empty spaces and putting each component in string array
        // and then removes numbers like 1. 2. and so on. leaves only list of move notations
        List<String> moves = new ArrayList<>();

        for(String part : cleaned.split(" ")){

            if (part.isEmpty() || part.matches("1-0|0-1|1/2-1/2|\\*")) {
                continue;
            }

            if(!(part.contains("-") && !part.contains("O"))){
                moves.add(part);
            }
        }
        return moves;
    }
}
