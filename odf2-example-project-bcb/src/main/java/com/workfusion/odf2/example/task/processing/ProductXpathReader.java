package com.workfusion.odf2.example.task.processing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.workfusion.odf2.example.model.Product;

class ProductXpathReader {

    private static final String ROW_COUNT_EXPRESSION = "count(//block[@blockType=\"Table\"]/row)";

    private static final String NAME_EXPRESSION_PATTERN = "//block[@blockType=\"Table\"]/row[%d]/cell[1]/text/par/line/formatting/text()";
    private static final String DESCRIPTION_EXPRESSION_PATTERN = "//block[@blockType=\"Table\"]/row[%d]/cell[2]/text/par/line/formatting/text()";
    private static final String PRICE_EXPRESSION_PATTERN = "//block[@blockType=\"Table\"]/row[%d]/cell[4]/text/par/line/formatting/text()";

    private final DocumentBuilderFactory builderFactory;
    private final XPath xPath;

    private final XPathExpression rowCountExpression;

    ProductXpathReader() {
        builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        this.xPath = XPathFactory.newInstance().newXPath();
        try {
            rowCountExpression = xPath.compile(ROW_COUNT_EXPRESSION);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Unable to parse predefined expressions", e);
        }
    }

    List<Product> fromUrl(String url) {
        final List<Product> result = new ArrayList<>();
        final Document xmlDocument = toXmlDocument(url);

        try {
            Number number = (Number) rowCountExpression.evaluate(xmlDocument, XPathConstants.NUMBER);

            for (int index = 2; index <= number.intValue(); index++) {
                Product product = new Product();
                product.setName(readTextContentByXpath(String.format(NAME_EXPRESSION_PATTERN, index), xmlDocument));
                product.setDescription(readTextContentByXpath(String.format(DESCRIPTION_EXPRESSION_PATTERN, index), xmlDocument));
                product.setPrice(readTextContentByXpath(String.format(PRICE_EXPRESSION_PATTERN, index), xmlDocument).replace("$", ""));

                if (product.getName().isEmpty() || product.getDescription().isEmpty() || product.getPrice().isEmpty()) {
                    continue;
                }

                result.add(product);
            }

            return result;
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(String.format("Unable to read a product from document: %s", url), e);
        }
    }

    private Document toXmlDocument(String url) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream())) {
            return builderFactory.newDocumentBuilder().parse(in);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new IllegalStateException(String.format("Error occurred while parsing document: %s", url), e);
        }
    }

    private String readTextContentByXpath(String xPathString, Document xmlDocument) {
        try {
            final NodeList nodeList = (NodeList) xPath.evaluate(xPathString, xmlDocument, XPathConstants.NODESET);
            final StringJoiner joiner = new StringJoiner(" ");

            for (int i = 0; i < nodeList.getLength(); i++) {
                joiner.add(nodeList.item(i).getTextContent());
            }
            return joiner.toString();
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Unable to parse predefined expressions", e);
        }
    }

}
