package cn.rongcloud.im.ui.widget.linkpreview;

/**
 * Callback that is invoked with before and after the loading of a link preview
 *
 */
public interface LinkPreviewCallback {

    void onPre();

    /**
     *
     * @param sourceContent
     *            Class with all contents from preview.
     * @param isNull
     *            Indicates if the content is null.
     */
    void onPos(SourceContent sourceContent, boolean isNull);
}
