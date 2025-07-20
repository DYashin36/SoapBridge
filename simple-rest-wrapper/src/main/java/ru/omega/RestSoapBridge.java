package ru.omega;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestSoapBridge extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        String soapRequest = requestBody.toString();
        System.out.println(">> Received Content-Type: " + req.getContentType());
        System.out.println(">> Received SOAP body:\n" + soapRequest);

        String soapResponse;
        if (soapRequest.contains("<bibl:GetShortRecordList")) {
            String searchExpression = extractSearchExpression(soapRequest);
            System.out.println(">> Parsed SearchExpression: " + searchExpression);
            soapResponse = generateShortRecordListResponse(searchExpression);
        } else if (soapRequest.contains("<imc:Codes>")) {
            String id = extractId(soapRequest);
            soapResponse = id.equals("55") ? generateSoapResponse(id) : generateErrorResponse();
        } else {
            soapResponse = generateErrorResponse();
        }

        resp.setContentType("application/soap+xml; charset=utf-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = resp.getWriter()) {
            out.print(soapResponse);
        }
    }

    private String extractId(String soap) {
        int start = soap.indexOf("<imc:Codes>") + "<imc:Codes>".length();
        int end = soap.indexOf("</imc:Codes>");
        if (start < end && start > 0 && end > 0) {
            return soap.substring(start, end).trim();
        }
        return "UNKNOWN";
    }

    private String extractSearchExpression(String soap) {
        int start = soap.indexOf("<bibl:SearchExpression>") + "<bibl:SearchExpression>".length();
        int end = soap.indexOf("</bibl:SearchExpression>");
        if (start < end && start > 0 && end > 0) {
            return soap.substring(start, end).trim();
        }
        return "";
    }

    private String generateShortRecordListResponse(String searchExpression) {
        String recordXml =
            "  <Records>\n" +
            "    <Title><Value>История России</Value></Title>\n" +
            "    <Subject>история</Subject>\n" +
            "    <Date>2020</Date>\n" +
            "    <Language>ru</Language>\n" +
            "    <Coverage><Value>Москва</Value></Coverage>\n" +
            "    <Creator>Иванов И.И.</Creator>\n" +
            "  </Records>\n";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
            "  <soap:Body>\n" +
            "    <GetShortRecordListResponse xmlns=\"http://www.omega-spb.ru/bibl\">\n" +
            "<m:BiblRecords>"+
            recordXml +
            "<m:BiblRecords>"+
            "    </GetShortRecordListResponse>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>";
    }

    // private String generateErrorResponse() {
    //     String exchangeXml =
    //         "<ExchangeXML xmlns=\"http://www.imc-dspace-new.org\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
    //         "    <Records>" +
    //         "    </Records>" +
    //         "</ExchangeXML>";

    //     return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    //         "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
    //         "  <soap:Body>\n" +
    //         "    <GetRecordsInfoResponse xmlns=\"http://imc.parus-s.ru\">\n" +
    //         exchangeXml +
    //         "    </GetRecordsInfoResponse>\n" +
    //         "  </soap:Body>\n" +
    //         "</soap:Envelope>";
    // }

    // private String generateSoapResponse(String id) {
    //     String exchangeXml =
    //         "<ExchangeXML xmlns=\"http://www.imc-dspace-new.org\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
    //         "    <Records>" +
    //         "        <Title><Qualifier/><Value>\"Африка внутри нас\"</Value></Title>" +
    //         "        <Date><Qualifier>Issued</Qualifier><Value>2006</Value></Date>" +
    //         "        <Identifier><Qualifier>Identifier</Qualifier><Value>RU/IS/BASE/234616783</Value></Identifier>" +
    //         "        <Relation><Qualifier>IsPartOf</Qualifier><Value>Обсерватория культуры</Value></Relation>" +
    //         "    </Records>" +
    //         "</ExchangeXML>";

    //     return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    //         "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
    //         "  <soap:Body>\n" +
    //         "    <GetRecordsInfoResponse xmlns=\"http://imc.parus-s.ru\">\n" +
    //         exchangeXml +
    //         "    </GetRecordsInfoResponse>\n" +
    //         "  </soap:Body>\n" +
    //         "</soap:Envelope>";
    // }

    private String generateBiblRecordsResponse()
    {
        String exchangeXml
                = "<ExchangeXML xmlns=\"http://www.imc-dspace-new.org\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "    <m:BiblRecords><Record>"
                +"<Contributor><Qualifier>Department</Qualifier><Value/></Contributor>"
                + "        <Contributor><Qualifier>Other</Qualifier><Value/></Contributor>"
                + "        <Contributor><Qualifier>Subdepartment</Qualifier><Value/></Contributor>"
                + "        <Date><Qualifier>Issued</Qualifier><Value>2006</Value></Date>"
                + "        <Date><Qualifier>Issued</Qualifier><Value/></Date>"
                + "        <Identifier><Qualifier>Citation</Qualifier><Value>Савицкая, Т.Е. Савицкая, Т.Е. \"Африка внутри нас\" : парадоксы современных процессов визуализации культуры // Обсерватория культуры. - 2006. - № 6. - Текст : непосредственный</Value></Identifier>"
                + "        <Identifier><Qualifier>Identifier</Qualifier><Value>RU/IS/BASE/234616783</Value></Identifier>"
                + "        <Identifier><Qualifier>ISBN</Qualifier><Value/></Identifier>"
                + "        <Identifier><Qualifier>ISMN</Qualifier><Value/></Identifier>"
                + "        <Identifier><Qualifier>ISSN</Qualifier><Value/></Identifier>"
                + "        <Identifier><Qualifier>Nps</Qualifier><Value/></Identifier>"
                + "        <Identifier><Qualifier>Orcid</Qualifier><Value/></Identifier>"
                + "        <Description><Qualifier>Abstract</Qualifier><Value/></Description>"
                + "        <Description><Qualifier>FirstPage</Qualifier><Value>30</Value></Description>"
                + "        <Description><Qualifier>LastPage</Qualifier><Value>35</Value></Description>"
                + "        <Format><Qualifier>Extent</Qualifier><Value/></Format>"
                + "        <Format><Qualifier>Mimetype</Qualifier><Value>Text</Value></Format>"
                + "        <Language><Qualifier>ISO</Qualifier><Value/></Language>"
                + "        <Publisher><Qualifier/><Value/></Publisher>"
                + "        <Relation><Qualifier>IsPartOf</Qualifier><Value>Обсерватория культуры</Value></Relation>"
                + "        <Rights><Qualifier/><Value/></Rights>"
                + "        <Rights><Qualifier>License</Qualifier><Value/></Rights>"
                + "        <Rights><Qualifier>Url</Qualifier><Value/></Rights>"
                + "        <Subject><Qualifier>RuBBK</Qualifier><Value/></Subject>"
                + "        <Subject><Qualifier>RuGASNTI</Qualifier><Value/></Subject>"
                + "        <Subject><Qualifier>Subject</Qualifier><Value>ПРЕПОДАВАНИЕ КУЛЬТУРОЛОГИИ</Value></Subject>"
                + "        <Subject><Qualifier>Subject</Qualifier><Value>ВЫСШЕЕ ОБРАЗОВАНИЕ</Value></Subject>"
                + "        <Subject><Qualifier>Subject</Qualifier><Value>ВЫСШАЯ ШКОЛА</Value></Subject>"
                + "        <Subject><Qualifier>UDC</Qualifier><Value/></Subject>"
                + "        <Title><Qualifier/><Value>\"Африка внутри нас\" : парадоксы современных процессов визуализации культуры</Value></Title>"
                + "        <Title><Qualifier>Alternative</Qualifier><Value/></Title>"
                + "        <Type><Qualifier/><Value>Text</Value></Type>"
                + "        <Thesis><Qualifier>Level</Qualifier><Value/></Thesis>"
                + "        <Thesis><Qualifier>Speciality</Qualifier><Value/></Thesis>"
                + "        <Source><Qualifier/><Value>Обсерватория культуры. - 2006. -  № 6. - Текст  : непосредственный</Value></Source>"
                + "        <Source><Qualifier/><Value/></Source>"
                + "    </Record></m:BiblRecords>"
                + "</ExchangeXML>";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "<soap:Body>"
                + "<GetRecordsInfoResponse xmlns=\"http://imc.parus-s.ru\">"
                + exchangeXml
                + "</GetRecordsInfoResponse>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }


    private String generateErrorResponse()
    {
        String exchangeXml
                = "<ExchangeXML xmlns=\"http://www.imc-dspace-new.org\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "    <Records>"
                + "    </Records>"
                + "</ExchangeXML>";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "<soap:Body>"
                + "<GetRecordsInfoResponse xmlns=\"http://imc.parus-s.ru\">"
                + exchangeXml
                + "</GetRecordsInfoResponse>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    private String generateSoapResponse(String id) {
        String exchangeXml
                = "<ExchangeXML xmlns=\"http://www.imc-dspace-new.org\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "    <Records>"
                + "        <Contributor><Qualifier>Department</Qualifier><Value/></Contributor>"
                + "        <Contributor><Qualifier>Other</Qualifier><Value/></Contributor>"
                + "        <Contributor><Qualifier>Subdepartment</Qualifier><Value/></Contributor>"
                + "        <Date><Qualifier>Issued</Qualifier><Value>2006</Value></Date>"
                + "        <Date><Qualifier>Issued</Qualifier><Value/></Date>"
                + "        <Identifier><Qualifier>Citation</Qualifier><Value>Савицкая, Т.Е. Савицкая, Т.Е. \"Африка внутри нас\" : парадоксы современных процессов визуализации культуры // Обсерватория культуры. - 2006. - № 6. - Текст : непосредственный</Value></Identifier>"
                + "        <Identifier><Qualifier>Identifier</Qualifier><Value>RU/IS/BASE/234616783</Value></Identifier>"
                + "        <Identifier><Qualifier>ISBN</Qualifier><Value/></Identifier>"
                + "        <Identifier><Qualifier>ISMN</Qualifier><Value/></Identifier>"
                + "        <Identifier><Qualifier>ISSN</Qualifier><Value/></Identifier>"
                + "        <Identifier><Qualifier>Nps</Qualifier><Value/></Identifier>"
                + "        <Identifier><Qualifier>Orcid</Qualifier><Value/></Identifier>"
                + "        <Description><Qualifier>Abstract</Qualifier><Value/></Description>"
                + "        <Description><Qualifier>FirstPage</Qualifier><Value>30</Value></Description>"
                + "        <Description><Qualifier>LastPage</Qualifier><Value>35</Value></Description>"
                + "        <Format><Qualifier>Extent</Qualifier><Value/></Format>"
                + "        <Format><Qualifier>Mimetype</Qualifier><Value>Text</Value></Format>"
                + "        <Language><Qualifier>ISO</Qualifier><Value/></Language>"
                + "        <Publisher><Qualifier/><Value/></Publisher>"
                + "        <Relation><Qualifier>IsPartOf</Qualifier><Value>Обсерватория культуры</Value></Relation>"
                + "        <Rights><Qualifier/><Value/></Rights>"
                + "        <Rights><Qualifier>License</Qualifier><Value/></Rights>"
                + "        <Rights><Qualifier>Url</Qualifier><Value/></Rights>"
                + "        <Subject><Qualifier>RuBBK</Qualifier><Value/></Subject>"
                + "        <Subject><Qualifier>RuGASNTI</Qualifier><Value/></Subject>"
                + "        <Subject><Qualifier>Subject</Qualifier><Value>ПРЕПОДАВАНИЕ КУЛЬТУРОЛОГИИ</Value></Subject>"
                + "        <Subject><Qualifier>Subject</Qualifier><Value>ВЫСШЕЕ ОБРАЗОВАНИЕ</Value></Subject>"
                + "        <Subject><Qualifier>Subject</Qualifier><Value>ВЫСШАЯ ШКОЛА</Value></Subject>"
                + "        <Subject><Qualifier>UDC</Qualifier><Value/></Subject>"
                + "        <Title><Qualifier/><Value>\"Африка внутри нас\" : парадоксы современных процессов визуализации культуры</Value></Title>"
                + "        <Title><Qualifier>Alternative</Qualifier><Value/></Title>"
                + "        <Type><Qualifier/><Value>Text</Value></Type>"
                + "        <Thesis><Qualifier>Level</Qualifier><Value/></Thesis>"
                + "        <Thesis><Qualifier>Speciality</Qualifier><Value/></Thesis>"
                + "        <Source><Qualifier/><Value>Обсерватория культуры. - 2006. -  № 6. - Текст  : непосредственный</Value></Source>"
                + "        <Source><Qualifier/><Value/></Source>"
                + "    </Records>"
                + "</ExchangeXML>";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "<soap:Body>"
                + "<GetRecordsInfoResponse xmlns=\"http://imc.parus-s.ru\">"
                + exchangeXml
                + "</GetRecordsInfoResponse>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }
}
