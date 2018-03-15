
import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import sun.awt.image.ImageWatched;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

public class MyVisitor extends HelloBaseVisitor<List<Context>> {

    Stack<List<Context>> stack=new Stack<List<Context>>();;
    Stack<List<Context>> multiple_res=new Stack<List<Context>>();;
    Map<String,List<Context>>  map=new HashMap<>();;
    List<Context> condition_list=new LinkedList<>();

    @Override
    public List<Context> visitApSingleSlash(HelloParser.ApSingleSlashContext ctx) {
        Document doc=XMLDocument.CreateRoot(ctx.Letter().getText());
        List<Context> temp=new LinkedList<Context>();
        temp.add(new Context(doc));
        stack.push(temp);
        return visit(ctx.rp());
    }

    @Override
    public List<Context> visitApDoubleSlash(HelloParser.ApDoubleSlashContext ctx) {
        Document doc=XMLDocument.CreateRoot(ctx.Letter().getText());
        List<Context> res=new LinkedList<Context>();
        Context context=new Context(doc);
        res.add(context);
        res.addAll(context.getdecendants());
        List unique_res=unique(res);
        stack.push(unique_res);
        return visit(ctx.rp());
    }

    @Override
    public List<Context> visitRpTagName(HelloParser.RpTagNameContext ctx) {
        List<Context> res=new LinkedList<Context>();
        List<Context> peek=stack.pop();
        for(int i=0;i<peek.size();i++){
            res.addAll(peek.get(i).gettagname(ctx.Letter().getText()));
        }
        stack.push(res);
        return res;
    }


    @Override
    public List<Context> visitRpAllChildren(HelloParser.RpAllChildrenContext ctx) {
        List<Context> res=new LinkedList<Context>();
        List<Context> peek=stack.pop();
        for(int i=0;i<peek.size();i++){
            res.addAll(peek.get(i).getchildren());
        }
        stack.push(res);
        return res;
    }

    @Override
    public List<Context> visitRpCurrent(HelloParser.RpCurrentContext ctx) {
        return stack.peek();
    }

    @Override
    public List<Context> visitRpParent(HelloParser.RpParentContext ctx) {
        List<Context> res=new LinkedList<Context>();
        List<Context> peek=stack.pop();
        for(int i=0;i<peek.size();i++){
            Context value=peek.get(i).getparent();
            if(value!=null) res.add(value);
        }
        List unique_res=unique(res);
        stack.push(unique_res);
        return unique_res;
    }

    public List<Context> unique(List<Context> input){
        Set<Node> set=new HashSet<>();
        List<Context> out=new LinkedList<>();
        for(int i=0;i<input.size();i++){
            Node node_test=input.get(i).node;
            if(set.contains(node_test)) continue;
            set.add(node_test);
            out.add(input.get(i));
        }
        return out;
    }

    @Override
    public List<Context> visitRpGetTextNode(HelloParser.RpGetTextNodeContext ctx) {
        List<Context> res=new LinkedList<Context>();
        List<Context> peek=stack.pop();
        for(int i=0;i<peek.size();i++){
            res.addAll(peek.get(i).gettextnode());
        }
        stack.push(res);
        return res;
    }

    @Override
    public List<Context> visitRpGetAttribute(HelloParser.RpGetAttributeContext ctx) {
        List<Context> res=new LinkedList<Context>();
        List<Context> peek=stack.pop();
        for(int i=0;i<peek.size();i++){
            if(peek.get(i).node.getAttributes()!=null){
                Context value=peek.get(i).getatt(ctx.Letter().getText());
                if(value!=null) res.add(value);
            }
        }
        stack.push(res);
        return res;
    }

    @Override
    public List<Context> visitRpAllAttribute(HelloParser.RpAllAttributeContext ctx) {
        List<Context> res=new LinkedList<Context>();
        List<Context> peek=stack.pop();
        for(int i=0;i<peek.size();i++){
            if(peek.get(i).node.getAttributes()!=null){
                List<Context> value=peek.get(i).getallatt();
                res.addAll(value);
            }
        }
        stack.push(res);
        return res;
    }

    @Override
    public List<Context> visitRpParenthesis(HelloParser.RpParenthesisContext ctx) {
        return visit(ctx.rp());
    }

    @Override
    public List<Context> visitRpSingleSlash(HelloParser.RpSingleSlashContext ctx) {
        visit(ctx.rp(0));
        return visit(ctx.rp(1));
    }

    @Override
    public List<Context> visitRpDoubleSlash(HelloParser.RpDoubleSlashContext ctx) {
        visit(ctx.rp(0));
        List<Context> rp1_result=stack.pop();
        List<Context> res=new LinkedList<>();
        for(int i=0;i<rp1_result.size();i++){
            res.addAll(rp1_result.get(i).getdecendants());
        }
        List unique_res=unique(res);
        stack.push(unique_res);
        return visit(ctx.rp(1));
    }


    @Override
    public List<Context> visitRpQuote(HelloParser.RpQuoteContext ctx) {
        List<Context> store_context=stack.peek();
        List<Context> concat=new LinkedList<>();
        List<Context> rp1_result=visit(ctx.rp(0));
        stack.pop();
        stack.push(store_context);
        List<Context> rp2_result=visit(ctx.rp(1));
        stack.pop();
        concat.addAll(rp1_result);
        concat.addAll(rp2_result);
        List unique_res=unique(concat);  //make two combined list unique
        stack.push(unique_res);
        return unique_res;
    }

    @Override
    public List<Context> visitRpFilter(HelloParser.RpFilterContext ctx) {
        visit(ctx.rp());
        return visit(ctx.f());
    }

    @Override
    public List<Context> visitFilterRp(HelloParser.FilterRpContext ctx) {
        List<Context> test_context=stack.pop();
        List<Context> filter_res=new LinkedList<>();
        for(int i=0;i<test_context.size();i++){
            List<Context> each_test=new LinkedList<>();
            each_test.add(test_context.get(i));
            stack.push(each_test);
            List<Context> each_result=visit(ctx.rp());
            stack.pop();
            if(each_result.size()!=0) filter_res.add(test_context.get(i));  //check if the node satisfy the filter condition
        }
        stack.push(filter_res);
        return filter_res;
    }

    @Override
    public List<Context> visitFilterindex(HelloParser.FilterindexContext ctx) {
        List<Context> test_context=stack.pop();
        List<Context> filter_res=new LinkedList<>();
        Integer pos=Integer.parseInt(ctx.NUM().getText());  //get that specific index from the node list
        if(pos>=test_context.size()) return filter_res;
        filter_res.add(test_context.get(pos));
        stack.push(filter_res);
        return filter_res;
    }

    @Override
    public List<Context> visitFilterAttribute(HelloParser.FilterAttributeContext ctx) {
        List<Context> test_context=stack.pop();
        List<Context> filter_res=new LinkedList<>();
        for(int i=0;i<test_context.size();i++){
            List<Context> each_test=new LinkedList<>();
            each_test.add(test_context.get(i));
            stack.push(each_test);
            List<Context> each_result=visit(ctx.rp());
            stack.pop();
            if(each_result.size()==0) continue;  // handle no such attribute for that node
            if(check_attribute_node(each_result,ctx.Letter().getText())) filter_res.add(test_context.get(i));
        }
        stack.push(filter_res);
        return filter_res;
    }

    //check if a element node has a attribute node with specific value

    public boolean check_attribute_node(List<Context> list, String attributevalue){
        for(int i=0;i<list.size();i++){
            Node each_Attribute_node=list.get(i).node;
            if(each_Attribute_node.getNodeValue().equals(attributevalue)) return true;
        }
        return false;
    }

    @Override
    public List<Context> visitFilterEq(HelloParser.FilterEqContext ctx) {
        List<Context> test_context=stack.pop();
        List<Context> filter_res=new LinkedList<>();
        for(int i=0;i<test_context.size();i++){
            List<Context> each_test=new LinkedList<>();
            each_test.add(test_context.get(i));
            stack.push(each_test);
            List<Context> each_result_1=visit(ctx.rp(0));  //get the first filter list
            stack.pop();
            stack.push(each_test);
            List<Context> each_result_2=visit(ctx.rp(1));  //get the second filter list
            stack.pop();
            if(check_if_satisfy(each_result_1,each_result_2)) filter_res.add(test_context.get(i));  //check if there exists two structure identical nodes in two list
        }
        stack.push(filter_res);
        return filter_res;
    }

    public boolean check_if_satisfy(List<Context> l1, List<Context> l2){
        for(int i=0;i<l1.size();i++){
            for(int j=0;j<l2.size();j++){
                Node root1=l1.get(i).node;
                Node root2=l2.get(j).node;
                if(is_equal(root1,root2)) return true;
            }
        }
        return false;  //what if two lists are empty?
    }

    // check if two nodes are structure identical

    public boolean is_equal(Node root1,Node root2){
        if(root1.getNodeType()!= root2.getNodeType()||root1.getChildNodes().getLength()!=root2.getChildNodes().getLength()) return false;
        if(root1.getNodeType()== Node.TEXT_NODE){
            if(!root1.getTextContent().equals( root2.getTextContent())) return false;
        }
        if(root1.getNodeType()== Node.ELEMENT_NODE){
            if(!root1.getNodeName().equals(root2.getNodeName())) return false;
        }
        for(int i=0;i<root1.getChildNodes().getLength();i++){
            Node test1=root1.getChildNodes().item(i);
            Node test2=root2.getChildNodes().item(i);
            if(!is_equal(test1,test2)) return false;
        }
        return true;
    }

    @Override
    public List<Context> visitFilterIs(HelloParser.FilterIsContext ctx) {
        List<Context> test_context=stack.pop();
        List<Context> filter_res=new LinkedList<>();
        for(int i=0;i<test_context.size();i++){
            List<Context> each_test=new LinkedList<>();
            each_test.add(test_context.get(i));
            stack.push(each_test);
            List<Context> each_result_1=visit(ctx.rp(0));
            stack.pop();
            stack.push(each_test);
            List<Context> each_result_2=visit(ctx.rp(1));
            stack.pop();
            if(check_if_identical(each_result_1,each_result_2)) filter_res.add(test_context.get(i));  //check if there exists a node in both list
        }
        stack.push(filter_res);
        return filter_res;
    }

    //check if there exists a node in both list

    public boolean check_if_identical(List<Context> l1, List<Context> l2){
        for(int i=0;i<l1.size();i++){
            for(int j=0;j<l2.size();j++){
                Node root1=l1.get(i).node;
                Node root2=l2.get(i).node;
                if(root1==root2) return true;
            }
        }
        return false;
    }

    @Override
    public List<Context> visitFilterQuote(HelloParser.FilterQuoteContext ctx) {
        return visit(ctx.f());
    }

    @Override
    public List<Context> visitFilterAnd(HelloParser.FilterAndContext ctx) {
        List<Context> test_context=stack.pop();
        List<Context> filter_res=new LinkedList<>();
        for(int i=0;i<test_context.size();i++){
            List<Context> each_test=new LinkedList<>();
            each_test.add(test_context.get(i));
            stack.push(each_test);
            List<Context> each_result_1=visit(ctx.f(0));
            stack.pop();
            stack.push(each_test);
            List<Context> each_result_2=visit(ctx.f(1));
            stack.pop();
            if(each_result_1.size()!=0&&each_result_2.size()!=0) filter_res.add(test_context.get(i));  //check if the node satisfy two filter conditions
        }
        stack.push(filter_res);
        return filter_res;
    }

    @Override
    public List<Context> visitFilterOr(HelloParser.FilterOrContext ctx) {
        List<Context> test_context=stack.pop();
        List<Context> filter_res=new LinkedList<>();
        for(int i=0;i<test_context.size();i++){
            List<Context> each_test=new LinkedList<>();
            each_test.add(test_context.get(i));
            stack.push(each_test);
            List<Context> each_result_1=visit(ctx.f(0));
            stack.pop();
            stack.push(each_test);
            List<Context> each_result_2=visit(ctx.f(1));
            stack.pop();
            if(each_result_1.size()!=0||each_result_2.size()!=0) filter_res.add(test_context.get(i));  //check is the node satisfy one of filter conditions
        }
        stack.push(filter_res);
        return filter_res;
    }

    @Override
    public List<Context> visitFilterNot(HelloParser.FilterNotContext ctx) {
        List<Context> test_context=stack.pop();
        List<Context> filter_res=new LinkedList<>();
        for(int i=0;i<test_context.size();i++){
            List<Context> each_test=new LinkedList<>();
            each_test.add(test_context.get(i));
            stack.push(each_test);
            List<Context> each_result=visit(ctx.f());
            if(each_result.size()==0) filter_res.add(test_context.get(i));  //check if the node unsatisfy the filter condition. if it was, then we want it.
        }
        stack.push(filter_res);
        return filter_res;
    }

    @Override
    public List<Context> visitXqvar(HelloParser.XqvarContext ctx) {
        return map.get(ctx.Letter().getText());  //get variable from the map(handle if it is not in the map)
    }

    @Override
    public List<Context> visitXqstr(HelloParser.XqstrContext ctx) {
        List<Context> res=new LinkedList<>();
        DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc=null;
        try {
            builder = factory.newDocumentBuilder();
            doc=(Document) builder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        res.add(new Context(doc,ctx.StringSentence().getText(),0));  //create a text node
        return res;
    }

    @Override
    public List<Context> visitXqap(HelloParser.XqapContext ctx) {
        return visit(ctx.ap());
    }

    @Override
    public List<Context> visitXqparenthesis(HelloParser.XqparenthesisContext ctx) {
        return visit(ctx.xq());
    }

    @Override
    public List<Context> visitXqquote(HelloParser.XqquoteContext ctx) {
        List<Context> res=new LinkedList<>();
        List<Context> res_1=visit(ctx.xq(0));
        if(!stack.isEmpty()) stack.pop();
        List<Context> res_2=visit(ctx.xq(1));
        if(!stack.isEmpty()) stack.pop();
        res.addAll(res_1);
        res.addAll(res_2);  //combine two xquery result list
        List unique_res=unique(res);  //make two combined list unique
        return unique_res;
    }

    @Override
    public List<Context> visitXqslash(HelloParser.XqslashContext ctx) {
        List<Context> res_1=visit(ctx.xq());
        if(!stack.isEmpty()) stack.pop();
        stack.push(res_1);
        List<Context> res_2=visit(ctx.rp());
        if(!stack.isEmpty()) stack.pop();
        return res_2;
    }

    @Override
    public List<Context> visitXqdoubleslash(HelloParser.XqdoubleslashContext ctx) {
        List<Context> res_1=visit(ctx.xq());  //get the result from xquery
        if(!stack.isEmpty()) stack.pop();  //maybe the result is on top of the stack, and we pop it, in the case that we run xpath next time(keep it only containing one element)
        List<Context> get_res_1_decendants=new LinkedList<>();
        for(int i=0;i<res_1.size();i++){
            get_res_1_decendants.addAll(res_1.get(i).getdecendants());  //get all decendants of results from the xquery
        }
        List unique_res=unique(get_res_1_decendants);
        stack.push(unique_res);  //push it into stack
        List<Context> res_2=visit(ctx.rp());  //run rp xpath
        if(!stack.isEmpty()) stack.pop();  //keep the stack empty after we finish the xquery
        return res_2;
    }

    @Override
    public List<Context> visitXqtag(HelloParser.XqtagContext ctx) {
        List<Context> res=new LinkedList<>();
        List<Context> xq_res=visit(ctx.xq());  //get children from the result of xquery
        DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc=null;
        try {
            builder = factory.newDocumentBuilder();
            doc=(Document) builder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Context element_node=new Context(doc,ctx.Letter(0).getText(),1);  //create a element node
        for(int i=0;i<xq_res.size();i++){
            Node append_node=doc.importNode(xq_res.get(i).node,true);
            element_node.node.appendChild(append_node);  //append deep copy of the children from the result of xquery
        }
        res.add(element_node);  //return a list with only one elememt node
        return res;
    }

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

    @Override
    public List<Context> visitXqflower(HelloParser.XqflowerContext ctx) {
        List<Context> new_return=new LinkedList<>();
        multiple_res.push(new_return);
        HelloParser.XqforclauseContext for_ctx=(HelloParser.XqforclauseContext)ctx.forclause();
        List<TerminalNode> variable_list = for_ctx.Letter();
        List<HelloParser.XqContext> xq_list=for_ctx.xq();
        HelloParser.XqletclauseContext let_ctx=(HelloParser.XqletclauseContext)ctx.letclause();  //get let context(might be null)
        HelloParser.XqwhereclauseContext where_ctx=(HelloParser.XqwhereclauseContext)ctx.whereclause();  //get where context(might be null)
        HelloParser.XqreturnclauseContext return_ctx=(HelloParser.XqreturnclauseContext)ctx.returnclause();  //get return context
        helper(0,variable_list,xq_list,let_ctx,where_ctx,return_ctx); //run flower expression, store result in store_return_res
        List<Context> unique_res=unique(multiple_res.pop());  //remove duplicates, clear it, for next return statement
//        System.out.println(unique_res.size());
//        for(int i=0;i<unique_res.size();i++) {
//            printhelper(unique_res.get(i).node);
//        }
        return unique_res;
    }

    public void helper(int k, List<TerminalNode> variable_list, List<HelloParser.XqContext> xq_list, HelloParser.XqletclauseContext let_ctx,  HelloParser.XqwhereclauseContext where_ctx, HelloParser.XqreturnclauseContext return_ctx){
        if(k>=variable_list.size()){
            if(let_ctx!=null) putLetVariable(let_ctx);  //run let clause
            if(where_ctx!=null){
                if(visit(where_ctx)==null) return;  //run where clause, if the result is null, do not run return clause
            }
            visit(return_ctx);  //run return clause, add result to store_return_res
            if(let_ctx!=null) removeLetVariable(let_ctx);  //remove let clause variables
            return;
        }
        String var_name=variable_list.get(k).getText();
        List<Context> cur_xq_res=visit(xq_list.get(k));
        for(int i=0;i<cur_xq_res.size();i++){
            List<Context> temp=new LinkedList<>();
            temp.add(cur_xq_res.get(i));
            map.put(var_name,temp);
            helper(k+1,variable_list,xq_list,let_ctx,where_ctx,return_ctx);
            map.remove(var_name);
        }
    }

    public void putLetVariable(HelloParser.XqletclauseContext let_ctx){  //put variables into map
        List<TerminalNode> let_variable_list = let_ctx.Letter();
        List<HelloParser.XqContext> let_xq_list=let_ctx.xq();
        for(int i=0;i<let_variable_list.size();i++){
            String var_name=let_variable_list.get(i).getText();
            List<Context> let_cur_xq_res=visit(let_xq_list.get(i));
            map.put(var_name,let_cur_xq_res);
        }
    }

    public void removeLetVariable(HelloParser.XqletclauseContext let_ctx){  //remove variables from map
        List<TerminalNode> let_variable_list = let_ctx.Letter();
        List<HelloParser.XqContext> let_xq_list=let_ctx.xq();
        for(int i=0;i<let_variable_list.size();i++){
            String var_name=let_variable_list.get(i).getText();
            map.remove(var_name);
        }
    }

    @Override
    public List<Context> visitXqlet(HelloParser.XqletContext ctx) {
        putLetVariable((HelloParser.XqletclauseContext)ctx.letclause());  //put variables into map
        List<Context> res=visit(ctx.xq());
        removeLetVariable((HelloParser.XqletclauseContext)ctx.letclause());  //remove variables from map
        return res;
    }

    public static String NodetoSting(Node node, int k){
        if(node.getChildNodes().getLength()==0){
            return node.getTextContent();
        }
        String temp="";
        if(k!=0) temp+="<"+node.getNodeName()+">";
        for(int i=0;i<node.getChildNodes().getLength();i++){
            temp+=NodetoSting(node.getChildNodes().item(i), k+1);
        }
        if(k!=0) temp+="</"+node.getNodeName()+">";
        return temp;
    }

    @Override
    public List<Context> visitXqjoin(HelloParser.XqjoinContext ctx) {
        List<Context> xq_result_1=visit(ctx.xq(0));  //first xquery result
        List<Context> xq_result_2=visit(ctx.xq(1));  //second xquery result
        List<TerminalNode> con_list_left=ctx.taglist().get(0).Letter();  //first condition list, correspond to second xquery result
        List<TerminalNode> con_list_right=ctx.taglist().get(1).Letter();  //second condition list, correspond to first xquery result
        Map<String, Context> find_corres_tag=new HashMap<>();
        for(int i=0;i<xq_result_2.size();i++){
            Context each_tuple=xq_result_2.get(i);  //get each tuple
            String key="";
            for(int j=0;j<each_tuple.getchildren().size();j++){
                Context each_tag=each_tuple.getchildren().get(j);  //get each tag
                for(int k=0;k<con_list_left.size();k++){
                    if(each_tag.node.getNodeName().equals(con_list_left.get(k).getText())){
                        key+=NodetoSting(each_tag.node,0)+",";  //convert tagnode to string
                    }
                }
            }

            find_corres_tag.put(key,each_tuple);  //store string-each_tuple pair
        }
        List<Context> res=new LinkedList<>();
        for(int i=0;i<xq_result_1.size();i++){
            Context each_tuple=xq_result_1.get(i);
            String key="";
            for(int j=0;j<each_tuple.getchildren().size();j++){
                Context each_tag=each_tuple.getchildren().get(j);
                for(int k=0;k<con_list_right.size();k++){
                    if(each_tag.node.getNodeName().equals(con_list_right.get(k).getText())){
                        key+=NodetoSting(each_tag.node,0)+",";
                    }
                }
            }

            if(find_corres_tag.containsKey(key)){  //hash check
                Context tuple_2=find_corres_tag.get(key);
                List<Node> combine=new LinkedList<>();
                for(int n=0;n<tuple_2.getchildren().size();n++) combine.add(tuple_2.getchildren().get(n).node);
                for(int n=0;n<each_tuple.getchildren().size();n++) combine.add(each_tuple.getchildren().get(n).node);
                DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
                DocumentBuilder builder;
                Document doc=null;
                try {
                    builder = factory.newDocumentBuilder();
                    doc=(Document) builder.newDocument();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }
                res.add(new Context(doc,combine));  //add new tuple to result list
            }
        }
        return res;
    }

    @Override
    public List<Context> visitXqwhereclause(HelloParser.XqwhereclauseContext ctx) {
        return visit(ctx.cond());
    }

    @Override
    public List<Context> visitXqreturnclause(HelloParser.XqreturnclauseContext ctx) {
        List<Context> res=visit(ctx.xq());
        List<Context> cumulate=multiple_res.pop();
        cumulate.addAll(res);
        multiple_res.push(cumulate);
        return res;
    }

    @Override
    public List<Context> visitCondeq(HelloParser.CondeqContext ctx) {
        List<Context> xq_result_1=visit(ctx.xq(0));
        List<Context> xq_result_2=visit(ctx.xq(1));
        if(check_if_satisfy(xq_result_1,xq_result_2)) return condition_list;  //check if there exists two structural identical node in each list
        else return null;
    }

    @Override
    public List<Context> visitCondis(HelloParser.CondisContext ctx) {
        List<Context> xq_result_1=visit(ctx.xq(0));
        List<Context> xq_result_2=visit(ctx.xq(1));
        if(check_if_identical(xq_result_1,xq_result_2)) return condition_list;  //check if there exists two identical node in each list
        else return null;
    }

    @Override
    public List<Context> visitCondempty(HelloParser.CondemptyContext ctx) {
        List<Context> xq_result=visit(ctx.xq());
        if(xq_result.size()==0) return condition_list;
        else return null;
    }

    @Override
    public List<Context> visitCondsatisfy(HelloParser.CondsatisfyContext ctx) {
        List<TerminalNode> variable_list = ctx.Letter();  //get variable list
        List<HelloParser.XqContext> xq_list=ctx.xq();  //get xquery list
        HelloParser.CondContext con_conctx=ctx.cond();  //get conditional context
        if(check_cond_satisfy(0,variable_list,xq_list,con_conctx)) return condition_list;  //check if there exists some variables satisfy the condition
        else return null;
    }

    public boolean check_cond_satisfy(int k, List<TerminalNode> variable_list, List<HelloParser.XqContext> xq_list, HelloParser.CondContext con_conctx){
        if(k>=variable_list.size()){
            List<Context> res=visit(con_conctx);  //check whether the condition satisfy or not under current variables context
            if(res==null) return false;
            else return true;
        }
        String var_name=variable_list.get(k).getText();
        List<Context> cur_xq_res=visit(xq_list.get(k));
        for(int i=0;i<cur_xq_res.size();i++){
            List<Context> temp=new LinkedList<>();
            temp.add(cur_xq_res.get(i));
            map.put(var_name,temp);  //put the variable(binding with a Context) in the map
            if(check_cond_satisfy(k+1,variable_list,xq_list,con_conctx)) return true;  //pass the variable(binding with a Context) to the next clause
            map.remove(var_name);  //remove it after passing
        }
        return false;
    }

    @Override
    public List<Context> visitCondparenthesis(HelloParser.CondparenthesisContext ctx) {
        return visit(ctx.cond());
    }

    @Override
    public List<Context> visitCondand(HelloParser.CondandContext ctx) {
        List<Context> xq_result_1=visit(ctx.cond(0));
        List<Context> xq_result_2=visit(ctx.cond(1));
        if(xq_result_1!=null&&xq_result_2!=null) return condition_list;
        else return null;
    }

    @Override
    public List<Context> visitCondor(HelloParser.CondorContext ctx) {
        List<Context> xq_result_1=visit(ctx.cond(0));
        List<Context> xq_result_2=visit(ctx.cond(1));
        if(xq_result_1!=null||xq_result_2!=null) return condition_list;
        else return null;
    }

    @Override
    public List<Context> visitCondnot(HelloParser.CondnotContext ctx) {
        List<Context> xq_result=visit(ctx.cond());
        if(xq_result==null) return condition_list;
        else return null;
    }
}
