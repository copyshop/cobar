package com.alibaba.cobar.compare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

// This class compares the files and folders between the frontendConnection and destination directory
public class Compare {

    private static String srcPath;
    private static String dstPath;
    private static String logPath;
    private String runTime = now();
    private String srcLogId = runTime + "_S.log";
    private String dstLogId = runTime + "_D.log";

    // compare frontendConnection and destination. In addition, write to log's path
    Compare(String src, String dst, String log) {
        srcPath = src;
        dstPath = dst;
        logPath = log;
        File logFileBuilder = new File(log);
        if (!logFileBuilder.exists()) {
            logFileBuilder.mkdirs();
        }
        // The log file will be stored in "Defaced" directory
        File logDefacedBuilder = new File(log + "/Defaced");
        if (!logDefacedBuilder.exists()) {
            logDefacedBuilder.mkdirs();
        }
    }

    //Getters
    public static String getSrcPath() {
        return srcPath;
    }

    public static String getDstPath() {
        return dstPath;
    }

    public static String getLogPath() {
        return logPath;
    }


    // Compare the files and write a log
    protected void CompareFiles(Boolean fullCopy) {
        CompareFiles(srcPath, dstPath, logPath, fullCopy);
    }

    private void CompareFiles(String src, String dst, String log, Boolean fullCopy) {

        // Set log files path
        File fileSrc = new File(log + "/" + srcLogId);
        File fileDst = new File(log + "/" + dstLogId);

        // Parse from string to directory name
        File dirSrc = new File(src);
        File dirDst = new File(dst);

        // Delete existing data - we don't need it
        if (fileSrc.exists())
            fileSrc.delete();
        if (fileDst.exists())
            fileDst.delete();

        // Hash all files recursively in both folders
        if (dirSrc.isDirectory())
            getAllSubDirsAndFiles(src, srcLogId);
        if (dirDst.isDirectory())
            getAllSubDirsAndFiles(dst, dstLogId);

        // Trying to compare the MD5 of both log files. If equal - no defacement occurred
        try {
            try {
                if (MD5.strToMD5(getHashStrings(fileSrc, srcLogId)).equals(MD5.strToMD5(getHashStrings(fileDst, dstLogId)))) {
                    // No problems...
                    if (fileSrc.exists())
                        fileSrc.delete();
                    if (fileDst.exists())
                        fileDst.delete();
                    System.out.println("Site is healthy @ " + now() + ".");
                } else {
                    System.out.println("Defacement detected @ " + now() + ". Started remediation process.");
                    if (fullCopy)
                        undefaceSite(dirSrc, dirDst, new File(log + "/Defaced/" + runTime));
                    else
                        undefaceSite(dirSrc, srcLogId, dirDst, dstLogId, new File(log + "/Defaced/" + runTime));
                    System.out.println("Remediation complete @ " + now() + ".");
                    System.out.println("Malicious files has been moved to path: " + log + "/Defaced/" + runTime);
                }
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    // Print all files with their hashes to a comparison file
    private static void getAllSubDirsAndFiles(String dir, String Id) {
        try {
            FileWriter fstream = new FileWriter(logPath + "/" + Id, true);
            BufferedWriter out = new BufferedWriter(fstream);
            // Parse to file object
            File objName = new File(dir);

            if (objName.isDirectory()) {
                File[] files = objName.listFiles();
                for (File obj : files) {
                    getAllSubDirsAndFiles(obj.toString(), Id);
                }
            } else if (objName.isFile()) {
                // Create checksum file
                out.append(MD5.fileToMD5(objName) + "\t" + objName + "\n");
                out.close();
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

    }

    // This method needed in order to hash all hashed of files
    private static String getHashStrings(File file, String Id) {

        try {
            BufferedReader inFile = new BufferedReader(new FileReader(logPath + "/" + Id));
            String strLn;
            String outStr = "";
            while ((strLn = inFile.readLine()) != null) {
                outStr += strLn.substring(0, 31);
            }
            inFile.close();
            try {
                return MD5.strToMD5(outStr);
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e) {
        }
        return null;
    }


    private Hashtable<String, String> getHashtableOfStrings(File file, String Id) {

        try {
            BufferedReader inFile = new BufferedReader(new FileReader(logPath + "/" + Id));
            String strLn;
            Hashtable<String, String> outStr = new Hashtable<String, String>();
            while ((strLn = inFile.readLine()) != null) {
                outStr.put(strLn.substring(0, 31), strLn.substring(33));
            }
            inFile.close();
            return outStr;
        } catch (IOException e) {
        }
        return null;
    }

    // If defacement found, all files from backup are copied to the defaced folder.
    private void undefaceSite(File srcDir, File dstDir, File junk) {
        if (dstDir.isDirectory()) {

            // Copy from frontendConnection
            try {
                copyDirectory(dstDir, junk);
                dstDir.delete();
                copyDirectory(srcDir, dstDir);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }


    private void undefaceSite(File logSrc, String srcId, File logDst, String dstId, File copyDefacedFiles) throws IOException {
        // Create directory for junk files
        if (!copyDefacedFiles.exists()) {
            copyDefacedFiles.mkdirs();
        }

        Hashtable<String, String> srcHash = getHashtableOfStrings(logSrc, srcId);
        Hashtable<String, String> dstHash = getHashtableOfStrings(logDst, dstId);
        Enumeration<String> srcKeys = srcHash.keys();
        String element = new String();

        // Copy files from frontendConnection to destination and check if destination files content has been changed
        while (srcKeys.hasMoreElements()) {
            element = srcKeys.nextElement().toString();
            if (!dstHash.containsKey(element)) {
                String source = new String(srcHash.get(element));
                String destination = new String(srcHash.get(element).replaceFirst(srcPath, dstPath));
                if (dstHash.containsValue(destination)) {
                    // The file has been changed on the website
                    String junk = new String(srcHash.get(element).replaceFirst(srcPath, copyDefacedFiles.getAbsolutePath() + "/"));
                    System.out.println("Source: " + source + "Destination: " + destination + "Junk: " + junk);
                    copyFile(new File(destination), new File(junk));
                    dstHash.values().remove(destination);

                }
                copyFile(new File(source), new File(destination));
            } else {
                dstHash.remove(element);
            }
            srcHash.remove(element);
        }
        srcKeys = srcHash.keys();
        Enumeration<String> dstKeys = dstHash.keys();

        // If destination web site includes files that do not exist in the frontendConnection, they will be deleted also
        while (dstKeys.hasMoreElements()) {

            element = dstKeys.nextElement().toString();
            String toDeletePath = new String(dstHash.get(element));
            File toDelete = new File(toDeletePath);
            copyFile(toDelete, new File(dstHash.get(element).replaceFirst(dstPath, copyDefacedFiles.getCanonicalPath())));
            toDelete.delete();
        }

    }

    // Binary copy all contents from frontendConnection directory to destination.
    private static void copyDirectory(File srcPath, File dstPath) throws IOException {
        if (srcPath.isDirectory()) {
            if (!dstPath.exists()) {
                dstPath.mkdirs();
            }

            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
            }
        } else {
            if (!srcPath.exists()) {
                System.out.println("File or directory does not exist.");
                System.exit(0);
            } else {
                InputStream in = new FileInputStream(srcPath);
                OutputStream out = new FileOutputStream(dstPath);

                System.out.println("File frontendConnection: " + srcPath + " | File destination: " + dstPath);


                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        }
        //System.out.println("Directory copied.");
    }

    private static void copyFile(File srcFile, File dstFile) throws IOException {
        if (srcFile.isDirectory() || dstFile.isDirectory() || !srcFile.exists()) {
            // for future writing TBD...
            return;
        } else {
            // Create sub-directories for the file.
            File subDirs = new File(dstFile.getParent());
            if (!subDirs.isDirectory())
                subDirs.mkdirs();

            // create a new file
            dstFile.createNewFile();

            // open streams
            InputStream in = new FileInputStream(srcFile);
            OutputStream out = new FileOutputStream(dstFile);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    // Return time-based string
    private static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm");
        return sdf.format(cal.getTime());

    }

}
