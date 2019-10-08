package com.kit.filetransmitter.entity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.Serializable;

/**
 * @author Kit
 * @date: 2019/10/3 22:39
 */
public class StorePath implements Serializable{

    private static final long serialVersionUID = 1L;

    private String filePath;
    private String openPath;

    public static final String LOCATION = "setting";

//    public static final String DEFAULT_FILE_PATH = "file";
//    public static final String DEFAULT_HISTORY_PATH = "message";
//    public static final String DEFAULT_OPEN_PATH = FileUtils.getUserDirectoryPath() + "\\Desktop";

    public StorePath() {
        filePath = "file";
        openPath = FileUtils.getUserDirectoryPath() + "\\Desktop";
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

//    public String getHistoryPath() {
//        return historyPath;
//    }
//
//    public void setHistoryPath(String historyPath) {
//        this.historyPath = historyPath;
//    }

    public String getOpenPath() {
        return openPath;
    }

    public void setOpenPath(String openPath) {
        this.openPath = openPath;
    }

    public void createPathIfNotExist() {
        File fP = new File(filePath);
//        File hP = new File(historyPath);
        File oP = new File(openPath);
        if (!fP.exists()) fP.mkdir();
//        if (!hP.exists()) hP.mkdir();
        if (!oP.exists()) oP.mkdir();
    }

    public boolean isValid() {
        return filePath != null && openPath != null;
    }

}
