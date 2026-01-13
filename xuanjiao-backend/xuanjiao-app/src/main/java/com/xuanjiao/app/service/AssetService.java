package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface AssetService {
    AssetDTO upload(MultipartFile file, AssetUploadCmd cmd, Long userId);
    AssetDTO getById(Long id);
    PageResult<AssetDTO> query(AssetQueryCmd cmd);
    PageResult<AssetDTO> queryWithRoleFilter(AssetQueryCmd cmd, Long userId);
    void delete(Long id);
    void updateStatusByApplicationId(Long applicationId, String status);
}
