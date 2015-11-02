package com.bullhorn.dataloader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FileUtil {

    private static Log log = LogFactory.getLog(FileUtil.class);

    public void deleteFile(String fileName) {
        // A File object to represent the filename
        File f = new File(fileName);

        // Make sure the file or directory exists and isn't write protected
        if (!f.exists())
            throw new IllegalArgumentException("Delete: no such file or directory: " + fileName);

        if (!f.canWrite())
            throw new IllegalArgumentException("Delete: write protected: " + fileName);

        // If it is a directory, make sure it is empty
        if (f.isDirectory()) {
            String[] files = f.list();
            if (files.length > 0)
                throw new IllegalArgumentException("Delete: directory not empty: " + fileName);
        }

        // Attempt to delete it
        boolean success = f.delete();

        if (!success)
            throw new IllegalArgumentException("Delete: deletion failed");
    }

    // assumes all files are in the same directory (can implement recursion if required)
    public List<File> getAllFiles(String fileDir) {
        List<File> list = new ArrayList<File>();
        File[] files = new File(fileDir).listFiles();
        for (int j = 0; j < files.length; j++) {
            list.add(files[j]);
        }
        return list;
    }


    public InputStream getPropertiesInputStream(String fileName) throws Exception {

        String os = System.getProperty("os.name").toLowerCase();
        String path = "/usr/local/bullhorn/conf/";
        if (os.contains("windows")) {
            path = "C:\\bullhorn\\conf\\";
        }
        InputStream inputStream = new FileInputStream(path + fileName);

        return inputStream;
    }

    public Properties getProps(String fileName) throws Exception {
        InputStream inputStream = getPropertiesInputStream(fileName);
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        return properties;
    }

    public void saveProps(String fileName, Properties properties) throws Exception {
        FileOutputStream fos = new FileOutputStream(fileName);
        properties.store(fos, "");
        fos.close();
    }

    public boolean isFileDone(String filePath) {
        boolean done = false;
        File ff = new File(filePath);
        if (ff.exists()) {
            // try for 10 minutes
            for (int timeout = 0; timeout < 10; timeout++) {
                RandomAccessFile ran = null;
                try {
                    ran = new RandomAccessFile(ff, "rw");
                    done = true;
                    break; // no errors, done waiting
                } catch (Exception ex) {
                } finally {
                    if (ran != null) try {
                        ran.close();
                    } catch (IOException ex) {
                    }
                    ran = null;
                }
                try {
                    Thread.sleep(60000); // wait a minute then try again
                } catch (InterruptedException ex) {
                }
            }
        } else {
            log.info("File does not exist: " + filePath);
            //TODO: update db
        }

        return done;
    }

    public boolean copyFile(String filePath, String destination) {

        boolean success = true;
        File source = new File(filePath);
        File target = new File(destination);

        try {
            FileUtils.copyFile(source, target);
        } catch (Exception e) {
            log.error(e);
            success = false;
        }

        return success;
    }
}
