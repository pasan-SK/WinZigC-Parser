import lexer.CharReader;
import lexer.WinZigLexer;
import parser.WinZigParser;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.Callable;

public class winzigc implements Callable<Integer> {
    private final File file;
    public winzigc(File file) {
        this.file = file;
    }

    public static void printTree(ASTNode node, int depth) {
        System.out.println(". ".repeat(depth) + node.toString());
        for (Node child : node.getChildren()) {
            if (child instanceof ASTNode) {
                printTree((ASTNode) child, depth + 1);
            } else if (child instanceof IdentifierNode) {
                System.out.println(". ".repeat(depth + 1) + child);
                String value = ((IdentifierNode) child).getIdentifierValue();
                System.out.println(". ".repeat(depth + 2) + String.format("%s(0)", value));
            }
        }
    }


    @Override
    public Integer call() throws Exception {

        String sourceCode = Files.readString(file.toPath());
        CharReader charReader = CharReader.from(sourceCode);
        WinZigLexer lexer = new WinZigLexer(charReader);
        WinZigParser parser = new WinZigParser(lexer);
        ASTNode node = parser.parse();

        if (lexer.hasErrors()) {
            System.err.println("Lexer failed due to errors.");
            System.err.println("============================");
            System.err.println(lexer.collectErrors());
        } else if (parser.hasErrors()) {
            System.err.println("Parser failed due to errors.");
            System.err.println("============================");
            System.err.println(parser.collectErrors());
        } else {
            printTree(node, 0);
        }
        return 0;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("" +
                    "Winzigc - A winzig compiler\n" +
                    "How to run: java winzigc –ast winzig_test_programs/winzig_01");
            System.exit(1);
        }
        try {
            if (Objects.equals(args[0], "-ast")){
                String programPath = args[1];
                File file = new File(programPath);
                winzigc compiler = new winzigc(file);
                int exitCode = compiler.call();
                System.exit(exitCode);
            }else{
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
