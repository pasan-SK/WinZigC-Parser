package lexer.tokens;

import diagnostics.Highlightable;

public class Minutiae implements Highlightable {
    private final TokenType type;
    private final String content;
    // Variables required for highlighted code for diagnostics.
    protected final int beginOffset;
    protected final int endOffset;

    public Minutiae(TokenType type, String content, int beginOffset, int endOffset) {
        this.type = type;
        this.content = content;
        this.beginOffset = beginOffset;
        this.endOffset = endOffset;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", type, content);
    }

    @Override
    public int getBeginOffset() {
        return beginOffset;
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }
}
