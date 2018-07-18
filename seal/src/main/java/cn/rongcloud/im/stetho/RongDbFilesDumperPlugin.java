package cn.rongcloud.im.stetho;

/**
 * Created by jiangecho on 2016/12/22.
 */

import android.content.Context;
import android.os.Environment;

import com.facebook.stetho.common.Util;
import com.facebook.stetho.dumpapp.ArgsHelper;
import com.facebook.stetho.dumpapp.DumpException;
import com.facebook.stetho.dumpapp.DumpUsageException;
import com.facebook.stetho.dumpapp.DumperContext;
import com.facebook.stetho.dumpapp.DumperPlugin;
import com.facebook.stetho.inspector.database.DatabaseFilesProvider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RongDbFilesDumperPlugin implements DumperPlugin {
    private static final String NAME = "rongDbFile";

    private final Context mContext;
    private final DatabaseFilesProvider mDatabaseFilesProvider;

    public RongDbFilesDumperPlugin(Context context, DatabaseFilesProvider databaseFilesProvider) {
        mContext = context;
        mDatabaseFilesProvider = databaseFilesProvider;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void dump(DumperContext dumpContext) throws DumpException {
        Iterator<String> args = dumpContext.getArgsAsList().iterator();

        String command = ArgsHelper.nextOptionalArg(args, "");
        if ("download".equals(command)) {
            doDownload(dumpContext.getStdout(), args);
        } else {
            doUsage(dumpContext.getStdout());
            if (!"".equals(command)) {
                throw new DumpUsageException("Unknown command: " + command);
            }
        }
    }

    private static File getBaseDir(Context context) {
        // getFilesDir() yields /data/data/<package>/files, we want the base package dir.
        return context.getFilesDir().getParentFile();
    }

    private void doDownload(PrintStream writer, Iterator<String> remainingArgs)
            throws DumpUsageException {
        String outputPath = ArgsHelper.nextArg(remainingArgs, "Must specify output file");

        File outFile = resolvePossibleSdcardPath(outputPath);
        if (outFile.exists()) {
            writer.println("error: out file exists");
            return;
        }

        List<File> selectedFiles = mDatabaseFilesProvider.getDatabaseFiles();
        try {
            OutputStream outputStream;
            if ("-".equals(outputPath)) {
                outputStream = writer;
            } else {
                outputStream = new FileOutputStream(resolvePossibleSdcardPath(outputPath));
            }
            ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(outputStream));
            boolean success = false;
            try {
                byte[] buf = new byte[2048];
                if (selectedFiles.size() > 0) {
                    addFiles(output, buf, selectedFiles.toArray(new File[selectedFiles.size()]));
                } else {
                    addFiles(output, buf, getBaseDir(mContext).listFiles());
                }
                success = true;
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                    Util.close(outputStream, !success);
                    if (success) {
                        throw e;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFiles(ZipOutputStream output, byte[] buf, File[] files) throws IOException {
        for (File file : files) {
            if (file.isDirectory()) {
                addFiles(output, buf, file.listFiles());
            } else {
                output.putNextEntry(
                        new ZipEntry(
                                relativizePath(
                                        getBaseDir(mContext).getParentFile(),
                                        file)));
                FileInputStream input = new FileInputStream(file);
                try {
                    copy(input, output, buf);
                } finally {
                    input.close();
                }
            }
        }
    }

    private static void copy(InputStream in, OutputStream out, byte[] buf) throws IOException {
        int n;
        while ((n = in.read(buf)) >= 0) {
            out.write(buf, 0, n);
        }
    }

    // Disclaimer: stupid implementation :)
    private static String relativizePath(File base, File path) {
        String baseStr = base.getAbsolutePath();
        String pathStr = path.getAbsolutePath();

        if (pathStr.startsWith(baseStr)) {
            return pathStr.substring(baseStr.length() + 1);
        } else {
            return pathStr;
        }
    }

    private static File resolvePossibleSdcardPath(String path) {
        if (path.startsWith("/")) {
            return new File(path);
        } else {
            return new File(Environment.getExternalStorageDirectory(), path);
        }
    }

    private void doUsage(PrintStream writer) {
        final String cmdName = "dumpapp " + NAME;

        String usagePrefix = "Usage: " + cmdName + " ";
        String blankPrefix = "       " + cmdName + " ";
        writer.println(usagePrefix + "<command>");
        writer.println(blankPrefix + "download <output.zip>");
        writer.println();
        writer.println(cmdName + " download: Fetch internal application storage");
    }
}

