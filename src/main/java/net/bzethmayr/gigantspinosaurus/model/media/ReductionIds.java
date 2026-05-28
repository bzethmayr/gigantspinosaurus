package net.bzethmayr.gigantspinosaurus.model.media;

public interface ReductionIds {
    short YCBCR_ID = 1;
    short YCBCR_VERSION = 0;
    short SPATIAL_ID = 2;
    short SPATIAL_VERSION = 0;

    int SOBEL_CELLS = 12;
    int SOBEL_OUTPUT_CELLS = 144;
    int DWT_OUTPUT_CELLS = 144;
    int REDUCED_OUTPUT_BYTES = 288;

    int DOWNSAMPLE_WIDTH = 160;
    int DOWNSAMPLE_HEIGHT = 90;
}
