package com.pos.service;

import com.pos.dto.response.ProductImportResponse;
import com.pos.entity.Product;
import com.pos.entity.Shop;
import com.pos.entity.User;
import com.pos.repository.ProductRepository;
import com.pos.repository.ShopRepository;
import com.pos.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class ProductImportService {
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public ProductImportService(
            ProductRepository productRepository,
            ShopRepository shopRepository,
            UserRepository userRepository
    ) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProductImportResponse importForShop(Long shopId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file is required");
        }
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        User owner = userRepository.findByShopIdOrderByCreatedAtAsc(shopId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Shop has no user account"));

        ProductImportResponse response = new ProductImportResponse();
        try (InputStream stream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(stream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                throw new IllegalArgumentException("Excel file has no product rows");
            }

            Map<String, Integer> columns = headerColumns(sheet.getRow(0));
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                int rowNumber = i + 1;
                if (row == null || isEmpty(row)) {
                    response.skipped();
                    continue;
                }
                try {
                    boolean updated = importRow(shop, owner, row, columns);
                    if (updated) response.updated();
                    else response.inserted();
                } catch (Exception error) {
                    response.addError(rowNumber, error.getMessage());
                }
            }
        } catch (IllegalArgumentException error) {
            throw error;
        } catch (Exception error) {
            throw new RuntimeException("Unable to import products from Excel", error);
        }
        return response;
    }

    public byte[] createTemplate() {
        try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] columns = {"name", "description", "costPrice", "price", "wholesalePrice", "vipPrice", "stock", "unitName", "packUnitName", "packSize", "barcode", "expiryDate"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Example Product");
            sample.createCell(1).setCellValue("Optional note");
            sample.createCell(2).setCellValue(500);
            sample.createCell(3).setCellValue(700);
            sample.createCell(4).setCellValue(650);
            sample.createCell(5).setCellValue(620);
            sample.createCell(6).setCellValue(20);
            sample.createCell(7).setCellValue("ခု");
            sample.createCell(8).setCellValue("ပါကင်");
            sample.createCell(9).setCellValue(12);
            sample.createCell(10).setCellValue("885000000001");
            sample.createCell(11).setCellValue("2026-12-31");

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (Exception error) {
            throw new RuntimeException("Unable to create product import template", error);
        }
    }

    private boolean importRow(Shop shop, User owner, Row row, Map<String, Integer> columns) {
        String name = text(row, columns, "name");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required");

        BigDecimal costPrice = money(row, columns, "costPrice", true);
        BigDecimal price = money(row, columns, "price", true);
        Integer stock = number(row, columns, "stock", true);
        String barcode = text(row, columns, "barcode");

        Product product = existing(shop.getId(), barcode, name);
        boolean updated = product != null;
        if (!updated) {
            product = new Product();
            product.setOwner(owner);
            product.setShop(shop);
        }

        product.setName(name.trim());
        product.setDescription(text(row, columns, "description"));
        product.setCostPrice(costPrice);
        product.setPrice(price);
        product.setWholesalePrice(money(row, columns, "wholesalePrice", false));
        product.setVipPrice(money(row, columns, "vipPrice", false));
        product.setStock(stock);
        product.setUnitName(defaultText(text(row, columns, "unitName"), "ခု"));
        product.setPackUnitName(text(row, columns, "packUnitName"));
        product.setPackSize(number(row, columns, "packSize", false, 1));
        product.setBarcode(blankToNull(barcode));
        product.setExpiryDate(date(row, columns, "expiryDate"));
        product.setDeleted(false);
        productRepository.save(product);
        return updated;
    }

    private Product existing(Long shopId, String barcode, String name) {
        if (barcode != null && !barcode.isBlank()) {
            var byBarcode = productRepository.findByShopIdAndBarcode(shopId, barcode.trim());
            if (byBarcode.isPresent()) return byBarcode.get();
        }
        return productRepository.findByShopIdAndNameIgnoreCase(shopId, name.trim()).orElse(null);
    }

    private Map<String, Integer> headerColumns(Row header) {
        if (header == null) throw new IllegalArgumentException("Header row is required");
        Map<String, Integer> columns = new HashMap<>();
        for (Cell cell : header) {
            String key = normalize(cell.getStringCellValue());
            if (key.isBlank()) continue;
            columns.put(canonical(key), cell.getColumnIndex());
        }
        require(columns, "name", "Product name");
        require(columns, "costPrice", "Cost price");
        require(columns, "price", "Selling price");
        require(columns, "stock", "Stock");
        return columns;
    }

    private void require(Map<String, Integer> columns, String key, String label) {
        if (!columns.containsKey(key)) {
            throw new IllegalArgumentException(label + " column is required");
        }
    }

    private String canonical(String key) {
        return switch (key) {
            case "name", "productname", "product", "itemname", "item", "ပစၥည်းအမည်", "ပစ္စည်းအမည်" -> "name";
            case "description", "desc", "details", "မှတ်ချက်" -> "description";
            case "cost", "costprice", "buyprice", "purchaseprice", "အရင်း", "အရင်းစျေး", "အရင်းဈေး" -> "costPrice";
            case "price", "sellingprice", "saleprice", "sellprice", "ရောင်းစျေး", "ရောင်းဈေး" -> "price";
            case "wholesale", "wholesaleprice", "လက်ကား", "လက်ကားစျေး", "လက်ကားဈေး" -> "wholesalePrice";
            case "vip", "vipprice", "customerprice", "specialprice", "အထူးစျေး", "အထူးဈေး" -> "vipPrice";
            case "stock", "quantity", "qty", "count", "အရေအတွက်" -> "stock";
            case "unit", "unitname", "baseunit", "ယူနစ်" -> "unitName";
            case "packunit", "packunitname", "packageunit", "ပါကင်ယူနစ်" -> "packUnitName";
            case "packsize", "packagesize", "conversion", "multiplier", "ပါကင်အရေအတွက်" -> "packSize";
            case "barcode", "bar code", "code" -> "barcode";
            case "expiry", "expirydate", "expiredate", "expirationdate", "သက်တမ်း" -> "expiryDate";
            default -> key;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replace("_", "").replace("-", "").replace(" ", "").toLowerCase(Locale.ROOT);
    }

    private boolean isEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) return false;
        }
        return true;
    }

    private String text(Row row, Map<String, Integer> columns, String key) {
        Integer index = columns.get(key);
        if (index == null) return null;
        Cell cell = row.getCell(index);
        if (cell == null) return null;
        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell).trim();
        return value.isBlank() ? null : value;
    }

    private BigDecimal money(Row row, Map<String, Integer> columns, String key, boolean required) {
        String value = text(row, columns, key);
        if ((value == null || value.isBlank()) && required) throw new IllegalArgumentException(key + " is required");
        if (value == null || value.isBlank()) return null;
        BigDecimal amount = new BigDecimal(value.replace(",", ""));
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException(key + " must be greater than zero");
        return amount;
    }

    private Integer number(Row row, Map<String, Integer> columns, String key, boolean required) {
        return number(row, columns, key, required, null);
    }

    private Integer number(Row row, Map<String, Integer> columns, String key, boolean required, Integer defaultValue) {
        String value = text(row, columns, key);
        if ((value == null || value.isBlank()) && required) throw new IllegalArgumentException(key + " is required");
        if (value == null || value.isBlank()) return defaultValue;
        int number = new BigDecimal(value.replace(",", "")).intValue();
        if (number < 0) throw new IllegalArgumentException(key + " cannot be negative");
        return number;
    }

    private LocalDate date(Row row, Map<String, Integer> columns, String key) {
        Integer index = columns.get(key);
        if (index == null) return null;
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String value = text(row, columns, key);
        return value == null ? null : LocalDate.parse(value);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
