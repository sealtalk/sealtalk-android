package io.rong.recognizer.speechtotext;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 解码 amr 格式音频文件的异步任务
 */
public class DecodeAmrTask extends AsyncTask<String, Void, byte[]> {

    private MediaExtractor extractor = new MediaExtractor();
    private MediaCodecWrapper codecWrapper;
    private DecodeCallback callback;

    public DecodeAmrTask(@NonNull DecodeCallback callback) {
        super();

        this.callback = callback;
    }

    @Override
    protected byte[] doInBackground(String... paths) {
        String path = paths[0];

        try {
            // BEGIN_INCLUDE(initialize_extractor)
            extractor.setDataSource(path);
            int nTracks = extractor.getTrackCount();

            // Begin by unselecting all of the tracks in the extractor, so we won't see
            // any tracks that we haven't explicitly selected.
            for (int i = 0; i < nTracks; ++i) {
                extractor.unselectTrack(i);
            }

            // Find the first audio track in the stream. In a real-world application
            // it's possible that the stream would contain multiple tracks, but this
            // sample assumes that we just want to startPlay the first one.
            for (int i = 0; i < nTracks; ++i) {
                // Try to create a video codec for this track. This call will return null if the
                // track is not a video track, or not a recognized video format. Once it returns
                // a valid MediaCodecWrapper, we can break out of the loop.
                codecWrapper = MediaCodecWrapper.fromAudioFormat(extractor.getTrackFormat(i));
                if (codecWrapper != null) {
                    extractor.selectTrack(i);
                    break;
                }
            }
            // END_INCLUDE(initialize_extractor)

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            while (!isCancelled()) {
                boolean isEos = ((extractor.getSampleFlags() & MediaCodec
                        .BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                // BEGIN_INCLUDE(write_sample)
                if (!isEos) {
                    // Try to submit the sample to the codec and if successful advance the
                    // extractor to the next available sample to read.
                    boolean result = codecWrapper.writeSample(extractor, false,
                            extractor.getSampleTime(), extractor.getSampleFlags());

                    if (result) {
                        // Advancing the extractor is a blocking operation and it MUST be
                        // executed outside the main thread in real applications.
                        extractor.advance();
                    }
                }
                // END_INCLUDE(write_sample)

                // Examine the sample at the head of the queue to see if its ready to be
                // rendered and is not zero sized End-of-Stream record.
                MediaCodec.BufferInfo out_bufferInfo = new MediaCodec.BufferInfo();
                codecWrapper.peekSample(out_bufferInfo);

                // BEGIN_INCLUDE(pop_sample)
                if (out_bufferInfo.size <= 0 && isEos) {
                    codecWrapper.stopAndRelease();
                    extractor.release();

                    return outputStream.toByteArray();
                } else {
                    // Pop the sample off the queue and use it
                    byte[] popSample = codecWrapper.popSample();
                    if (popSample != null) {
                        outputStream.write(popSample);
                    }
                }
                // END_INCLUDE(pop_sample)
            }
        } catch (IOException e) {
            e.printStackTrace();

            release();
        }

        return null;
    }

    private void release() {
        if (codecWrapper != null) {
            codecWrapper.stopAndRelease();
        }
        extractor.release();
    }

    @Override
    protected void onPostExecute(byte[] decodedData) {
        callback.onCallback(decodedData);
    }

    @Override
    protected void onCancelled() {
        release();

        callback.onCallback(null);
    }

}
