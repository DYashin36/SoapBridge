package ru.omega;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class RestSoapBridge extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Несмотря на application/json, мы принимаем XML
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        String soapRequest = requestBody.toString();
        System.out.println(">> Received Content-Type: " + req.getContentType());
        System.out.println(">> Received SOAP body:\n" + soapRequest);

        // Здесь — простейший парсинг ID вручную (или через XML-библиотеку)
        String id = extractId(soapRequest);
        System.out.println("----");
        String soapResponse = generateSoapResponse(id);

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
            return soap.substring(start, end);
        }
        return "UNKNOWN";
    }

    private String generateSoapResponse(String id) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">" +
                "<soap:Body>" +
                "<GetRecordsInfoResponse xmlns=\"http://imc.parus-s.ru\">" +
                "<Record><ID>" + id + "</ID></Record>" +
                "</GetRecordsInfoResponse>" +
                "</soap:Body>" +
                "</soap:Envelope>";
    }
}