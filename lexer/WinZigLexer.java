package lexer;

import lexer.tokens.*;

import java.util.ArrayList;
import java.util.List;

public class WinZigLexer extends AbstractLexer {
    public WinZigLexer(CharReader charReader) {
        super(charReader);
    }

    public Token read() {
        processLeadingMinutiae();

        charReader.beginMarking();
        if (charReader.isEOF()) {
            return createToken(TokenType.EOF_TOKEN);
        }

        char c = charReader.peek();
        charReader.advance();
        switch (c) {
            // Arithmetic Operators
            case LexerTerminals.PLUS:
                return createToken(TokenType.PLUS_TOKEN);
            case LexerTerminals.MINUS:
                return createToken(TokenType.MINUS_TOKEN);
            case LexerTerminals.MULTIPLY:
                return createToken(TokenType.MULTIPLY_TOKEN);
            case LexerTerminals.DIVIDE:
                return createToken(TokenType.DIVIDE_TOKEN);
            // Binary operators
            case LexerTerminals.EQUAL:
                return createToken(TokenType.EQUAL_TOKEN);
            case LexerTerminals.LT:
                return processLt();
            case LexerTerminals.GT:
                return processGt();
            // Punctuations
            case LexerTerminals.OPEN_BRACKET:
                return createToken(TokenType.OPEN_BRACKET_TOKEN);
            case LexerTerminals.CLOSE_BRACKET:
                return createToken(TokenType.CLOSE_BRACKET_TOKEN);
            case LexerTerminals.SEMICOLON:
                return createToken(TokenType.SEMICOLON_TOKEN);
            case LexerTerminals.COMMA:
                return createToken(TokenType.COMMA_TOKEN);
            case LexerTerminals.COLON:
                return processColon();
            case LexerTerminals.DOT:
                return processDot();
            // Digits
            case LexerTerminals.DIGIT_0:
            case LexerTerminals.DIGIT_1:
            case LexerTerminals.DIGIT_2:
            case LexerTerminals.DIGIT_3:
            case LexerTerminals.DIGIT_4:
            case LexerTerminals.DIGIT_5:
            case LexerTerminals.DIGIT_6:
            case LexerTerminals.DIGIT_7:
            case LexerTerminals.DIGIT_8:
            case LexerTerminals.DIGIT_9:
                return processInteger();
            // Quotes
            case LexerTerminals.QUOTE:
                return processChar();
            case LexerTerminals.DOUBLE_QUOTE:
                return processString();
            default:
                if (isIdentifierInitialChar(c)) {
                    return processKeywordOrIdentifier();
                }
                // Unexpected character: return a minutiae token
                return createErrorToken("Unexpected character: %c", c);
        }
    }

    /*
     * -------------------------------
     * Other token creation methods
     * -------------------------------
     */

    private Token processColon() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.EQUAL) {
            char nextNextChar = charReader.peek(1);
            if (nextNextChar == LexerTerminals.COLON) {
                charReader.advance(2);
                return createToken(TokenType.SWAP_TOKEN);
            }
            charReader.advance();
            return createToken(TokenType.ASSIGNMENT_TOKEN);
        }
        return createToken(TokenType.COLON_TOKEN);
    }

    private Token processDot() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.DOT) {
            charReader.advance();
            return createToken(TokenType.DOUBLE_DOTS_TOKEN);
        }
        return createToken(TokenType.SINGLE_DOT_TOKEN);
    }

    private Token processLt() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.EQUAL) {
            charReader.advance();
            return createToken(TokenType.LT_EQUAL_TOKEN);
        }
        if (nextChar == LexerTerminals.GT) {
            charReader.advance();
            return createToken(TokenType.NOT_EQUAL_TOKEN);
        }
        return createToken(TokenType.LT_TOKEN);
    }

    private Token processGt() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.EQUAL) {
            charReader.advance();
            return createToken(TokenType.GT_EQUAL_TOKEN);
        }
        return createToken(TokenType.GT_TOKEN);
    }

    private Token processInteger() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            if (isDigit(nextChar)) {
                charReader.advance();
                continue;
            }
            break;
        }
        return createLiteralToken(TokenType.INTEGER_LITERAL);
    }

    private Token processChar() {
        char nextChar = charReader.peek();
        if (nextChar != LexerTerminals.QUOTE) {
            char nextNextChar = charReader.peek(1);
            if (nextNextChar == LexerTerminals.QUOTE) {
                charReader.advance(2);
                return createLiteralToken(TokenType.CHAR_LITERAL);
            }
        }
        return createErrorToken("Expected character literal in the form 'c'.");
    }

    private Token processString() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            charReader.advance();
            if (nextChar == LexerTerminals.DOUBLE_QUOTE) {
                return createLiteralToken(TokenType.STRING_LITERAL);
            }
        }
        return createErrorToken("Expected string literal in the form \"string\".");
    }

    private Token processKeywordOrIdentifier() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            if (isIdentifierChar(nextChar)) {
                charReader.advance();
                continue;
            }
            break;
        }
        String s = charReader.getMarkedChars();
        switch (s) {
            case LexerTerminals.PROGRAM_KEYWORD:
                return createToken(TokenType.PROGRAM_KEYWORD);
            case LexerTerminals.VAR_KEYWORD:
                return createToken(TokenType.VAR_KEYWORD);
            case LexerTerminals.CONST_KEYWORD:
                return createToken(TokenType.CONST_KEYWORD);
            case LexerTerminals.TYPE_KEYWORD:
                return createToken(TokenType.TYPE_KEYWORD);
            case LexerTerminals.FUNCTION_KEYWORD:
                return createToken(TokenType.FUNCTION_KEYWORD);
            case LexerTerminals.RETURN_KEYWORD:
                return createToken(TokenType.RETURN_KEYWORD);
            case LexerTerminals.BEGIN_KEYWORD:
                return createToken(TokenType.BEGIN_KEYWORD);
            case LexerTerminals.END_KEYWORD:
                return createToken(TokenType.END_KEYWORD);
            case LexerTerminals.OUTPUT_KEYWORD:
                return createToken(TokenType.OUTPUT_KEYWORD);
            case LexerTerminals.IF_KEYWORD:
                return createToken(TokenType.IF_KEYWORD);
            case LexerTerminals.THEN_KEYWORD:
                return createToken(TokenType.THEN_KEYWORD);
            case LexerTerminals.ELSE_KEYWORD:
                return createToken(TokenType.ELSE_KEYWORD);
            case LexerTerminals.WHILE_KEYWORD:
                return createToken(TokenType.WHILE_KEYWORD);
            case LexerTerminals.DO_KEYWORD:
                return createToken(TokenType.DO_KEYWORD);
            case LexerTerminals.CASE_KEYWORD:
                return createToken(TokenType.CASE_KEYWORD);
            case LexerTerminals.OF_KEYWORD:
                return createToken(TokenType.OF_KEYWORD);
            case LexerTerminals.OTHERWISE_KEYWORD:
                return createToken(TokenType.OTHERWISE_KEYWORD);
            case LexerTerminals.REPEAT_KEYWORD:
                return createToken(TokenType.REPEAT_KEYWORD);
            case LexerTerminals.FOR_KEYWORD:
                return createToken(TokenType.FOR_KEYWORD);
            case LexerTerminals.UNTIL_KEYWORD:
                return createToken(TokenType.UNTIL_KEYWORD);
            case LexerTerminals.LOOP_KEYWORD:
                return createToken(TokenType.LOOP_KEYWORD);
            case LexerTerminals.POOL_KEYWORD:
                return createToken(TokenType.POOL_KEYWORD);
            case LexerTerminals.EXIT_KEYWORD:
                return createToken(TokenType.EXIT_KEYWORD);
            case LexerTerminals.MOD_KEYWORD:
                return createToken(TokenType.MOD_KEYWORD);
            case LexerTerminals.AND_KEYWORD:
                return createToken(TokenType.AND_KEYWORD);
            case LexerTerminals.OR_KEYWORD:
                return createToken(TokenType.OR_KEYWORD);
            case LexerTerminals.NOT_KEYWORD:
                return createToken(TokenType.NOT_KEYWORD);
            case LexerTerminals.READ_KEYWORD:
                return createToken(TokenType.READ_KEYWORD);
            case LexerTerminals.SUCC_KEYWORD:
                return createToken(TokenType.SUCC_KEYWORD);
            case LexerTerminals.PRED_KEYWORD:
                return createToken(TokenType.PRED_KEYWORD);
            case LexerTerminals.CHR_KEYWORD:
                return createToken(TokenType.CHR_KEYWORD);
            case LexerTerminals.ORD_KEYWORD:
                return createToken(TokenType.ORD_KEYWORD);
            case LexerTerminals.EOF_KEYWORD:
                return createToken(TokenType.EOF_KEYWORD);
            default:
                return createIdentifierToken();
        }
    }

    /*
     * -------------------------------
     * Minutiae creation methods
     * -------------------------------
     */

    private void processLeadingMinutiae() {
        processSyntaxMinutiae(this.leadingMinutiae, true);
    }

    private List<Minutiae> processTrailingMinutiae() {
        List<Minutiae> minutiae = new ArrayList<>();
        processSyntaxMinutiae(minutiae, false);
        return minutiae;
    }

    private void processSyntaxMinutiae(List<Minutiae> minutiae, boolean isLeading) {
        while (!charReader.isEOF()) {
            charReader.beginMarking();
            char c = charReader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                case LexerTerminals.FORM_FEED:
                    minutiae.add(processWhitespaceMinutiae());
                    break;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    minutiae.add(processEndOfLineMinutiae());
                    if (isLeading) {
                        break;
                    }
                    return;
                case LexerTerminals.OPEN_BRACE:
                    minutiae.add(processMultilineCommentMinutiae());
                    break;
                case LexerTerminals.HASH:
                    minutiae.add(processCommentMinutiae());
                    break;
                default:
                    return;
            }
        }
    }

    private Minutiae processWhitespaceMinutiae() {
        while (!charReader.isEOF()) {
            char c = charReader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                case LexerTerminals.FORM_FEED:
                    charReader.advance();
                    continue;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                default:
                    break;
            }
            break;
        }

        return createMinutiae(TokenType.WHITESPACE_MINUTIAE);
    }

    private Minutiae processEndOfLineMinutiae() {
        char c = charReader.peek();
        switch (c) {
            case LexerTerminals.NEWLINE:
                charReader.advance();
                return createMinutiae(TokenType.END_OF_LINE_MINUTIAE);
            case LexerTerminals.CARRIAGE_RETURN:
                charReader.advance();
                if (charReader.peek() == LexerTerminals.NEWLINE) {
                    charReader.advance();
                }
                return createMinutiae(TokenType.END_OF_LINE_MINUTIAE);
            default:
                return createErrorMinutiae("Unexpected eof found.");
        }
    }

    private Minutiae processCommentMinutiae() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            if (nextChar == LexerTerminals.NEWLINE || nextChar == LexerTerminals.CARRIAGE_RETURN) {
                break;
            }
            charReader.advance();
        }
        return createMinutiae(TokenType.COMMENT_MINUTIAE);
    }

    private Minutiae processMultilineCommentMinutiae() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            charReader.advance();
            if (nextChar == LexerTerminals.CLOSE_BRACE) {
                return createMinutiae(TokenType.MULTILINE_COMMENT_MINUTIAE);
            }
        }
        return createErrorMinutiae("Expected multi-line comments in form { COMMENT }.");
    }

    /*
     * -------------------------------
     * Token creation methods
     * -------------------------------
     */

    private Token createErrorToken(String error, Object... args) {
        Token errorToken = createToken(TokenType.ERROR);
        addError(errorToken, error, args);
        return errorToken;
    }

    private Minutiae createErrorMinutiae(String error, Object... args) {
        Minutiae errorMinutiae = createMinutiae(TokenType.ERROR);
        addError(errorMinutiae, error, args);
        return errorMinutiae;
    }

    private Token createToken(TokenType kind) {
        int beginOffset = charReader.getMarkBeginOffset();
        int endOffset = charReader.getOffset();
        return new Token(kind, getLeadingMinutiae(), processTrailingMinutiae(),
                beginOffset, endOffset);
    }

    private Token createIdentifierToken() {
        int beginOffset = charReader.getMarkBeginOffset();
        int endOffset = charReader.getOffset();
        return new IdentifierToken(charReader.getMarkedChars(), getLeadingMinutiae(), processTrailingMinutiae(),
                beginOffset, endOffset);
    }

    private Token createLiteralToken(TokenType kind) {
        int beginOffset = charReader.getMarkBeginOffset();
        int endOffset = charReader.getOffset();
        return new LiteralToken(kind, charReader.getMarkedChars(), getLeadingMinutiae(), processTrailingMinutiae(),
                beginOffset, endOffset);
    }

    private Minutiae createMinutiae(TokenType kind) {
        int beginOffset = charReader.getMarkBeginOffset();
        int endOffset = charReader.getOffset();
        return new Minutiae(kind, charReader.getMarkedChars(), beginOffset, endOffset);
    }
}
