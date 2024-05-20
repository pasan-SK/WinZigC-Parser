package parser.nodes;

import java.util.ArrayList;
import java.util.List;

public class ASTNode implements Node {
    private final NodeType nodeType;
    private final List<Node> children;

    public ASTNode(NodeType nodeType) {
        this.nodeType = nodeType;
        this.children = new ArrayList<>();
    }

    public void addChild(Node node) {
        this.children.add(node);
    }

    public List<Node> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", nodeType.getValue(), children.size());
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public Node getChild(int i) {
        return children.get(i);
    }

    public int getSize() {
        return children.size();
    }

    @Override
    public int getBeginOffset() {
        if (getSize() > 0) {
            return children.get(0).getBeginOffset();
        }
        return -1;
    }

    @Override
    public int getEndOffset() {
        if (getSize() > 0) {
            return children.get(getSize() - 1).getEndOffset();
        }
        return -1;
    }
}
