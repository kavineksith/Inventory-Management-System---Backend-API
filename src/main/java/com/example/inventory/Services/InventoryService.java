package com.example.inventory.Services;

import com.example.inventory.DTO.CreateInventoryDTO;
import com.example.inventory.DTO.InventoryListDTO;
import com.example.inventory.DTO.UpdateInventoryDTO;
import com.example.inventory.Exceptions.DuplicateInventoryException;
import com.example.inventory.Exceptions.InvalidInventoryDataException;
import com.example.inventory.Exceptions.InventoryNotFoundException;
import com.example.inventory.Model.Inventory;
import com.example.inventory.Repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryMapper inventoryMapper;
    private final InventoryRepository inventoryRepository;
    private final FileStorageService fileStorageService;
    private final PdfService pdfService;
    private final EmailService emailService;

    @Value("${app.admin.email:#{null}}")
    private String adminEmail;

    public InventoryService(InventoryMapper inventoryMapper,
                            InventoryRepository inventoryRepository,
                            FileStorageService fileStorageService,
                            PdfService pdfService,
                            EmailService emailService) {
        this.inventoryMapper = inventoryMapper;
        this.inventoryRepository = inventoryRepository;
        this.fileStorageService = fileStorageService;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    // helper methods
    private Inventory findInventoryByPLU(String plu_code) {
        return inventoryRepository.findByPLU(plu_code)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory with PLU code " + plu_code + " not found"));
    }

    private void validateItemExists(String pluCode) {
        if(inventoryRepository.findByPLU(pluCode).isEmpty()) {
            throw new InventoryNotFoundException("Item with PLU " + pluCode + " not found");
        }
    }

    private void validateItemDoesNotExist(String plu_code) {
        if (inventoryRepository.findByPLU(plu_code).isPresent()) {
            throw new DuplicateInventoryException("Inventory with PLU code " + plu_code + " already exists");
        }
    }

    private void sendInventoryNotification(String action, Inventory item) {
        if (adminEmail != null && !adminEmail.isBlank()) {
            switch (action) {
                case "CREATED" -> emailService.sendInventoryCreatedEmail(
                        adminEmail, item.getItemName(), item.getPLU(), item.getQTY(), item.getPrice());
                case "UPDATED" -> emailService.sendInventoryUpdatedEmail(
                        adminEmail, item.getItemName(), item.getPLU(), item.getQTY(), item.getPrice());
                case "DELETED" -> emailService.sendInventoryDeletedEmail(
                        adminEmail, item.getItemName(), item.getPLU());
            }
        }
    }

    // CRUD operations
    public Inventory createInventoryItem(CreateInventoryDTO createInventoryDTO, MultipartFile imageFile) throws IOException {
        validateItemDoesNotExist(createInventoryDTO.getPLU());

        // Handle image upload
        String imageFilename = null;
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            if (!fileStorageService.isValidImageFile(imageFile)) {
                throw new InvalidInventoryDataException("Invalid image file. Only JPEG, PNG, GIF, and WebP are allowed.");
            }
            imageFilename = fileStorageService.storeFile(imageFile, createInventoryDTO.getPLU());
            imageUrl = fileStorageService.getFileUrl(imageFilename);
        }

        Inventory inventoryItem = inventoryMapper.toEntity(createInventoryDTO);
        inventoryItem.setImageFilename(imageFilename);
        inventoryItem.setImageUrl(imageUrl);

        Inventory savedItem = inventoryRepository.save(inventoryItem);

        // Send async email notification
        sendInventoryNotification("CREATED", savedItem);

        return savedItem;
    }

    public Inventory updateInventoryItem(String plu_code, UpdateInventoryDTO updateInventoryDTO, MultipartFile imageFile) throws IOException {
        validateItemExists(plu_code);

        Inventory inventoryItem = findInventoryByPLU(plu_code);

        // Handle image update
        if (imageFile != null && !imageFile.isEmpty()) {
            if (!fileStorageService.isValidImageFile(imageFile)) {
                throw new InvalidInventoryDataException("Invalid image file. Only JPEG, PNG, GIF, and WebP are allowed.");
            }

            // Delete old image if exists
            if (inventoryItem.getImageFilename() != null) {
                fileStorageService.deleteFile(inventoryItem.getImageFilename());
            }

            // Store new image
            String imageFilename = fileStorageService.storeFile(imageFile, plu_code);
            String imageUrl = fileStorageService.getFileUrl(imageFilename);

            inventoryItem.setImageFilename(imageFilename);
            inventoryItem.setImageUrl(imageUrl);
        }

        inventoryMapper.toUpdateInventory(inventoryItem, updateInventoryDTO);
        Inventory savedItem = inventoryRepository.save(inventoryItem);

        // Send async email notification
        sendInventoryNotification("UPDATED", savedItem);

        return savedItem;
    }

    public void deleteInventoryItem(String plu_code) throws IOException {
        validateItemExists(plu_code);

        Inventory inventoryItem = findInventoryByPLU(plu_code);

        // Send async email notification before deletion
        sendInventoryNotification("DELETED", inventoryItem);

        // Delete associated image file
        if (inventoryItem.getImageFilename() != null) {
            fileStorageService.deleteFile(inventoryItem.getImageFilename());
        }

        inventoryRepository.delete(inventoryItem);
    }

    @Transactional(readOnly = true)
    public InventoryListDTO findInventoryItem(String plu_code) {
        Inventory inventoryItem = findInventoryByPLU(plu_code);
        return inventoryMapper.toPreviewInventory(inventoryItem);
    }

    @Transactional(readOnly = true)
    public List<InventoryListDTO> findAllInventoryItems() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toPreviewInventory)
                .toList();
    }

    // PDF Generation methods
    @Transactional(readOnly = true)
    public byte[] generateInventoryReportPdf() {
        List<Inventory> inventoryItems = inventoryRepository.findAll();
        return pdfService.generateInventoryReportPdf(inventoryItems);
    }

    @Transactional(readOnly = true)
    public byte[] generateInventoryItemPdf(String plu_code) {
        Inventory inventoryItem = findInventoryByPLU(plu_code);
        return pdfService.generateSingleInventoryItemPdf(inventoryItem);
    }

    @Transactional(readOnly = true)
    public List<Inventory> getAllInventoryEntities() {
        return inventoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Inventory getInventoryEntityByPLU(String plu_code) {
        return findInventoryByPLU(plu_code);
    }
}
