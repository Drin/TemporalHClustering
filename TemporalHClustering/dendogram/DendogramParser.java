package TemporalHClustering.dendogram;

import TemporalHClustering.dataTypes.Isolate;
import TemporalHClustering.dendogram.DendogramTree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class provides an API for parsing XML files produced by
 * PyroMark pyrosequencers.
 * @author amontana
 * @version 1
 */
public class DendogramParser extends DefaultHandler {
   private Document mDom = null;

   /*
    * Tags for relevant data
    */
   private final String DENDOGRAM_ROOT = "IsolateClusters";
   private final String TAG_DENDOGRAM_TREE = "tree";
   private final String TAG_NODE = "node";
   private final String TAG_LEAF = "leaf";

   /**
    * Class constructor that will initialize Document object from File parameter.
    */
   public DendogramParser(String fileName) {
      File file = new File(fileName);

      try {
         mDom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
      }
      catch (SAXException e) {
         e.printStackTrace();
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      catch (ParserConfigurationException e) {
         System.err.println("Invalid parser configuration");
      }
      catch (Exception err) {
         System.err.println("unknown error: " + err);
      }

      mDom.getDocumentElement().normalize();
   }

   public DendogramTree parseDendogram(double threshold) {
      DendogramTree dendogram = new DendogramTree();

      if (mDom == null) {
         System.out.println("dom not built.");
         return null;
      }


      NodeList treeList = mDom.getElementsByTagName(TAG_DENDOGRAM_TREE);

      for (int treeNdx = 0; treeNdx < treeList.getLength(); treeNdx++) {
         dendogram.addIsolateList(parseNodeChildNode((Element) treeList.item(treeNdx), threshold));
      }

      return dendogram;
   }

   public List<Isolate> parseNodeChildNode(Element clusterElem, double threshold) {
      double correlation = Double.valueOf(clusterElem.getAttribute("correlation"));
      List<Isolate> isolateList = new ArrayList<Isolate>();

      if (correlation >= threshold) {
         NodeList isolateNodes = clusterElem.getElementsByTagName(TAG_LEAF);

         System.out.println("extracting leaves...");
         for (int isolateNdx = 0; isolateNdx < isolateNodes.getLength(); isolateNdx++) {
            Isolate isolate = getIsolate(isolateNodes.item(isolateNdx));
            if (isolate != null) {
               System.out.println("extracting isolate " + isolate + "...");
               isolateList.add(isolate);
            }
         }
         System.out.println("finished extracting leaves...");
      }

      else {
      //TODO this basically duplicates all of the work done above. it's terrible. maybe it's because I'm doing it recursively
      //and branches are overlapping?
         NodeList nodeList = clusterElem.getElementsByTagName(TAG_NODE);

         for (int nodeNdx = 0; nodeNdx < nodeList.getLength(); nodeNdx++) {
            List<Isolate> tmpIsolateList = parseNodeChildNode((Element) nodeList.item(nodeNdx), threshold);
            isolateList.addAll(tmpIsolateList);

            System.out.println("correlation for current node: " + correlation);
            printIsolateList(tmpIsolateList);
         }
      }
      return isolateList;
   }

   private Isolate getIsolate(Node isolateNode) {
      String isolateName = ((Element) isolateNode).getAttribute("data");

      System.err.println("parsing isolateName " + isolateName + "...");

      if (isolateName == null || isolateName.equals("")) {
         return null;
      }

      return new Isolate(isolateName);
   }

   private Node getNode(Node root, String name) {
      if (root.getNodeName().equals("name"))
         return root;
      
      else if (root != null) {
         NodeList children = root.getChildNodes();
         for (int nodeNdx = 0; nodeNdx < children.getLength(); nodeNdx++) {
            Node testNode = children.item(nodeNdx);
            
            if (testNode.getNodeName().equals(name)) {
               return testNode;
            }
         }
         
         for (int nodeNdx = 0; nodeNdx < children.getLength(); nodeNdx++) {
            Element testNode = (Element) getNode(children.item(nodeNdx), name);
            
            if (testNode.getNodeName().equals(name)) {
               return testNode;
            }
         }
      }
      
      return null;
   }
   
   private String getNodeValue(Node node) {
      if (node == null) {
         System.out.println("null Node Value");
      }
      if (node.getChildNodes() != null) {
         NodeList children = node.getChildNodes();
         
         for (int nodeNdx = 0; nodeNdx < children.getLength(); nodeNdx++) {
            if (children.item(nodeNdx) != null) {
               if (children.item(nodeNdx).getNodeValue() != null) {
                  return children.item(nodeNdx).getNodeValue();
               }
            }
         }
      }
      
      return null;
   }

   private void printIsolateList(List<Isolate> isolateList) {
      System.out.println("==== list of isolates in a cluster ====");

      for (Isolate isolate : isolateList) {
         System.out.println("\t" + isolate);
      }

   }

   public Document getDom() {
      return mDom;
   }
}
