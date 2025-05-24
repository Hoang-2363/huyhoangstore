package com.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ProductFilterRequest {
    private List<String> brandsType;
    private List<String> categoriesType;
    private List<String> prices;
    private List<String> genders;
    private List<String> straps;
    private List<String> movementTypes;
    private List<String> caseSizes;
    private List<String> thicknesses;
    private List<String> glassMaterials;
    private List<String> caseMaterials;
    private List<String> waterResistanceLevels;
}
