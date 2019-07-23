package com.github.gumtreediff.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Utils {
    /**
     * Run system command and return the output
     *
     * @param dir
     * @param commands
     * @return
     */
    public static String runSystemCommand(String dir, String... commands) {
        StringBuilder builder = new StringBuilder();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(commands, null, new File(dir));

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) {
                builder.append(s);
                builder.append("\n");
                //                if (verbose) log(s);
            }

            while ((s = stdError.readLine()) != null) {
                builder.append(s);
                builder.append("\n");
                //                if (verbose) log(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    /**
     * Parse the output of git status porcelain and return changed file pairs
     *
     * @return
     */
    public static ArrayList<FilePair> getChangedFiles(String status) {
        ArrayList<FilePair> filePairList = new ArrayList<>();
        String lines[] = status.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String temp[] = lines[i].trim().split("\\s+");
            String symbol = temp[0];
            String path = temp[1];
            ChangeType changeType = getTypeFromSymbol(symbol);
            FilePair filePair = null;
            switch (changeType) {
                case MODIFIED:
                    filePair = new FilePair(changeType, path, path);
                    break;
                case ADDED:
                case UNTRACKED:
                    filePair = new FilePair(changeType, "", path);
                    break;
                case DELETED:
                    filePair = new FilePair(changeType, path, "");
                    break;
                case RENAMED:
                case COPIED:
                    if (temp.length == 4) {
                        String oldPath = temp[1];
                        String newPath = temp[3];
                        filePair = new FilePair(changeType, oldPath, newPath);
                    }
                    break;
                default:
                    break;
            }
            if (filePair != null) {
                filePairList.add(filePair);
            }
        }
        return filePairList;
    }

    private static ChangeType getTypeFromSymbol(String symbol) {
        for (ChangeType type : ChangeType.values()) {
            if (symbol.equals(type.symbol)) {
                return type;
            }
        }
        return ChangeType.UNMODIFIED;
    }
}

