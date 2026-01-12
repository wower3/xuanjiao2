package com.xuanjiao.app.service.impl;

import com.xuanjiao.app.service.UsageApplyService;
import com.xuanjiao.app.service.WorkflowEngineService;
import com.xuanjiao.client.dto.*;
import com.xuanjiao.domain.usage.entity.UsageApply;
import com.xuanjiao.domain.usage.repository.UsageApplyRepository;
import com.xuanjiao.infrastructure.dataobject.ApprovalInstanceDO;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.mapper.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.mapper.AssetMapper;
import com.xuanjiao.infrastructure.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsageApplyServiceImpl implements UsageApplyService {

    @Autowired
    private UsageApplyRepository usageApplyRepository;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApprovalInstanceMapper approvalInstanceMapper;

    @Override
    @Transactional
    public UsageApplyDTO apply(UsageApplyCmd cmd, Long userId) {
        // 检查素材是否存在
        AssetDO asset = assetMapper.selectById(cmd.getAssetId());
        if (asset == null) {
            throw new RuntimeException("素材不存在");
        }

        // 检查是否已有待审批的申请
        UsageApply existing = usageApplyRepository.findByAssetAndUser(cmd.getAssetId(), userId, "PENDING");
        if (existing != null) {
            throw new RuntimeException("您已有待审批的使用申请，请勿重复申请");
        }

        // 检查是否已有已通过的申请
        UsageApply approved = usageApplyRepository.findByAssetAndUser(cmd.getAssetId(), userId, "APPROVED");
        if (approved != null) {
            throw new RuntimeException("您已有通过的使用申请，无需再次申请");
        }

        UsageApply usageApply = new UsageApply();
        usageApply.setAssetId(cmd.getAssetId());
        usageApply.setUserId(userId);
        usageApply.setPurpose(cmd.getPurpose());
        usageApply.setScope(cmd.getScope());
        usageApply.setWorkflowId(cmd.getWorkflowId());
        usageApply.setCreateTime(LocalDateTime.now());

        // 根据是否选择审批流程设置状态
        if (cmd.getWorkflowId() != null) {
            usageApply.setStatus("PENDING");
            usageApplyRepository.save(usageApply);
            // 启动审批流程
            Long instanceId = workflowEngineService.startProcess(
                    cmd.getWorkflowId(), "ASSET_USAGE", usageApply.getId(), userId);
            usageApply.setApprovalInstanceId(instanceId);
            usageApplyRepository.update(usageApply);
        } else {
            usageApply.setStatus("APPROVED");
            usageApplyRepository.save(usageApply);
        }

        return convert(usageApply);
    }

    @Override
    public PageResult<UsageApplyDTO> queryMyApplications(UsageApplyQueryCmd cmd, Long userId) {
        int offset = (cmd.getPageNum() - 1) * cmd.getPageSize();
        List<UsageApply> list = usageApplyRepository.findByCondition(
                cmd.getAssetId(), cmd.getStatus(), offset, cmd.getPageSize());
        // 过滤出当前用户的申请
        list = list.stream().filter(apply -> apply.getUserId().equals(userId)).collect(Collectors.toList());
        long total = usageApplyRepository.countByCondition(cmd.getAssetId(), cmd.getStatus());

        List<UsageApplyDTO> dtoList = list.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, total, cmd.getPageNum(), cmd.getPageSize());
    }

    @Override
    public boolean canUseAsset(Long assetId, Long userId) {
        // 检查是否有已通过的申请
        UsageApply approved = usageApplyRepository.findByAssetAndUser(assetId, userId, "APPROVED");
        return approved != null;
    }

    private UsageApplyDTO convert(UsageApply usageApply) {
        if (usageApply == null) return null;
        UsageApplyDTO dto = new UsageApplyDTO();
        BeanUtils.copyProperties(usageApply, dto);

        // 填充素材名称
        if (usageApply.getAssetId() != null) {
            AssetDO asset = assetMapper.selectById(usageApply.getAssetId());
            if (asset != null) {
                dto.setAssetName(asset.getName());
            }
        }

        // 填充用户名称
        if (usageApply.getUserId() != null) {
            UserDO user = userMapper.selectById(usageApply.getUserId());
            if (user != null) {
                dto.setUserName(user.getRealName());
            }
        }

        // 填充流程名称
        if (usageApply.getApprovalInstanceId() != null) {
            ApprovalInstanceDO instance = approvalInstanceMapper.selectById(usageApply.getApprovalInstanceId());
            if (instance != null && instance.getWorkflowId() != null) {
                dto.setWorkflowId(instance.getWorkflowId());
            }
        }

        return dto;
    }
}
