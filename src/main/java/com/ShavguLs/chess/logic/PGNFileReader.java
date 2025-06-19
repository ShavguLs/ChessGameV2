package com.ShavguLs.chess.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PGNFileReader {
    public static List<String> readGames(String filePath) throws IOException{
        String content = Files.readString(Path.of(filePath));


        content = content.replaceAll("\r\n", "\n");


        String[] games = content.split("(?=\\[Event )");

        List<String> cleanGames = new ArrayList<>();
        for (String game : games) {
            game = game.trim();
            if (!game.isEmpty()) {
                cleanGames.add(game);
            }
        }

        return cleanGames;
    }

}

