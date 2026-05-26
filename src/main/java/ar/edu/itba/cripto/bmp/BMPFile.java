package ar.edu.itba.cripto.bmp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BMPFile {

    private static final int HEADER_SIZE = 54;
    private static final int BITS_PER_PIXEL = 8;
    private byte[] header; //54 bytes
    private byte[] data;
    private int width;
    private int height;
    private int bitsPerPixel;

    public BMPFile(String path) throws IOException{
        byte[] file;
        try {
            file = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IOException("An error occurred while reading the BMP file: " + e.getMessage(), e);
        }

        if (file.length < HEADER_SIZE || file[0] != 'B' || file[1] != 'M') {
            throw new IOException("Invalid BMP file format.");
        }

        bitsPerPixel = ((file[29] & 0xFF) << 8) | (file[28] & 0xFF);
        if(bitsPerPixel != BITS_PER_PIXEL){
            throw new IOException("Solo se soportan BMP de 8 bits por pixel");
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

        int size = file.length - offset;
        data = new byte[size];
        System.arraycopy(file, offset, data, 0, size);
    }

    public byte[] getHeader() { return header; }

    public byte[] getData() { return data; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public int getBitsPerPixel() { return bitsPerPixel; }
}
