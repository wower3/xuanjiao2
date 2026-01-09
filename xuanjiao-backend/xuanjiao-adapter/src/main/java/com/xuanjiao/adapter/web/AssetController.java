package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.AssetService;
import com.xuanjiao.client.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.File;

@Api(tags = "素材管理")
@RestController
@RequestMapping("/asset")
public class AssetController {

    @Resource
    private AssetService assetService;

    @ApiOperation("上传素材")
    @PostMapping("/upload")
    public Result<AssetDTO> upload(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute AssetUploadCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(assetService.upload(file, cmd, userId));
    }

    @ApiOperation("查询素材详情")
    @GetMapping("/{id}")
    public Result<AssetDTO> getById(@PathVariable Long id) {
        return Result.success(assetService.getById(id));
    }

    @ApiOperation("分页查询素材")
    @GetMapping("/list")
    public Result<PageResult<AssetDTO>> list(AssetQueryCmd cmd) {
        return Result.success(assetService.query(cmd));
    }

    @ApiOperation("删除素材")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return Result.success();
    }

    @ApiOperation("预览素材")
    @GetMapping("/preview/{id}")
    public ResponseEntity<FileSystemResource> preview(@PathVariable Long id) {
        AssetDTO asset = assetService.getById(id);
        if (asset == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(asset.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = getMediaType(asset.getType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new FileSystemResource(file));
    }

    private MediaType getMediaType(String type) {
        switch (type) {
            case "IMAGE": return MediaType.IMAGE_JPEG;
            case "VIDEO": return MediaType.valueOf("video/mp4");
            case "DOCUMENT": return MediaType.APPLICATION_PDF;
            default: return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
