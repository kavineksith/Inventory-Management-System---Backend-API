package com.example.inventory.Services;

import com.example.inventory.DTO.CreateInventoryDTO;
import com.example.inventory.DTO.InventoryListDTO;
import com.example.inventory.DTO.UpdateInventoryDTO;
import com.example.inventory.Model.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {
    public Inventory toEntity(CreateInventoryDTO dto) {
        // Add validation to ensure PLU is not null
        if (dto.getPLU() == null || dto.getPLU().trim().isEmpty()) {
            throw new IllegalArgumentException("PLU code cannot be null or empty");
        }

        return new Inventory(
                dto.getPLU().trim(), // Trim whitespace
                dto.getItemName().trim(),
                dto.getQTY(),
                dto.getPrice(),
                null, // ImageUrl will be set later
                null  // ImageFilename will be set later
        );
    }

    public InventoryListDTO toPreviewInventory(Inventory inventory) {
        return new InventoryListDTO(
                inventory.getInventoryID(),
                inventory.getPLU(),
                inventory.getItemName(),
                inventory.getQTY(),
                inventory.getPrice(),
                inventory.getImageUrl(),
                inventory.getImageFilename()
        );
    }

    public void toUpdateInventory(Inventory inventory, UpdateInventoryDTO dto) {
        // Add null checks for update data
        if (dto.getItemName() != null) {
            inventory.setItemName(dto.getItemName().trim());
        }

        inventory.setQTY(dto.getQTY());
        inventory.setPrice(dto.getPrice());
    }
}