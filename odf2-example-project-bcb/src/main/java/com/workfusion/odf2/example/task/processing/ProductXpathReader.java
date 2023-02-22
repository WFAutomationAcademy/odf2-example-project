package com.workfusion.odf2.example.task.processing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.workfusion.odf2.example.model.Product;

class ProductXpathReader {

    private static final String NAME_EXPRESSION = "/document/page/block[5]/row[2]/cell[1]/text/par[1]/line/formatting/text()";
    private static final String DESCRIPTION_EXPRESSION = "/document/page/block[5]/row[2]/cell[2]/text/par/line[1]/formatting/text()";
    private static final String PRICE_EXPRESSION = "/document/page/block[5]/row[2]/cell[4]/text/par/line/formatting/text()";

    private final DocumentBuilderFactory builderFactory;

    private final XPathExpression nameExpression;
    private final XPathExpression descriptionExpression;
    private final XPathExpression priceExpression;

    ProductXpathReader() {
        builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            nameExpression = xPath.compile(NAME_EXPRESSION);
            descriptionExpression = xPath.compile(DESCRIPTION_EXPRESSION);
            priceExpression = xPath.compile(PRICE_EXPRESSION);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Unable to parse predefined expressions", e);
        }
    }

    Product fromUrl(String url) {
        Document xmlDocument = toXmlDocument(url);

        try {
            Product product = new Product();
            product.setName(nameExpression.evaluate(xmlDocument, XPathConstants.STRING).toString());
            product.setDescription(descriptionExpression.evaluate(xmlDocument, XPathConstants.STRING).toString());
            product.setPrice(priceExpression.evaluate(xmlDocument, XPathConstants.STRING).toString());
            return product;
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

}
