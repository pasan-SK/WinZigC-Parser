package parser;

import diagnostics.DiagnosticCollector;
import diagnostics.Highlightable;
import lexer.AbstractLexer;
import lexer.tokens.Token;
import lexer.tokens.TokenType;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;
import parser.nodes.NodeType;

import java.util.Stack;
import java.util.function.Supplier;

public abstract class AbstractParser extends DiagnosticCollector {
    protected final Stack<Node> nodeStack;
    protected final TokenReader tokenReader;

    protected AbstractParser(AbstractLexer lexer) {
        this.nodeStack = new Stack<>();
        this.tokenReader = new TokenReader(lexer);
    }

    public abstract ASTNode parse();

    protected int parseIdentifier(TokenType tokenType) {
        Token nextToken = readToken(tokenType);
        nodeStack.push(new IdentifierNode(nextToken));
        return 1;
    }

    protected int parseToken(TokenType tokenType) {
        readToken(tokenType);
        return 0;
    }

    private Token readToken(TokenType kind) {
        TokenType tokenType = peekNextKind();
        if (tokenType == kind) {
            return tokenReader.read();
        } else {
            Token foundToken = tokenReader.peek(0);
            addError(foundToken, "Expected %s[%s] but found %s",
                    kind, kind.getValue(), foundToken);
            // Return the next token anyway.
            return foundToken;
        }
    }

    protected int parseList(Supplier<Integer> parser, TokenType separator) {
        int itemCount = 0;
        itemCount += parser.get();
        TokenType tokenType = peekNextKind();
        while (tokenType == separator) {
            itemCount += parseToken(separator);
            itemCount += parser.get();
            tokenType = peekNextKind();
        }
        return itemCount;
    }

    protected int buildTree(NodeType kind, int childrenCount) {
        Stack<Node> children = new Stack<>();
        for (int i = 0; i < childrenCount; i++) {
            children.push(nodeStack.pop());
        }
        ASTNode node = new ASTNode(kind);
        for (int i = 0; i < childrenCount; i++) {
            node.addChild(children.pop());
        }
        nodeStack.push(node);
        return 1;
    }

    protected TokenType peekNextKind() {
        return peekNextKind(0);
    }

    protected TokenType peekNextKind(int skip) {
        return tokenReader.peek(skip).getType();
    }

    @Override
    public String highlightedSegment(Highlightable highlightable) {
        return tokenReader.highlightedSegment(highlightable);
    }
}
