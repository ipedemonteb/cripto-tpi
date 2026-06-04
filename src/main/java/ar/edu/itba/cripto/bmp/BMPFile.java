package ar.edu.itba.cripto.bmp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;

public class BMPFile {

    static final int HEADER_SIZE = 54;
    private static final int BITS_PER_PIXEL = 8;

    private static final int OFFSET_FILE_SIZE = 2;
    private static final int OFFSET_SEED_LOW = 6;
    private static final int OFFSET_SEED_HIGH = 7;
    private static final int OFFSET_SHADOW_LOW = 8;
    private static final int OFFSET_SHADOW_HIGH = 9;
    private static final int OFFSET_PIXEL_DATA = 10;
    private static final int OFFSET_WIDTH = 18;
    private static final int OFFSET_HEIGHT = 22;
    private static final int OFFSET_BITS_PER_PIXEL = 28;
    private static final int OFFSET_COMPRESSION = 30;
    private static final int OFFSET_IMAGE_SIZE = 34;

    private byte[] header;
    private byte[] palette;
    private byte[] data;
    private int width;
    private int height;
    private int bitsPerPixel;
    private Path path;

    private BMPFile() {
        this.path = null;
    }

    public BMPFile(String filePath) throws IOException {
        this.path = Paths.get(filePath).toAbsolutePath();
        byte[] file;
        try {
            file = Files.readAllBytes(this.path);
        } catch (IOException e) {
            throw new IOException("An error occurred while reading the BMP file: " + e.getMessage(), e);
        }

        if (file.length < HEADER_SIZE || file[0] != 'B' || file[1] != 'M') {
            throw new IOException("Invalid BMP file format.");
        }

        bitsPerPixel = ((file[OFFSET_BITS_PER_PIXEL + 1] & 0xFF) << 8) | (file[OFFSET_BITS_PER_PIXEL] & 0xFF);
        if (bitsPerPixel != BITS_PER_PIXEL) {
            throw new IOException("Only 8 bits per pixel are supported.");
        }

        width = ((file[OFFSET_WIDTH + 3] & 0xFF) << 24) | ((file[OFFSET_WIDTH + 2] & 0xFF) << 16) |
                ((file[OFFSET_WIDTH + 1] & 0xFF) << 8) | (file[OFFSET_WIDTH] & 0xFF);
        height = ((file[OFFSET_HEIGHT + 3] & 0xFF) << 24) | ((file[OFFSET_HEIGHT + 2] & 0xFF) << 16) |
                ((file[OFFSET_HEIGHT + 1] & 0xFF) << 8) | (file[OFFSET_HEIGHT] & 0xFF);

        int isCompressed = ((file[OFFSET_COMPRESSION + 3] & 0xFF) << 24) | ((file[OFFSET_COMPRESSION + 2] & 0xFF) << 16) |
                ((file[OFFSET_COMPRESSION + 1] & 0xFF) << 8) | (file[OFFSET_COMPRESSION] & 0xFF);
        if (isCompressed != 0) {
            throw new IOException("Compressed BMP files are not supported");
        }

        header = new byte[HEADER_SIZE];
        System.arraycopy(file, 0, header, 0, HEADER_SIZE);

        int offset = ((file[OFFSET_PIXEL_DATA + 3] & 0xFF) << 24) | ((file[OFFSET_PIXEL_DATA + 2] & 0xFF) << 16) |
                ((file[OFFSET_PIXEL_DATA + 1] & 0xFF) << 8) | (file[OFFSET_PIXEL_DATA] & 0xFF);

        int paletteSize = offset - HEADER_SIZE;
        palette = new byte[paletteSize];
        System.arraycopy(file, HEADER_SIZE, palette, 0, paletteSize);

        int rowSize = ((width + 3) / 4) * 4;
        data = new byte[width * height];
        for (int r = 0; r < height; r++) {
            System.arraycopy(file, offset + r * rowSize, data, r * width, width);
        }
    }

    /**
     * Creates an output BMP using template's header/palette, with new dimensions and pixel data.
     * Reserved bytes 6-9 (seed and shadow number) are cleared.
     */
    public static BMPFile createOutput(BMPFile template, int outWidth, int outHeight, byte[] pixelData) {
        BMPFile out = new BMPFile();
        out.header = Arrays.copyOf(template.header, HEADER_SIZE);
        out.palette = Arrays.copyOf(template.palette, template.palette.length);
        out.bitsPerPixel = template.bitsPerPixel;
        out.width = outWidth;
        out.height = outHeight;
        out.data = pixelData;

        // Width
        out.header[OFFSET_WIDTH] = (byte)(outWidth & 0xFF);
        out.header[OFFSET_WIDTH + 1] = (byte)((outWidth >> 8) & 0xFF);
        out.header[OFFSET_WIDTH + 2] = (byte)((outWidth >> 16) & 0xFF);
        out.header[OFFSET_WIDTH + 3] = (byte)((outWidth >> 24) & 0xFF);
        // Height
        out.header[OFFSET_HEIGHT] = (byte)(outHeight & 0xFF);
        out.header[OFFSET_HEIGHT + 1] = (byte)((outHeight >> 8) & 0xFF);
        out.header[OFFSET_HEIGHT + 2] = (byte)((outHeight >> 16) & 0xFF);
        out.header[OFFSET_HEIGHT + 3] = (byte)((outHeight >> 24) & 0xFF);
        // File size
        int rowSize = ((outWidth + 3) / 4) * 4;
        int outPixelDataSize = rowSize * outHeight;
        int fileSize = HEADER_SIZE + out.palette.length + outPixelDataSize;
        out.header[OFFSET_FILE_SIZE] = (byte)(fileSize & 0xFF);
        out.header[OFFSET_FILE_SIZE + 1] = (byte)((fileSize >> 8) & 0xFF);
        out.header[OFFSET_FILE_SIZE + 2] = (byte)((fileSize >> 16) & 0xFF);
        out.header[OFFSET_FILE_SIZE + 3] = (byte)((fileSize >> 24) & 0xFF);
        // Image data size
        out.header[OFFSET_IMAGE_SIZE] = (byte)(outPixelDataSize & 0xFF);
        out.header[OFFSET_IMAGE_SIZE + 1] = (byte)((outPixelDataSize >> 8) & 0xFF);
        out.header[OFFSET_IMAGE_SIZE + 2] = (byte)((outPixelDataSize >> 16) & 0xFF);
        out.header[OFFSET_IMAGE_SIZE + 3] = (byte)((outPixelDataSize >> 24) & 0xFF);
        // Clear reserved bytes (seed and shadow number)
        out.header[OFFSET_SEED_LOW] = 0; out.header[OFFSET_SEED_HIGH] = 0;
        out.header[OFFSET_SHADOW_LOW] = 0; out.header[OFFSET_SHADOW_HIGH] = 0;

        return out;
    }

    public Path getPath() { return path; }
    public byte[] getHeader() { return header; }
    public byte[] getData() { return data; }
    public byte[] getPalette() { return palette; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getBitsPerPixel() { return bitsPerPixel; }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int[] getDataAsIntArray() {
        int[] result = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i] & 0xFF;
        }
        return result;
    }

    public int getSeed() {
        return ((header[OFFSET_SEED_HIGH] & 0xFF) << 8) | (header[OFFSET_SEED_LOW] & 0xFF);
    }

    public void setSeed(int seed) {
        header[OFFSET_SEED_LOW] = (byte)(seed & 0xFF);
        header[OFFSET_SEED_HIGH] = (byte)((seed >> 8) & 0xFF);
    }

    public int getShadowNumber() {
        return ((header[OFFSET_SHADOW_HIGH] & 0xFF) << 8) | (header[OFFSET_SHADOW_LOW] & 0xFF);
    }

    public void setShadowNumber(int shadowNumber) {
        header[OFFSET_SHADOW_LOW] = (byte)(shadowNumber & 0xFF);
        header[OFFSET_SHADOW_HIGH] = (byte)((shadowNumber >> 8) & 0xFF);
    }

    public void save(Path outputPath) throws IOException {
        int rowSize = ((width + 3) / 4) * 4;
        int pixelDataSize = rowSize * height;
        byte[] output = new byte[HEADER_SIZE + palette.length + pixelDataSize];
        System.arraycopy(header, 0, output, 0, HEADER_SIZE);
        System.arraycopy(palette, 0, output, HEADER_SIZE, palette.length);
        int offset = HEADER_SIZE + palette.length;
        for (int r = 0; r < height; r++) {
            System.arraycopy(data, r * width, output, offset + r * rowSize, width);
        }
        Files.write(outputPath, output);
    }

    public void save() throws IOException {
        save(path);
    }
}
