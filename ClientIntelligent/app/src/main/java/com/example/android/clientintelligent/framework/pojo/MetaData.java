package com.example.android.clientintelligent.framework.pojo;

public class MetaData {
    private final int nImageSizeX;
    private final int nImageSizeY;
    private final int nBytesPerChannel;
    private final int nChannelsPerPixel;
    private final int nOutputSize;

    public MetaData(int nImageSizeX, int nImageSizeY, int nBytesPerChannel, int nChannelsPerPixel, int nOutputSize) {
        this.nImageSizeX = nImageSizeX;
        this.nImageSizeY = nImageSizeY;
        this.nBytesPerChannel = nBytesPerChannel;
        this.nChannelsPerPixel = nChannelsPerPixel;
        this.nOutputSize = nOutputSize;
    }

    public MetaData(DataSet dataSet) {
        this.nImageSizeX = dataSet.getImageSizeX();
        this.nImageSizeY = dataSet.getImageSizeY();
        this.nBytesPerChannel = dataSet.getBytesPerChannel();
        this.nChannelsPerPixel = dataSet.getChannelsPerPixel();
        this.nOutputSize = dataSet.getOutputSize();
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

    public int getOutputSize() {
        return nOutputSize;
    }
}
