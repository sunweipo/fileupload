package com.swp.fileupload.util;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月22日 下午4:35:08 $
 */


import cn.hutool.core.lang.Validator;

import java.io.*;
import java.util.*;

public final class FileUtils {
    public static final String FILE_TYPE_TXT = ".txt";
    public static final String FILE_TYPE_XLS = ".xls";
    public static final String FILE_TYPE_TEMP = ".temp";
    public static final String FILE_TYPE_ZIP = ".zip";
    private static final Set<String> FILE_TYPE_VALID_SET = new HashSet();

    static {
        FILE_TYPE_VALID_SET.add(".txt");
        FILE_TYPE_VALID_SET.add(".xls");
        FILE_TYPE_VALID_SET.add(".temp");
        FILE_TYPE_VALID_SET.add(".zip");
    }

    public static List<File> getFiles(String dir, Set<String> fileType) {
        List<File> resultList = new ArrayList();
        File file = new File(dir);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();

            for (int i = 0; i < files.length; ++i) {
                if (isValidFile(files[i], fileType)) {
                    resultList.add(files[i]);
                }
            }
        }

        return resultList;
    }

    public static List<File> getFiles(String dir, String fileType) {
        if (!FILE_TYPE_VALID_SET.contains(fileType)) {
            return Collections.emptyList();
        }
        else {
            Set<String> fileTypeSet = new HashSet();
            fileTypeSet.add(fileType);
            return getFiles(dir, fileTypeSet);
        }
    }

    private static boolean isValidFile(File file, Set<String> fileTypeSet) {
        if (fileTypeSet != null && !fileTypeSet.isEmpty()) {
            if (file != null && file.isFile()) {
                String fileName = file.getName();
                int lastIndexOfPoint = fileName.lastIndexOf(".");
                if (lastIndexOfPoint == -1) {
                    return false;
                }
                else {
                    String fileType = fileName.substring(lastIndexOfPoint);
                    return fileTypeSet.contains(fileType) && FILE_TYPE_VALID_SET.contains(fileType);
                }
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    private static boolean isValidFile(File file) {
        if (file != null && file.isFile()) {
            String fileName = file.getName();
            int lastIndexOfPoint = fileName.lastIndexOf(".");
            if (lastIndexOfPoint == -1) {
                return false;
            }
            else {
                String fileType = fileName.substring(lastIndexOfPoint);
                return FILE_TYPE_VALID_SET.contains(fileType);
            }
        }
        else {
            return false;
        }
    }

    public static List<File> getFiles(String dir) {
        return getFiles(dir, ".txt");
    }

    public static boolean removeFile(String dir, String fileName) {
        return removeFile(dir + File.separator + fileName);
    }

    public static boolean removeFile(String fullPathName) {
        File file = new File(fullPathName);
        return isValidFile(file) ? file.delete() : false;
    }

    public static File getFile(String dir, String fileName) {
        return getFile(dir + File.separator + fileName);
    }

    public static File getFile(String fullPathName) {
        File file = new File(fullPathName);
        if (isValidFile(file)) {
            return file.isFile() ? file : null;
        }
        else {
            return null;
        }
    }

    public static void clearFile(String dir, Set<String> fileType, long interval) {
        List<File> files = getFiles(dir, fileType);
        Iterator var6 = files.iterator();

        while (var6.hasNext()) {
            File file = (File) var6.next();
            if (file.isFile() && System.currentTimeMillis() - file.lastModified() > interval) {
                file.delete();
            }
        }

    }

    public static void clearFile(String dir, String fileType, long interval) {
        Set<String> fileTypeSet = new HashSet();
        fileTypeSet.add(fileType);
        clearFile(dir, fileTypeSet, interval);
    }

    public static void clearFile(String dir, long interval) {
        clearFile(dir, ".txt", interval);
    }

    public static String createFold(String root, String directory) {
        String fold2Path = root;
        if (!Validator.isEmpty(directory)) {
            fold2Path = root + File.separator + directory;
        }

        File fold1 = new File(fold2Path);
        if (!fold1.exists()) {
            fold1.mkdirs();
        }

        return fold2Path;
    }

    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        FileInputStream input = null;
        BufferedInputStream inBuff = null;
        FileOutputStream output = null;
        BufferedOutputStream outBuff = null;

        try {
            input = new FileInputStream(sourceFile);
            inBuff = new BufferedInputStream(input);
            output = new FileOutputStream(targetFile);
            outBuff = new BufferedOutputStream(output);
            byte[] b = new byte[5120];

            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }

            outBuff.flush();
        }
        catch (Exception var11) {
            ;
        }
        finally {
            if (inBuff != null) {
                inBuff.close();
            }

            if (outBuff != null) {
                outBuff.close();
            }

            if (output != null) {
                output.close();
            }

            if (input != null) {
                input.close();
            }

        }

    }

    public static void copyDirectiory(String sourceDir, String targetDir) throws IOException {
        File targetFold = new File(targetDir);
        if (!targetFold.exists()) {
            targetFold.mkdirs();
        }

        File[] file = (new File(sourceDir)).listFiles();
        if (!Validator.isEmpty(file)) {
            for (int i = 0; i < file.length; ++i) {
                if (file[i].isFile()) {
                    File sourceFile = file[i];
                    File targetFile = new File((new File(targetDir)).getAbsolutePath() + File.separator
                            + file[i].getName());
                    copyFile(sourceFile, targetFile);
                }

                if (file[i].isDirectory()) {
                    String dir1 = sourceDir + "/" + file[i].getName();
                    String dir2 = targetDir + "/" + file[i].getName();
                    copyDirectiory(dir1, dir2);
                }
            }

        }
    }

    public static boolean deleteFile(File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                String[] children = file.list();

                for (int i = 0; i < children.length; ++i) {
                    boolean success = deleteFile(new File(file, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

            return file.delete();
        }
        else {
            return true;
        }
    }

    public static String addDomain(String domain, String filePath) {
        return !Validator.isEmpty(filePath) && !filePath.startsWith("http") ? domain + filePath : filePath;
    }

    public static String getFileExt(String fileName) {
        if (!Validator.isEmpty(fileName) && fileName.length() >= 2) {
            String s = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
            return s.trim().toLowerCase();
        }
        return "";
    }

    public static boolean isImage(String ext) {
        if (Validator.isEmpty(ext)) {
            return false;
        }
        else {
            String[] picExts = new String[] { "jpg", "jpeg", "png", "bmp", "gif", "webp" };
            Set<String> picExtSet = Collections.unmodifiableSet(new HashSet(Arrays.asList(picExts)));
            return picExtSet.contains(ext.toLowerCase());
        }
    }


    public static String getFileType(String fileSuffix) {
        if (Validator.isEmpty(fileSuffix)) {
            return "";
        }
        else {
            Map<String, String> suffix2Type = new HashMap();
            suffix2Type.put("doc", "word");
            suffix2Type.put("docx", "word");
            suffix2Type.put("xls", "excel");
            suffix2Type.put("xlsx", "excel");
            suffix2Type.put("ppt", "ppt");
            suffix2Type.put("pptx", "ppt");
            suffix2Type.put("pdf", "pdf");
            suffix2Type.put("txt", "txt");
            return suffix2Type.get(fileSuffix.toLowerCase());
        }
    }

    public static String getFileName(String name) {
        return !Validator.isEmpty(name) && name.indexOf(".") != -1 ? name.substring(0, name.lastIndexOf(".")) : name;
    }
}
