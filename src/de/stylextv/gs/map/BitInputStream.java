package de.stylextv.gs.map;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream extends InputStream {
	
    private int bitbuff = 0;
    private int bitbuff_len = 0;
    private boolean closed = false;
    private final InputStream input;
    private final boolean closeInput;

    public BitInputStream(InputStream inputStream) {
        this(inputStream, true);
    }

    public BitInputStream(InputStream inputStream, boolean closeInputStream) {
        this.input = inputStream;
        this.closeInput = closeInputStream;
    }

    @Override
    public int available() throws IOException {
        if (this.closed) {
            throw new IOException("Stream is closed");
        }
        return this.input.available();
    }

    @Override
    public int read() throws IOException {
        return readBits(8);
    }

    public int readBits(int nBits) throws IOException {
        if (this.closed) {
            throw new IOException("Stream is closed");
        }
        while (this.bitbuff_len < nBits) {
            int readByte = -1;
            try {
                readByte = this.input.read();
            } catch (IOException ex) {}
            if (readByte == -1) {
                return -1;
            }
            this.bitbuff |= (readByte << this.bitbuff_len);
            this.bitbuff_len += 8;
        }
        int result = bitbuff & ((1 << nBits) - 1);
        this.bitbuff >>= nBits;
        this.bitbuff_len -= nBits;
        return result;
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            if (this.closeInput) {
                this.input.close();
            }
        }
    }
    
}