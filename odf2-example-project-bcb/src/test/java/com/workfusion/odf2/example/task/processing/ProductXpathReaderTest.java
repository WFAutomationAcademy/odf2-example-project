package com.workfusion.odf2.example.task.processing;

import java.net.URL;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf2.example.model.Product;

import static org.assertj.core.api.Assertions.assertThat;

class ProductXpathReaderTest {

    @Test
    @DisplayName("should read product from provided document")
    void shouldReadProductFromProvidedDocument() throws Exception {
        // given
        ProductXpathReader reader = new ProductXpathReader();

        // when
        Product product = reader.fromUrl(getResourceUrl("test-ocr-result.xml"));

        // then
        assertThat(product.getName()).isEqualToIgnoringCase("Huawei P10 Plus VKY-L29");
        assertThat(product.getDescription()).isEqualToIgnoringCase("HUAWEI P10 PLUS SINGLE SIM 5.5\" INCH");
        assertThat(product.getPrice()).isEqualTo("$777.00");
    }

    private String getResourceUrl(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        Objects.requireNonNull(resource, String.format("Unable to find '%s' resource", resource));
        return resource.toString();
    }

}
