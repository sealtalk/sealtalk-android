package cn.rongcloud.im.server.network.http;

import android.text.TextUtils;
import android.util.Log;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import cn.rongcloud.im.server.utils.MD5;


public class BreakpointHttpResponseHandler extends AsyncHttpResponseHandler {

    private final String tag = BreakpointHttpResponseHandler.class.getSimpleName();

    /** 临时文件后缀 **/
    private static final String TEMP_SUFFIX = ".download";

    /** 请求url **/
    private String url;
    /** 临时文件，上一次保存的数据 **/
    private File tempFile;
    /** 最后目标文件 **/
    private File targetFile;
    /** 文件保存目录 **/
    private File baseDirFile;

    /** 上一次保存文件的大小 **/
    private long previousFileSize;
    /** 文件总大小 **/
    private long totalSize;
    /** 已经下载文件的大小 **/
    private long downloadSize;
    /** 是否暂停标识  **/
    private boolean interrupt = false;
    /** RandomAccessFile **/
    private RandomAccessFile randomAccessFile;

    /**
     * 构造方法
     */
    public BreakpointHttpResponseHandler(String url, String rootFile) {
        this.url = url;
        String fileName = getFileName(url);
        this.baseDirFile = new File(rootFile);
        this.targetFile = new File(rootFile, fileName);
        this.tempFile = new File(rootFile, fileName + TEMP_SUFFIX);

        if (!this.baseDirFile.exists()) {
            this.baseDirFile.mkdirs();
        }
    }

    /**
     * 根据url得到文件名
     * @param url
     * @return
     */
    public String getFileName(String url) {
        StringBuilder fileName = new StringBuilder(MD5.encrypt(url));
        if (!TextUtils.isEmpty(url)) {
            if (url.indexOf(".") > 0) {
                int index = url.lastIndexOf(".");
                fileName.append(url.substring(index, url.length()));
            }
        }
        return fileName.toString();
    }

    public void onSuccess(File file) {

    }

    public void onSuccess(int statusCode, File file) {
        onSuccess(file);
    }

    public void onSuccess(int statusCode, Header[] headers, File file) {
        onSuccess(statusCode, file);
    }

    @SuppressWarnings("deprecation")
    public void onFailure(Throwable e, File response) {
        onFailure(e);
    }

    public void onFailure(int statusCode, Throwable e, File response) {
        onFailure(e, response);
    }

    public void onFailure(int statusCode, Header[] headers, Throwable e, File response) {
        onFailure(statusCode, e, response);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        onFailure(statusCode, headers, error, getTargetFile());
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        onSuccess(statusCode, headers, getTargetFile());
    }

    @Override
    public void sendResponseMessage(HttpResponse response) {

        if (!Thread.currentThread().isInterrupted() && !interrupt) {

            Throwable error = null;
            InputStream instream = null;
            StatusLine status = response.getStatusLine();
            HttpEntity entity = response.getEntity();

            try {
                if (entity == null) {
                    throw new IOException("Fail download. entity is null.");
                }

                // 处理contentLength
                long contentLength = entity.getContentLength();
                if (contentLength == -1) {
                    contentLength = entity.getContent().available();
                }

                //如果临时文件存在，得到之前下载的大小
                if (tempFile.exists()) {
                    previousFileSize = tempFile.length();
                }

                // 得到总大小，包括之前已经下载的
                totalSize = contentLength + previousFileSize;
                if (targetFile.exists() && totalSize == targetFile.length()) {
                    Log.e(tag, "Output file already exists. Skipping download.");
                    sendSuccessMessage(status.getStatusCode(), response.getAllHeaders(), "success".getBytes());
                    return;
                }

                //获取当前下载文件流
                instream = entity.getContent();
                if (instream == null) {
                    throw new IOException("Fail download. instream is null.");
                }

                randomAccessFile = new RandomAccessFile(tempFile, "rw");
                randomAccessFile.seek(randomAccessFile.length());

                byte[] buffer = new byte[BUFFER_SIZE];
                int length, count = 0;
                while ((length = instream.read(buffer)) != -1 && !Thread.currentThread().isInterrupted() && !interrupt) {
                    count += length;
                    downloadSize = count + previousFileSize;
                    randomAccessFile.write(buffer, 0, length);
                    sendProgressMessage((int)downloadSize, (int)totalSize);
                }

                //判断下载大小与总大小不一致
                if (!Thread.currentThread().isInterrupted() && !interrupt) {
                    if (downloadSize != totalSize && totalSize != -1) {
                        throw new IOException("Fail download. totalSize not eq downloadSize.");
                    }
                }

            } catch (IllegalStateException e) {
                e.printStackTrace();
                error = e;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                error = e;
            } catch (IOException e) {
                e.printStackTrace();
                error = e;
            } finally {
                try {
                    if (instream != null) instream.close();
                    if (randomAccessFile != null) randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    error = e;
                }
            }

            // additional cancellation check as getResponseData() can take non-zero time to process
            if (!Thread.currentThread().isInterrupted() && !interrupt) {
                if (status.getStatusCode() >= 300 || error != null) {
                    sendFailureMessage(status.getStatusCode(),
                                       response.getAllHeaders(),  error.getMessage().getBytes(),
                                       new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()));
                } else {
                    tempFile.renameTo(targetFile);
                    sendSuccessMessage(status.getStatusCode(), response.getAllHeaders(), "success".getBytes());
                }
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTempFile(File tempFile) {
        this.tempFile = tempFile;
    }

    public File getTempFile() {
        return tempFile;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public long getPreviousFileSize() {
        return previousFileSize;
    }

    public void setPreviousFileSize(long previousFileSize) {
        this.previousFileSize = previousFileSize;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }
}
