package com.workfusion.odf2.example.task.processing;

import java.net.URL;
import java.util.List;
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
        List<Product> products = reader.fromUrl(getResourceUrl("test-ocr-result.xml"));

        // then
        assertThat(products).hasSize(10);

        Product product = products.get(2);
        assertThat(product.getName()).isEqualToIgnoringCase("Huawei Honor 6X Dual Camera Unlocked Smartphone 32");
        assertThat(product.getDescription()).isEqualToIgnoringCase("Dual Lenses Deliver Professional-Grade lmages. More Filters, More Creativity, More Memories to Share. Vivid Photos.");
        assertThat(product.getPrice()).isEqualTo("179.99");
    }

    private String getResourceUrl(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        Objects.requireNonNull(resource, String.format("Unable to find '%s' resource", resource));
        return resource.toString();
    }

}
