package com.pos.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductImportResponse {
    private int inserted;
    private int updated;
    private int skipped;
    private List<String> errors = new ArrayList<>();

    public void inserted() {
        inserted++;
    }

    public void updated() {
        updated++;
    }

    public void skipped() {
        skipped++;
    }

    public void addError(int rowNumber, String message) {
        errors.add("Row " + rowNumber + ": " + message);
    }
}
