import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class Rewriter extends Serializer{

    boolean state=false;  //the state would change to true if it is in return clause
    Map<String, List<String>> root_map=new HashMap<>();  //store root-children pair
    Map<String,String> var_to_xq=new HashMap<>();  //store variable-xquery pair
    Map<String,String> store_parent=new HashMap<>();  //store variable-parent pair
    Map<String,String> con_map=new HashMap<>();  //store variable_before_condition-variable_after_condition pair
    Set<String> cur_varset=new HashSet<>();  //store the variable we have seen
    String default_string="";

    @Override
    public String visitXqforclause(HelloParser.XqforclauseContext ctx) {
        List<TerminalNode> var_list=ctx.Letter();  //get the variable list
        List<HelloParser.XqContext> xq_list=ctx.xq();  //get the xquery list
        for(int i=0;i<var_list.size();i++){
            String cur_var="$"+var_list.get(i).getText();  //get each variable
            String cur_xq_result=visit(xq_list.get(i));  //get each xquery result
            if(cur_xq_result.charAt(0)=='$'){  //if it starts with '$'
                String parent_var=cur_xq_result.substring(0,cur_xq_result.indexOf('/'));  //get parent variable
                store_parent.put(cur_var,parent_var);  //put variable-parent pair in the map
                while(store_parent.containsKey(parent_var)&&!store_parent.get(parent_var).equals("null")){
                    parent_var=store_parent.get(parent_var);
                }
                root_map.get(parent_var).add(cur_var);  //update root-children pair in the map
                var_to_xq.put(cur_var,cur_xq_result);  // variable-xquery pair in the map
            }
            else{  //if it starts with 'doc'
                store_parent.put(cur_var,"null");  //root-null
                root_map.put(cur_var,new LinkedList<>());
                root_map.get(cur_var).add(cur_var);
                var_to_xq.put(cur_var,cur_xq_result);
            }
        }
        return default_string;
    }

    @Override
    public String visitCondeq(HelloParser.CondeqContext ctx) {
        String xq_result_1=visit(ctx.xq(0));
        String xq_result_2=visit(ctx.xq(1));
        if(xq_result_1.charAt(0)=='$'&&xq_result_2.charAt(0)=='$'){
            con_map.put(xq_result_1.substring(0),xq_result_2.substring(0));
            con_map.put(xq_result_2.substring(0),xq_result_1.substring(0));
        }
        else if(xq_result_1.charAt(0)=='$'){
            con_map.put(xq_result_1.substring(0),xq_result_2);
        }
        else if(xq_result_2.charAt(0)=='$'){
            con_map.put(xq_result_2.substring(0),xq_result_1);
        }
        else{
            System.out.println("it won't work");
        }
        return default_string;
    }

    @Override
    public String visitXqwhereclause(HelloParser.XqwhereclauseContext ctx) {
        visit(ctx.cond());
        return default_string;
    }

    @Override
    public String visitXqvar(HelloParser.XqvarContext ctx) {
        if(state) return "$tuple/"+ctx.Letter().getText()+"/*";  //in the return clause
        else return super.visitXqvar(ctx);
    }

    @Override
    public String visitXqreturnclause(HelloParser.XqreturnclauseContext ctx) {
        return "return \n"+visit(ctx.xq());
    }

    @Override
    public String visitXqflower(HelloParser.XqflowerContext ctx) {
        visit(ctx.forclause());
        visit(ctx.whereclause());
        Iterator<Map.Entry<String, List<String>>> it = root_map.entrySet().iterator();
        String res="for $tuple in ";
        String left=helper(it.next());
        while(it.hasNext()){
            Map.Entry<String, List<String>> pair = it.next();
            String root= pair.getKey();
            List<String> children=pair.getValue();
            String right=helper(pair);
            List<String> join_list_left=new LinkedList<>();
            List<String> join_list_right=new LinkedList<>();
            for(int i=0;i<children.size();i++){
                String each_child=children.get(i);
                if(con_map.containsKey(each_child)){
                    String var_after_eq=con_map.get(each_child);
                    if(cur_varset.contains(var_after_eq)){
                        join_list_left.add(each_child.substring(1));
                        join_list_right.add(var_after_eq.substring(1));
                    }
                }
            }

            left="join( "+left+","+right+","+"["+String.join(",",join_list_left)+"],"+"["+String.join(",",join_list_right)+"]"+")";
        }
        res+=left+"\n";
        //return clause
        state=true;
        res+=visit(ctx.returnclause());
        return res;
    }

    public String helper(Map.Entry<String, List<String>> pair){
        String root= pair.getKey();
        List<String> children=pair.getValue();
        //store in the cur_varset
        for(int i=0;i<children.size();i++){
            cur_varset.add(children.get(i));
        }
        //for clause
        String res="for ";
        for(int i=0;i<children.size();i++){
            res+=children.get(i)+" in "+var_to_xq.get(children.get(i));
            if(i!=children.size()-1) res+=",";
            res+="\n";
        }
        //where clause
        LinkedList<String> where_clause=new LinkedList<>();
        for(int i=0;i<children.size();i++){
            String each_child=children.get(i);
            if(!con_map.containsKey(each_child)) continue;
            String var_after_eq=con_map.get(each_child);
            if(children.contains(var_after_eq)){  //variable eq variable
                where_clause.add("where "+each_child+" eq "+var_after_eq);
            }
            else if(var_after_eq.charAt(0)!='$'){  //variable eq stringsentence
                where_clause.add("where "+each_child+" eq "+var_after_eq);
            }
        }
        if(where_clause.size()!=0){
            res+=String.join(",",where_clause);
        }
        //return clause
        res+="return <tuple> {";
        for(int i=0;i<children.size();i++){
            String each_child=children.get(i);
            res+="<"+each_child.substring(1)+">{"+each_child+"}</"+each_child.substring(1)+">";
            if(i!=(children.size()-1)) res+=",";
        }
        res+="}</tuple>\n";
        return res;
    }





}
