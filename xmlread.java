import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class xmlread {

    String FileName;
    public xmlread(String Filename){
        this.FileName=Filename;
    }

    public static Gpx Readxml(String name) {
        Gpx gpx = new Gpx();
        try {
            System.out.println("inside the xmlread");
            File xmlDoc =new File(name);
            DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuild = dbFact.newDocumentBuilder();
            Document doc = dBuild.parse(xmlDoc);
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

            String creator = doc.getDocumentElement().getAttribute("creator");
            System.out.println("GPX creator: " + creator);
            gpx.setCreator(creator);


            NodeList nList = doc.getElementsByTagName("wpt");

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                //System.out.println("Node name " + nNode.getNodeName() + " " + (i + 1));
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                   /* System.out.println("lat " + eElement.getAttribute("lat"));
                    System.out.println("lon " + eElement.getAttribute("lon"));
                    System.out.println("ele " + eElement.getElementsByTagName("ele").item(0).getTextContent());
                    System.out.println("time " + eElement.getElementsByTagName("time").item(0).getTextContent());*/



                    waypoint wp= new waypoint(Double.parseDouble(eElement.getAttribute("lat")),Double.parseDouble(eElement.getAttribute("lon")),Double.parseDouble(eElement.getElementsByTagName("ele").item(0).getTextContent()),eElement.getElementsByTagName("time").item(0).getTextContent(),gpx.getCreator());
                    gpx.add(wp);


                }
                // System.out.println("--------------------------------------");

            }

        } catch (Exception e) {

        }
        return gpx;

    }
}