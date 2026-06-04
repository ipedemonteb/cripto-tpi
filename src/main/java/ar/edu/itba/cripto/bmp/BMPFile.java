package ar.edu.itba.cripto.bmp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;

public class BMPFile {

    static final int HEADER_SIZE = 54;
    private static final int BITS_PER_PIXEL = 8;

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

        bitsPerPixel = ((file[29] & 0xFF) << 8) | (file[28] & 0xFF);
        if (bitsPerPixel != BITS_PER_PIXEL) {
            throw new IOException("Only 8 bits per pixel are supported.");
        }

        width = ((file[21] & 0xFF) << 24) | ((file[20] & 0xFF) << 16) |
                ((file[19] & 0xFF) << 8) | (file[18] & 0xFF);
        height = ((file[25] & 0xFF) << 24) | ((file[24] & 0xFF) << 16) |
                ((file[23] & 0xFF) << 8) | (file[22] & 0xFF);

        int isCompressed = ((file[33] & 0xFF) << 24) | ((file[32] & 0xFF) << 16) |
                ((file[31] & 0xFF) << 8) | (file[30] & 0xFF);
        if (isCompressed != 0) {
            throw new IOException("Compressed BMP files are not supported");
        }

        header = new byte[HEADER_SIZE];
        System.arraycopy(file, 0, header, 0, HEADER_SIZE);

        int offset = ((file[13] & 0xFF) << 24) | ((file[12] & 0xFF) << 16) |
                ((file[11] & 0xFF) << 8) | (file[10] & 0xFF);

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
        out.header[18] = (byte)(outWidth & 0xFF);
        out.header[19] = (byte)((outWidth >> 8) & 0xFF);
        out.header[20] = (byte)((outWidth >> 16) & 0xFF);
        out.header[21] = (byte)((outWidth >> 24) & 0xFF);
        // Height
        out.header[22] = (byte)(outHeight & 0xFF);
        out.header[23] = (byte)((outHeight >> 8) & 0xFF);
        out.header[24] = (byte)((outHeight >> 16) & 0xFF);
        out.header[25] = (byte)((outHeight >> 24) & 0xFF);
        // File size
        int rowSize = ((outWidth + 3) / 4) * 4;
        int outPixelDataSize = rowSize * outHeight;
        int fileSize = HEADER_SIZE + out.palette.length + outPixelDataSize;
        out.header[2] = (byte)(fileSize & 0xFF);
        out.header[3] = (byte)((fileSize >> 8) & 0xFF);
        out.header[4] = (byte)((fileSize >> 16) & 0xFF);
        out.header[5] = (byte)((fileSize >> 24) & 0xFF);
        // Image data size
        out.header[34] = (byte)(outPixelDataSize & 0xFF);
        out.header[35] = (byte)((outPixelDataSize >> 8) & 0xFF);
        out.header[36] = (byte)((outPixelDataSize >> 16) & 0xFF);
        out.header[37] = (byte)((outPixelDataSize >> 24) & 0xFF);
        // Clear reserved bytes (seed and shadow number)
        out.header[6] = 0; out.header[7] = 0;
        out.header[8] = 0; out.header[9] = 0;

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
        return ((header[7] & 0xFF) << 8) | (header[6] & 0xFF);
    }

    public void setSeed(int seed) {
        header[6] = (byte)(seed & 0xFF);
        header[7] = (byte)((seed >> 8) & 0xFF);
    }

    public int getShadowNumber() {
        return ((header[9] & 0xFF) << 8) | (header[8] & 0xFF);
    }

    public void setShadowNumber(int shadowNumber) {
        header[8] = (byte)(shadowNumber & 0xFF);
        header[9] = (byte)((shadowNumber >> 8) & 0xFF);
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
