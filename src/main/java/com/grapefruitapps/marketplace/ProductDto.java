package com.grapefruitapps.marketplace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProductDto {
    @Null
    private Long id;

    @NotNull
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String name;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private String category;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}
