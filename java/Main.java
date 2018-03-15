
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

public class Main {

    String res="";

    public static void printhelper(Node element){
        if(element.getChildNodes().getLength()==0){
            System.out.print("     "+element.getTextContent()+" ");
            return;
        }
        System.out.println("<"+element.getNodeName()+">");
        for(int i=0;i<element.getChildNodes().getLength();i++){
            printhelper(element.getChildNodes().item(i));
        }
        System.out.println();
        System.out.println("</"+element.getNodeName()+">");
    }

    public static void main(String args[]) throws IOException {
        ANTLRFileStream inputStream = new ANTLRFileStream(args[0]);
        HelloLexer lexer = new HelloLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        HelloParser parser = new HelloParser(tokenStream);
        // begin parsing at ap() rule //
        ParseTree tree = parser.xq();

        List<Context> res=new MyVisitor().visit(tree);

//        String res=new Rewriter().visit(tree);
//        System.out.println(res);
//        System.out.println(NodetoSting(res.get(0).node));
//        System.out.println(res.get(0).node.getTextContent());
        for(int i=0;i<res.size();i++) {
            printhelper(res.get(i).node);
        }

    }
}
