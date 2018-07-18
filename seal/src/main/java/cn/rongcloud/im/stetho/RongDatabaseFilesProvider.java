package cn.rongcloud.im.stetho;

import android.content.Context;

import com.facebook.stetho.inspector.database.DatabaseFilesProvider;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangecho on 2016/11/23.
 */

public class RongDatabaseFilesProvider implements DatabaseFilesProvider {
    private Context context;

    public RongDatabaseFilesProvider(Context context) {
        this.context = context;
    }

    private static FilenameFilter rongDbFilenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            if (filename.equals("IMKitUserInfoCache")
                    || filename.equals("SealUserInfo")
                    || filename.equals("storage")
                    || filename.endsWith(".db")) {
                return true;
            }
            return false;
        }
    };

    @Override
    public List<File> getDatabaseFiles() {
        List<File> dbFiles = new ArrayList<>();
        File dir = context.getFilesDir();
        dbFiles.addAll(listFiles(dir, rongDbFilenameFilter));

        List<File> databaseFiles = new ArrayList<>();
        for (String databaseName : context.databaseList()) {
            databaseFiles.add(context.getDatabasePath(databaseName));
        }
        dbFiles.addAll(databaseFiles);
        return dbFiles;
    }

    private List<File> listFiles(File dir, FilenameFilter fileNameFilter) {
        if (dir == null || dir.isFile()) {
            return null;
        }
        List<File> fileList = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && fileNameFilter.accept(dir, file.getName())) {
                    fileList.add(file);
                } else {
                    List<File> tmp = listFiles(file, fileNameFilter);
                    if (tmp != null && tmp.size() > 0) {
                        fileList.addAll(tmp);
                    }
                }
            }
        }
        return fileList;
    }
}
