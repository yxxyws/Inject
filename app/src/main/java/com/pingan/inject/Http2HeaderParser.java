package com.pingan.inject;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by yangyun980 on 17/4/17.
 */

public class Http2HeaderParser {
    private static final int PREFIX_4_BITS = 0x0f;
    private static final int PREFIX_5_BITS = 0x1f;
    private static final int PREFIX_6_BITS = 0x3f;
    private static final int PREFIX_7_BITS = 0x7f;

    static final byte TYPE_DATA = 0x0;
    static final byte TYPE_HEADERS = 0x1;
    static final byte TYPE_PRIORITY = 0x2;
    static final byte TYPE_RST_STREAM = 0x3;
    static final byte TYPE_SETTINGS = 0x4;
    static final byte TYPE_PUSH_PROMISE = 0x5;
    static final byte TYPE_PING = 0x6;
    static final byte TYPE_GOAWAY = 0x7;
    static final byte TYPE_WINDOW_UPDATE = 0x8;
    static final byte TYPE_CONTINUATION = 0x9;

    static final byte FLAG_NONE = 0x0;
    static final byte FLAG_ACK = 0x1; // Used for settings and ping.
    static final byte FLAG_END_STREAM = 0x1; // Used for headers and data.
    static final byte FLAG_END_HEADERS = 0x4; // Used for headers and continuation.
    static final byte FLAG_END_PUSH_PROMISE = 0x4;
    static final byte FLAG_PADDED = 0x8; // Used for headers and data.
    static final byte FLAG_PRIORITY = 0x20; // Used for headers.
    static final byte FLAG_COMPRESSED = 0x20; // Used for data.

    /**
     * The initial max frame size, applied independently writing to, or reading from the peer.
     */
    static final int INITIAL_MAX_FRAME_SIZE = 0x4000; // 16384

    private static final Header[] STATIC_HEADER_TABLE = new Header[]{
            new Header(Header.TARGET_AUTHORITY, ""),
            new Header(Header.TARGET_METHOD, "GET"),
            new Header(Header.TARGET_METHOD, "POST"),
            new Header(Header.TARGET_PATH, "/"),
            new Header(Header.TARGET_PATH, "/index.html"),
            new Header(Header.TARGET_SCHEME, "http"),
            new Header(Header.TARGET_SCHEME, "https"),
            new Header(Header.RESPONSE_STATUS, "200"),
            new Header(Header.RESPONSE_STATUS, "204"),
            new Header(Header.RESPONSE_STATUS, "206"),
            new Header(Header.RESPONSE_STATUS, "304"),
            new Header(Header.RESPONSE_STATUS, "400"),
            new Header(Header.RESPONSE_STATUS, "404"),
            new Header(Header.RESPONSE_STATUS, "500"),
            new Header("accept-charset", ""),
            new Header("accept-encoding", "gzip, deflate"),
            new Header("accept-language", ""),
            new Header("accept-ranges", ""),
            new Header("accept", ""),
            new Header("access-control-allow-origin", ""),
            new Header("age", ""),
            new Header("allow", ""),
            new Header("authorization", ""),
            new Header("cache-control", ""),
            new Header("content-disposition", ""),
            new Header("content-encoding", ""),
            new Header("content-language", ""),
            new Header("content-length", ""),
            new Header("content-location", ""),
            new Header("content-range", ""),
            new Header("content-type", ""),
            new Header("cookie", ""),
            new Header("date", ""),
            new Header("etag", ""),
            new Header("expect", ""),
            new Header("expires", ""),
            new Header("from", ""),
            new Header("host", ""),
            new Header("if-match", ""),
            new Header("if-modified-since", ""),
            new Header("if-none-match", ""),
            new Header("if-range", ""),
            new Header("if-unmodified-since", ""),
            new Header("last-modified", ""),
            new Header("link", ""),
            new Header("location", ""),
            new Header("max-forwards", ""),
            new Header("proxy-authenticate", ""),
            new Header("proxy-authorization", ""),
            new Header("range", ""),
            new Header("referer", ""),
            new Header("refresh", ""),
            new Header("retry-after", ""),
            new Header("server", ""),
            new Header("set-cookie", ""),
            new Header("strict-transport-security", ""),
            new Header("transfer-encoding", ""),
            new Header("user-agent", ""),
            new Header("vary", ""),
            new Header("via", ""),
            new Header("www-authenticate", "")
    };

    public List<Header> parse(byte[] b, int off, int len) {
        headerList.clear();
        Source source = new Source();
        source.pos = off;
        source.data = b;
        source.end = off + len;
        while (nextFrame(source)) {
            source.end = off + len;
        }
        return new ArrayList<Header>(headerList);
    }

    public boolean nextFrame(Source source) {
        try {
            source.require(9); // Frame header size
        } catch (IOException e) {
            return false; // This might be a normal socket close.
        }

        try {
            /*  0                   1                   2                   3
           *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
           * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
           * |                 Length (24)                   |
           * +---------------+---------------+---------------+
           * |   Type (8)    |   Flags (8)   |
           * +-+-+-----------+---------------+-------------------------------+
           * |R|                 Stream Identifier (31)                      |
           * +=+=============================================================+
           * |                   Frame Payload (0...)                      ...
           * +---------------------------------------------------------------+
           */
            int length = readMedium(source);
            byte type = (byte) (source.readByte() & 0xff);
            byte flags = (byte) (source.readByte() & 0xff);
            int streamId = (source.readInt() & 0x7fffffff); // Ignore reserved bit.
            long threadId = Thread.currentThread().getId();

            source.end = source.pos + length;
            switch (type) {
                //case TYPE_DATA:
                //readData(handler, length, flags, streamId);
                //    break;

                case TYPE_HEADERS:
                    readHeaders(source, length, flags, streamId);
                    break;
                case TYPE_SETTINGS:
                    readSettings(source, length, flags, streamId);
                    break;

                default:
                    //其它的不想解析了
                    source.skip(length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void readHeaders(Source source, int length, byte flags, int streamId) throws IOException {
        if (streamId == 0)
            return;
        boolean endStream = (flags & FLAG_END_STREAM) != 0;

        short padding = (flags & FLAG_PADDED) != 0 ? (short) (source.readByte() & 0xff) : 0;

        if ((flags & FLAG_PRIORITY) != 0) {
            readPriority(source, streamId);
            length -= 5; // account for above read.
        }
        length = lengthWithoutPadding(length, flags, padding);

        readHeaderBlock(source, length, padding, flags, streamId);
    }

    private void readHeaderBlock(Source source, int length, short padding, byte flags, int streamId) throws IOException {
//        continuation.length = continuation.left = length;
//        continuation.padding = padding;
//        continuation.flags = flags;
//        continuation.streamId = streamId;

        // TODO: Concat multi-value headers with 0x0, except COOKIE, which uses 0x3B, 0x20.
        // http://tools.ietf.org/html/draft-ietf-httpbis-http2-17#section-8.1.2.5
        source.end = source.end - padding;
        readHeaders(source);
    }


    void readHeaders(Source source) throws IOException {
        while (!source.exhausted()) {
            int b = source.readByte() & 0xff;
            if (b == 0x80) { // 10000000
                throw new IOException("index == 0");
            } else if ((b & 0x80) == 0x80) { // 1NNNNNNN
                int index = readInt(source, b, PREFIX_7_BITS);
                readIndexedHeader(index - 1);
            } else if (b == 0x40) { // 01000000
                readLiteralHeaderWithIncrementalIndexingNewName(source);
            } else if ((b & 0x40) == 0x40) {  // 01NNNNNN
                int index = readInt(source, b, PREFIX_6_BITS);
                readLiteralHeaderWithIncrementalIndexingIndexedName(source, index - 1);
            } else if ((b & 0x20) == 0x20) {  // 001NNNNN
                maxDynamicTableByteCount = readInt(source, b, PREFIX_5_BITS);
                if (maxDynamicTableByteCount < 0
                        || maxDynamicTableByteCount > headerTableSizeSetting) {
                    throw new IOException("Invalid dynamic table size update " + maxDynamicTableByteCount);
                }
                adjustDynamicTableByteCount();
            } else if (b == 0x10 || b == 0) { // 000?0000 - Ignore never indexed bit.
                readLiteralHeaderWithoutIndexingNewName(source);
            } else { // 000?NNNN - Ignore never indexed bit.
                int index = readInt(source, b, PREFIX_4_BITS);
                readLiteralHeaderWithoutIndexingIndexedName(source, index - 1);
            }
        }
    }

    private void readLiteralHeaderWithoutIndexingNewName(Source source) throws IOException {
        ByteString name = checkLowercase(readByteString(source));
        ByteString value = readByteString(source);
        headerList.add(new Header(name, value));
    }

    private void readLiteralHeaderWithoutIndexingIndexedName(Source source, int index) throws IOException {
        ByteString name = getName(index);
        ByteString value = readByteString(source);
        headerList.add(new Header(name, value));
    }

    int readInt(Source source, int firstByte, int prefixMask) throws IOException {
        int prefix = firstByte & prefixMask;
        if (prefix < prefixMask) {
            return prefix; // This was a single byte value.
        }

        // This is a multibyte value. Read 7 bits at a time.
        int result = prefixMask;
        int shift = 0;
        while (true) {
            int b = readByte(source);
            if ((b & 0x80) != 0) { // Equivalent to (b >= 128) since b is in [0..255].
                result += (b & 0x7f) << shift;
                shift += 7;
            } else {
                result += b << shift; // Last byte.
                break;
            }
        }
        return result;
    }

    private int readByte(Source source) throws IOException {
        return source.readByte() & 0xff;
    }

    private void readSettings(Source source, int length, byte flags, int streamId)
            throws IOException {
        if (streamId != 0) throw ioException("TYPE_SETTINGS streamId != 0");
        if ((flags & FLAG_ACK) != 0) {
            if (length != 0) throw ioException("FRAME_SIZE_ERROR ack frame should be empty!");
            //handler.ackSettings();
            return;
        }

        if (length % 6 != 0) throw ioException("TYPE_SETTINGS length %% 6 != 0: %s", length);
        Settings settings = new Settings();
        for (int i = 0; i < length; i += 6) {
            short id = source.readShort();
            int value = source.readInt();

            switch (id) {
                case 1: // SETTINGS_HEADER_TABLE_SIZE
                    break;
                case 2: // SETTINGS_ENABLE_PUSH
                    if (value != 0 && value != 1) {
                        throw ioException("PROTOCOL_ERROR SETTINGS_ENABLE_PUSH != 0 or 1");
                    }
                    break;
                case 3: // SETTINGS_MAX_CONCURRENT_STREAMS
                    id = 4; // Renumbered in draft 10.
                    break;
                case 4: // SETTINGS_INITIAL_WINDOW_SIZE
                    id = 7; // Renumbered in draft 10.
                    if (value < 0) {
                        throw ioException("PROTOCOL_ERROR SETTINGS_INITIAL_WINDOW_SIZE > 2^31 - 1");
                    }
                    break;
                case 5: // SETTINGS_MAX_FRAME_SIZE
                    if (value < INITIAL_MAX_FRAME_SIZE || value > 16777215) {
                        throw ioException("PROTOCOL_ERROR SETTINGS_MAX_FRAME_SIZE: %s", value);
                    }
                    break;
                case 6: // SETTINGS_MAX_HEADER_LIST_SIZE
                    break; // Advisory only, so ignored.
                default:
                    throw ioException("PROTOCOL_ERROR invalid settings id: %s", id);
            }
            settings.set(id, 0, value);
        }
        //handler.settings(false, settings);
        if (settings.getHeaderTableSize() >= 0) {
            headerTableSizeSetting(settings.getHeaderTableSize());
        }
    }

    private final List<Header> headerList = new ArrayList<>();
    private int headerTableSizeSetting;
    private int maxDynamicTableByteCount = 4096;
    // Visible for testing.
    Header[] dynamicTable = new Header[8];
    // Array is populated back to front, so new entries always have lowest index.
    int nextHeaderIndex = dynamicTable.length - 1;
    int headerCount = 0;
    int dynamicTableByteCount = 0;


    int maxDynamicTableByteCount() {
        return maxDynamicTableByteCount;
    }

    /**
     * Called by the reader when the peer sent {@link Settings#HEADER_TABLE_SIZE}.
     * While this establishes the maximum dynamic table size, the
     * {@link #maxDynamicTableByteCount} set during processing may limit the
     * table size to a smaller amount.
     * <p> Evicts entries or clears the table as needed.
     */
    void headerTableSizeSetting(int headerTableSizeSetting) {
        this.headerTableSizeSetting = headerTableSizeSetting;
        this.maxDynamicTableByteCount = headerTableSizeSetting;
        adjustDynamicTableByteCount();
    }

    private void adjustDynamicTableByteCount() {
        if (maxDynamicTableByteCount < dynamicTableByteCount) {
            if (maxDynamicTableByteCount == 0) {
                clearDynamicTable();
            } else {
                evictToRecoverBytes(dynamicTableByteCount - maxDynamicTableByteCount);
            }
        }
    }

    private void clearDynamicTable() {
        headerList.clear();
        Arrays.fill(dynamicTable, null);
        nextHeaderIndex = dynamicTable.length - 1;
        headerCount = 0;
        dynamicTableByteCount = 0;
    }

    /**
     * Returns the count of entries evicted.
     */
    private int evictToRecoverBytes(int bytesToRecover) {
        int entriesToEvict = 0;
        if (bytesToRecover > 0) {
            // determine how many headers need to be evicted.
            for (int j = dynamicTable.length - 1; j >= nextHeaderIndex && bytesToRecover > 0; j--) {
                bytesToRecover -= dynamicTable[j].hpackSize;
                dynamicTableByteCount -= dynamicTable[j].hpackSize;
                headerCount--;
                entriesToEvict++;
            }
            System.arraycopy(dynamicTable, nextHeaderIndex + 1, dynamicTable,
                    nextHeaderIndex + 1 + entriesToEvict, headerCount);
            nextHeaderIndex += entriesToEvict;
        }
        return entriesToEvict;
    }

    /**
     * index == -1 when new.
     */
    private void insertIntoDynamicTable(int index, Header entry) {
        headerList.add(entry);

        int delta = entry.hpackSize;
        if (index != -1) { // Index -1 == new header.
            delta -= dynamicTable[dynamicTableIndex(index)].hpackSize;
        }

        // if the new or replacement header is too big, drop all entries.
        if (delta > maxDynamicTableByteCount) {
            clearDynamicTable();
            return;
        }

        // Evict headers to the required length.
        int bytesToRecover = (dynamicTableByteCount + delta) - maxDynamicTableByteCount;
        int entriesEvicted = evictToRecoverBytes(bytesToRecover);

        if (index == -1) { // Adding a value to the dynamic table.
            if (headerCount + 1 > dynamicTable.length) { // Need to grow the dynamic table.
                Header[] doubled = new Header[dynamicTable.length * 2];
                System.arraycopy(dynamicTable, 0, doubled, dynamicTable.length, dynamicTable.length);
                nextHeaderIndex = dynamicTable.length - 1;
                dynamicTable = doubled;
            }
            index = nextHeaderIndex--;
            dynamicTable[index] = entry;
            headerCount++;
        } else { // Replace value at same position.
            index += dynamicTableIndex(index) + entriesEvicted;
            dynamicTable[index] = entry;
        }
        dynamicTableByteCount += delta;
    }

    private void readPriority(Source source, int streamId) throws IOException {
        int w1 = source.readInt();
        boolean exclusive = (w1 & 0x80000000) != 0;
        int streamDependency = (w1 & 0x7fffffff);
        int weight = (source.readByte() & 0xff) + 1;
        //handler.priority(streamId, streamDependency, weight, exclusive);
    }

    private int lengthWithoutPadding(int length, byte flags, short padding)
            throws IOException {
        if ((flags & FLAG_PADDED) != 0) length--; // Account for reading the padding length.
        if (padding > length) {
            throw ioException("PROTOCOL_ERROR padding %s > remaining length %s", padding, length);
        }
        return (short) (length - padding);
    }

    private static IOException ioException(String message, Object... args) throws IOException {
        throw new IOException(format(message, args));
    }

    /**
     * Returns a {@link Locale#US} formatted {@link String}.
     */
    public static String format(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

    private void readIndexedHeader(int index) throws IOException {
        if (isStaticHeader(index)) {
            Header staticEntry = STATIC_HEADER_TABLE[index];
            headerList.add(staticEntry);
        } else {
            int dynamicTableIndex = dynamicTableIndex(index - STATIC_HEADER_TABLE.length);
            if (dynamicTableIndex < 0 || dynamicTableIndex > dynamicTable.length - 1) {
                throw new IOException("Header index too large " + (index + 1));
            }
            headerList.add(dynamicTable[dynamicTableIndex]);
        }
    }

    private void readLiteralHeaderWithIncrementalIndexingNewName(Source source) throws IOException {
        ByteString name = checkLowercase(readByteString(source));
        ByteString value = readByteString(source);
        insertIntoDynamicTable(-1, new Header(name, value));
    }

    private void readLiteralHeaderWithIncrementalIndexingIndexedName(Source source, int nameIndex)
            throws IOException {
        ByteString name = getName(nameIndex);
        ByteString value = readByteString(source);
        insertIntoDynamicTable(-1, new Header(name, value));
    }

    /**
     * Reads a potentially Huffman encoded byte string.
     */
    ByteString readByteString(Source source) throws IOException {
        int firstByte = source.readByte();
        boolean huffmanDecode = (firstByte & 0x80) == 0x80; // 1NNNNNNN
        int length = readInt(source, firstByte, PREFIX_7_BITS);

        if (huffmanDecode) {
            return ByteString.of(Huffman.get().decode(source.readByteArray(length)));
        } else {
            return source.readByteString(length);
        }
    }

    // referencedHeaders is relative to nextHeaderIndex + 1.
    private int dynamicTableIndex(int index) {
        return nextHeaderIndex + 1 + index;
    }

    private ByteString getName(int index) {
        if (isStaticHeader(index)) {
            return STATIC_HEADER_TABLE[index].name;
        } else {
            return dynamicTable[dynamicTableIndex(index - STATIC_HEADER_TABLE.length)].name;
        }
    }

    private boolean isStaticHeader(int index) {
        return index >= 0 && index <= STATIC_HEADER_TABLE.length - 1;
    }

    private int readMedium(Source source) throws IOException {
        return (source.readByte() & 0xff) << 16
                | (source.readByte() & 0xff) << 8
                | (source.readByte() & 0xff);
    }

    public static class Source {
        int pos;
        byte[] data;
        int end;

        public boolean skip(int length) throws IOException {
            if (end - pos < length)
                throw new EOFException();
            pos += length;
            return true;
        }

        public boolean require(long byteCount) throws IOException {
            if (end - pos < byteCount)
                throw new EOFException();
            return true;
        }

        public byte readByte() throws IOException {
            require(1);
            byte b = data[pos++];
            return b;
        }

        public int readInt() throws IOException {
            require(4);

            return (readByte() & 0xff) << 24
                    | (readByte() & 0xff) << 16
                    | (readByte() & 0xff) << 8
                    | (readByte() & 0xff);
        }

        public short readShort() throws IOException {
            require(2);
            int s = (readByte() & 0xff) << 8
                    | (readByte() & 0xff);
            return (short) s;
        }

        public boolean exhausted() {
            if (pos >= end)
                return true;

            return false;
        }

        public ByteString readByteString(int byteCount) throws IOException {
            return new ByteString(readByteArray(byteCount));
        }

        public byte[] readByteArray(int byteCount) throws EOFException {
            if (byteCount > end - pos)
                throw new ArrayIndexOutOfBoundsException(
                        String.format("size=%s offset=%s byteCount=%s", end - pos, pos, byteCount));

            if (byteCount > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
            }

            byte[] result = new byte[byteCount];
            System.arraycopy(data, pos, result, 0, byteCount);
            pos = pos + byteCount;

            return result;
        }

//        public void readFully(byte[] sink) throws EOFException {
//            int offset = 0;
//            while (offset < sink.length) {
//                int read = read(sink, offset, sink.length - offset);
//                if (read == -1) throw new EOFException();
//                offset += read;
//            }
//        }
    }

    /**
     * An HTTP/2 response cannot contain uppercase header characters and must
     * be treated as malformed.
     */
    private static ByteString checkLowercase(ByteString name) throws IOException {
        for (int i = 0, length = name.size(); i < length; i++) {
            byte c = name.getByte(i);
            if (c >= 'A' && c <= 'Z') {
                throw new IOException("PROTOCOL_ERROR response malformed: mixed case name: " + name.utf8());
            }
        }
        return name;
    }

    public static void checkOffsetAndCount(long size, long offset, long byteCount) {
        if ((offset | byteCount) < 0 || offset > size || size - offset < byteCount) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("size=%s offset=%s byteCount=%s", size, offset, byteCount));
        }
    }
}
