import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Service {
    public static String performRequest(String urlAddress) throws IOException {
        String result = "";
        try {
            URL url = new URL(urlAddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String temp = "";
                for (; (temp = br.readLine()) != null; ) {
                    result += temp;
                    result += System.lineSeparator();
                }
            }
        } catch (IOException e) {
            throw e;
        }
        return result;
    }

    public static Currency getCurrencyFromNode(Element currencyElement) {
        if (!currencyElement.getTagName().equals("currency")) {
            return null;
        }

        String r030 = currencyElement.getElementsByTagName("r030").item(0).getTextContent();
        String txt = currencyElement.getElementsByTagName("txt").item(0).getTextContent();
        double rate = Double.valueOf(currencyElement.getElementsByTagName("rate").item(0).getTextContent());
        String cc = currencyElement.getElementsByTagName("cc").item(0).getTextContent();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yyyy");
        String dateText = currencyElement.getElementsByTagName("exchangedate").item(0).getTextContent();
        Date exchangeDate = new Date();
        try {
            exchangeDate = sdf.parse(dateText);
        } catch (ParseException e) {
            System.out.println("Error load exchangedate");
        }

        Currency currency = new Currency(r030, txt, rate, cc, exchangeDate);
        return currency;
    }

    public static Exchange parseXML(String request) {
        Exchange exchange = new Exchange();
        String result = "";
        try {
            result = Service.performRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(result)));
            Element root = document.getDocumentElement();
            NodeList currencies = root.getChildNodes();

            for (int i = 0; i < currencies.getLength(); i++) {
                Node node = currencies.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    Currency currency = getCurrencyFromNode(element);
                    if (currency != null) {
                        exchange.addCurrency(currency);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exchange;
    }

    private static Element elementFromCurrency(Currency currency, Document document) {
        Element currencyElement = document.createElement("currency");

        Element r030 = document.createElement("r030");
        r030.setTextContent(currency.getR030());

        Element txt = document.createElement("txt");
        txt.setTextContent(currency.getTxt());

        Element rate = document.createElement("txt");
        rate.setTextContent(String.valueOf(currency.getRate()));

        Element cc = document.createElement("cc");
        cc.setTextContent(currency.getCc());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yyyy");
        Element exchangeDate = document.createElement("exchangedate");
        exchangeDate.setTextContent(sdf.format(currency.getExchangeDate()));

        currencyElement.appendChild(r030);
        currencyElement.appendChild(txt);
        currencyElement.appendChild(rate);
        currencyElement.appendChild(cc);
        currencyElement.appendChild(exchangeDate);

        return currencyElement;
    }

    public static void saveToXML(Exchange exchange, String fileName) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("exchange");
            document.appendChild(root);

            for (Currency currency : exchange.getCurrencies()) {
                Element currencyElement = elementFromCurrency(currency, document);
                root.appendChild(currencyElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult streamResult = new StreamResult(fileName);
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(source, streamResult);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }
}