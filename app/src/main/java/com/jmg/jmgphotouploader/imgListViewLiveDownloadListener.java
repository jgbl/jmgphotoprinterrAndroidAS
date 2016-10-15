package com.jmg.jmgphotouploader;

import android.widget.ImageView;

import com.microsoft.live.LiveDownloadOperation;
import com.microsoft.live.LiveDownloadOperationListener;

/**
 * Created by hmnatalie on 15.10.16.
 */
public abstract class imgListViewLiveDownloadListener implements LiveDownloadOperationListener {
    public ImageView Image = null;
    public ImgListItem item = null;
    public LiveDownloadOperation operation = null;
    public imgListViewLiveDownloadListener(ImageView Image, ImgListItem item)
    {
        this.Image = Image;
        this.item = item;
    }
    public void cancel()
    {
        operation.cancel();
    }
}
