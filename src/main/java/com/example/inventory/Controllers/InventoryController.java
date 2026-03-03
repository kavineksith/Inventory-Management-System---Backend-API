package com.example.inventory.Controllers;

import com.example.inventory.DTO.CreateInventoryDTO;
import com.example.inventory.DTO.ErrorResponseDTO;
import com.example.inventory.DTO.InventoryListDTO;
import com.example.inventory.DTO.UpdateInventoryDTO;
import com.example.inventory.Exceptions.DuplicateInventoryException;
import com.example.inventory.Exceptions.InvalidInventoryDataException;
import com.example.inventory.Exceptions.InventoryNotFoundException;
import com.example.inventory.Model.Inventory;
import com.example.inventory.Services.FileStorageService;
import com.example.inventory.Services.InventoryService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryService inventoryService;
    private final FileStorageService fileStorageService;

    public InventoryController(InventoryService inventoryService, FileStorageService fileStorageService) {
        this.inventoryService = inventoryService;
        this.fileStorageService = fileStorageService;
    }

    // Regular CRUD Operations

    /*
    // Create new item with JSON (no image)
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Inventory> createItemJson(@Valid @RequestBody CreateInventoryDTO createInventoryDTO) {
        try {
            Inventory createdItem = inventoryService.createInventoryItem(createInventoryDTO, null);
            return ResponseEntity.ok(createdItem);
        } catch (IOException e) {
            throw new InvalidInventoryDataException("Failed to create item: " + e.getMessage());
        }
    }
    */

    // Create new item with form data and optional image
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Inventory> createItemWithImage(
            @Valid @ModelAttribute CreateInventoryDTO createInventoryDTO,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        try {
            Inventory createdItem = inventoryService.createInventoryItem(createInventoryDTO, imageFile);
            return ResponseEntity.ok(createdItem);
        } catch (IOException e) {
            throw new InvalidInventoryDataException("Failed to store image file: " + e.getMessage());
        }
    }

    // Search item by PLU Code
    @GetMapping("/search/{pluCode}")
    public ResponseEntity<InventoryListDTO> getItem(@RequestParam String pluCode) {
        InventoryListDTO item = inventoryService.findInventoryItem(pluCode);
        return ResponseEntity.ok(item);
    }

    /*
    // Update item by PLU Code with JSON (no image update)
    @PutMapping(value = "/update/{pluCode}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Inventory> updateItemJson(
            @PathVariable String pluCode,
            @Valid @RequestBody UpdateInventoryDTO updateInventoryDTO) {

        try {
            Inventory updatedItem = inventoryService.updateInventoryItem(pluCode, updateInventoryDTO, null);
            return ResponseEntity.ok(updatedItem);
        } catch (IOException e) {
            throw new InvalidInventoryDataException("Failed to update item: " + e.getMessage());
        }
    }
    */

    // Update item by PLU Code with optional image
    @PutMapping(value = "/update/{pluCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Inventory> updateItemWithImage(
            @PathVariable String pluCode,
            @Valid @ModelAttribute UpdateInventoryDTO updateInventoryDTO,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        try {
            Inventory updatedItem = inventoryService.updateInventoryItem(pluCode, updateInventoryDTO, imageFile);
            return ResponseEntity.ok(updatedItem);
        } catch (IOException e) {
            throw new InvalidInventoryDataException("Failed to store image file: " + e.getMessage());
        }
    }

    // Delete item by PLU code
    @DeleteMapping("/delete/{pluCode}")
    public ResponseEntity<?> deleteItem(@PathVariable String pluCode) {
        try {
            inventoryService.deleteInventoryItem(pluCode);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            throw new InvalidInventoryDataException("Failed to delete associated image: " + e.getMessage());
        }
    }

    // Get all items
    @GetMapping("/list")
    public ResponseEntity<List<InventoryListDTO>> getAllItems() {
        List<InventoryListDTO> items = inventoryService.findAllInventoryItems();
        if (items.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(items);
        }
    }

    // Serve image files
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = fileStorageService.getFilePath(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // You might want to determine this dynamically
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PDF ENDPOINTS

    // Generate PDF report for all inventory items
    @GetMapping("/pdf/report")
    public ResponseEntity<byte[]> generateInventoryReportPdf() {
        try {
            byte[] pdfBytes = inventoryService.generateInventoryReportPdf();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "inventory-report.pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Generate PDF for specific inventory item by PLU code
    @GetMapping("/pdf/item/{pluCode}")
    public ResponseEntity<byte[]> generateInventoryItemPdf(@PathVariable String pluCode) {
        try {
            byte[] pdfBytes = inventoryService.generateInventoryItemPdf(pluCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "inventory-item-" + pluCode + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (InventoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Exception handling
    @ExceptionHandler(DuplicateInventoryException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicationException(DuplicateInventoryException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("DUPLICATE_ITEM", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(InventoryNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("ITEM_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(InvalidInventoryDataException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidInventoryDataException(InvalidInventoryDataException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("INVALID_ITEM_DATA", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}