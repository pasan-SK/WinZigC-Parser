package lexer;


import diagnostics.Highlightable;
import diagnostics.TextHighlighter;

import java.util.Arrays;
import java.util.StringJoiner;

public class CharReader implements TextHighlighter {
    private final char[] charBuffer;
    private final int charBufferLength;
    private int markBeginOffset;
    private int offset;

    private CharReader(char[] buffer) {
        this.charBuffer = buffer;
        this.charBufferLength = buffer.length;
        this.markBeginOffset = 0;
        this.offset = 0;
    }

    public static CharReader from(String text) {
        return new CharReader(text.toCharArray());
    }

    public char peek() {
        if (offset < charBufferLength) {
            return charBuffer[offset];
        }
        return Character.MAX_VALUE;
    }

    public char peek(int k) {
        int kOffset = offset + k;
        if (kOffset < charBufferLength) {
            return charBuffer[kOffset];
        }
        return Character.MAX_VALUE;
    }

    public void advance() {
        offset++;
    }

    public void advance(int k) {
        offset += k;
    }

    public void beginMarking() {
        markBeginOffset = offset;
    }

    public String getMarkedChars() {
        return new String(Arrays.copyOfRange(charBuffer, markBeginOffset, offset));
    }

    public boolean isEOF() {
        return offset >= charBufferLength;
    }

    public int getMarkBeginOffset() {
        return markBeginOffset;
    }

    public int getOffset() {
        return offset;
    }

    public String highlightedSegment(Highlightable highlightable) {
        int startOffset = highlightable.getBeginOffset();
        int endOffset = highlightable.getEndOffset();
        if (startOffset == -1 || endOffset == -1) {
            return "";
        }

        int lineNumber = 1;
        for (int i = 0; i < charBufferLength && i < startOffset; i++) {
            if (charBuffer[i] == '\n') lineNumber++;
        }

        int prevCrOffset, nextCrOffset;
        for (prevCrOffset = startOffset; prevCrOffset >= 0; prevCrOffset--) {
            if (charBufferLength <= startOffset || charBuffer[prevCrOffset] == '\n') break;
        }

        for (nextCrOffset = endOffset; nextCrOffset < charBufferLength; nextCrOffset++) {
            if (charBuffer[nextCrOffset] == '\n') break;
        }
        StringJoiner textSb = new StringJoiner("\n");
        StringBuilder codeSbLine = new StringBuilder();
        StringBuilder highlightSbLine = new StringBuilder();
        for (int i = prevCrOffset + 1; i < nextCrOffset; i++) {
            if (charBuffer[i] == '\n') {
                textSb.add(codeSbLine);
                codeSbLine = new StringBuilder();
                if (!highlightSbLine.toString().isBlank()) {
                    textSb.add(highlightSbLine);
                    highlightSbLine = new StringBuilder();
                }
                continue;
            }
            codeSbLine.append(charBuffer[i]);
            if (i >= startOffset && i < endOffset) {
                highlightSbLine.append('^');
            } else {
                highlightSbLine.append(' ');
            }
            if (charBuffer[i] == '\t') {
                highlightSbLine.append('\t');
            }
        }
        String positionText = String.format("---- Line %s, column %s", lineNumber, startOffset - prevCrOffset);
        return textSb.add(codeSbLine).add(highlightSbLine)
                .add(positionText).toString();
    }
}
