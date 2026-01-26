package com.xuanjiao.adapter.web.asset;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.asset.AssetService;
import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.app.usage.UsageLogService;
import com.xuanjiao.client.dto.*;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

@Api(tags = "素材管理")
@RestController
@RequestMapping("/asset")
public class AssetController {

    private static final Logger logger = LoggerFactory.getLogger(AssetController.class);

    @Resource
    private AssetService assetService;

    @Resource
    private UsageApplyService usageApplyService;

    @Resource
    private UsageLogService usageLogService;

    @Resource
    private AssetMapper assetMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UsageApplyAssetMapper usageApplyAssetMapper;

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
    public Result<PageResult<AssetDTO>> list(AssetQueryCmd cmd, @RequestAttribute("userId") Long userId) {
        return Result.success(assetService.queryWithRoleFilter(cmd, userId));
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

    @ApiOperation("下载素材")
    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable Long id, @RequestAttribute("userId") Long userId, HttpServletRequest request) {
        AssetDTO asset = assetService.getById(id);
        if (asset == null) {
            return ResponseEntity.notFound().build();
        }
        // 检查使用权限：所有用户（包括上传者）都需要通过使用审批才能下载
        boolean canDownload = usageApplyService.canUseAsset(id, userId);
        if (!canDownload) {
            return ResponseEntity.status(403).body("您没有下载此素材的权限，请先申请使用");
        }

        // 记录下载使用日志
        try {
            String ip = getClientIp(request);
            UserDO user = userMapper.selectById(userId);
            String deptName = user != null && user.getDeptId() != null ? getDeptName(user.getDeptId()) : null;

            // 通过中间表查询该用户对该素材的使用配置信息
            List<UsageApplyAssetDO> applyAssets = usageApplyAssetMapper.findByAssetId(id);
            logger.info("查询到 {} 条使用申请关联记录", applyAssets.size());

            String usageDescription = null;
            String usagePublishChannel = null;
            for (UsageApplyAssetDO applyAsset : applyAssets) {
                // 找到该用户已通过的使用申请
                UsageApplyDTO usageApply = usageApplyService.getById(applyAsset.getUsageApplyId());
                logger.info("检查使用申请 - usageApplyId: {}, userId: {}, status: {}",
                    applyAsset.getUsageApplyId(),
                    usageApply != null ? usageApply.getUserId() : null,
                    usageApply != null ? usageApply.getStatus() : null);

                if (usageApply != null && usageApply.getUserId().equals(userId) && "APPROVED".equals(usageApply.getStatus())) {
                    usageDescription = applyAsset.getUsageDescription();
                    usagePublishChannel = applyAsset.getUsagePublishChannel();
                    logger.info("找到匹配的使用申请，description: {}, channel: {}", usageDescription, usagePublishChannel);
                    break;
                }
            }

            usageLogService.logDownload(id, userId, ip, deptName, usageDescription, usagePublishChannel);
            logger.info("使用日志记录成功 - assetId: {}, userId: {}", id, userId);
        } catch (Exception e) {
            logger.error("记录使用日志失败 - assetId: {}, userId: {}, error: {}", id, userId, e.getMessage(), e);
        }

        File file = new File(asset.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = getMediaType(asset.getType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Content-Disposition", "attachment; filename=\"" + asset.getName() + "\"")
                .body(new FileSystemResource(file));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getDeptName(Long deptId) {
        // TODO: 实现获取部门名称的逻辑
        // 这里需要注入 DeptMapper 并查询部门信息
        return null;
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
