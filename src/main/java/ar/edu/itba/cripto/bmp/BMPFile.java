package ar.edu.itba.cripto.bmp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class BMPFile {

    private static final int HEADER_SIZE = 54;
    private static final int BITS_PER_PIXEL = 8;
    private byte[] header; // 54 bytes
    private byte[] palette; // color table between header and pixel data (may be empty)
    private byte[] data;   // pixel data starting at the BMP pixel offset
    private int width;
    private int height;
    private int bitsPerPixel;
    private final Path path;

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
        if(bitsPerPixel != BITS_PER_PIXEL){
            throw new IOException("Only 8 bits per pixel are supported.");
        }

        width = ((file[21] & 0xFF) << 24) | ((file[20] & 0xFF) << 16) |
                ((file[19] & 0xFF) << 8) | (file[18] & 0xFF);
        height = ((file[25] & 0xFF) << 24) | ((file[24] & 0xFF) << 16) |
                ((file[23] & 0xFF) << 8) | (file[22] & 0xFF);

        //@TODO: Check if we should accept compressed files
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

        int size = file.length - offset;
        data = new byte[size];
        System.arraycopy(file, offset, data, 0, size);
    }

    public Path getPath() { return path; }

    public byte[] getHeader() { return header; }

    public byte[] getData() { return data; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public int getBitsPerPixel() { return bitsPerPixel; }

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
        header[6] = (byte) (seed & 0xFF);
        header[7] = (byte) ((seed >> 8) & 0xFF);
    }

    public int getShadowNumber() {
        return ((header[9] & 0xFF) << 8) | (header[8] & 0xFF);
    }

    public void setShadowNumber(int shadowNumber) {
        header[8] = (byte) (shadowNumber & 0xFF);
        header[9] = (byte) ((shadowNumber >> 8) & 0xFF);
    }

    //TODO: Check if having an outhpath is ok
    public void save(Path outputPath) throws IOException {
        byte[] output = new byte[HEADER_SIZE + palette.length + data.length];
        System.arraycopy(header, 0, output, 0, HEADER_SIZE);
        System.arraycopy(palette, 0, output, HEADER_SIZE, palette.length);
        System.arraycopy(data, 0, output, HEADER_SIZE + palette.length, data.length);
        Files.write(outputPath, output);
    }

    public void save() throws IOException {
        save(path);
    }
}
