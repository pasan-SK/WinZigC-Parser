package lexer.tokens;

import java.util.List;

public class LiteralToken extends Token {
    private final String value;

    public LiteralToken(TokenType kind, String value, List<Minutiae> leading, List<Minutiae> trailing,
                        int beginOffset, int endOffset) {
        super(kind, leading, trailing, beginOffset, endOffset);
        this.value = value;
    }

    @Override
    public String getTokenValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", type, value);
    }
}
