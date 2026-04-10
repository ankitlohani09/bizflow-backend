package com.bizflow.modules.returns.repository;

import com.bizflow.modules.returns.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    List<ReturnItem> findAllByReturnRefId(Long returnId);
}