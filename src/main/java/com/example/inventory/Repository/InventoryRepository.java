package com.example.inventory.Repository;

import com.example.inventory.Model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    /**
     * Find inventory item by PLU code
     */
    @Query("SELECT i FROM Inventory i WHERE i.PLU = :pluCode")
    Optional<Inventory> findByPLU(@Param("pluCode") String pluCode);

    /**
     * Delete inventory item by PLU code
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Inventory i WHERE i.PLU = :pluCode")
    void deleteByPLU(@Param("pluCode") String pluCode);

    /**
     * Check if inventory item exists by PLU code
     */
    boolean existsByPLU(String PLU);
}