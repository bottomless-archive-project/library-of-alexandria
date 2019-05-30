package com.github.loa.indexer.service.index.base64;

public class ByteRingBuffer {

    private final byte[] buffer;
    private final int capacity;

    private int available;
    private int idxGet;
    private int idxPut;

    public ByteRingBuffer(int capacity) {
        this.capacity = capacity;
        buffer = new byte[this.capacity];
    }

    /**
     * Gets as many of the requested bytes as available from this buffer.
     *
     * @return number of bytes actually got from this buffer (0 if no bytes are available)
     */
    public int get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    /**
     * Gets as many of the requested bytes as available from this buffer.
     *
     * @return number of bytes actually got from this buffer (0 if no bytes are available)
     */
    public int get(byte[] dst, int off, int len) {
        if (available == 0) {
            return 0;
        }

        // limit is last index to read + 1
        int limit = idxGet < idxPut ? idxPut : capacity;
        int count = Math.min(limit - idxGet, len);
        System.arraycopy(buffer, idxGet, dst, off, count);
        idxGet += count;

        if (idxGet == capacity) {
            // Array end reached, check if we have more
            int count2 = Math.min(len - count, idxPut);
            if (count2 > 0) {
                System.arraycopy(buffer, 0, dst, off + count, count2);
                idxGet = count2;
                count += count2;
            } else {
                idxGet = 0;
            }
        }
        available -= count;
        return count;
    }

    /**
     * Puts as many of the given bytes as possible into this buffer.
     *
     * @return number of bytes actually put into this buffer (0 if the buffer is full)
     */
    public int put(byte[] src) {
        return put(src, 0, src.length);
    }

    /**
     * Puts as many of the given bytes as possible into this buffer.
     *
     * @return number of bytes actually put into this buffer (0 if the buffer is full)
     */
    public int put(byte[] src, int off, int len) {
        if (available == capacity) {
            return 0;
        }

        // limit is last index to put + 1
        int limit = idxPut < idxGet ? idxGet : capacity;
        int count = Math.min(limit - idxPut, len);
        System.arraycopy(src, off, buffer, idxPut, count);
        idxPut += count;

        if (idxPut == capacity) {
            // Array end reached, check if we have more
            int count2 = Math.min(len - count, idxGet);
            if (count2 > 0) {
                System.arraycopy(src, off + count, buffer, 0, count2);
                idxPut = count2;
                count += count2;
            } else {
                idxPut = 0;
            }
        }
        available += count;
        return count;
    }

    /**
     * Returns the number of bytes available and can be get without additional puts.
     */
    public int available() {
        return available;
    }
}