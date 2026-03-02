package com.xuanjiao.adapter.web.asset;

import com.xuanjiao.app.asset.AssetService;
import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.app.usage.UsageLogService;
import com.xuanjiao.client.approval.ApprovalProgressDTO;
import com.xuanjiao.client.asset.AssetDTO;
import com.xuanjiao.client.asset.AssetQueryCmd;
import com.xuanjiao.client.asset.AssetUploadCmd;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.Result;
import com.xuanjiao.client.usage.UsageApplyDTO;
import com.xuanjiao.client.asset.AssetAdminDeleteCmd;
import com.xuanjiao.client.asset.AssetAdjustDeleteTimeCmd;
import com.xuanjiao.client.asset.AssetDeleteCmd;
import com.xuanjiao.client.asset.AssetGetDetailQry;
import com.xuanjiao.client.asset.AssetGetMyApprovedQry;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.util.List;

/**
 * 素材管理控制器
 *
 * <p>提供素材的增删改查、上传、下载、预览等功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>素材上传：支持图片、视频、文档三种类型</li>
 *   <li>素材查询：分页查询、详情查询、我的已录入素材</li>
 *   <li>素材下载：需通过使用审批，记录使用日志</li>
 *   <li>素材删除：管理员彻底删除、调整删除时间、手动触发清理</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "素材管理")
@RestController
@RequestMapping("/asset")
public class AssetController {

    /**
     * 日志记录器
     *
     * <p>用于记录素材操作日志，便于问题排查。</p>
     */
    private static final Logger logger = LoggerFactory.getLogger(AssetController.class);

    /** MIME 类型常量 */
    private static final String MEDIA_TYPE_VIDEO_MP4 = "video/mp4";

    /**
     * 素材服务
     *
     * <p>处理素材的增删改查业务逻辑。</p>
     */
    @Resource
    private AssetService assetService;

    /**
     * 素材使用申请服务
     *
     * <p>用于检查用户是否有权限使用素材。</p>
     */
    @Resource
    private UsageApplyService usageApplyService;

    /**
     * 使用日志服务
     *
     * <p>用于记录素材下载日志。</p>
     */
    @Resource
    private UsageLogService usageLogService;

    /**
     * 素材数据访问对象
     *
     * <p>用于查询素材信息。</p>
     */
    @Resource
    private AssetMapper assetMapper;

    /**
     * 用户数据访问对象
     *
     * <p>用于查询用户信息。</p>
     */
    @Resource
    private UserMapper userMapper;

    /**
     * 部门数据访问对象
     *
     * <p>用于查询部门信息。</p>
     */
    @Resource
    private DeptMapper deptMapper;

    /**
     * 素材使用申请关联数据访问对象
     *
     * <p>用于查询素材的使用配置信息。</p>
     */
    @Resource
    private UsageApplyAssetMapper usageApplyAssetMapper;

    /**
     * 上传素材
     *
     * <p>上传新的素材文件，支持图片、视频、文档三种类型。
     * 视频文件可同时上传缩略图。上传成功后素材进入待审批状态。</p>
     *
     * @param file 素材文件
     * @param thumbnailFile 缩略图文件（可选，仅视频需要）
     * @param cmd 上传命令，包含素材名称、类型、版权信息、标签等
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 上传后的素材信息
     */
    @ApiOperation("上传素材")
    @PostMapping("/upload")
    public Result<AssetDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @ModelAttribute AssetUploadCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(assetService.upload(file, thumbnailFile, cmd, userId));
    }

    /**
     * 查询素材详情
     *
     * <p>根据素材ID查询详细信息，包括素材名称、类型、大小、路径、
     * 上传者、版权信息、标签等。</p>
     *
     * @param qry 查询条件，包含素材ID
     * @return 素材详情信息
     */
    @ApiOperation("查询素材详情")
    @PostMapping("/getDetail")
    public Result<AssetDTO> getDetail(@Valid @RequestBody AssetGetDetailQry qry) {
        return Result.success(assetService.getById(qry.getId()));
    }

    /**
     * 分页查询素材
     *
     * <p>分页查询素材列表，支持按名称、类型、状态、标签等条件筛选。
     * 根据用户角色权限进行数据过滤。</p>
     *
     * @param cmd 查询命令，包含分页参数和筛选条件
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 分页的素材列表
     */
    @ApiOperation("分页查询素材")
    @PostMapping("/list")
    public Result<PageResult<AssetDTO>> list(@Valid @RequestBody AssetQueryCmd cmd, @RequestAttribute("userId") Long userId) {
        return Result.success(assetService.queryWithRoleFilter(cmd, userId));
    }

    /**
     * 查询用户已录入的素材（APPROVED状态）
     *
     * <p>查询当前用户上传且已通过审批的素材列表，
     * 用于素材使用申请时选择素材。</p>
     *
     * @param qry 查询条件，包含分页参数、名称和类型筛选条件
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 分页的素材列表
     */
    @ApiOperation("查询用户已录入的素材（APPROVED状态）")
    @PostMapping("/getMyApproved")
    public Result<PageResult<AssetDTO>> getMyApprovedAssets(
            @Valid @RequestBody AssetGetMyApprovedQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(assetService.getMyApprovedAssets(qry.getName(), qry.getType(), qry.getPageNum(), qry.getPageSize(), userId));
    }

    /**
     * 管理员彻底删除素材
     *
     * <p>管理员彻底删除指定素材，包括文件和数据库记录。
     * 仅系统管理员（ROLE_ID=1）可执行此操作。</p>
     *
     * @param cmd 删除命令，包含素材ID和删除原因
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 操作结果
     */
    @ApiOperation("管理员彻底删除素材")
    @PostMapping("/adminDelete")
    public Result<Void> adminDelete(
            @Valid @RequestBody AssetAdminDeleteCmd cmd,
            @RequestAttribute("userId") Long userId) {
        Boolean isAdmin = checkIsAdmin(userId);
        assetService.adminDelete(cmd.getId(), cmd.getReason(), userId, isAdmin);
        return Result.success();
    }

    /**
     * 管理员调整素材删除时间（测试功能）
     *
     * <p>将素材的删除审批时间调整为7天前，用于测试定时清理任务。
     * 仅系统管理员（ROLE_ID=1）可执行此操作。</p>
     *
     * @param cmd 调整命令，包含素材ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 操作结果
     */
    @ApiOperation("管理员调整素材删除时间（测试功能）")
    @PostMapping("/adjustDeleteTime")
    public Result<Void> adjustDeleteTime(@Valid @RequestBody AssetAdjustDeleteTimeCmd cmd, @RequestAttribute("userId") Long userId) {
        Boolean isAdmin = checkIsAdmin(userId);
        assetService.adjustDeleteTime(cmd.getId(), isAdmin);
        return Result.success();
    }

    /**
     * 手动触发定时任务（测试功能）
     *
     * <p>手动触发素材清理定时任务，清理满足条件的已删除素材。
     * 仅系统管理员（ROLE_ID=1）可执行此操作。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 清理的素材数量
     */
    @ApiOperation("手动触发定时任务（测试功能）")
    @PostMapping("/admin/trigger-cleanup")
    public Result<Integer> triggerCleanupTask(@RequestAttribute("userId") Long userId) {
        Boolean isAdmin = checkIsAdmin(userId);
        int count = assetService.triggerCleanupTask(isAdmin);
        return Result.success(count);
    }

    /**
     * 检查用户是否是系统管理员
     *
     * <p>判断指定用户是否具有系统管理员角色（ROLE_ID=1）。</p>
     *
     * @param userId 用户ID
     * @return true 表示是系统管理员，false 表示不是
     */
    private Boolean checkIsAdmin(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // 系统管理员的role_id是1
        return user.getRoleId() != null && user.getRoleId().equals(1L);
    }

    /**
     * 删除素材
     *
     * <p>删除指定素材。仅草稿状态的素材可以直接删除，
     * 已审批的素材需要通过删除申请流程。</p>
     *
     * @param cmd 删除命令，包含素材ID
     * @return 操作结果
     */
    @ApiOperation("删除素材")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody AssetDeleteCmd cmd) {
        assetService.delete(cmd.getId());
        return Result.success();
    }

    /**
     * 预览素材
     *
     * <p>直接在浏览器中预览素材文件，支持图片、视频、文档类型。
     * 此接口无需登录认证，可直接通过URL访问。</p>
     *
     * @param id 素材ID
     * @return 素材文件响应实体
     */
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
        // 图片和文档使用原有逻辑，视频根据文件路径判断类型
        MediaType mediaType;
        if ("VIDEO".equals(asset.getType())) {
            mediaType = getVideoMediaType(asset.getFilePath());
        } else {
            mediaType = getMediaType(asset.getType());
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(org.springframework.http.CacheControl.noCache())
                .body(new FileSystemResource(file));
    }

    /**
     * 查看视频缩略图
     *
     * <p>获取视频素材的缩略图图片。此接口无需登录认证。</p>
     *
     * @param id 素材ID
     * @return 缩略图文件响应实体
     */
    @ApiOperation("查看视频缩略图")
    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<FileSystemResource> viewThumbnail(@PathVariable Long id) {
        AssetDTO asset = assetService.getById(id);
        if (asset == null || asset.getThumbnailPath() == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(asset.getThumbnailPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(org.springframework.http.CacheControl.noCache())
                .body(new FileSystemResource(file));
    }

    /**
     * 下载素材
     *
     * <p>下载指定素材文件。用户需要先通过素材使用审批才能下载。
     * 下载时会自动记录使用日志，包括使用描述、发布渠道等信息。</p>
     *
     * @param id 素材ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @param request HTTP请求对象，用于获取客户端IP
     * @return 素材文件响应实体，无权限时返回403错误
     */
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

    /**
     * 获取客户端真实IP地址
     *
     * <p>从请求头中获取客户端IP，支持代理服务器场景。</p>
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
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

    /**
     * 获取部门名称
     *
     * <p>根据部门ID查询部门名称。</p>
     *
     * @param deptId 部门ID
     * @return 部门名称，如果部门不存在返回null
     */
    private String getDeptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        DeptDO dept = deptMapper.selectById(deptId);
        return dept != null ? dept.getName() : null;
    }

    /**
     * 根据素材类型获取MIME类型
     *
     * <p>将素材类型映射为对应的HTTP Content-Type。</p>
     *
     * @param type 素材类型（IMAGE/VIDEO/DOCUMENT）
     * @return MIME类型
     */
    private MediaType getMediaType(String type) {
        switch (type) {
            case "IMAGE": return MediaType.IMAGE_JPEG;
            case "VIDEO": return MediaType.valueOf(MEDIA_TYPE_VIDEO_MP4);
            case "DOCUMENT": return MediaType.APPLICATION_PDF;
            default: return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * 根据文件路径获取视频的MIME类型
     *
     * <p>根据视频文件扩展名判断具体的MIME类型。</p>
     *
     * @param filePath 视频文件路径
     * @return 视频MIME类型
     */
    private MediaType getVideoMediaType(String filePath) {
        if (filePath == null) {
            return MediaType.valueOf(MEDIA_TYPE_VIDEO_MP4);
        }
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".mp4")) {
            return MediaType.valueOf(MEDIA_TYPE_VIDEO_MP4);
        } else if (lowerPath.endsWith(".webm")) {
            return MediaType.valueOf("video/webm");
        } else if (lowerPath.endsWith(".ogg")) {
            return MediaType.valueOf("video/ogg");
        } else if (lowerPath.endsWith(".mov")) {
            return MediaType.valueOf("video/quicktime");
        } else if (lowerPath.endsWith(".avi")) {
            return MediaType.valueOf("video/x-msvideo");
        } else if (lowerPath.endsWith(".mkv")) {
            return MediaType.valueOf("video/x-matroska");
        } else {
            return MediaType.valueOf(MEDIA_TYPE_VIDEO_MP4); // 默认返回mp4
        }
    }
}
