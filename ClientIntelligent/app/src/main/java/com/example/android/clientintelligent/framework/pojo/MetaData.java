package com.example.android.clientintelligent.framework.pojo;

public class MetaData {
    private final int nImageSizeX;
    private final int nImageSizeY;
    private final int nBytesPerChannel;
    private final int nChannelsPerPixel;

    public MetaData(int nImageSizeX, int nImageSizeY, int nBytesPerChannel, int nChannelsPerPixel) {
        this.nImageSizeX = nImageSizeX;
        this.nImageSizeY = nImageSizeY;
        this.nBytesPerChannel = nBytesPerChannel;
        this.nChannelsPerPixel = nChannelsPerPixel;
    }

    public MetaData(Data data) {
        this.nImageSizeX = data.getImageSizeX();
        this.nImageSizeY = data.getImageSizeY();
        this.nBytesPerChannel = data.getBytesPerChannel();
        this.nChannelsPerPixel = data.getChannelsPerPixel();
    }

    public int getImageSizeX() {
        return nImageSizeX;
    }

    public int getImageSizeY() {
        return nImageSizeY;
    }

    public int getBytesPerChannel() {
        return nBytesPerChannel;
    }

    public int getChannelsPerPixel() {
        return nChannelsPerPixel;
    }
}
