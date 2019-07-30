package com.github.gumtreediff.client;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    public static ArrayList<FilePair> getChangedFiles(String repoDir, String status) {
        ArrayList<FilePair> filePairList = new ArrayList<>();
        String lines[] = status.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String temp[] = lines[i].trim().split("\\s+");
            String symbol = temp[0];
            String relativePath = temp[1];
            String absolutePath = repoDir + File.separator + relativePath;
            ChangeType changeType = getTypeFromSymbol(symbol);
            FilePair filePair = null;
            switch (changeType) {
                case MODIFIED:
                    filePair = new FilePair(changeType, relativePath, relativePath, getContentAtHEAD(repoDir, relativePath), readFileToString(absolutePath));
                    break;
                case ADDED:
                case UNTRACKED:
                    filePair = new FilePair(changeType, "", relativePath, "", readFileToString(absolutePath));
                    break;
                case DELETED:
                    filePair = new FilePair(changeType, relativePath, "", getContentAtHEAD(repoDir, relativePath), "");
                    break;
                case RENAMED:
                case COPIED:
                    if (temp.length == 4) {
                        String oldPath = temp[1];
                        String newPath = temp[3];
                        String newAbsPath = repoDir + File.separator + temp[3];
                        filePair = new FilePair(changeType, oldPath, newPath, getContentAtHEAD(repoDir, oldPath), readFileToString(newAbsPath));
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

    /**
     * Parse the output of git diff SHA and return changed file pairs
     *
     * @return
     */
    public static ArrayList<FilePair> getChangedFilesAtCommit(String repoDir, String commitID) {
        String output =
                Utils.runSystemCommand(
                        repoDir,
                        "git",
                        "diff",
                        "--name-status", commitID, commitID + "~");
        ArrayList<FilePair> filePairList = new ArrayList<>();
        String lines[] = output.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String temp[] = lines[i].trim().split("\\s+");
            String symbol = temp[0];
            String relativePath = temp[1];
//            String absolutePath = repoDir + File.separator + relativePath;
            ChangeType changeType = getTypeFromSymbol(symbol);
            FilePair filePair = null;
            switch (changeType) {
                case MODIFIED:
                    filePair = new FilePair(changeType, relativePath, relativePath, getContentAtCommit(repoDir, relativePath, commitID + "~"), getContentAtCommit(repoDir, relativePath, commitID));
                    break;
                case ADDED:
                case UNTRACKED:
                    filePair = new FilePair(changeType, "", relativePath, "", getContentAtCommit(repoDir, relativePath, commitID));
                    break;
                case DELETED:
                    filePair = new FilePair(changeType, relativePath, "", getContentAtCommit(repoDir, relativePath, commitID + "~"), "");
                    break;
                case RENAMED:
                case COPIED:
                    if (temp.length == 4) {
                        String oldPath = temp[1];
                        String newPath = temp[3];
                        filePair = new FilePair(changeType, oldPath, newPath, getContentAtCommit(repoDir, oldPath, commitID + "~"), getContentAtCommit(repoDir, newPath, commitID));
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

    /**
     * Get the file content at HEAD
     *
     * @param relativePath
     * @return
     */
    private static String getContentAtHEAD(String repoDir, String relativePath) {
        String output = runSystemCommand(repoDir,
                "git",
                "show",
                "HEAD:" + relativePath);
        if (output != null) {
            return output;
        } else {
            return "";
        }
    }

    /**
     * Get the file content at commit
     *
     * @param relativePath
     * @return
     */
    private static String getContentAtCommit(String repoDir, String relativePath, String commitID) {
        String output = runSystemCommand(repoDir,
                "git",
                "show",
                commitID + ":" + relativePath);
        if (output != null) {
            return output;
        } else {
            return "";
        }
    }

    /**
     * Read the content of a given file.
     *
     * @param path to be read
     * @return string content of the file, or null in case of errors.
     */
    public static String readFileToString(String path) {
        String content = "";
        File file = new File(path);
        if (file.exists()) {
            String fileEncoding = "UTF-8";
            try (BufferedReader reader =
                         Files.newBufferedReader(Paths.get(path), Charset.forName(fileEncoding))) {
                content = reader.lines().collect(Collectors.joining("\n"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(path + " does not exist!");
        }
        return content;
    }

    /**
     * Writes the given content in the file of the given file path.
     *
     * @param filePath
     * @param content
     * @return boolean indicating the success of the write operation.
     */
    public static boolean writeContentToPath(String filePath, String content) {
        if (!content.isEmpty()) {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
                writer.write(content);
                writer.flush();
                writer.close();
            } catch (NullPointerException ne) {
                ne.printStackTrace();
                // empty, necessary for integration with git version control system
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Delete all files and subfolders to clear the directory
     *
     * @param dir absolute path
     * @return
     */
    public static boolean clearDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            return false;
        }

        String[] content = file.list();
        for (String name : content) {
            File temp = new File(dir, name);
            if (temp.isDirectory()) {
                clearDir(temp.getAbsolutePath());
                temp.delete();
            } else {
                if (!temp.delete()) {
                    System.err.println("Failed to delete the directory: " + name);
                }
            }
        }
        return true;
    }
}

