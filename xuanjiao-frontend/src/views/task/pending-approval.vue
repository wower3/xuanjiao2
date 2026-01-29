<template>
  <div class="pending-approval-page">
    <el-card>
      <template #header>待我审批</template>

      <el-table :data="list" v-loading="loading">
        <el-table-column label="申请单ID" width="120">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: 500;">AP-{{ row.applicationId || row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请标题" min-width="200" prop="businessName" />
        <el-table-column prop="workflowName" label="审批流程" />
        <el-table-column prop="applicantName" label="申请人" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="任务类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.taskType === 'RESTART_SUB_WORKFLOW'" type="warning">重新发起子流程</el-tag>
            <el-tag v-else type="info">普通审批</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.taskType === 'RESTART_SUB_WORKFLOW'" link type="warning" @click="handleOpenRestartDetail(row)">重新发起</el-button>
            <template v-else>
              <el-button link type="info" @click="handleViewDetail(row)">查看详情</el-button>
              <el-button link type="primary" @click="handleOpenDetail(row)">审批</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        @change="loadData"
      />
    </el-card>

    <!-- 审批详情对话框 -->
    <el-dialog v-model="showApproveDialog" title="审批详情" width="1000px" @closed="resetForm">
      <div v-loading="loadingDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请单ID" :span="2">
            AP-{{ taskDetail.applicationId || taskDetail.id }}
          </el-descriptions-item>
          <el-descriptions-item label="申请标题" :span="2">
            {{ taskDetail.applicationTitle || taskDetail.businessName }}
          </el-descriptions-item>
          <el-descriptions-item label="发起人">
            {{ taskDetail.applicantName }}
          </el-descriptions-item>
          <el-descriptions-item label="审批流程">
            {{ taskDetail.workflowName }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(taskDetail.status)" size="small">
              {{ getStatusText(taskDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前阶段">
            {{ taskDetail.currentStageName || taskDetail.stageName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ taskDetail.createTime }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 素材信息 -->
        <div v-if="taskDetail.assetType || taskDetail.assetCount" style="margin-top: 20px">
          <h4>素材信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="素材类型">
              {{ taskDetail.assetType || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="素材数量">
              {{ taskDetail.assetCount || 0 }} 个
            </el-descriptions-item>
          </el-descriptions>

          <!-- 素材列表 -->
          <div v-if="taskDetail.assetList && taskDetail.assetList.length > 0" style="margin-top: 15px">
            <div class="asset-list-header">素材清单</div>
            <el-table :data="taskDetail.assetList" size="small" border>
              <el-table-column prop="id" label="素材ID" width="80" />
              <el-table-column prop="name" label="素材名称" min-width="150" />
              <el-table-column prop="type" label="类型" width="80" />
              <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
              <el-table-column prop="publishChannel" label="发布渠道" width="120" show-overflow-tooltip />
              <el-table-column label="预览" width="100">
                <template #default="{ row }">
                  <el-button
                    v-if="row.type === 'IMAGE' && (row.thumbnailPath || row.filePath)"
                    type="primary"
                    size="small"
                    link
                    @click="previewImage(row)"
                  >
                    预览图片
                  </el-button>
                  <el-button
                    v-else-if="row.type === 'VIDEO' && row.filePath"
                    type="primary"
                    size="small"
                    link
                    @click="previewVideo(row)"
                  >
                    预览视频
                  </el-button>
                  <span v-else style="color: #909399; font-size: 12px;">不支持预览</span>
                </template>
              </el-table-column>
              <el-table-column label="附件" width="100">
                <template #default="{ row }">
                  <el-button
                    v-if="row.copyrightFilePath"
                    type="success"
                    size="small"
                    link
                    @click="downloadAttachment(row.copyrightFilePath, row.name)"
                  >
                    下载附件
                  </el-button>
                  <span v-else style="color: #909399; font-size: 12px;">无附件</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getAssetStatusType(row.status)" size="small">
                    {{ getAssetStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 审批进度 -->
        <div v-if="taskDetail.approvalProgress && taskDetail.approvalProgress.length > 0" style="margin-top: 20px">

          <!-- 主流程进度 -->
          <div v-if="mainWorkflowProgress.length > 0" style="margin-bottom: 25px">
            <div class="workflow-section-header">
              <el-icon style="color: #409EFF; margin-right: 8px;"><Document /></el-icon>
              <span class="workflow-section-title">主流程审批进度</span>
            </div>
            <div class="progress-list">
              <div
                v-for="(progress, index) in mainWorkflowProgress"
                :key="'main-' + (progress.id || progress.stageId)"
                class="progress-item"
                :class="{
                  'active': progress.status === 'PENDING',
                  'approved': progress.status === 'APPROVED',
                  'rejected': progress.status === 'REJECTED',
                  'returned': progress.status === 'RETURNED',
                  'not-started': progress.status === 'NOT_STARTED'
                }"
              >
                <div class="progress-icon">
                  <el-icon v-if="progress.status === 'PENDING'"><Clock /></el-icon>
                  <el-icon v-else-if="progress.status === 'APPROVED'"><SuccessFilled /></el-icon>
                  <el-icon v-else-if="progress.status === 'REJECTED'"><CircleCloseFilled /></el-icon>
                  <el-icon v-else-if="progress.status === 'RETURNED'"><WarningFilled /></el-icon>
                  <el-icon v-else><MoreFilled /></el-icon>
                </div>
                <div class="progress-content">
                  <div class="progress-stage">
                    {{ progress.stageName }}
                    <el-tag
                      v-if="progress.status"
                      :type="getStatusType(progress.status)"
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ getStatusText(progress.status) }}
                    </el-tag>
                  </div>
                  <div v-if="progress.approvers && progress.approvers.length > 0" class="progress-approvers">
                    <div v-for="approver in progress.approvers" :key="approver.id" class="approver-item">
                      <div class="approver-name">
                        {{ approver.name }}
                        <span v-if="approver.status === 'APPROVED'" style="color: #67C23A;">✓</span>
                        <span v-else-if="approver.status === 'REJECTED'" style="color: #F56C6C;">✗</span>
                        <span v-else style="color: #909399;">待审批</span>
                      </div>
                      <div v-if="approver.comment" class="approver-comment">
                        <span class="comment-label">意见:</span> {{ approver.comment }}
                      </div>
                    </div>
                  </div>
                  <div v-else-if="progress.status === 'NOT_STARTED'" class="progress-approvers" style="color: #909399; font-style: italic;">
                    尚未到达此阶段
                  </div>
                  <div class="progress-status">
                    <span v-if="progress.approveTime" style="color: #909399; font-size: 12px">
                      {{ progress.approveTime }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 子流程进度 -->
          <div v-if="subWorkflowProgress.length > 0">
            <div class="workflow-section-header sub-workflow">
              <el-icon style="color: #E6A23C; margin-right: 8px;"><Folder /></el-icon>
              <span class="workflow-section-title">子流程审批进度</span>
            </div>
            <div class="progress-list sub-workflow-list">
              <div
                v-for="(progress, index) in subWorkflowProgress"
                :key="'sub-' + (progress.id || progress.stageId)"
                class="progress-item sub-workflow-item"
                :class="{
                  'active': progress.status === 'PENDING',
                  'approved': progress.status === 'APPROVED',
                  'rejected': progress.status === 'REJECTED',
                  'returned': progress.status === 'RETURNED',
                  'not-started': progress.status === 'NOT_STARTED'
                }"
              >
                <div class="progress-icon">
                  <el-icon v-if="progress.status === 'PENDING'"><Clock /></el-icon>
                  <el-icon v-else-if="progress.status === 'APPROVED'"><SuccessFilled /></el-icon>
                  <el-icon v-else-if="progress.status === 'REJECTED'"><CircleCloseFilled /></el-icon>
                  <el-icon v-else-if="progress.status === 'RETURNED'"><WarningFilled /></el-icon>
                  <el-icon v-else><MoreFilled /></el-icon>
                </div>
                <div class="progress-content">
                  <div class="progress-stage">
                    {{ progress.stageName }}
                    <el-tag
                      v-if="progress.status"
                      :type="getStatusType(progress.status)"
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ getStatusText(progress.status) }}
                    </el-tag>
                  </div>
                  <div v-if="progress.approvers && progress.approvers.length > 0" class="progress-approvers">
                    <div v-for="approver in progress.approvers" :key="approver.id" class="approver-item">
                      <div class="approver-name">
                        {{ approver.name }}
                        <span v-if="approver.status === 'APPROVED'" style="color: #67C23A;">✓</span>
                        <span v-else-if="approver.status === 'REJECTED'" style="color: #F56C6C;">✗</span>
                        <span v-else style="color: #909399;">待审批</span>
                      </div>
                      <div v-if="approver.comment" class="approver-comment">
                        <span class="comment-label">意见:</span> {{ approver.comment }}
                      </div>
                    </div>
                  </div>
                  <div v-else-if="progress.status === 'NOT_STARTED'" class="progress-approvers" style="color: #909399; font-style: italic;">
                    尚未到达此阶段
                  </div>
                  <div class="progress-status">
                    <span v-if="progress.approveTime" style="color: #909399; font-size: 12px">
                      {{ progress.approveTime }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- 同阶段其他审批人 -->
        <div v-if="taskDetail.otherApprovers && taskDetail.otherApprovers.length > 0" style="margin-top: 20px;">
          <h4>同阶段其他审批人</h4>
          <el-tag v-for="approver in taskDetail.otherApprovers" :key="approver.id" style="margin-right: 5px; margin-bottom: 5px;">
            {{ approver.name }}
            <el-tag v-if="approver.status === 'APPROVED'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="approver.status === 'REJECTED'" type="danger" size="small">已驳回</el-tag>
            <el-tag v-else type="info" size="small">待审批</el-tag>
          </el-tag>
        </div>

        <!-- 当前待审批人 -->
        <div v-if="taskDetail.pendingApprovers && taskDetail.pendingApprovers.length > 0" style="margin-top: 20px">
          <h4>当前待审批人</h4>
          <div>
            <el-tag
              v-for="approver in taskDetail.pendingApprovers"
              :key="approver.id"
              type="warning"
              style="margin-right: 10px"
            >
              {{ approver.name }}
            </el-tag>
          </div>
        </div>

        <!-- 选择下一层审批人 -->
        <div v-if="taskDetail.canSelectNextApprovers && taskDetail.nextStageId" style="margin-top: 20px;">
          <div style="border: 1px solid #409EFF; border-radius: 6px; padding: 16px; background-color: #FAFAFA">
            <div style="display: flex; align-items: center; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #DCDFE6">
              <el-icon style="color: #409EFF; margin-right: 8px;"><Document /></el-icon>
              <span style="font-weight: bold; color: #409EFF; font-size: 14px">主流程下一层审批人（{{ taskDetail.nextStageName }}）</span>
              <el-tag v-if="taskDetail.nextStageApproveType === 'OR'" type="warning" size="small" style="margin-left: auto">或签</el-tag>
              <el-tag v-else type="success" size="small" style="margin-left: auto">会签</el-tag>
            </div>

            <el-alert
              title="提示"
              type="info"
              :closable="false"
              style="margin-bottom: 15px"
            >
              <template v-if="taskDetail.nextStageApproveType === 'OR'">
                或签：请为每个子流程选择审批人，并从其他配置中选择 1 个审批人，共需要选择
                {{ (taskDetail.subWorkflows?.length || 0) + 1 }} 个审批人。
              </template>
              <template v-else>
                会签：请按照配置顺序为每个配置项选择一个审批人，共需要选择
                {{ (taskDetail.nextStageApproverConfigs?.length || 0) + (taskDetail.subWorkflows?.length || 0) }} 个审批人。
              </template>
            </el-alert>

            <div v-if="taskDetail.nextStageApproverConfigs && taskDetail.nextStageApproverConfigs.length > 0">
              <div v-for="(config, index) in taskDetail.nextStageApproverConfigs" :key="config.configId" style="margin-bottom: 15px;">
                <div style="display: flex; align-items: center; margin-bottom: 5px;">
                  <span style="display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background-color: #409EFF; color: white; font-size: 12px; margin-right: 8px;">{{ index + 1 }}</span>
                  <span style="font-weight: bold; color: #606266;">{{ config.approverTypeName }}：{{ config.approverName }}</span>
                </div>
                <el-select
                  v-model="selectedNextApprovers[config.configId]"
                  filterable
                  placeholder="请选择审批人"
                  style="width: 100%;"
                  :clearable="true"
                  @change="handleNormalApproverChange"
                >
                  <el-option
                    v-for="user in config.availableUsers"
                    :key="user.id"
                    :label="user.realName || user.username"
                    :value="user.id"
                  >
                    <span>{{ user.realName || user.username }}</span>
                    <span style="color: #909399; font-size: 12px; margin-left: 10px;">
                      {{ user.deptName }} / {{ user.roleName }}
                    </span>
                  </el-option>
                </el-select>
              </div>
              <!-- 已选择提示 -->
              <div style="margin-top: 8px; color: #67C23A; font-size: 12px">
                已选择 {{ Object.values(selectedNextApprovers).filter(v => v !== null && v !== undefined).length }} / {{ taskDetail.nextStageApproveType === 'OR' ? 1 : (taskDetail.nextStageApproverConfigs?.length || 0) }} 位审批人
              </div>
            </div>
            <div v-else style="color: #F56C6C; font-size: 12px;">
              下一层没有配置审批人
            </div>
          </div>
        </div>

        <!-- 子流程审批人选择 -->
        <div v-if="taskDetail.hasSubWorkflows && taskDetail.subWorkflows && taskDetail.subWorkflows.length > 0 && taskDetail.canSelectNextApprovers" style="margin-top: 20px;">
          <el-alert
            title="提示"
            type="warning"
            :closable="false"
            style="margin-bottom: 15px"
          >
            当前阶段包含子流程，您需要为每个子流程选择第一层审批人。子流程将独立运行，不影响主流程。
          </el-alert>
          <div v-for="subWorkflow in taskDetail.subWorkflows" :key="subWorkflow.id" style="margin-bottom: 20px; padding: 15px; border: 1px solid #E6A23C; border-radius: 6px; background-color: #FFFBF0">
            <div style="display: flex; align-items: center; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #DCDFE6">
              <el-icon style="color: #E6A23C; margin-right: 8px;"><Folder /></el-icon>
              <span style="font-weight: bold; color: #E6A23C; font-size: 14px">子流程：{{ subWorkflow.name || '未命名' }} (ID: {{ subWorkflow.id }})</span>
              <span v-if="subWorkflow.workflowType" style="color: #909399; font-size: 12px; margin-left: 10px;">
                ({{ subWorkflow.workflowType === 'ASSET_UPLOAD' ? '素材录入' : '素材使用' }})
              </span>
              <el-tag v-if="subWorkflow.approveType === 'OR'" type="warning" size="small" style="margin-left: auto">或签</el-tag>
              <el-tag v-else type="success" size="small" style="margin-left: auto">会签</el-tag>
            </div>
            <!-- 子流程未配置阶段或审批人 -->
            <div v-if="!subWorkflow.approverConfigs || subWorkflow.approverConfigs.length === 0" style="color: #F56C6C; font-size: 13px;">
              <el-icon style="vertical-align: middle;"><WarningFilled /></el-icon>
              该子流程未配置阶段或审批人，请在流程设计器中配置。
            </div>
            <!-- 子流程有配置 -->
            <template v-else>
              <div style="margin-bottom: 10px; color: #606266; font-size: 13px">
                <template v-if="subWorkflow.approveType === 'OR'">
                  或签：请从以下配置中选择 1 个审批人
                </template>
                <template v-else>
                  会签：请按照配置顺序为每个配置项选择一个审批人，共需要选择 {{ subWorkflow.approverCount }} 个审批人。
                </template>
              </div>
              <div v-for="(config, index) in subWorkflow.approverConfigs" :key="config.configId" style="margin-bottom: 10px;">
                <div style="display: flex; align-items: center; margin-bottom: 5px;">
                  <span style="display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background-color: #E6A23C; color: white; font-size: 12px; margin-right: 8px;">{{ index + 1 }}</span>
                  <span style="font-weight: bold; color: #606266;">{{ config.approverTypeName || '未知类型' }}：{{ config.approverName || '未命名' }}</span>
                  <span v-if="!config.availableUsers || config.availableUsers.length === 0" style="color: #F56C6C; font-size: 12px; margin-left: 10px;">
                    （无可选用户，请检查用户状态）
                  </span>
                </div>
                <el-select
                  v-model="selectedSubWorkflowApprovers[subWorkflow.id][config.configId]"
                  filterable
                  placeholder="请选择审批人"
                  style="width: 100%;"
                  clearable
                  @change="handleSubWorkflowApproverChange(subWorkflow.id)"
                  :disabled="!config.availableUsers || config.availableUsers.length === 0"
                >
                  <el-option
                    v-for="user in (config.availableUsers || [])"
                    :key="user.id"
                    :label="user.realName || user.username"
                    :value="user.id"
                  >
                    <span>{{ user.realName || user.username }}</span>
                    <span style="color: #909399; font-size: 12px; margin-left: 10px;">
                      {{ user.deptName }} / {{ user.roleName }}
                    </span>
                  </el-option>
                </el-select>
              </div>
              <!-- 已选择提示 -->
              <div style="margin-top: 8px; color: #67C23A; font-size: 12px">
                已选择 {{ Object.values(selectedSubWorkflowApprovers[subWorkflow.id] || {}).filter(v => v !== null && v !== undefined).length }} / {{ subWorkflow.approveType === 'OR' ? 1 : subWorkflow.approverCount }} 位审批人
              </div>
            </template>
          </div>
        </div>

        <!-- 审批意见 -->
        <div style="margin-top: 20px;">
          <div style="font-weight: bold; margin-bottom: 10px;">审批意见：</div>
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </div>

        <!-- 退回原因（可选） -->
        <div style="margin-top: 15px;" v-if="showReturnReasonInput">
          <div style="font-weight: bold; margin-bottom: 10px; color: #E6A23C;">退回原因：</div>
          <el-input
            v-model="returnForm.reason"
            type="textarea"
            :rows="2"
            placeholder="请输入退回原因（可选）"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="showApproveDialog = false" :disabled="submitting">取消</el-button>
        <el-button type="warning" @click="showReturnReasonInput = !showReturnReasonInput" :disabled="submitting" v-if="!showReturnReasonInput">退回</el-button>
        <el-button type="danger" @click="submitApprove(false)" :loading="submitting">驳回</el-button>
        <el-button type="success" @click="submitApprove(true)" :loading="submitting">通过</el-button>
        <el-button type="warning" @click="submitReturn" :loading="submitting" v-if="showReturnReasonInput">确认退回</el-button>
        <el-button @click="showReturnReasonInput = false" v-if="showReturnReasonInput">取消退回</el-button>
      </template>
    </el-dialog>

    <!-- 只读详情对话框 -->
    <el-dialog v-model="showViewDialog" title="工单详情" width="1000px">
      <div v-loading="loadingDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请单ID" :span="2">
            AP-{{ viewDetail.applicationId || viewDetail.id }}
          </el-descriptions-item>
          <el-descriptions-item label="申请标题" :span="2">
            {{ viewDetail.applicationTitle || viewDetail.businessName }}
          </el-descriptions-item>
          <el-descriptions-item label="发起人">
            {{ viewDetail.applicantName }}
          </el-descriptions-item>
          <el-descriptions-item label="审批流程">
            {{ viewDetail.workflowName }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(viewDetail.status)" size="small">
              {{ getStatusText(viewDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前阶段">
            {{ viewDetail.currentStageName || viewDetail.stageName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ viewDetail.createTime }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 素材信息 -->
        <div v-if="viewDetail.assetType || viewDetail.assetCount" style="margin-top: 20px">
          <h4>素材信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="素材类型">
              {{ viewDetail.assetType || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="素材数量">
              {{ viewDetail.assetCount || 0 }} 个
            </el-descriptions-item>
          </el-descriptions>

          <!-- 素材列表 -->
          <div v-if="viewDetail.assetList && viewDetail.assetList.length > 0" style="margin-top: 15px">
            <div class="asset-list-header">素材清单</div>
            <el-table :data="viewDetail.assetList" size="small" border>
              <el-table-column prop="id" label="素材ID" width="80" />
              <el-table-column prop="name" label="素材名称" min-width="150" />
              <el-table-column prop="type" label="类型" width="80" />
              <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
              <el-table-column prop="publishChannel" label="发布渠道" width="120" show-overflow-tooltip />
              <el-table-column label="预览" width="100">
                <template #default="{ row }">
                  <el-button
                    v-if="row.type === 'IMAGE' && (row.thumbnailPath || row.filePath)"
                    type="primary"
                    size="small"
                    link
                    @click="previewImage(row)"
                  >
                    预览图片
                  </el-button>
                  <el-button
                    v-else-if="row.type === 'VIDEO' && row.filePath"
                    type="primary"
                    size="small"
                    link
                    @click="previewVideo(row)"
                  >
                    预览视频
                  </el-button>
                  <span v-else style="color: #909399; font-size: 12px;">不支持预览</span>
                </template>
              </el-table-column>
              <el-table-column label="附件" width="100">
                <template #default="{ row }">
                  <el-button
                    v-if="row.copyrightFilePath"
                    type="success"
                    size="small"
                    link
                    @click="downloadAttachment(row.copyrightFilePath, row.name)"
                  >
                    下载附件
                  </el-button>
                  <span v-else style="color: #909399; font-size: 12px;">无附件</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getAssetStatusType(row.status)" size="small">
                    {{ getAssetStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 审批进度 -->
        <div v-if="viewDetail.approvalProgress && viewDetail.approvalProgress.length > 0" style="margin-top: 20px">

          <!-- 主流程进度 -->
          <div v-if="mainWorkflowProgressView.length > 0" style="margin-bottom: 25px">
            <div class="workflow-section-header">
              <el-icon style="color: #409EFF; margin-right: 8px;"><Document /></el-icon>
              <span class="workflow-section-title">主流程审批进度</span>
            </div>
            <div class="progress-list">
              <div
                v-for="(progress, index) in mainWorkflowProgressView"
                :key="'main-' + (progress.id || progress.stageId)"
                class="progress-item"
                :class="{
                  'active': progress.status === 'PENDING',
                  'approved': progress.status === 'APPROVED',
                  'rejected': progress.status === 'REJECTED',
                  'returned': progress.status === 'RETURNED',
                  'not-started': progress.status === 'NOT_STARTED'
                }"
              >
                <div class="progress-icon">
                  <el-icon v-if="progress.status === 'PENDING'"><Clock /></el-icon>
                  <el-icon v-else-if="progress.status === 'APPROVED'"><SuccessFilled /></el-icon>
                  <el-icon v-else-if="progress.status === 'REJECTED'"><CircleCloseFilled /></el-icon>
                  <el-icon v-else-if="progress.status === 'RETURNED'"><WarningFilled /></el-icon>
                  <el-icon v-else><MoreFilled /></el-icon>
                </div>
                <div class="progress-content">
                  <div class="progress-stage">
                    {{ progress.stageName }}
                    <el-tag
                      v-if="progress.status"
                      :type="getStatusType(progress.status)"
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ getStatusText(progress.status) }}
                    </el-tag>
                  </div>
                  <div v-if="progress.approvers && progress.approvers.length > 0" class="progress-approvers">
                    <div v-for="approver in progress.approvers" :key="approver.id" class="approver-item">
                      <div class="approver-name">
                        {{ approver.name }}
                        <span v-if="approver.status === 'APPROVED'" style="color: #67C23A;">✓</span>
                        <span v-else-if="approver.status === 'REJECTED'" style="color: #F56C6C;">✗</span>
                        <span v-else style="color: #909399;">待审批</span>
                      </div>
                      <div v-if="approver.comment" class="approver-comment">
                        <span class="comment-label">意见:</span> {{ approver.comment }}
                      </div>
                    </div>
                  </div>
                  <div v-else-if="progress.status === 'NOT_STARTED'" class="progress-approvers" style="color: #909399; font-style: italic;">
                    尚未到达此阶段
                  </div>
                  <div class="progress-status">
                    <span v-if="progress.approveTime" style="color: #909399; font-size: 12px">
                      {{ progress.approveTime }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 子流程进度 -->
          <div v-if="subWorkflowProgressView.length > 0">
            <div class="workflow-section-header sub-workflow">
              <el-icon style="color: #E6A23C; margin-right: 8px;"><Folder /></el-icon>
              <span class="workflow-section-title">子流程审批进度</span>
            </div>
            <div class="progress-list sub-workflow-list">
              <div
                v-for="(progress, index) in subWorkflowProgressView"
                :key="'sub-' + (progress.id || progress.stageId)"
                class="progress-item sub-workflow-item"
                :class="{
                  'active': progress.status === 'PENDING',
                  'approved': progress.status === 'APPROVED',
                  'rejected': progress.status === 'REJECTED',
                  'returned': progress.status === 'RETURNED',
                  'not-started': progress.status === 'NOT_STARTED'
                }"
              >
                <div class="progress-icon">
                  <el-icon v-if="progress.status === 'PENDING'"><Clock /></el-icon>
                  <el-icon v-else-if="progress.status === 'APPROVED'"><SuccessFilled /></el-icon>
                  <el-icon v-else-if="progress.status === 'REJECTED'"><CircleCloseFilled /></el-icon>
                  <el-icon v-else-if="progress.status === 'RETURNED'"><WarningFilled /></el-icon>
                  <el-icon v-else><MoreFilled /></el-icon>
                </div>
                <div class="progress-content">
                  <div class="progress-stage">
                    {{ progress.stageName }}
                    <el-tag
                      v-if="progress.status"
                      :type="getStatusType(progress.status)"
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ getStatusText(progress.status) }}
                    </el-tag>
                  </div>
                  <div v-if="progress.approvers && progress.approvers.length > 0" class="progress-approvers">
                    <div v-for="approver in progress.approvers" :key="approver.id" class="approver-item">
                      <div class="approver-name">
                        {{ approver.name }}
                        <span v-if="approver.status === 'APPROVED'" style="color: #67C23A;">✓</span>
                        <span v-else-if="approver.status === 'REJECTED'" style="color: #F56C6C;">✗</span>
                        <span v-else style="color: #909399;">待审批</span>
                      </div>
                      <div v-if="approver.comment" class="approver-comment">
                        <span class="comment-label">意见:</span> {{ approver.comment }}
                      </div>
                    </div>
                  </div>
                  <div v-else-if="progress.status === 'NOT_STARTED'" class="progress-approvers" style="color: #909399; font-style: italic;">
                    尚未到达此阶段
                  </div>
                  <div class="progress-status">
                    <span v-if="progress.approveTime" style="color: #909399; font-size: 12px">
                      {{ progress.approveTime }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- 当前待审批人 -->
        <div v-if="viewDetail.pendingApprovers && viewDetail.pendingApprovers.length > 0" style="margin-top: 20px">
          <h4>当前待审批人</h4>
          <div>
            <el-tag
              v-for="approver in viewDetail.pendingApprovers"
              :key="approver.id"
              type="warning"
              style="margin-right: 10px"
            >
              {{ approver.name }}
            </el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showViewDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, SuccessFilled, CircleCloseFilled, WarningFilled, Document, Folder, MoreFilled } from '@element-plus/icons-vue'
import { getPendingApproval, getTaskDetail, getInstanceDetail, approve, returnTask } from '@/api/task'
import { selectNextStageApproversWithSubWorkflows } from '@/api/workflow'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const showApproveDialog = ref(false)
const showViewDialog = ref(false)
const loadingDetail = ref(false)
const submitting = ref(false)
const currentTask = ref<any>(null)
const approveForm = reactive({ comment: '' })
const showReturnReasonInput = ref(false)
const returnForm = reactive({ reason: '' })

// 下一层审批人选择
const selectedNextApprovers = ref<Record<number, number>>({})

// 子流程审批人选择
const selectedSubWorkflowApprovers = ref<Record<number, Record<number, number>>>({})

// 任务详情
const taskDetail = ref<any>({
  id: null,
  applicationId: null,
  businessName: '',
  applicationTitle: '',
  workflowName: '',
  currentStageName: '',
  stageName: '',
  applicantName: '',
  approveType: '',
  status: '',
  assetType: '',
  assetCount: null,
  createTime: '',
  canSelectNextApprovers: false,
  nextStageId: null,
  nextStageName: '',
  nextStageApproveType: '',
  otherApprovers: [],
  selectedNextApprovers: [],
  approvalProgress: [],
  hasSubWorkflows: false,
  subWorkflows: [],
  pendingApprovers: []
})

// 只读详情
const viewDetail = ref<any>({
  id: null,
  applicationId: null,
  businessName: '',
  applicationTitle: '',
  workflowName: '',
  currentStageName: '',
  stageName: '',
  applicantName: '',
  status: '',
  createTime: '',
  approvalProgress: [],
  assetList: []
})

// 分离主流程和子流程进度
const mainWorkflowProgress = computed(() => {
  if (!taskDetail.value || !taskDetail.value.approvalProgress) return []
  return taskDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow !== 1)
})

const subWorkflowProgress = computed(() => {
  if (!taskDetail.value || !taskDetail.value.approvalProgress) return []
  return taskDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow === 1)
})

// 只读详情的主流程和子流程进度
const mainWorkflowProgressView = computed(() => {
  if (!viewDetail.value || !viewDetail.value.approvalProgress) return []
  return viewDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow !== 1)
})

const subWorkflowProgressView = computed(() => {
  if (!viewDetail.value || !viewDetail.value.approvalProgress) return []
  return viewDetail.value.approvalProgress.filter((p: any) => p.isSubWorkflow === 1)
})

async function loadData() {
  loading.value = true
  try {
    const res = await getPendingApproval(query)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function handleOpenDetail(row: any) {
  currentTask.value = row
  showApproveDialog.value = true
  loadingDetail.value = true

  approveForm.comment = ''
  selectedNextApprovers.value = {}
  selectedSubWorkflowApprovers.value = {}

  try {
    // 修改为调用 getTaskDetail 获取任务详情（包含下一层审批人选择信息）
    const res = await getTaskDetail(row.id)
    taskDetail.value = res.data

    if (res.data.canSelectNextApprovers && res.data.nextStageId && res.data.nextStageApproverConfigs) {
      selectedNextApprovers.value = {}
    }

    if (res.data.hasSubWorkflows && res.data.subWorkflows && res.data.subWorkflows.length > 0 && res.data.canSelectNextApprovers) {
      for (const subWorkflow of res.data.subWorkflows) {
        selectedSubWorkflowApprovers.value[subWorkflow.id] = {}
      }
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载详情失败')
  } finally {
    loadingDetail.value = false
  }
}

async function handleViewDetail(row: any) {
  showViewDialog.value = true
  loadingDetail.value = true

  try {
    const res = await getInstanceDetail(row.instanceId)
    viewDetail.value = res.data
  } catch (e: any) {
    ElMessage.error(e.message || '加载详情失败')
  } finally {
    loadingDetail.value = false
  }
}

// 处理普通审批人选择变化（或签时只允许选1个）
function handleNormalApproverChange() {
  if (taskDetail.value.nextStageApproveType === 'OR') {
    const selectedKeys = Object.keys(selectedNextApprovers.value).filter(
      key => selectedNextApprovers.value[key] !== null && selectedNextApprovers.value[key] !== undefined
    )
    if (selectedKeys.length > 1) {
      const lastKey = selectedKeys[selectedKeys.length - 1]
      const lastValue = selectedNextApprovers.value[lastKey]
      selectedNextApprovers.value = {}
      selectedNextApprovers.value[lastKey] = lastValue
    }
  }
}

// 处理子流程审批人选择变化（或签时只允许选1个）
function handleSubWorkflowApproverChange(subWorkflowId: number) {
  const subWorkflow = taskDetail.value.subWorkflows?.find((sw: any) => sw.id === subWorkflowId)
  if (subWorkflow && subWorkflow.approveType === 'OR') {
    const subSelected = selectedSubWorkflowApprovers.value[subWorkflowId]
    if (subSelected) {
      const selectedKeys = Object.keys(subSelected).filter(
        key => subSelected[key] !== null && subSelected[key] !== undefined
      )
      if (selectedKeys.length > 1) {
        const lastKey = selectedKeys[selectedKeys.length - 1]
        const lastValue = subSelected[lastKey]
        selectedSubWorkflowApprovers.value[subWorkflowId] = {}
        selectedSubWorkflowApprovers.value[subWorkflowId][lastKey] = lastValue
      }
    }
  }
}

async function submitApprove(passed: boolean) {
  if (passed && taskDetail.value.canSelectNextApprovers) {
    const configs = taskDetail.value.nextStageApproverConfigs || []
    const isOrSign = taskDetail.value.nextStageApproveType === 'OR'

    if (isOrSign) {
      const selectedCount = Object.values(selectedNextApprovers.value).filter(v => v !== null && v !== undefined).length

      if (selectedCount === 0) {
        ElMessage.warning('请从普通审批人配置中选择至少 1 个审批人')
        return
      }

      if (taskDetail.value.hasSubWorkflows && taskDetail.value.subWorkflows) {
        for (const subWorkflow of taskDetail.value.subWorkflows) {
          const subSelected = selectedSubWorkflowApprovers.value[subWorkflow.id]
          const subConfigs = subWorkflow.approverConfigs || []
          const subSelectedCount = subSelected ? Object.values(subSelected).filter(v => v !== null && v !== undefined).length : 0

          if (subWorkflow.approveType === 'OR') {
            if (subSelectedCount === 0) {
              ElMessage.warning(`请为子流程"${subWorkflow.name}"选择至少 1 位审批人（或签）`)
              return
            }
          } else {
            if (subSelectedCount < subConfigs.length) {
              ElMessage.warning(`请为子流程"${subWorkflow.name}"选择所有配置项的审批人（已选择 ${subSelectedCount}/${subConfigs.length}）`)
              return
            }
          }
        }
      }
    } else {
      const selectedCount = Object.keys(selectedNextApprovers.value).length
      const requiredCount = configs.length

      if (selectedCount === 0) {
        ElMessage.warning('请选择下一层审批人')
        return
      }

      if (selectedCount < requiredCount) {
        ElMessage.warning(`请为所有配置项选择审批人（已选择 ${selectedCount}/${requiredCount}）`)
        return
      }

      if (taskDetail.value.hasSubWorkflows && taskDetail.value.subWorkflows) {
        for (const subWorkflow of taskDetail.value.subWorkflows) {
          const subSelected = selectedSubWorkflowApprovers.value[subWorkflow.id]
          const subConfigs = subWorkflow.approverConfigs || []
          const subSelectedCount = subSelected ? Object.keys(subSelected).length : 0

          if (subConfigs.length > 0 && subSelectedCount < subConfigs.length) {
            ElMessage.warning(`请为子流程"${subWorkflow.name}"选择所有配置项的审批人（已选择 ${subSelectedCount}/${subConfigs.length}）`)
            return
          }
        }
      }
    }
  }

  submitting.value = true
  try {
    if (passed && taskDetail.value.canSelectNextApprovers) {
      const configs = taskDetail.value.nextStageApproverConfigs || []
      const approverIds: number[] = []
      for (const config of configs) {
        const selectedUserId = selectedNextApprovers.value[config.configId]
        if (selectedUserId) {
          approverIds.push(selectedUserId)
        }
      }

      const subWorkflowApproverIds: Record<number, number[]> = {}
      if (taskDetail.value.hasSubWorkflows && taskDetail.value.subWorkflows) {
        for (const subWorkflow of taskDetail.value.subWorkflows) {
          const subConfigs = subWorkflow.approverConfigs || []
          const subApproverIds: number[] = []
          for (const subConfig of subConfigs) {
            const subSelected = selectedSubWorkflowApprovers.value[subWorkflow.id]
            if (subSelected && subSelected[subConfig.configId]) {
              subApproverIds.push(subSelected[subConfig.configId])
            }
          }
          if (subApproverIds.length > 0) {
            subWorkflowApproverIds[subWorkflow.id] = subApproverIds
          }
        }
      }

      if (approverIds.length > 0 || Object.keys(subWorkflowApproverIds).length > 0) {
        await selectNextStageApproversWithSubWorkflows({
          taskId: currentTask.value.id,
          approverIds: approverIds,
          subWorkflowApproverIds: subWorkflowApproverIds
        })
      }
    }

    await approve(currentTask.value.id, approveForm.comment, passed)

    ElMessage.success(passed ? '审批通过' : '已驳回')
    showApproveDialog.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 退回到上一层级
async function submitReturn() {
  submitting.value = true
  try {
    await returnTask(currentTask.value.id, returnForm.reason)
    ElMessage.success('退回成功')
    showApproveDialog.value = false
    showReturnReasonInput.value = false
    returnForm.reason = ''
    loadData()
  } catch (e: any) {
    ElMessage.error('退回失败: ' + (e.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  approveForm.comment = ''
  returnForm.reason = ''
  showReturnReasonInput.value = false
  selectedNextApprovers.value = {}
  selectedSubWorkflowApprovers.value = {}
  taskDetail.value = {
    businessName: '',
    workflowName: '',
    stageName: '',
    applicantName: '',
    approveType: '',
    canSelectNextApprovers: false,
    nextStageId: null,
    nextStageName: '',
    nextStageApproveType: '',
    otherApprovers: [],
    selectedNextApprovers: [],
    approvalProgress: [],
    hasSubWorkflows: false,
    subWorkflows: []
  }
}

function getStatusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    RETURNED: 'warning',
    NOT_STARTED: 'info'
  }
  return map[status] || 'info'
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    RETURNED: '已退回',
    NOT_STARTED: '未开始'
  }
  return map[status] || status
}

function handleOpenRestartDetail(row: any) {
  // TODO: Implement restart sub workflow dialog
  ElMessage.info('重新发起子流程功能待实现')
}

function getAssetStatusType(status: string) {
  const typeMap: Record<string, string> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'AVAILABLE': 'success',
    'USED': 'info'
  }
  return typeMap[status] || 'info'
}

function getAssetStatusText(status: string) {
  const textMap: Record<string, string> = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已驳回',
    'AVAILABLE': '可用',
    'USED': '已使用'
  }
  return textMap[status] || status
}

// 预览图片 - 使用后端API
function previewImage(asset: any) {
  if (asset.id) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
    const fullUrl = `${baseUrl}/asset/preview/${asset.id}`
    window.open(fullUrl, '_blank')
  } else {
    ElMessage.warning('素材ID为空，无法预览')
  }
}

// 预览视频 - 使用后端API
function previewVideo(asset: any) {
  if (asset.id) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
    const fullUrl = `${baseUrl}/asset/preview/${asset.id}`
    window.open(fullUrl, '_blank')
  } else {
    ElMessage.warning('素材ID为空，无法预览')
  }
}

// 下载附件 - 使用后端API
async function downloadAttachment(filePath: string, assetName: string) {
  try {
    // 注意：filePath 是版权附件路径，不是素材路径，需要直接访问
    // 检查是否是绝对路径（Windows 盘符开头）
    const isAbsolutePath = /^[A-Za-z]:/.test(filePath)
    const isHttpUrl = filePath.startsWith('http://') || filePath.startsWith('https://')

    let downloadUrl: string
    if (isHttpUrl) {
      downloadUrl = filePath
    } else if (isAbsolutePath) {
      // 绝对路径无法直接访问，提示用户
      ElMessage.warning('版权附件仅支持在线查看，暂不支持下载')
      return
    } else {
      // 相对路径，拼接baseUrl
      const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
      downloadUrl = `${baseUrl}${filePath}`
    }

    // 创建隐藏的a标签触发下载
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `${assetName}-附件.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e: any) {
    ElMessage.error('下载失败: ' + (e.message || '未知错误'))
  }
}

onMounted(loadData)
</script>

<style scoped>
.progress-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  background: #e6f7ff;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
  transition: all 0.3s;
}

/* 子流程列表样式 */
.sub-workflow-list {
  padding-left: 0;
}

.sub-workflow-item {
  background: #fffbf0;
  border-left-color: #E6A23C;
}

.progress-item.active {
  border-left-color: #1890ff;
  background: #bae7ff;
}

.sub-workflow-item.active {
  background: #fff3e0;
  border-left-color: #FF9800;
}

.progress-item.approved {
  border-left-color: #52c41a;
  background: #d9f7be;
}

.sub-workflow-item.approved {
  background: #e8f5e9;
}

.progress-item.rejected {
  border-left-color: #ff4d4f;
  background: #ffccc7;
}

.sub-workflow-item.rejected {
  background: #ffebee;
}

.progress-item.returned {
  border-left-color: #FA8C16;
  background: #fff7e6;
}

.sub-workflow-item.returned {
  background: #fff7e6;
}

.progress-item.not-started {
  border-left-color: #d9d9d9;
  background: #f5f5f5;
  opacity: 0.7;
}

.sub-workflow-item.not-started {
  background: #ffebee;
}

.progress-icon {
  margin-right: 12px;
  font-size: 20px;
}

.progress-item.active .progress-icon {
  color: #1890ff;
}

.progress-item.approved .progress-icon {
  color: #52c41a;
}

.progress-item.rejected .progress-icon {
  color: #ff4d4f;
}

.progress-item.returned .progress-icon {
  color: #FA8C16;
}

.progress-content {
  flex: 1;
}

.progress-stage {
  font-weight: bold;
  margin-bottom: 5px;
}

.progress-approvers {
  margin: 5px 0;
  color: #606266;
}

.approver-item {
  margin-bottom: 8px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
}

.approver-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.approver-comment {
  margin-top: 4px;
  padding: 6px 10px;
  background: #fff;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  border-left: 2px solid #E6A23C;
}

.comment-label {
  font-weight: bold;
  color: #909399;
  margin-right: 4px;
}

.progress-status {
  display: flex;
  align-items: center;
}

/* 素材列表样式 */
.asset-list-header {
  font-weight: bold;
  color: #606266;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}

/* 流程区域标题 */
.workflow-section-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(90deg, #e6f7ff 0%, #ffffff 100%);
  border-left: 4px solid #409EFF;
  border-radius: 4px;
  margin-bottom: 15px;
}

.workflow-section-header.sub-workflow {
  background: linear-gradient(90deg, #fffbf0 0%, #ffffff 100%);
  border-left-color: #E6A23C;
}

.workflow-section-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
}

h4 {
  margin: 15px 0 10px;
  color: #303133;
}
</style>
