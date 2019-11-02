package it.davidemerli.secretsanta;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class SettingsReader {

    static List<String> getLinesFromFile(File file) throws IOException {
        List<String> list = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(file));

        //Ignores lines starting with '#'
        br.lines().filter(s -> !s.startsWith("#")).forEach(list::add);
        br.close();

        return list;
    }
}
