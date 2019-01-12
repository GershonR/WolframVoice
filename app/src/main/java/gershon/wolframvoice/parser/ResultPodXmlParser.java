package gershon.wolframvoice.parser;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import gershon.wolframvoice.model.ResultPod;

public class ResultPodXmlParser {

    public static ArrayList<ResultPod> parseResultXml(String resultXml, String query) {

        ArrayList<ResultPod> resultPods = new ArrayList<>();
        int totalResultPods = 0;
        System.out.println(resultXml);

        try {
            Document document = loadXMLFromString(resultXml);
            document.getDocumentElement().normalize();

            Element rootElement = (Element) (document.getElementsByTagName("queryresult")).item(0);
            totalResultPods = Integer.parseInt(rootElement.getAttribute("numpods"));

            NodeList pods = rootElement.getElementsByTagName("pod");

            if(pods.getLength() == 0) {
                Element futureTopic = (Element) (document.getElementsByTagName("futuretopic").item(0));
                Element didYouMean = (Element) (document.getElementsByTagName("didyoumeans").item(0));
                Element assumption = (Element) (document.getElementsByTagName("assumption").item(0));
                Element error = (Element) (document.getElementsByTagName("error").item(0));
                if(futureTopic != null && StringUtils.isNotEmpty(futureTopic.getAttribute("topic"))) {
                    ResultPod resultPod = new ResultPod();
                    resultPod.setTitle(futureTopic.getAttribute("topic"));
                    resultPod.setDescription(futureTopic.getAttribute("msg"));
                    resultPods.add(resultPod);
                } else if (didYouMean != null && StringUtils.isNotEmpty(didYouMean.getTextContent())) {
                    ResultPod resultPod = new ResultPod();
                    resultPod.setTitle("Did you mean");
                    resultPod.setDescription(didYouMean.getTextContent());
                    resultPods.add(resultPod);
                } else if (error != null) {
                    ResultPod resultPod = new ResultPod();
                    resultPod.setTitle("Error");
                    resultPod.setDescription("Wolfram Alpha could not process that query");
                    resultPods.add(resultPod);
                } else if (assumption != null) {

                    NodeList values = assumption.getElementsByTagName("value");
                    ResultPod resultPod = new ResultPod();
                    resultPod.setTitle("Too many results");
                    resultPod.setDescription("Did you mean the following things?");
                    resultPods.add(resultPod);

                    for(int count = 0; count < values.getLength(); count++) {
                        Node value = values.item(count);
                        ResultPod valuePod = new ResultPod();
                        Element valueElement =  (Element) value;
                        valuePod.setTitle(valueElement.getAttribute("name"));
                        valuePod.setDescription(valueElement.getAttribute("desc"));
                        resultPods.add(valuePod);
                    }
                }

            }

            for (int count = 0; count < pods.getLength(); count++) {

                Node pod = pods.item(count);
                Element subPodElement = (Element) ((Element) pod).getElementsByTagName("subpod").item(0);
                Element descriptionElement = (Element) (subPodElement.getElementsByTagName("plaintext").item(0));

                ResultPod resultPod = new ResultPod();

                resultPod.setDefaultCard(false);

                // Set result pod title and description

                resultPod.setTitle(((Element) pod).getAttribute("title"));

                // Set description only if it is available

                if (StringUtils.isNotEmpty(descriptionElement.getTextContent()))
                    resultPod.setDescription(descriptionElement.getTextContent());

                // Set image source URL if any

                Element imageElement = (Element) (subPodElement.getElementsByTagName("img").item(0));
                if (imageElement != null && StringUtils.isNotEmpty(imageElement.getAttribute("src"))) {
                    resultPod.setImageSource(imageElement.getAttribute("src"));
                }

                // Do not add information if both image source and description is missing

                if (StringUtils.isNotEmpty(resultPod.getImageSource()) || StringUtils.isNotEmpty(resultPod.getDescription()))
                    resultPods.add(resultPod);
            }

            if (resultPods.size() > 0)
                return resultPods;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource source = new InputSource(new StringReader(xml));
        return builder.parse(source);
    }
}
