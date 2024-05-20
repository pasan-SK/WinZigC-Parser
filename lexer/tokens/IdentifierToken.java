package lexer.tokens;

import java.util.List;

public class IdentifierToken extends Token {
    private final String value;

    public IdentifierToken(String value, List<Minutiae> leading, List<Minutiae> trailing,
                           int beginOffset, int endOffset) {
        super(TokenType.IDENTIFIER, leading, trailing, beginOffset, endOffset);
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
