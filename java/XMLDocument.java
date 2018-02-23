
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class XMLDocument {

    public static Document CreateRoot(String filename) {
        DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc=null;
        try {
            builder = factory.newDocumentBuilder();
            doc=(Document) builder.parse(filename);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return doc;
    }

    public static void main(String[] args) {
        Document doc=CreateRoot("book.xml");
        System.out.println(doc.getElementsByTagName("title").item(0).getTextContent());
    }
}
