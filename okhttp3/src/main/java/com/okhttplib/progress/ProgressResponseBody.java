package com.okhttplib.progress;


import android.os.Message;

import com.okhttplib.bean.ProgressMessage;
import com.okhttplib.callback.ProgressCallback;
import com.okhttplib.handler.OkMainHandler;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class ProgressResponseBody extends ResponseBody{

    private final ResponseBody originalResponseBody;
    private final ProgressCallback progressCallback;
    private BufferedSource bufferedSink;

    public ProgressResponseBody(ResponseBody originalResponseBody, ProgressCallback progressCallback) {
        this.originalResponseBody = originalResponseBody;
        this.progressCallback = progressCallback;
    }

    @Override
    public long contentLength() {
        return originalResponseBody.contentLength();
    }

    @Override
    public MediaType contentType() {
        return originalResponseBody.contentType();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(source(originalResponseBody.source()));
        }
        return bufferedSink;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            long contentLength = 0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if(null != progressCallback){
                    //主线程回调
                    Message msg = new ProgressMessage(OkMainHandler.PROGRESS_CALLBACK,
                            progressCallback,
                            totalBytesRead,
                            contentLength,
                            bytesRead == -1)
                            .build();
                    OkMainHandler.getInstance().sendMessage(msg);
                }
                return bytesRead;
            }
        };
    }


}
