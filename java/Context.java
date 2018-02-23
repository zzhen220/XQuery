
import org.w3c.dom.*;

import java.util.*;

public class Context {
    Node node;

    Context(Node a){
        node=a;
    }

    Context(Document doc, String str ,int flag){
        if(flag==0){
            node=doc.createTextNode(str.substring(1,str.length()-1));
        }
        else node=doc.createElement(str);
    }

    public List<Context> gettagname(String tagname){
        List<Context> res=new LinkedList<Context>();
        NodeList list=node.getChildNodes();
        for(int i=0;i<list.getLength();i++){
            if(list.item(i).getNodeType()== Node.ELEMENT_NODE&&list.item(i).getNodeName().equals(tagname)){  //only include elememnt node with that specific tagname
                res.add(new Context(list.item(i)));
            }
        }
        return res;
    }

    public List<Context> getdecendants(){
        return each_node_decendants(node);
    }

    public List<Context> each_node_decendants(Node node){
        List<Context> res=new LinkedList<>();
        res.add(new Context(node));
        if(node.getChildNodes().getLength()==0) return res;
        NodeList node_children=node.getChildNodes();
        for(int i=0;i<node_children.getLength();i++){
            if(node_children.item(i).getNodeType()!= Node.ELEMENT_NODE) continue;  //only include element decendants
            List<Context> node_children_result=each_node_decendants(node_children.item(i));
            res.addAll(node_children_result);
        }
        return res;
    }

    public Context getparent(){
        if(node.getNodeType()== Node.DOCUMENT_NODE) return null; //handle the document root node
        return new Context(node.getParentNode());
    }

    public List<Context> getchildren(){
        List<Context> res=new LinkedList<>();
        NodeList list=node.getChildNodes();
        for(int i=0;i<list.getLength();i++){
            if(list.item(i).getNodeType()!= Node.ELEMENT_NODE) continue;  //only include element node
            res.add(new Context(list.item(i)));
        }
        return res;
    }

    public Context getatt(String attributename){
        Node attr=node.getAttributes().getNamedItem(attributename);
        if(attr==null) return null;  //handle do not have that attribute name
        return new Context(attr);
    }

    public List<Context> getallatt(){
        List<Context> res=new LinkedList<>();
        NamedNodeMap attribute_map=node.getAttributes();  //get all attribute node of that node
        for(int i=0;i<attribute_map.getLength();i++){
            res.add(new Context(attribute_map.item(i)));
        }
        return res;
    }

    public List<Context> gettextnode(){
        List<Context> res=new LinkedList<>();
        NodeList list=node.getChildNodes();
        for(int i=0;i<list.getLength();i++){
            if(list.item(i).getNodeType()!= Node.TEXT_NODE) continue;  //only include text node
            res.add(new Context(list.item(i)));
        }
        return res;
    }

}
