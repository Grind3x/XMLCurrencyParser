public class Main {
    public static void main(String[] args) {
        String request = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?xml";
        Exchange exchange = Service.parseXML(request);
        System.out.println(exchange);

        Service.saveToXML(exchange, "Exchange.xml");


    }
}
