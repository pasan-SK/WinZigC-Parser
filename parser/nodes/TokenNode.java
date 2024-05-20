package parser.nodes;

import lexer.tokens.Token;
import lexer.tokens.TokenType;

public class TokenNode implements Node {
    protected final TokenType tokenType;
    protected final String value;
    protected final Token token;

    public TokenNode(Token token) {
        this(token.getType(), token.getTokenValue(), token);
    }

    protected TokenNode(TokenType tokenType, String value, Token token) {
        this.tokenType = tokenType;
        this.value = value;
        this.token = token;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s(0)", value);
    }

    @Override
    public int getBeginOffset() {
        return token.getBeginOffset();
    }

    @Override
    public int getEndOffset() {
        return token.getEndOffset();
    }
}
