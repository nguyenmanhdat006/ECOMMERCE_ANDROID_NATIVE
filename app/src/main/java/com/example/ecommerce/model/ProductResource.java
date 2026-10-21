package com.example.ecommerce.model;

import com.google.gson.annotations.SerializedName;

/** Một ảnh của sản phẩm. */
public class ProductResource {
    private String id;
    private String name;
    private String url;
    private String type;

    @SerializedName(value = "isPrimary", alternate = {"primary"})
    private boolean primary;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getType() { return type; }
    public boolean isPrimary() { return primary; }
}
