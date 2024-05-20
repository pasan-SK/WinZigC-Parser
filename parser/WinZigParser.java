package parser;

import lexer.WinZigLexer;
import lexer.tokens.TokenType;
import parser.nodes.ASTNode;
import parser.nodes.Node;
import parser.nodes.NodeType;

public class WinZigParser extends AbstractParser {
    public WinZigParser(WinZigLexer lexer) {
        super(lexer);
    }

    @Override
    public ASTNode parse() {
        parseWinZig();
        Node winZigNode = nodeStack.pop();
        if (winZigNode instanceof ASTNode) {
            if (!nodeStack.isEmpty()) addError(winZigNode, "Internal error: More items left in parser stack.");
            return (ASTNode) winZigNode;
        }
        // This should not reach.
        throw new IllegalStateException(String.format("[%s] but remaining %s.", winZigNode, nodeStack));
    }

    private void parseWinZig() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.PROGRAM_KEYWORD);
        itemCount += parseName();
        itemCount += parseToken(TokenType.COLON_TOKEN);
        itemCount += parseConsts();
        itemCount += parseTypes();
        itemCount += parseDclns();
        itemCount += parseSubProgs();
        itemCount += parseBody();
        itemCount += parseName();
        itemCount += parseToken(TokenType.SINGLE_DOT_TOKEN);
        buildTree(NodeType.PROGRAM, itemCount);
    }

    private int parseConsts() {
        int itemCount = 0;
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.CONST_KEYWORD) {
            itemCount += parseToken(TokenType.CONST_KEYWORD);
            itemCount += parseList(this::parseConst, TokenType.COMMA_TOKEN);
            itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
        }
        return buildTree(NodeType.CONSTS, itemCount);
    }

    private int parseConst() {
        int itemCount = 0;
        itemCount += parseName();
        itemCount += parseToken(TokenType.EQUAL_TOKEN);
        itemCount += parseConstValue();
        return buildTree(NodeType.CONST, itemCount);
    }

    private int parseConstValue() {
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.INTEGER_LITERAL) {
            return parseIdentifier(TokenType.INTEGER_LITERAL);
        } else if (tokenType == TokenType.CHAR_LITERAL) {
            return parseIdentifier(TokenType.CHAR_LITERAL);
        } else {
            return parseName();
        }
    }

    private int parseTypes() {
        int itemCount = 0;
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.TYPE_KEYWORD) {
            itemCount += parseToken(TokenType.TYPE_KEYWORD);
            itemCount += parseType();
            itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
            // After types is always Dclns(nullable), SubProgs(nullable) and Body.
            // So must be followed by 'var', 'function' or 'begin' at the end.
            tokenType = peekNextKind();
            while (tokenType != TokenType.VAR_KEYWORD
                    && tokenType != TokenType.FUNCTION_KEYWORD
                    && tokenType != TokenType.BEGIN_KEYWORD) {
                itemCount += parseType();
                itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
                tokenType = peekNextKind();
            }
        }
        return buildTree(NodeType.TYPES, itemCount);
    }

    private int parseType() {
        int itemCount = 0;
        itemCount += parseName();
        itemCount += parseToken(TokenType.EQUAL_TOKEN);
        itemCount += parseLitList();
        return buildTree(NodeType.TYPE, itemCount);
    }

    private int parseLitList() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseName, TokenType.COMMA_TOKEN);
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeType.LIT, itemCount);
    }

    private int parseSubProgs() {
        int itemCount = 0;
        TokenType tokenType = peekNextKind();
        while (tokenType == TokenType.FUNCTION_KEYWORD) {
            itemCount += parseFcn();
            tokenType = peekNextKind();
        }
        return buildTree(NodeType.SUBPROGS, itemCount);
    }

    private int parseFcn() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.FUNCTION_KEYWORD);
        itemCount += parseName();
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseParams();
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        itemCount += parseToken(TokenType.COLON_TOKEN);
        itemCount += parseName();
        itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
        itemCount += parseConsts();
        itemCount += parseTypes();
        itemCount += parseDclns();
        itemCount += parseBody();
        itemCount += parseName();
        itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
        return buildTree(NodeType.FCN, itemCount);
    }

    private int parseParams() {
        int itemCount = 0;
        itemCount += parseList(this::parseDcln, TokenType.SEMICOLON_TOKEN);
        return buildTree(NodeType.PARAMS, itemCount);
    }

    private int parseDclns() {
        int itemCount = 0;
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.VAR_KEYWORD) {
            itemCount += parseToken(TokenType.VAR_KEYWORD);
            itemCount += parseDcln();
            itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
            // Dclns is followed by SubProgs(nullable) and Body.
            // So must be followed by 'function' or 'begin' at the end.
            tokenType = peekNextKind();
            while (tokenType != TokenType.FUNCTION_KEYWORD
                    && tokenType != TokenType.BEGIN_KEYWORD) {
                itemCount += parseDcln();
                itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
                tokenType = peekNextKind();
            }
        }
        return buildTree(NodeType.DCLNS, itemCount);
    }

    private int parseDcln() {
        int itemCount = 0;
        itemCount += parseList(this::parseName, TokenType.COMMA_TOKEN);
        itemCount += parseToken(TokenType.COLON_TOKEN);
        itemCount += parseName();
        return buildTree(NodeType.VAR, itemCount);
    }

    private int parseBody() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.BEGIN_KEYWORD);
        itemCount += parseList(this::parseStatement, TokenType.SEMICOLON_TOKEN);
        itemCount += parseToken(TokenType.END_KEYWORD);
        return buildTree(NodeType.BLOCK, itemCount);
    }

    private int parseStatement() {
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.OUTPUT_KEYWORD) {
            return parseOutputStatement();
        } else if (tokenType == TokenType.IF_KEYWORD) {
            return parseIfStatement();
        } else if (tokenType == TokenType.WHILE_KEYWORD) {
            return parseWhileStatement();
        } else if (tokenType == TokenType.REPEAT_KEYWORD) {
            return parseRepeatStatement();
        } else if (tokenType == TokenType.FOR_KEYWORD) {
            return parseForStatement();
        } else if (tokenType == TokenType.LOOP_KEYWORD) {
            return parseLoopStatement();
        } else if (tokenType == TokenType.CASE_KEYWORD) {
            return parseCaseStatement();
        } else if (tokenType == TokenType.READ_KEYWORD) {
            return parseReadStatement();
        } else if (tokenType == TokenType.EXIT_KEYWORD) {
            return parseExitStatement();
        } else if (tokenType == TokenType.RETURN_KEYWORD) {
            return parseReturnStatement();
        } else if (tokenType == TokenType.BEGIN_KEYWORD) {
            return parseBody();
        } else {
            // The second token of assignment is either ':=' or ':=:'.
            TokenType peekKind = peekNextKind(1);
            if (peekKind == TokenType.ASSIGNMENT_TOKEN || peekKind == TokenType.SWAP_TOKEN) {
                return parseAssignmentStatement();
            } else {
                return buildTree(NodeType.NULL_STATEMENT, 0);
            }
        }
    }

    private int parseOutputStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.OUTPUT_KEYWORD);
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseOutExp, TokenType.COMMA_TOKEN);
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeType.OUTPUT_STATEMENT, itemCount);
    }

    private int parseIfStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.IF_KEYWORD);
        itemCount += parseExpression();
        itemCount += parseToken(TokenType.THEN_KEYWORD);
        itemCount += parseStatement();
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.ELSE_KEYWORD) {
            itemCount += parseToken(TokenType.ELSE_KEYWORD);
            itemCount += parseStatement();
        }
        return buildTree(NodeType.IF_STATEMENT, itemCount);
    }

    private int parseWhileStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.WHILE_KEYWORD);
        itemCount += parseExpression();
        itemCount += parseToken(TokenType.DO_KEYWORD);
        itemCount += parseStatement();
        return buildTree(NodeType.WHILE_STATEMENT, itemCount);
    }

    private int parseRepeatStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.REPEAT_KEYWORD);
        itemCount += parseList(this::parseStatement, TokenType.SEMICOLON_TOKEN);
        itemCount += parseToken(TokenType.UNTIL_KEYWORD);
        itemCount += parseExpression();
        return buildTree(NodeType.REPEAT_STATEMENT, itemCount);
    }

    private int parseForStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.FOR_KEYWORD);
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseForStat();
        itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
        itemCount += parseForExp();
        itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
        itemCount += parseForStat();
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        itemCount += parseStatement();
        return buildTree(NodeType.FOR_STATEMENT, itemCount);
    }

    private int parseLoopStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.LOOP_KEYWORD);
        itemCount += parseList(this::parseStatement, TokenType.SEMICOLON_TOKEN);
        itemCount += parseToken(TokenType.POOL_KEYWORD);
        return buildTree(NodeType.LOOP_STATEMENT, itemCount);
    }

    private int parseCaseStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.CASE_KEYWORD);
        itemCount += parseExpression();
        itemCount += parseToken(TokenType.OF_KEYWORD);
        itemCount += parseCaseClauses();
        itemCount += parseOtherwiseClause();
        itemCount += parseToken(TokenType.END_KEYWORD);
        return buildTree(NodeType.CASE_STATEMENT, itemCount);
    }

    private int parseReadStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.READ_KEYWORD);
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseName, TokenType.COMMA_TOKEN);
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeType.READ_STATEMENT, itemCount);
    }

    private int parseExitStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.EXIT_KEYWORD);
        return buildTree(NodeType.EXIT_STATEMENT, itemCount);
    }

    private int parseReturnStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.RETURN_KEYWORD);
        itemCount += parseExpression();
        return buildTree(NodeType.RETURN_STATEMENT, itemCount);
    }

    private int parseOutExp() {
        int itemCount = 0;
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.STRING_LITERAL) {
            itemCount += parseStringNode();
            return buildTree(NodeType.STRING_OUT_EXP, itemCount);
        } else {
            itemCount += parseExpression();
            return buildTree(NodeType.INTEGER_OUT_EXP, itemCount);
        }
    }

    private int parseStringNode() {
        return parseIdentifier(TokenType.STRING_LITERAL);
    }

    private int parseCaseClauses() {
        int itemCount = 0;
        itemCount += parseCaseClause();
        itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
        // Case clauses must be followed by otherwise(nullable) or 'end'.
        // So must be followed bt 'otherwise' or 'end'.
        TokenType tokenType = peekNextKind();
        while (tokenType != TokenType.OTHERWISE_KEYWORD
                && tokenType != TokenType.END_KEYWORD) {
            itemCount += parseCaseClause();
            itemCount += parseToken(TokenType.SEMICOLON_TOKEN);
            tokenType = peekNextKind();
        }
        return itemCount;
    }

    private int parseCaseClause() {
        int itemCount = 0;
        itemCount += parseList(this::parseCaseExpression, TokenType.COMMA_TOKEN);
        itemCount += parseToken(TokenType.COLON_TOKEN);
        itemCount += parseStatement();
        return buildTree(NodeType.CASE_CLAUSE, itemCount);
    }

    private int parseCaseExpression() {
        int itemCount = 0;
        itemCount += parseConstValue();
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.DOUBLE_DOTS_TOKEN) {
            itemCount += parseToken(TokenType.DOUBLE_DOTS_TOKEN);
            itemCount += parseConstValue();
            return buildTree(NodeType.DOUBLE_DOTS_CLAUSE, itemCount);
        }
        return itemCount;
    }

    private int parseOtherwiseClause() {
        int itemCount = 0;
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.OTHERWISE_KEYWORD) {
            itemCount += parseToken(TokenType.OTHERWISE_KEYWORD);
            itemCount += parseStatement();
            return buildTree(NodeType.OTHERWISE_CLAUSE, itemCount);
        }
        return itemCount;
    }

    private int parseAssignmentStatement() {
        int itemCount = 0;
        itemCount += parseName();
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.ASSIGNMENT_TOKEN) {
            itemCount += parseToken(TokenType.ASSIGNMENT_TOKEN);
            itemCount += parseExpression();
            return buildTree(NodeType.ASSIGNMENT_STATEMENT, itemCount);
        } else {
            itemCount += parseToken(TokenType.SWAP_TOKEN);
            itemCount += parseName();
            return buildTree(NodeType.SWAP_STATEMENT, itemCount);
        }
    }

    private int parseForStat() {
        // ForStat is always followed by ';'.
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.SEMICOLON_TOKEN) {
            return buildTree(NodeType.NULL_STATEMENT, 0);
        } else {
            return parseAssignmentStatement();
        }
    }

    private int parseForExp() {
        // ForStat is always followed by ';'.
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.SEMICOLON_TOKEN) {
            return buildTree(NodeType.TRUE, 0);
        } else {
            return parseExpression();
        }
    }

    private int parseExpression() {
        int itemCount = 0;
        itemCount += parseTerm();
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.LT_EQUAL_TOKEN) {
            itemCount += parseToken(TokenType.LT_EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeType.LT_EQUAL_EXPRESSION, itemCount);
        } else if (tokenType == TokenType.LT_TOKEN) {
            itemCount += parseToken(TokenType.LT_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeType.LT_EXPRESSION, itemCount);
        } else if (tokenType == TokenType.GT_EQUAL_TOKEN) {
            itemCount += parseToken(TokenType.GT_EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeType.GT_EQUAL_EXPRESSION, itemCount);
        } else if (tokenType == TokenType.GT_TOKEN) {
            itemCount += parseToken(TokenType.GT_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeType.GT_EXPRESSION, itemCount);
        } else if (tokenType == TokenType.EQUAL_TOKEN) {
            itemCount += parseToken(TokenType.EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeType.EQUALS_EXPRESSION, itemCount);
        } else if (tokenType == TokenType.NOT_EQUAL_TOKEN) {
            itemCount += parseToken(TokenType.NOT_EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeType.NOT_EQUALS_EXPRESSION, itemCount);
        }
        return itemCount;
    }

    private int parseTerm() {
        int itemCount = 0;
        itemCount += parseFactor();
        TokenType tokenType = peekNextKind();
        while (tokenType == TokenType.PLUS_TOKEN
                || tokenType == TokenType.MINUS_TOKEN
                || tokenType == TokenType.OR_KEYWORD) {
            if (tokenType == TokenType.PLUS_TOKEN) {
                itemCount += parseToken(TokenType.PLUS_TOKEN);
                itemCount += parseFactor();
                itemCount = buildTree(NodeType.ADD_EXPRESSION, itemCount);
            } else if (tokenType == TokenType.MINUS_TOKEN) {
                itemCount += parseToken(TokenType.MINUS_TOKEN);
                itemCount += parseFactor();
                itemCount = buildTree(NodeType.SUBTRACT_EXPRESSION, itemCount);
            } else {
                itemCount += parseToken(TokenType.OR_KEYWORD);
                itemCount += parseFactor();
                itemCount = buildTree(NodeType.OR_EXPRESSION, itemCount);
            }
            tokenType = peekNextKind();
        }
        return itemCount;
    }

    private int parseFactor() {
        int itemCount = 0;
        itemCount += parsePrimary();
        TokenType tokenType = peekNextKind();
        while (tokenType == TokenType.MULTIPLY_TOKEN
                || tokenType == TokenType.DIVIDE_TOKEN
                || tokenType == TokenType.AND_KEYWORD
                || tokenType == TokenType.MOD_KEYWORD) {
            if (tokenType == TokenType.MULTIPLY_TOKEN) {
                itemCount += parseToken(TokenType.MULTIPLY_TOKEN);
                itemCount += parsePrimary();
                itemCount = buildTree(NodeType.MULTIPLY_EXPRESSION, itemCount);
            } else if (tokenType == TokenType.DIVIDE_TOKEN) {
                itemCount += parseToken(TokenType.DIVIDE_TOKEN);
                itemCount += parsePrimary();
                itemCount = buildTree(NodeType.DIVIDE_EXPRESSION, itemCount);
            } else if (tokenType == TokenType.AND_KEYWORD) {
                itemCount += parseToken(TokenType.AND_KEYWORD);
                itemCount += parsePrimary();
                itemCount = buildTree(NodeType.AND_EXPRESSION, itemCount);
            } else {
                itemCount += parseToken(TokenType.MOD_KEYWORD);
                itemCount += parsePrimary();
                itemCount = buildTree(NodeType.MOD_EXPRESSION, itemCount);
            }
            tokenType = peekNextKind();
        }
        return itemCount;
    }

    private int parsePrimary() {
        int itemCount = 0;
        TokenType tokenType = peekNextKind();
        if (tokenType == TokenType.MINUS_TOKEN) {
            itemCount += parseNegativeExpression();
        } else if (tokenType == TokenType.PLUS_TOKEN) {
            itemCount += parseToken(TokenType.PLUS_TOKEN);
            itemCount += parsePrimary();
        } else if (tokenType == TokenType.NOT_KEYWORD) {
            itemCount += parseNotExpression();
        } else if (tokenType == TokenType.EOF_KEYWORD) {
            itemCount += parseEofExpression();
        } else if (tokenType == TokenType.INTEGER_LITERAL) {
            itemCount += parseIdentifier(TokenType.INTEGER_LITERAL);
        } else if (tokenType == TokenType.CHAR_LITERAL) {
            itemCount += parseIdentifier(TokenType.CHAR_LITERAL);
        } else if (tokenType == TokenType.OPEN_BRACKET_TOKEN) {
            itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
            itemCount += parseExpression();
            itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        } else if (tokenType == TokenType.SUCC_KEYWORD) {
            itemCount += parseSuccExpression();
        } else if (tokenType == TokenType.PRED_KEYWORD) {
            itemCount += parsePredExpression();
        } else if (tokenType == TokenType.CHR_KEYWORD) {
            itemCount += parseChrExpression();
        } else if (tokenType == TokenType.ORD_KEYWORD) {
            itemCount += parseOrdExpression();
        } else {
            TokenType nextNextKind = peekNextKind(1);
            // Is this correct?
            if (nextNextKind == TokenType.OPEN_BRACKET_TOKEN) {
                itemCount += parseCallExpression();
            } else {
                itemCount += parseIdentifier(TokenType.IDENTIFIER);
            }
        }
        return itemCount;
    }

    private int parseNegativeExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.MINUS_TOKEN);
        itemCount += parsePrimary();
        return buildTree(NodeType.NEGATIVE_EXPRESSION, itemCount);
    }

    private int parseNotExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.NOT_KEYWORD);
        itemCount += parsePrimary();
        return buildTree(NodeType.NOT_EXPRESSION, itemCount);
    }

    private int parseEofExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.EOF_KEYWORD);
        return buildTree(NodeType.EOF_EXPRESSION, itemCount);
    }

    private int parseCallExpression() {
        int itemCount = 0;
        itemCount += parseName();
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseExpression, TokenType.COMMA_TOKEN);
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeType.CALL_EXPRESSION, itemCount);
    }

    private int parseSuccExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.SUCC_KEYWORD);
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeType.SUCC_EXPRESSION, itemCount);
    }

    private int parsePredExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.PRED_KEYWORD);
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeType.PRED_EXPRESSION, itemCount);
    }

    private int parseChrExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.CHR_KEYWORD);
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeType.CHR_EXPRESSION, itemCount);
    }

    private int parseOrdExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenType.ORD_KEYWORD);
        itemCount += parseToken(TokenType.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(TokenType.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeType.ORD_EXPRESSION, itemCount);
    }

    private int parseName() {
        parseIdentifier(TokenType.IDENTIFIER);
        return 1;
    }

}
