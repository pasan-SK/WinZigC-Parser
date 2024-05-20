package lexer.tokens;

import diagnostics.Highlightable;

import java.util.List;

public class Token implements Highlightable {
    protected final TokenType type;
    protected final List<Minutiae> leading;
    protected final List<Minutiae> trailing;
    // Variables required for highlighted code for diagnostics.
    protected final int beginOffset;
    protected final int endOffset;

    public Token(TokenType type, List<Minutiae> leading, List<Minutiae> trailing,
                 int beginOffset, int endOffset) {
        this.type = type;
        this.leading = leading;
        this.trailing = trailing;
        this.beginOffset = beginOffset;
        this.endOffset = endOffset;
    }

    public TokenType getType() {
        return type;
    }

    public String getTokenValue() {
        return type.getValue();
    }

    @Override
    public String toString() {
        return type.toString();
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
