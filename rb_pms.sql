/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : rb_pms

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 29/06/2026 15:05:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for complaint
-- ----------------------------
DROP TABLE IF EXISTS `complaint`;
CREATE TABLE `complaint`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_id` bigint(20) NOT NULL COMMENT '业主ID',
  `room_id` bigint(20) NOT NULL COMMENT '房间ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'COMPLAINT' COMMENT '类型：COMPLAINT投诉/SUGGESTION建议',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/DONE/REJECTED/APPROVED/CANCELLED/待处理/处理中/已完成/已驳回/建议被采纳/已取消',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '回复内容',
  `reply_date` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_complaint_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_complaint_room` FOREIGN KEY (`room_id`) REFERENCES `prop_unit` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '投诉建议' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of complaint
-- ----------------------------
INSERT INTO `complaint` VALUES (1, 1, 1, '楼道垃圾未及时清理', '3号楼1单元楼道垃圾堆放超过3天未清理，影响生活环境。', 'COMPLAINT', 'PROCESSING', '已安排保洁人员处理，预计今日完成。', '2025-03-10 09:00:00', '2026-06-23 09:42:38');
INSERT INTO `complaint` VALUES (2, 2, 5, '建议增设快递柜', '小区快递经常丢失，建议增设智能快递柜。', 'SUGGESTION', 'PENDING', '下半年会开设的', '2026-06-24 18:46:06', '2026-06-23 09:42:38');
INSERT INTO `complaint` VALUES (3, 3, 1, '夜间施工噪音', '小区附近夜间施工噪音过大，影响休息。', 'COMPLAINT', 'PROCESSING', '已联系施工方协调施工时间', '2026-06-22 00:00:00', '2026-06-23 16:31:44');
INSERT INTO `complaint` VALUES (4, 5, 6, '建议增加儿童游乐设施', '小区内儿童游乐设施太少，建议增加。', 'SUGGESTION', 'PROCESSING', NULL, NULL, '2026-06-23 16:31:44');
INSERT INTO `complaint` VALUES (5, 7, 1, '电梯故障频发', '1号楼电梯近期频繁故障，存在安全隐患。', 'COMPLAINT', 'DONE', '已安排电梯维保公司全面检修', '2026-06-21 00:00:00', '2026-06-23 16:31:44');
INSERT INTO `complaint` VALUES (6, 1, 1, '噪音扰民', '楼上住户深夜走动噪音大，影响休息', 'COMPLAINT', 'DONE', '已上门协调，业主承诺注意', '2026-06-18 00:00:00', '2026-06-23 16:35:26');
INSERT INTO `complaint` VALUES (7, 2, 5, '建议增设垃圾桶', '小区垃圾投放点过少，建议增设', 'SUGGESTION', 'APPROVED', '已纳入采购计划', '2026-06-20 00:00:00', '2026-06-23 16:35:26');
INSERT INTO `complaint` VALUES (8, 3, 1, '物业费不透明', '物业费明细不清楚，要求公示', 'COMPLAINT', 'PROCESSING', '正在整理费用明细', '2026-06-22 00:00:00', '2026-06-23 16:35:26');
INSERT INTO `complaint` VALUES (9, 4, 3, '建议增加绿化', '小区绿化面积不足，建议增加', 'SUGGESTION', 'PENDING', NULL, NULL, '2026-06-23 16:35:26');
INSERT INTO `complaint` VALUES (10, 5, 6, '公摊电费过高', '每季度公摊电费过高，要求核查', 'COMPLAINT', 'PROCESSING', '已联系电力公司核查', '2026-06-21 00:00:00', '2026-06-23 16:35:26');
INSERT INTO `complaint` VALUES (11, 6, 4, '建议增设充电桩', '建议增设新能源车充电桩', 'SUGGESTION', 'PENDING', NULL, NULL, '2026-06-23 16:35:26');
INSERT INTO `complaint` VALUES (12, 7, 1, '公共区域卫生差', '楼道公共区域清洁不到位', 'COMPLAINT', 'DONE', '已加强保洁频次', '2026-06-16 00:00:00', '2026-06-23 16:35:26');
INSERT INTO `complaint` VALUES (13, 1, 1, '11', '11', 'COMPLAINT', 'PENDING', NULL, NULL, '2026-06-25 09:40:55');

-- ----------------------------
-- Table structure for complaint_reply
-- ----------------------------
DROP TABLE IF EXISTS `complaint_reply`;
CREATE TABLE `complaint_reply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `complaint_id` bigint(20) NOT NULL,
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `admin_id` bigint(20) NULL DEFAULT NULL,
  `owner_id` bigint(20) NULL DEFAULT NULL,
  `admin_read` tinyint(1) NULL DEFAULT 0,
  `owner_read` tinyint(1) NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_complaint_id`(`complaint_id` ASC) USING BTREE,
  CONSTRAINT `fk_reply_complaint` FOREIGN KEY (`complaint_id`) REFERENCES `complaint` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of complaint_reply
-- ----------------------------
INSERT INTO `complaint_reply` VALUES (1, 1, '你好业主已经打扫完成啦', NULL, NULL, 1, 0, '2026-06-24 19:59:27');
INSERT INTO `complaint_reply` VALUES (2, 4, '好的', NULL, NULL, 1, 0, '2026-06-24 20:07:22');
INSERT INTO `complaint_reply` VALUES (3, 13, '11', NULL, NULL, 1, 0, '2026-06-25 09:42:48');
INSERT INTO `complaint_reply` VALUES (4, 13, '11', NULL, NULL, 1, 0, '2026-06-25 09:42:55');
INSERT INTO `complaint_reply` VALUES (5, 1, '好的', NULL, 1, 1, 0, '2026-06-26 17:21:09');
INSERT INTO `complaint_reply` VALUES (6, 1, '好的1', NULL, NULL, 1, 0, '2026-06-26 17:22:10');
INSERT INTO `complaint_reply` VALUES (7, 1, '三号楼这又有垃圾了', NULL, 1, 1, 0, '2026-06-29 14:36:37');
INSERT INTO `complaint_reply` VALUES (8, 1, '好的，马上处理', NULL, NULL, 1, 0, '2026-06-29 14:43:48');

-- ----------------------------
-- Table structure for decoration_record
-- ----------------------------
DROP TABLE IF EXISTS `decoration_record`;
CREATE TABLE `decoration_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id` bigint(20) NOT NULL COMMENT '房间ID',
  `owner_id` bigint(20) NOT NULL COMMENT '业主ID',
  `decoration_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '装修类型：REMODELING翻新/RENOVATION改造/NEW新建',
  `start_date` date NOT NULL COMMENT '开工日期',
  `end_date` date NULL DEFAULT NULL COMMENT '完工日期',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '状态：PENDING待批/APPROVED批准/REJECTED驳回/IN_PROGRESS施工中/DONE完工',
  `approval_date` date NULL DEFAULT NULL COMMENT '批准日期',
  `contractor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '施工单位',
  `supervisor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '监理人',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  CONSTRAINT `fk_decoration_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_decoration_room` FOREIGN KEY (`room_id`) REFERENCES `prop_unit` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '装修管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of decoration_record
-- ----------------------------
INSERT INTO `decoration_record` VALUES (1, 1, 1, 'REMODELING', '2025-02-15', '2025-03-20', 'IN_PROGRESS', '2025-02-10', '装修公司C', '张监理', '厨房改造', '2026-06-23 09:42:38');
INSERT INTO `decoration_record` VALUES (2, 8, 6, 'NEW', '2025-01-10', '2025-02-28', 'DONE', '2025-01-05', '装修公司D', '李监理', '精装修交付', '2026-06-23 09:42:38');
INSERT INTO `decoration_record` VALUES (3, 4, 6, 'RENOVATION', '2026-06-13', NULL, 'IN_PROGRESS', '2026-06-15', '装修公司E', '王监理', '客厅改造', '2026-06-23 16:31:44');
INSERT INTO `decoration_record` VALUES (4, 2, 9, 'NEW', '2026-06-03', '2026-06-18', 'DONE', '2026-06-08', '装修公司F', '刘监理', '精装修交付', '2026-06-23 16:31:44');
INSERT INTO `decoration_record` VALUES (5, 8, 10, 'REMODELING', '2026-06-26', '2026-12-24', 'APPROVED', '2026-06-24', '装修公司G', '李监理', '商铺装修', '2026-06-23 16:31:44');
INSERT INTO `decoration_record` VALUES (6, 3, 4, 'REMODELING', '2026-06-03', NULL, 'IN_PROGRESS', '2026-06-08', '装修公司H', '张监理', '全屋翻新', '2026-06-23 16:35:26');
INSERT INTO `decoration_record` VALUES (7, 4, 6, 'RENOVATION', '2026-06-18', '2026-07-18', 'APPROVED', '2026-06-20', '装修公司I', '王监理', '客厅改造', '2026-06-23 16:35:26');
INSERT INTO `decoration_record` VALUES (8, 8, 3, 'RENOVATION', '2026-05-24', '2026-06-18', 'DONE', '2026-05-29', '装修公司J', '李监理', '精装修', '2026-06-23 16:35:26');
INSERT INTO `decoration_record` VALUES (9, 10, 16, 'NEW', '2026-06-28', NULL, 'PENDING', NULL, '装修公司K', '刘监理', '新装修', '2026-06-23 16:35:26');
INSERT INTO `decoration_record` VALUES (10, 12, 12, 'REMODELING', '2026-06-13', '2026-07-13', 'IN_PROGRESS', '2026-06-16', '装修公司L', '赵监理', '卧室改造', '2026-06-23 16:35:26');

-- ----------------------------
-- Table structure for facility
-- ----------------------------
DROP TABLE IF EXISTS `facility`;
CREATE TABLE `facility`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `facility_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备编号',
  `facility_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备名称',
  `facility_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备类型：电梯/消防/供电/供水/门禁/监控',
  `building_id` bigint(20) NOT NULL COMMENT '所在楼栋ID',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '具体位置',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1正常 2维修中 0停用',
  `install_date` date NULL DEFAULT NULL COMMENT '安装日期',
  `warranty_end` date NULL DEFAULT NULL COMMENT '保修截止日期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_facility_no`(`facility_no` ASC) USING BTREE,
  INDEX `idx_building_id`(`building_id` ASC) USING BTREE,
  CONSTRAINT `fk_facility_building` FOREIGN KEY (`building_id`) REFERENCES `prop_building` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备设施' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of facility
-- ----------------------------
INSERT INTO `facility` VALUES (1, 'ELEV-CH1-01', '1号楼1号电梯', '电梯', 1, '1号楼东侧', 1, '2020-01-01', '2025-12-31', '2026-06-23 09:42:38');
INSERT INTO `facility` VALUES (2, 'ELEV-CH1-02', '1号楼2号电梯', '电梯', 1, '1号楼西侧', 1, '2020-01-01', '2025-12-31', '2026-06-23 09:42:38');
INSERT INTO `facility` VALUES (3, 'FIRE-CH1-01', '1号楼消防系统', '消防', 1, '1号楼一楼', 1, '2020-06-01', '2026-05-31', '2026-06-23 09:42:38');
INSERT INTO `facility` VALUES (4, 'ELEV-CH2-01', '2号楼1号电梯', '电梯', 2, '2号楼南侧', 0, '2020-03-01', '2025-12-31', '2026-06-23 09:42:38');
INSERT INTO `facility` VALUES (5, 'ELEV-CH3-01', '3号楼1号电梯', '电梯', 4, '3号楼东侧', 1, '2022-01-01', '2027-12-31', '2026-06-23 16:31:44');
INSERT INTO `facility` VALUES (6, 'WATER-CH2-01', '2号楼供水系统', '供水', 2, '2号楼地下室', 1, '2020-06-01', '2025-12-31', '2026-06-23 16:31:44');
INSERT INTO `facility` VALUES (7, 'DOOR-CH1-01', '1号楼门禁系统', '门禁', 1, '1号楼大堂', 1, '2021-03-01', '2026-03-01', '2026-06-23 16:31:44');
INSERT INTO `facility` VALUES (8, 'ELEV-CH4-01', '4号楼1号电梯', '电梯', 4, '4号楼东侧', 1, '2023-01-01', '2028-12-31', '2026-06-23 16:35:26');
INSERT INTO `facility` VALUES (9, 'ELEV-CH4-02', '4号楼2号电梯', '电梯', 4, '4号楼西侧', 1, '2023-01-01', '2028-12-31', '2026-06-23 16:35:26');
INSERT INTO `facility` VALUES (10, 'ELEV-CH5-01', '5号楼1号电梯', '电梯', 5, '5号楼北侧', 2, '2022-06-01', '2027-06-01', '2026-06-23 16:35:26');
INSERT INTO `facility` VALUES (11, 'GATE-CH1-01', '1号楼门禁系统', '门禁', 1, '1号楼大堂', 1, '2021-03-01', '2026-03-01', '2026-06-23 16:35:26');
INSERT INTO `facility` VALUES (12, 'GATE-CH2-01', '2号楼门禁系统', '门禁', 2, '2号楼大堂', 2, '2021-05-01', '2026-05-01', '2026-06-23 16:35:26');
INSERT INTO `facility` VALUES (13, 'FIRE-CH2-01', '2号楼消防系统', '消防', 2, '2号楼负一层', 1, '2020-08-01', '2025-08-01', '2026-06-23 16:35:26');
INSERT INTO `facility` VALUES (14, 'WATER-CH3-01', '3号楼供水系统', '供水', 4, '3号楼地下室', 1, '2022-03-01', '2027-03-01', '2026-06-23 16:35:26');
INSERT INTO `facility` VALUES (15, 'MONITOR-01', '小区监控系统', '监控', 1, '物业中心', 1, '2021-01-01', '2026-01-01', '2026-06-23 16:35:26');

-- ----------------------------
-- Table structure for facility_maintenance
-- ----------------------------
DROP TABLE IF EXISTS `facility_maintenance`;
CREATE TABLE `facility_maintenance`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `facility_id` bigint(20) NOT NULL COMMENT '设备ID',
  `maintenance_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '维保类型：ROUTINE例行/REPAIR维修/INSPECTION巡检',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '维保标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '维保内容',
  `maintenance_date` date NOT NULL COMMENT '维保日期',
  `maintenance_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '维保人员',
  `cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '费用',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DONE' COMMENT '状态：DONE/PENDING',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_facility_id`(`facility_id` ASC) USING BTREE,
  CONSTRAINT `fk_maintenance_facility` FOREIGN KEY (`facility_id`) REFERENCES `facility` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备维保记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of facility_maintenance
-- ----------------------------
INSERT INTO `facility_maintenance` VALUES (1, 1, 'ROUTINE', '电梯例行保养', '1号楼1号电梯季度保养', '2025-03-01', '维保公司A', 800.00, 'DONE', NULL, '2026-06-23 09:42:38');
INSERT INTO `facility_maintenance` VALUES (2, 1, 'REPAIR', '电梯异响维修', '运行时存在异响，已更换轴承', '2025-02-15', '维保公司A', 1200.00, 'DONE', '已修复', '2026-06-23 09:42:38');
INSERT INTO `facility_maintenance` VALUES (3, 3, 'INSPECTION', '消防系统季度检查', '1号楼消防设施全面检查', '2025-03-10', '消防维保公司B', 500.00, 'DONE', '一切正常', '2026-06-23 09:42:38');
INSERT INTO `facility_maintenance` VALUES (4, 4, 'REPAIR', '2号楼电梯维修', '电梯运行异响，已更换配件', '2026-06-20', '维保公司C', 1500.00, 'DONE', '已恢复正常', '2026-06-23 16:31:44');
INSERT INTO `facility_maintenance` VALUES (5, 5, 'ROUTINE', '供水系统保养', '2号楼供水系统季度保养', '2026-06-18', '维保公司D', 600.00, 'DONE', NULL, '2026-06-23 16:31:44');
INSERT INTO `facility_maintenance` VALUES (6, 6, 'INSPECTION', '门禁系统巡检', '1号楼门禁系统季度巡检', '2026-06-22', '维保公司E', 300.00, 'DONE', '一切正常', '2026-06-23 16:31:44');
INSERT INTO `facility_maintenance` VALUES (7, 4, 'ROUTINE', '2号楼电梯保养', '2号楼电梯例行季度保养', '2026-06-13', '维保公司A', 800.00, 'DONE', NULL, '2026-06-23 16:35:26');
INSERT INTO `facility_maintenance` VALUES (8, 5, 'REPAIR', '4号楼电梯维修', '电梯运行卡顿，已更换导轨', '2026-06-16', '维保公司B', 2000.00, 'DONE', '已恢复正常', '2026-06-23 16:35:26');
INSERT INTO `facility_maintenance` VALUES (9, 6, 'INSPECTION', '5号楼电梯巡检', '5号楼电梯安全巡检', '2026-06-18', '维保公司C', 400.00, 'DONE', '一切正常', '2026-06-23 16:35:26');
INSERT INTO `facility_maintenance` VALUES (10, 7, 'REPAIR', '1号楼门禁维修', '门禁系统频繁故障', '2026-06-20', '维保公司D', 600.00, 'DONE', '已更换主板', '2026-06-23 16:35:26');
INSERT INTO `facility_maintenance` VALUES (11, 8, 'INSPECTION', '2号楼门禁巡检', '2号楼门禁系统季度巡检', '2026-06-21', '维保公司E', 300.00, 'DONE', '正常', '2026-06-23 16:35:26');
INSERT INTO `facility_maintenance` VALUES (12, 9, 'ROUTINE', '2号楼消防检测', '2号楼消防系统季度检测', '2026-06-19', '消防维保公司A', 500.00, 'DONE', '合格', '2026-06-23 16:35:26');

-- ----------------------------
-- Table structure for house_purchase_application
-- ----------------------------
DROP TABLE IF EXISTS `house_purchase_application`;
CREATE TABLE `house_purchase_application`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `unit_id` bigint(20) NOT NULL,
  `building_id` bigint(20) NOT NULL,
  `unit_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `owner_id` bigint(20) NOT NULL,
  `purchase_amount` decimal(15, 2) NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING',
  `approval_date` datetime NULL DEFAULT NULL,
  `approval_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_unit_id`(`unit_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of house_purchase_application
-- ----------------------------
INSERT INTO `house_purchase_application` VALUES (3, 11, 4, '3-201', 1, NULL, NULL, 'CANCELLED', NULL, NULL, '2026-06-29 09:19:02', '2026-06-29 09:20:19');
INSERT INTO `house_purchase_application` VALUES (4, 11, 4, '3-201', 1, 1320000.00, NULL, 'COMPLETED', '2026-06-29 09:32:51', '', '2026-06-29 09:29:14', '2026-06-29 09:47:42');

-- ----------------------------
-- Table structure for inspection_record
-- ----------------------------
DROP TABLE IF EXISTS `inspection_record`;
CREATE TABLE `inspection_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `building_id` bigint(20) NOT NULL COMMENT '楼栋ID',
  `room_id` bigint(20) NOT NULL COMMENT '房间ID',
  `inspector_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '检查员',
  `inspector_id` bigint(20) NULL DEFAULT NULL COMMENT '检查员用户ID',
  `score` int(11) NOT NULL COMMENT '得分(0-100)',
  `check_date` date NULL DEFAULT NULL COMMENT '检查日期',
  `issues` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '问题描述',
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PASS' COMMENT 'PASS/RECTIFY/FAIL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_building_id`(`building_id` ASC) USING BTREE,
  INDEX `idx_check_date`(`check_date` ASC) USING BTREE,
  CONSTRAINT `fk_inspection_building` FOREIGN KEY (`building_id`) REFERENCES `prop_building` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '巡检记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inspection_record
-- ----------------------------
INSERT INTO `inspection_record` VALUES (1, 1, 1, '张管家', 2, 80, '2025-03-01', '走廊有垃圾', 'RECTIFY', '2026-06-23 09:42:38');
INSERT INTO `inspection_record` VALUES (2, 1, 2, '张管家', 2, 78, '2025-03-01', '消防通道堆放杂物', 'RECTIFY', '2026-06-23 09:42:38');
INSERT INTO `inspection_record` VALUES (3, 1, 1, '李管家', NULL, 95, '2026-06-21', NULL, 'PASS', '2026-06-23 16:30:21');
INSERT INTO `inspection_record` VALUES (4, 2, 5, '李管家', NULL, 70, '2026-06-21', '楼道照明不足', 'RECTIFY', '2026-06-23 16:30:21');
INSERT INTO `inspection_record` VALUES (5, 1, 7, '张管家', NULL, 88, '2026-06-18', NULL, 'PASS', '2026-06-23 16:30:21');
INSERT INTO `inspection_record` VALUES (6, 1, 1, '张管家', NULL, 95, '2026-06-13', NULL, 'PASS', '2026-06-23 16:35:26');
INSERT INTO `inspection_record` VALUES (7, 1, 2, '张管家', NULL, 82, '2026-06-13', '墙角潮湿', 'RECTIFY', '2026-06-23 16:35:26');
INSERT INTO `inspection_record` VALUES (8, 2, 5, '张管家', NULL, 68, '2026-06-15', '安全通道堵塞', 'RECTIFY', '2026-06-23 16:35:26');
INSERT INTO `inspection_record` VALUES (9, 2, 6, '张管家', NULL, 90, '2026-06-15', NULL, 'PASS', '2026-06-23 16:35:26');
INSERT INTO `inspection_record` VALUES (10, 3, 7, '李管家', NULL, 75, '2026-06-18', '灭火器过期', 'RECTIFY', '2026-06-23 16:35:26');
INSERT INTO `inspection_record` VALUES (11, 3, 8, '李管家', NULL, 88, '2026-06-18', NULL, 'PASS', '2026-06-23 16:35:26');
INSERT INTO `inspection_record` VALUES (12, 4, 9, '李管家', NULL, 92, '2026-06-20', NULL, 'PASS', '2026-06-23 16:35:26');
INSERT INTO `inspection_record` VALUES (13, 4, 10, '李管家', NULL, 55, '2026-06-20', '多处问题需整改', 'FAIL', '2026-06-23 16:35:26');
INSERT INTO `inspection_record` VALUES (14, 1, 1, '张管家', NULL, 90, '2026-06-24', '', 'PASS', '2026-06-24 10:09:24');
INSERT INTO `inspection_record` VALUES (15, 8, 1, '万管家', NULL, 90, '2026-06-24', '优秀', 'PASS', '2026-06-24 18:39:12');

-- ----------------------------
-- Table structure for owner
-- ----------------------------
DROP TABLE IF EXISTS `owner`;
CREATE TABLE `owner`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业主编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '关联用户ID',
  `building_id` bigint(20) NULL DEFAULT NULL COMMENT '楼栋ID',
  `room_id` bigint(20) NULL DEFAULT NULL COMMENT '房屋ID',
  `property_cert` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产权证号',
  `unit_section` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属单元',
  `register_date` date NULL DEFAULT NULL COMMENT '登记日期',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1在册 0已迁出',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_owner_no`(`owner_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_building_id`(`building_id` ASC) USING BTREE,
  CONSTRAINT `fk_owner_room` FOREIGN KEY (`room_id`) REFERENCES `prop_unit` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_owner_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '业主' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of owner
-- ----------------------------
INSERT INTO `owner` VALUES (1, 'OWN2024001', '李明', '男', '13800000001', 'liming@property.com', NULL, 3, 4, 11, '湘(2022)不动产权第001号', '1单元', '2022-06-01', 1, '2026-06-23 09:42:38');
INSERT INTO `owner` VALUES (2, 'OWN2024002', '王芳', '女', '13800000002', 'wangfang@property.com', NULL, 4, 2, 5, '湘(2021)不动产权第002号', '5单元', '2021-03-15', 0, '2026-06-23 09:42:38');
INSERT INTO `owner` VALUES (3, 'OWN2024003', '赵强', '男', '13800000003', 'zhaoqiang@property.com', NULL, 5, 1, 1, '湘(2022)不动产权第003号', '1单元', '2022-06-01', 1, '2026-06-23 09:42:38');
INSERT INTO `owner` VALUES (4, 'OWN2024004', '刘洋', '男', '13800000004', 'liuyang@property.com', NULL, 6, 1, 3, '湘(2020)不动产权第004号', '12单元', '2020-09-01', 1, '2026-06-23 09:42:38');
INSERT INTO `owner` VALUES (5, 'OWN2024005', '陈静', '女', '13800000005', 'chenjing@property.com', NULL, 7, 2, 6, '湘(2021)不动产权第005号', '5单元', '2021-03-15', 0, '2026-06-23 09:42:38');
INSERT INTO `owner` VALUES (6, 'OWN2024006', '周磊', '男', '13800000006', 'zhoulei@property.com', NULL, 8, 1, 4, '湘(2023)不动产权第006号', '8单元', '2023-01-10', 1, '2026-06-23 09:42:38');
INSERT INTO `owner` VALUES (7, 'OWN2024007', '吴敏', '女', '13800000007', 'wumin@property.com', NULL, 10, 1, 1, '湘(2023)不动产权第007号', '1单元', '2023-06-01', 1, '2026-06-23 16:29:12');
INSERT INTO `owner` VALUES (8, 'OWN2024008', '郑涛', '男', '13800000008', 'zhengtao@property.com', NULL, 13, 2, 5, '湘(2022)不动产权第008号', '5单元', '2022-08-15', 1, '2026-06-23 16:29:12');
INSERT INTO `owner` VALUES (9, 'OWN2024009', '孙丽', '女', '13800000009', 'sunli@property.com', NULL, NULL, 1, 2, '湘(2023)不动产权第009号', '1单元', '2023-03-20', 0, '2026-06-23 16:29:12');
INSERT INTO `owner` VALUES (10, 'OWN2024010', '钱华', '男', '13800000010', 'qianhua@property.com', NULL, NULL, 3, 7, '湘(2021)不动产权第010号', 'B区', '2021-11-01', 0, '2026-06-23 16:29:12');
INSERT INTO `owner` VALUES (11, 'OWN2024011', '李芳', '女', '13800000011', 'lifang@property.com', NULL, 14, 4, 9, '湘(2024)不动产权第011号', '1单元', '2024-01-15', 1, '2026-06-23 16:35:26');
INSERT INTO `owner` VALUES (12, 'OWN2024012', '王建', '男', '13800000012', 'wangjian@property.com', NULL, NULL, 5, 12, '湘(2023)不动产权第012号', '1单元', '2023-07-01', 0, '2026-06-23 16:35:26');
INSERT INTO `owner` VALUES (13, 'OWN2024013', '张丽', '女', '13800000013', 'zhangli@property.com', NULL, 15, 6, 14, '湘(2024)不动产权第013号', 'S区', '2024-03-20', 1, '2026-06-23 16:35:26');
INSERT INTO `owner` VALUES (14, 'OWN2024014', '刘伟', '男', '13800000014', 'liuwei@property.com', NULL, 16, 7, 17, '湘(2023)不动产权第014号', 'M区', '2023-09-10', 1, '2026-06-23 16:35:26');
INSERT INTO `owner` VALUES (15, 'OWN2024015', '陈芳', '女', '13800000015', 'chenfang@property.com', NULL, NULL, 1, 1, '湘(2024)不动产权第015号', '1单元', '2024-06-01', 0, '2026-06-23 16:35:26');
INSERT INTO `owner` VALUES (16, 'OWN2024016', '赵岩', '男', '13800000016', 'zhaoyan@property.com', NULL, 17, 4, 10, '湘(2024)不动产权第016号', '2单元', '2024-02-01', 1, '2026-06-23 16:35:26');
INSERT INTO `owner` VALUES (17, 'OWN2024017', '黄静', '女', '13800000017', 'huangjing@property.com', NULL, 18, 5, 13, '湘(2023)不动产权第017号', '1单元', '2023-12-01', 1, '2026-06-23 16:35:26');
INSERT INTO `owner` VALUES (24, 'OWN2026001', '谢格逸', '女', '18800000001', 'liming@property.com', NULL, 12, 8, 22, '湘(2024)不动产权第022号', '1单元', '2022-06-01', 1, '2026-06-24 10:33:47');
INSERT INTO `owner` VALUES (25, 'OWN2026002', '田琪', '男', '18964568965', 'tianqi@property.com', NULL, 20, 4, 9, '湘(2024)不动产权第023号', '2单元', '2026-06-25', 1, '2026-06-25 16:17:41');
INSERT INTO `owner` VALUES (26, 'OWN2026003', '陈晨', '男', '15623458963', 'chenchen@property.com', NULL, 21, 4, 9, '湘(2026)不动产权第023号', '3单元', '2026-06-25', 1, '2026-06-25 16:50:46');
INSERT INTO `owner` VALUES (27, 'OWN2026004', '李立', '男', '15678964523', 'lili@property.com', NULL, 22, 4, 13, '湘(2026)不动产权第028号', '4单元', '2026-06-25', 1, '2026-06-25 16:57:11');
INSERT INTO `owner` VALUES (28, 'OWN2026005', '田婷', '男', '15623547896', 'tianting@property.com', NULL, 23, 4, 14, '湘(2026)不动产权第030号', '5单元', '2026-06-25', 1, '2026-06-25 16:58:44');

-- ----------------------------
-- Table structure for parking_application
-- ----------------------------
DROP TABLE IF EXISTS `parking_application`;
CREATE TABLE `parking_application`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parking_space_id` bigint(20) NOT NULL COMMENT '车位ID',
  `owner_id` bigint(20) NOT NULL COMMENT '业主ID',
  `application_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请类型：RENT租赁/RENEW续租/CANCEL退租/TRANSFER转让/CHANGE更换/ADD新增',
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车牌号',
  `vehicle_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车辆类型',
  `rental_duration` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租赁期限：MONTHLY/QUARTERLY/YEARLY',
  `start_date` date NULL DEFAULT NULL COMMENT '租赁开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '租赁结束日期',
  `transfer_to_owner_id` bigint(20) NULL DEFAULT NULL COMMENT '转让目标业主ID',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请原因/备注',
  `purchase_amount` decimal(12, 2) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT 'PENDING待审批/APPROVED已批准/REJECTED已驳回/CANCELLED已取消/DONE已完成',
  `approval_date` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approval_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审批备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parking_space_id`(`parking_space_id` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `fk_parking_app_transfer`(`transfer_to_owner_id` ASC) USING BTREE,
  CONSTRAINT `fk_parking_app_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_parking_app_space` FOREIGN KEY (`parking_space_id`) REFERENCES `parking_space` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_parking_app_transfer` FOREIGN KEY (`transfer_to_owner_id`) REFERENCES `owner` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车位申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of parking_application
-- ----------------------------
INSERT INTO `parking_application` VALUES (1, 4, 8, 'RENT', '湘A·88888', '小型车', 'MONTHLY', '2026-07-01', '2026-07-31', NULL, '申请固定车位', NULL, 'APPROVED', '2026-06-25 10:00:00', '审批通过，请按时缴费', '2026-06-24 09:00:00', '2026-06-25 10:00:00');
INSERT INTO `parking_application` VALUES (2, 7, 10, 'RENT', '湘A·66666', 'SUV', 'QUARTERLY', '2026-06-15', '2026-09-14', NULL, '申请月租车位，用于日常通勤', NULL, 'APPROVED', '2026-06-20 14:30:00', '批准，租期3个月', '2026-06-15 08:30:00', '2026-06-20 14:30:00');
INSERT INTO `parking_application` VALUES (3, 11, 11, 'RENT', '湘A·99999', '小型车', 'YEARLY', '2026-07-01', '2027-06-30', NULL, '长期租赁车位', NULL, 'APPROVED', '2026-06-26 10:40:27', '同意请业主遵循车位管理规则', '2026-06-26 09:00:00', '2026-06-26 10:40:27');
INSERT INTO `parking_application` VALUES (4, 11, 13, 'RENT', '湘A·55555', '小型车', 'MONTHLY', '2026-06-20', '2026-07-19', NULL, '临时租用', NULL, 'REJECTED', '2026-06-22 16:00:00', '该车位已被预定，请选择其他车位', '2026-06-20 10:00:00', '2026-06-22 16:00:00');
INSERT INTO `parking_application` VALUES (5, 13, 14, 'RENT', '湘A·77777', '货车', 'MONTHLY', '2026-06-25', '2026-07-24', NULL, '货车停放需求', NULL, 'PENDING', NULL, NULL, '2026-06-25 14:00:00', '2026-06-25 14:00:00');
INSERT INTO `parking_application` VALUES (7, 2, 4, 'CANCEL', NULL, NULL, NULL, NULL, NULL, NULL, '搬家不再需要使用车位', NULL, 'APPROVED', '2026-06-22 10:00:00', '退租批准，车位已释放', '2026-06-20 11:00:00', '2026-06-22 10:00:00');
INSERT INTO `parking_application` VALUES (8, 6, 7, 'TRANSFER', '湘A·66666', 'SUV', 'QUARTERLY', '2026-06-15', '2026-09-14', 11, '转让给李芳', NULL, 'APPROVED', '2026-06-21 15:00:00', '转让审批通过', '2026-06-19 09:00:00', '2026-06-21 15:00:00');
INSERT INTO `parking_application` VALUES (9, 18, 24, 'RENT', '湘A·11111', '小型车', 'MONTHLY', '2026-07-01', '2026-07-31', NULL, '新业主申请车位', NULL, 'PENDING', NULL, NULL, '2026-06-26 08:00:00', '2026-06-26 08:00:00');
INSERT INTO `parking_application` VALUES (10, 10, 16, 'RENEW', '湘A·22222', 'SUV', 'QUARTERLY', '2026-07-10', '2026-10-09', NULL, '续租3个月', NULL, 'PENDING', NULL, NULL, '2026-06-25 16:30:00', '2026-06-25 16:30:00');
INSERT INTO `parking_application` VALUES (11, 1, 3, 'RENT', '湘A·33333', '小型车', 'MONTHLY', '2026-06-28', '2026-07-27', NULL, '临时停放', NULL, 'REJECTED', '2026-06-24 17:00:00', '该车位已被占用', '2026-06-23 19:00:00', '2026-06-24 17:00:00');
INSERT INTO `parking_application` VALUES (12, 13, 17, 'CANCEL', NULL, NULL, NULL, NULL, NULL, NULL, '已购买私家车位，退租', NULL, 'PENDING', NULL, NULL, '2026-06-26 07:00:00', '2026-06-26 07:00:00');
INSERT INTO `parking_application` VALUES (13, 12, 12, 'RENT', '湘A·44444', '小型车', 'YEARLY', '2026-07-01', '2027-06-30', NULL, '年租车位', NULL, 'APPROVED', '2026-06-23 11:00:00', '批准年租', '2026-06-22 13:00:00', '2026-06-23 11:00:00');
INSERT INTO `parking_application` VALUES (14, 19, 26, 'RENT', '湘A·66688', '小型车', 'MONTHLY', '2026-07-01', '2026-07-31', NULL, '申请月租车位', NULL, 'PENDING', NULL, NULL, '2026-06-26 10:30:00', '2026-06-26 10:30:00');
INSERT INTO `parking_application` VALUES (15, 17, 25, 'RENT', '湘A·77788', 'SUV', 'MONTHLY', '2026-07-01', '2026-07-31', NULL, 'SUV车位需求', NULL, 'PENDING', NULL, NULL, '2026-06-26 11:00:00', '2026-06-26 11:00:00');
INSERT INTO `parking_application` VALUES (27, 1, 1, 'PURCHASE', '湘A·99988', 'SUV', 'PURCHASE', '2026-06-29', NULL, NULL, '', 150000.00, 'DONE', '2026-06-29 14:16:56', '', '2026-06-29 14:16:46', '2026-06-29 14:17:05');
INSERT INTO `parking_application` VALUES (28, 2, 1, 'ADD', '湘A·11223', 'SUV', 'PURCHASE', '2026-06-29', NULL, NULL, '', NULL, 'PENDING', NULL, NULL, '2026-06-29 14:18:34', '2026-06-29 14:18:34');

-- ----------------------------
-- Table structure for parking_space
-- ----------------------------
DROP TABLE IF EXISTS `parking_space`;
CREATE TABLE `parking_space`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `space_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位编号',
  `building_id` bigint(20) NOT NULL COMMENT '所属楼栋ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'FIXED' COMMENT '类型：FIXED固定/TEMP临时',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1空闲 2占用 0停用',
  `owner_id` bigint(20) NULL DEFAULT NULL COMMENT '关联业主ID',
  `area_sqm` decimal(8, 2) NULL DEFAULT NULL COMMENT '面积(㎡)',
  `monthly_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '月租费',
  `purchase_price` decimal(12, 2) NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车牌号',
  `vehicle_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车辆类型：小型车/SUV/货车',
  `rent_start_date` date NULL DEFAULT NULL COMMENT '租赁开始日期',
  `rent_end_date` date NULL DEFAULT NULL COMMENT '租赁结束日期',
  `rental_duration` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租赁期限：MONTHLY/QUARTERLY/YEARLY',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_space_no`(`space_no` ASC) USING BTREE,
  INDEX `idx_building_id`(`building_id` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  CONSTRAINT `fk_parking_building` FOREIGN KEY (`building_id`) REFERENCES `prop_building` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_parking_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车位管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of parking_space
-- ----------------------------
INSERT INTO `parking_space` VALUES (1, 'CH1-001', 1, 'FIXED', 2, 1, 12.50, 300.00, 150000.00, '2026-06-23 09:42:38', '湘A·99988', 'SUV', NULL, NULL, 'PURCHASE');
INSERT INTO `parking_space` VALUES (2, 'CH1-002', 1, 'FIXED', 1, NULL, 12.50, 300.00, 150000.00, '2026-06-23 09:42:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `parking_space` VALUES (3, 'CH2-001', 2, 'FIXED', 2, 2, 12.50, 300.00, 145000.00, '2026-06-23 09:42:38', '湘A·34567', '小型车', '2026-04-01', '2026-06-30', 'QUARTERLY');
INSERT INTO `parking_space` VALUES (4, 'CH2-002', 2, 'FIXED', 1, NULL, 12.50, 300.00, 145000.00, '2026-06-23 09:42:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `parking_space` VALUES (5, 'CH_B-001', 3, 'TEMP', 1, NULL, 15.00, 500.00, NULL, '2026-06-23 09:42:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `parking_space` VALUES (6, 'CH3-001', 4, 'FIXED', 2, 11, 12.50, 300.00, 155000.00, '2026-06-23 16:31:44', '湘A·66666', 'SUV', '2026-06-15', '2026-09-14', 'QUARTERLY');
INSERT INTO `parking_space` VALUES (7, 'CH3-002', 4, 'FIXED', 1, NULL, 12.50, 300.00, 155000.00, '2026-06-23 16:31:44', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `parking_space` VALUES (8, 'CH2-003', 2, 'FIXED', 2, 8, 12.50, 300.00, 145000.00, '2026-06-23 16:31:44', '湘A·88888', '小型车', '2026-07-01', '2026-07-31', 'MONTHLY');
INSERT INTO `parking_space` VALUES (9, 'CH4-001', 4, 'FIXED', 2, 11, 12.50, 300.00, 155000.00, '2026-06-23 16:35:26', '湘A·99999', '小型车', '2026-07-01', '2027-06-30', 'YEARLY');
INSERT INTO `parking_space` VALUES (10, 'CH4-002', 4, 'FIXED', 2, 16, 12.50, 300.00, 155000.00, '2026-06-23 16:35:26', '湘A·22222', 'SUV', '2026-07-10', '2026-10-09', 'QUARTERLY');
INSERT INTO `parking_space` VALUES (11, 'CH4-003', 4, 'FIXED', 2, 11, 12.50, 300.00, 155000.00, '2026-06-23 16:35:26', '湘A·99999', '小型车', '2026-07-01', '2027-06-30', 'YEARLY');
INSERT INTO `parking_space` VALUES (12, 'CH5-001', 5, 'FIXED', 2, 12, 12.50, 300.00, 160000.00, '2026-06-23 16:35:26', '湘A·44444', '小型车', '2026-07-01', '2027-06-30', 'YEARLY');
INSERT INTO `parking_space` VALUES (13, 'CH5-002', 5, 'FIXED', 1, NULL, 12.50, 300.00, 160000.00, '2026-06-23 16:35:26', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `parking_space` VALUES (14, 'CH_STREET-01', 6, 'TEMP', 1, NULL, 15.00, 500.00, NULL, '2026-06-23 16:35:26', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `parking_space` VALUES (15, 'CH_STREET-02', 6, 'TEMP', 1, NULL, 15.00, 500.00, NULL, '2026-06-23 16:35:26', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `parking_space` VALUES (16, 'CH_PARK-001', 8, 'FIXED', 2, 14, 25.00, 400.00, 200000.00, '2026-06-23 16:35:26', '湘A·77777', '货车', '2026-06-25', '2026-07-24', 'MONTHLY');
INSERT INTO `parking_space` VALUES (17, '11', 1, 'MONTHLY', 1, NULL, 11.00, 111.00, NULL, '2026-06-25 08:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `parking_space` VALUES (18, 'CH6-001', 1, 'MONTHLY', 1, NULL, 12.50, 300.00, NULL, '2026-06-26 09:41:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `parking_space` VALUES (19, 'CH6-002', 1, 'MONTHLY', 1, NULL, 12.50, 300.00, NULL, '2026-06-26 09:41:31', NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for payment_record
-- ----------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `fee_id` bigint(20) NOT NULL COMMENT '关联账单ID',
  `room_id` bigint(20) NOT NULL COMMENT '房间ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '实缴金额',
  `pay_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'CASH' COMMENT '支付方式：CASH/ALIPAY/WECHAT/BANK/CARD',
  `pay_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '缴费时间',
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_fee_id`(`fee_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_pay_date`(`pay_date` ASC) USING BTREE,
  CONSTRAINT `fk_payment_fee` FOREIGN KEY (`fee_id`) REFERENCES `property_fee` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '缴费记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payment_record
-- ----------------------------
INSERT INTO `payment_record` VALUES (3, 4, 2, 350.00, 'CASH', '2026-06-24 10:10:12', '系统同步', '同步账单缴费状态', '2026-06-24 10:10:12');
INSERT INTO `payment_record` VALUES (4, 6, 6, 418.00, 'CASH', '2026-06-24 10:10:12', '系统同步', '同步账单缴费状态', '2026-06-24 10:10:12');
INSERT INTO `payment_record` VALUES (5, 7, 1, 400.00, 'CASH', '2026-06-24 10:10:12', '系统同步', '同步账单缴费状态', '2026-06-24 10:10:12');
INSERT INTO `payment_record` VALUES (6, 8, 2, 400.00, 'CASH', '2026-06-24 10:10:12', '系统同步', '同步账单缴费状态', '2026-06-24 10:10:12');
INSERT INTO `payment_record` VALUES (7, 10, 4, 400.00, 'CASH', '2026-06-24 10:10:12', '系统同步', '同步账单缴费状态', '2026-06-24 10:10:12');
INSERT INTO `payment_record` VALUES (8, 12, 6, 470.00, 'CASH', '2026-06-24 10:10:12', '系统同步', '同步账单缴费状态', '2026-06-24 10:10:12');
INSERT INTO `payment_record` VALUES (9, 13, 7, 260.00, 'CASH', '2026-06-24 10:10:12', '系统同步', '同步账单缴费状态', '2026-06-24 10:10:12');
INSERT INTO `payment_record` VALUES (11, 2, 1, 415.50, 'ALIPAY', '2026-06-26 02:36:14', '李明', NULL, '2026-06-26 10:36:14');
INSERT INTO `payment_record` VALUES (15, 16, 11, 1320000.00, 'WECHAT', '2026-06-29 09:47:42', '李明', NULL, '2026-06-29 09:47:42');
INSERT INTO `payment_record` VALUES (16, 19, 1, 145000.00, 'WECHAT', '2026-06-29 13:33:54', '李明', NULL, '2026-06-29 13:33:54');
INSERT INTO `payment_record` VALUES (17, 17, 1, 150000.00, 'WECHAT', '2026-06-29 13:33:59', '李明', NULL, '2026-06-29 13:33:59');
INSERT INTO `payment_record` VALUES (18, 18, 1, 145000.00, 'WECHAT', '2026-06-29 13:34:03', '李明', NULL, '2026-06-29 13:34:03');
INSERT INTO `payment_record` VALUES (19, 20, 1, 155000.00, 'WECHAT', '2026-06-29 13:42:53', '李明', NULL, '2026-06-29 13:42:53');
INSERT INTO `payment_record` VALUES (20, 21, 11, 111.00, 'WECHAT', '2026-06-29 14:10:18', '李明', NULL, '2026-06-29 14:10:18');
INSERT INTO `payment_record` VALUES (21, 22, 11, 150000.00, 'WECHAT', '2026-06-29 14:17:05', '李明', NULL, '2026-06-29 14:17:05');

-- ----------------------------
-- Table structure for prop_announcement
-- ----------------------------
DROP TABLE IF EXISTS `prop_announcement`;
CREATE TABLE `prop_announcement`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `publish_date` date NULL DEFAULT NULL COMMENT '发布日期',
  `publisher` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布人',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED/DRAFT/ARCHIVED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告通知' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prop_announcement
-- ----------------------------
INSERT INTO `prop_announcement` VALUES (1, '春季消防演练通知', '请于3月20日参加小区消防演练，届时请配合物业安排。', '2026-06-24', '物业中心', 'PUBLISHED', '2026-06-23 09:42:38');
INSERT INTO `prop_announcement` VALUES (2, '电梯维保公告', '3月15日-17日1号楼电梯例行维保，请业主错峰出行。', '2025-03-05', '物业中心', 'PUBLISHED', '2026-06-23 09:42:38');
INSERT INTO `prop_announcement` VALUES (3, '夏季用电安全提醒', '夏季高温，请业主注意用电安全，避免同时使用大功率电器。', '2026-06-20', '物业中心', 'PUBLISHED', '2026-06-23 16:31:44');
INSERT INTO `prop_announcement` VALUES (4, '小区绿化改造通知', '7月1日起小区将进行绿化改造，工期约15天，给您带来不便敬请谅解。', '2026-06-28', '物业中心', 'PUBLISHED', '2026-06-23 16:31:44');
INSERT INTO `prop_announcement` VALUES (5, '物业费缴纳提醒', '2025年第一季度物业费已开始收取，请各位业主及时缴纳。', '2026-06-13', '物业中心', 'PUBLISHED', '2026-06-23 16:31:44');
INSERT INTO `prop_announcement` VALUES (6, '物业费调整公告', '根据物价局要求，2025年7月起物业费将进行调整，具体标准请咨询物业中心。', '2026-06-03', '物业中心', 'PUBLISHED', '2026-06-23 16:35:26');
INSERT INTO `prop_announcement` VALUES (7, '小区停车管理规定', '为规范小区停车秩序，即日起实行车位登记制度，请业主及时办理。', '2026-06-08', '物业中心', 'PUBLISHED', '2026-06-23 16:35:26');
INSERT INTO `prop_announcement` VALUES (8, '端午节活动通知', '6月10日小区将举办端午节包粽子活动，欢迎各位业主踊跃参加。', '2026-06-18', '物业中心', 'PUBLISHED', '2026-06-23 16:35:26');
INSERT INTO `prop_announcement` VALUES (9, '电梯维保计划', '7月1日-7月3日各楼栋电梯将进行定期维保，具体时间请关注各楼栋通知。', '2026-07-03', '物业中心', 'DRAFT', '2026-06-23 16:35:26');

-- ----------------------------
-- Table structure for prop_building
-- ----------------------------
DROP TABLE IF EXISTS `prop_building`;
CREATE TABLE `prop_building`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `building_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '楼栋名称',
  `building_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '楼栋编码',
  `floors` int(11) NOT NULL DEFAULT 6 COMMENT '楼层数',
  `building_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '混合' COMMENT '类型：住宅/商业/混合',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_building_code`(`building_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房屋楼栋' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prop_building
-- ----------------------------
INSERT INTO `prop_building` VALUES (1, '翠湖苑1号楼', 'CH1', 18, '住宅', '翠湖苑小区东区', '高层住宅', 1, '2026-06-23 09:42:38');
INSERT INTO `prop_building` VALUES (2, '翠湖苑2号楼', 'CH2', 18, '住宅', '翠湖苑小区东区', '高层住宅', 2, '2026-06-23 09:42:38');
INSERT INTO `prop_building` VALUES (3, '翠湖苑商业楼', 'CH_B', 6, '商业', '翠湖苑小区南门', '底商与办公', 3, '2026-06-23 09:42:38');
INSERT INTO `prop_building` VALUES (4, '翠湖苑3号楼', 'CH3', 18, '住宅', '翠湖苑小区西区', '高层住宅', 4, '2026-06-23 16:29:12');
INSERT INTO `prop_building` VALUES (5, '翠湖苑会所', 'CH_CLUB', 3, '商业', '翠湖苑小区中心', '小区会所', 5, '2026-06-23 16:29:12');
INSERT INTO `prop_building` VALUES (6, '翠湖苑4号楼', 'CH4', 11, '住宅', '翠湖苑小区北区', '小高层', 6, '2026-06-23 16:35:26');
INSERT INTO `prop_building` VALUES (7, '翠湖苑5号楼', 'CH5', 6, '住宅', '翠湖苑小区北区', '多层洋房', 7, '2026-06-23 16:35:26');
INSERT INTO `prop_building` VALUES (8, '翠湖苑商业街', 'CH_STREET', 2, '商业', '翠湖苑小区东门', '商业街', 8, '2026-06-23 16:35:26');
INSERT INTO `prop_building` VALUES (9, '翠湖苑综合楼', 'CH_MIX', 12, '混合', '翠湖苑小区西门', '商住混合', 9, '2026-06-23 16:35:26');
INSERT INTO `prop_building` VALUES (10, '翠湖苑地下车库', 'CH_PARK', 1, '商业', '翠湖苑小区地下', '地下停车场', 10, '2026-06-23 16:35:26');

-- ----------------------------
-- Table structure for prop_unit
-- ----------------------------
DROP TABLE IF EXISTS `prop_unit`;
CREATE TABLE `prop_unit`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `building_id` bigint(20) NOT NULL COMMENT '楼栋ID',
  `unit_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房号',
  `floor` int(11) NOT NULL DEFAULT 1 COMMENT '楼层',
  `unit_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '户型',
  `area_sqm` decimal(8, 2) NOT NULL DEFAULT 80.00 COMMENT '面积(㎡)',
  `owner_count` int(11) NOT NULL DEFAULT 0 COMMENT '登记业主数',
  `owner_id` bigint(20) NULL DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1已入住 2空置 0装修中',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_building_unit`(`building_id` ASC, `unit_no` ASC) USING BTREE,
  INDEX `idx_building_id`(`building_id` ASC) USING BTREE,
  INDEX `idx_prop_unit_owner_id`(`owner_id` ASC) USING BTREE,
  CONSTRAINT `fk_unit_building` FOREIGN KEY (`building_id`) REFERENCES `prop_building` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房屋单元' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of prop_unit
-- ----------------------------
INSERT INTO `prop_unit` VALUES (1, 1, '1-101', 1, '两室一厅', 89.00, 2, 1, 1, '南向', '2026-06-23 09:42:38');
INSERT INTO `prop_unit` VALUES (2, 1, '1-102', 1, '一室一厅', 76.00, 1, 9, 1, '北向', '2026-06-23 09:42:38');
INSERT INTO `prop_unit` VALUES (3, 1, '12-01', 12, '复式', 120.00, 1, 4, 1, '顶层复式', '2026-06-23 09:42:38');
INSERT INTO `prop_unit` VALUES (4, 1, '8-803', 8, '两室一厅', 89.00, 1, 6, 1, NULL, '2026-06-23 09:42:38');
INSERT INTO `prop_unit` VALUES (5, 2, '5-501', 5, '三室两厅', 95.00, 1, 2, 1, NULL, '2026-06-23 09:42:38');
INSERT INTO `prop_unit` VALUES (6, 2, '5-502', 5, '三室两厅', 95.00, 1, 5, 1, NULL, '2026-06-23 09:42:38');
INSERT INTO `prop_unit` VALUES (7, 3, 'B101', 1, '商铺', 150.00, 1, 10, 1, '临街商铺', '2026-06-23 09:42:38');
INSERT INTO `prop_unit` VALUES (8, 3, 'B102', 1, '商铺', 80.00, 0, NULL, 0, '装修中', '2026-06-23 09:42:38');
INSERT INTO `prop_unit` VALUES (9, 4, '3-101', 1, '两室一厅', 88.00, 0, 11, 1, '南向', '2026-06-23 16:29:12');
INSERT INTO `prop_unit` VALUES (10, 4, '3-102', 1, '一室一厅', 70.00, 0, 16, 1, '北向', '2026-06-23 16:29:12');
INSERT INTO `prop_unit` VALUES (11, 4, '3-201', 2, '两室一厅', 88.00, 1, 1, 1, '空置', '2026-06-23 16:29:12');
INSERT INTO `prop_unit` VALUES (12, 5, 'C101', 1, '商铺', 200.00, 0, 12, 1, '会所配套', '2026-06-23 16:29:12');
INSERT INTO `prop_unit` VALUES (13, 4, '4-101', 1, '两室一厅', 85.00, 1, 17, 1, '南向采光好', '2026-06-23 16:35:26');
INSERT INTO `prop_unit` VALUES (14, 4, '4-201', 2, '三室两厅', 110.00, 0, 13, 2, '空置', '2026-06-23 16:35:26');
INSERT INTO `prop_unit` VALUES (15, 4, '4-301', 3, '复式', 150.00, 0, NULL, 0, '装修中', '2026-06-23 16:35:26');
INSERT INTO `prop_unit` VALUES (16, 5, '5-101', 1, '两室一厅', 80.00, 0, NULL, 1, '南北通透', '2026-06-23 16:35:26');
INSERT INTO `prop_unit` VALUES (17, 5, '5-102', 1, '一室一厅', 60.00, 0, 14, 2, '空置', '2026-06-23 16:35:26');
INSERT INTO `prop_unit` VALUES (18, 6, 'S101', 1, '商铺', 180.00, 1, NULL, 1, '临街旺铺', '2026-06-23 16:35:26');
INSERT INTO `prop_unit` VALUES (19, 6, 'S102', 1, '商铺', 120.00, 0, NULL, 1, '便利店', '2026-06-23 16:35:26');
INSERT INTO `prop_unit` VALUES (20, 6, 'S103', 1, '商铺', 90.00, 0, NULL, 2, '空置', '2026-06-23 16:35:26');
INSERT INTO `prop_unit` VALUES (21, 7, 'M101', 1, '商铺', 200.00, 0, NULL, 0, '装修中', '2026-06-23 16:35:26');
INSERT INTO `prop_unit` VALUES (22, 8, 'G101', -1, '车库', 25.00, 0, 24, 1, '标准车位', '2026-06-23 16:35:26');

-- ----------------------------
-- Table structure for property_fee
-- ----------------------------
DROP TABLE IF EXISTS `property_fee`;
CREATE TABLE `property_fee`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id` bigint(20) NOT NULL COMMENT '房间ID',
  `bill_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账期YYYY-MM',
  `management_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '物业费',
  `public_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '公摊费',
  `total_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '合计',
  `pay_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID/OVERDUE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fee_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PROPERTY' COMMENT 'PROPERTY=物业费, HOUSE_PURCHASE=购房款',
  `house_purchase_id` bigint(20) NULL DEFAULT NULL COMMENT '关联的购房申请ID',
  `parking_purchase_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_room_month`(`room_id` ASC, `bill_month` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物业费账单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of property_fee
-- ----------------------------
INSERT INTO `property_fee` VALUES (1, 1, '2025-02', 280.00, 120.01, 400.01, 'UNPAID', '2026-06-23 09:42:38', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (2, 1, '2025-03', 280.00, 135.50, 415.50, 'PAID', '2026-06-23 09:42:38', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (3, 5, '2025-03', 320.00, 98.00, 418.00, 'UNPAID', '2026-06-23 09:42:38', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (4, 2, '2026-06', 250.00, 100.00, 350.00, 'PAID', '2026-06-23 16:31:44', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (5, 3, '2026-06', 380.00, 150.00, 530.00, 'UNPAID', '2026-06-23 16:31:44', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (6, 6, '2026-06', 320.00, 98.00, 418.00, 'PAID', '2026-06-23 16:31:44', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (7, 1, '2026-05', 280.00, 120.00, 400.00, 'PAID', '2026-06-23 16:31:44', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (8, 2, '2025-01', 280.00, 120.00, 400.00, 'PAID', '2026-06-23 16:35:26', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (9, 3, '2025-02', 380.00, 150.00, 530.00, 'OVERDUE', '2026-06-23 16:35:26', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (10, 4, '2025-03', 280.00, 120.00, 400.00, 'PAID', '2026-06-23 16:35:26', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (11, 5, '2025-04', 320.00, 98.00, 418.00, 'UNPAID', '2026-06-23 16:35:26', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (12, 6, '2025-05', 350.00, 120.00, 470.00, 'PAID', '2026-06-23 16:35:26', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (13, 7, '2025-05', 180.00, 80.00, 260.00, 'PAID', '2026-06-23 16:35:26', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (14, 8, '2025-06', 150.00, 60.00, 210.00, 'UNPAID', '2026-06-23 16:35:26', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (15, 9, '2025-06', 280.00, 120.00, 400.00, 'UNPAID', '2026-06-23 16:35:26', 'PROPERTY', NULL, NULL);
INSERT INTO `property_fee` VALUES (16, 11, '2026-06', 1320000.00, 0.00, 1320000.00, 'PAID', '2026-06-29 09:32:51', 'HOUSE_PURCHASE', 4, NULL);
INSERT INTO `property_fee` VALUES (17, 1, '2026-06', 150000.00, 0.00, 150000.00, 'PAID', '2026-06-29 13:23:09', 'PARKING_PURCHASE', NULL, 20);
INSERT INTO `property_fee` VALUES (18, 1, '2026-06', 145000.00, 0.00, 145000.00, 'PAID', '2026-06-29 13:27:47', 'PARKING_PURCHASE', NULL, 21);
INSERT INTO `property_fee` VALUES (19, 1, '2026-06', 145000.00, 0.00, 145000.00, 'PAID', '2026-06-29 13:29:40', 'PARKING_PURCHASE', NULL, 22);
INSERT INTO `property_fee` VALUES (20, 1, '2026-06', 155000.00, 0.00, 155000.00, 'PAID', '2026-06-29 13:40:32', 'PARKING_PURCHASE', NULL, 23);
INSERT INTO `property_fee` VALUES (21, 11, '2026-06', 111.00, 0.00, 111.00, 'PAID', '2026-06-29 14:02:01', 'PARKING_PURCHASE', NULL, 25);
INSERT INTO `property_fee` VALUES (22, 11, '2026-06', 150000.00, 0.00, 150000.00, 'PAID', '2026-06-29 14:16:56', 'PARKING_PURCHASE', NULL, 27);

-- ----------------------------
-- Table structure for repair_request
-- ----------------------------
DROP TABLE IF EXISTS `repair_request`;
CREATE TABLE `repair_request`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_id` bigint(20) NOT NULL COMMENT '报修业主ID',
  `room_id` bigint(20) NOT NULL COMMENT '房间ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报修标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '问题描述',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：URGENT/HIGH/MEDIUM/LOW',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/DONE/CANCELLED',
  `submit_date` date NOT NULL COMMENT '提交日期',
  `finish_date` date NULL DEFAULT NULL COMMENT '完成日期',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  CONSTRAINT `fk_repair_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_repair_room` FOREIGN KEY (`room_id`) REFERENCES `prop_unit` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '报修申请' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair_request
-- ----------------------------
INSERT INTO `repair_request` VALUES (1, 1, 1, '水龙头漏水', '厨房洗菜池水龙头持续滴水', 'MEDIUM', 'DONE', '2025-03-01', '2026-06-24', NULL, '2026-06-23 09:42:38');
INSERT INTO `repair_request` VALUES (2, 1, 1, '空调不制冷', '客厅空调开启后无冷风', 'HIGH', 'DONE', '2025-02-20', '2026-06-24', '已联系维修师傅', '2026-06-23 09:42:38');
INSERT INTO `repair_request` VALUES (3, 3, 1, '门锁损坏', '入户门锁难以正常开启', 'MEDIUM', 'DONE', '2025-02-10', '2025-02-12', '已更换锁芯', '2026-06-23 09:42:38');
INSERT INTO `repair_request` VALUES (4, 4, 3, '灯管闪烁', '书房灯管频繁闪烁', 'LOW', 'DONE', '2025-03-05', '2026-06-24', NULL, '2026-06-23 09:42:38');
INSERT INTO `repair_request` VALUES (5, 5, 6, '网络故障', '家中宽带无法连接', 'URGENT', 'DONE', '2025-03-08', '2026-06-24', NULL, '2026-06-23 09:42:38');
INSERT INTO `repair_request` VALUES (6, 7, 1, '马桶堵塞', '卫生间马桶下水不畅', 'MEDIUM', 'DONE', '2026-06-23', '2026-06-24', NULL, '2026-06-23 16:29:12');
INSERT INTO `repair_request` VALUES (7, 8, 5, '墙面开裂', '客厅墙面出现裂缝', 'HIGH', 'PROCESSING', '2026-06-20', NULL, '已联系施工队', '2026-06-23 16:29:12');
INSERT INTO `repair_request` VALUES (8, 9, 2, '插座烧坏', '厨房插座烧坏，无法使用', 'LOW', 'PENDING', '2026-06-22', NULL, NULL, '2026-06-23 16:29:12');
INSERT INTO `repair_request` VALUES (9, 7, 1, '窗户关不严', '卧室窗户关不严，漏风', 'MEDIUM', 'DONE', '2026-06-16', '2026-06-18', '已更换密封条', '2026-06-23 16:29:12');
INSERT INTO `repair_request` VALUES (10, 10, 7, '商铺水管爆裂', '商铺内水管突然爆裂', 'URGENT', 'PROCESSING', '2026-06-22', NULL, '已联系水务公司', '2026-06-23 16:29:12');
INSERT INTO `repair_request` VALUES (11, 11, 9, '水压不足', '高层水压不足，洗澡困难', 'HIGH', 'PENDING', '2026-06-21', NULL, NULL, '2026-06-23 16:35:26');
INSERT INTO `repair_request` VALUES (13, 13, 14, '下水道反味', '卫生间下水道异味严重', 'LOW', 'DONE', '2026-06-22', '2026-06-24', NULL, '2026-06-23 16:35:26');
INSERT INTO `repair_request` VALUES (14, 14, 17, '卷帘门故障', '商铺卷帘门无法正常升降', 'URGENT', 'DONE', '2026-06-13', '2026-06-16', '已更换电机', '2026-06-23 16:35:26');
INSERT INTO `repair_request` VALUES (15, 15, 1, '浴室玻璃自爆', '浴室玻璃门突然爆裂', 'URGENT', 'PROCESSING', '2026-06-22', NULL, '紧急处理中', '2026-06-23 16:35:26');
INSERT INTO `repair_request` VALUES (16, 16, 10, '空调漏水', '客厅空调室内机漏水', 'MEDIUM', 'PENDING', '2026-06-19', NULL, NULL, '2026-06-23 16:35:26');
INSERT INTO `repair_request` VALUES (17, 17, 13, '房门变形', '卧室门受潮变形关不上', 'MEDIUM', 'DONE', '2026-06-08', '2026-06-13', '已更换新门', '2026-06-23 16:35:26');
INSERT INTO `repair_request` VALUES (21, 24, 22, '空调故障', '空调不制冷了，太热了，请尽快维修', 'HIGH', 'DONE', '2026-06-24', '2026-06-24', '已经安排维修师傅上门了', '2026-06-24 18:27:25');
INSERT INTO `repair_request` VALUES (22, 1, 1, '11', '11', 'MEDIUM', 'PENDING', '2026-06-25', NULL, NULL, '2026-06-25 09:40:26');
INSERT INTO `repair_request` VALUES (23, 1, 1, '888888', '123', 'MEDIUM', 'DONE', '2026-06-26', NULL, NULL, '2026-06-26 17:18:58');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NOT NULL DEFAULT 0,
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `portal` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'admin',
  `status` tinyint(4) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '数据概览', '/dashboard', 'dashboard', 1, 'admin', 1);
INSERT INTO `sys_menu` VALUES (2, 0, '楼栋管理', '/buildings', 'buildings', 2, 'admin', 1);
INSERT INTO `sys_menu` VALUES (3, 0, '房屋管理', '/units', 'rooms', 3, 'admin', 1);
INSERT INTO `sys_menu` VALUES (4, 0, '业主管理', '/owners', 'owners', 4, 'admin', 1);
INSERT INTO `sys_menu` VALUES (5, 0, '报修管理', '/repairs', 'repairs', 5, 'admin', 1);
INSERT INTO `sys_menu` VALUES (6, 0, '巡检记录', '/inspection-records', 'hygiene', 6, 'admin', 1);
INSERT INTO `sys_menu` VALUES (7, 0, '访客登记', '/visitors', 'visitors', 7, 'admin', 1);
INSERT INTO `sys_menu` VALUES (8, 0, '公告通知', '/announcements', 'announce', 8, 'admin', 1);
INSERT INTO `sys_menu` VALUES (9, 0, '物业费管理', '/property-fees', 'utility', 9, 'admin', 1);
INSERT INTO `sys_menu` VALUES (10, 0, '缴费记录', '/payments', 'payment', 10, 'admin', 1);
INSERT INTO `sys_menu` VALUES (11, 0, '车位管理', '/parking', 'parking', 11, 'admin', 1);
INSERT INTO `sys_menu` VALUES (12, 0, '投诉建议', '/complaints', 'complaint', 12, 'admin', 1);
INSERT INTO `sys_menu` VALUES (13, 0, '设备管理', '/facilities', 'facility', 13, 'admin', 1);
INSERT INTO `sys_menu` VALUES (14, 0, '装修管理', '/decoration', 'decoration', 14, 'admin', 1);
INSERT INTO `sys_menu` VALUES (15, 0, '个人资料', '/profile', 'profile', 15, 'admin', 1);
INSERT INTO `sys_menu` VALUES (16, 0, '购房申请', '/house-purchases', 'house', 16, 'admin', 1);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ADMIN', '管理员', '系统全部权限', 1);
INSERT INTO `sys_role` VALUES (2, 'PROPERTY_MANAGER', '物业管家', '管理楼栋、房屋与报修', 1);
INSERT INTO `sys_role` VALUES (3, 'OWNER', '业主', '客户端自助服务', 1);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `menu_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id` ASC, `menu_id` ASC) USING BTREE,
  INDEX `fk_rm_menu`(`menu_id` ASC) USING BTREE,
  CONSTRAINT `fk_rm_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_rm_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2, 1, 2);
INSERT INTO `sys_role_menu` VALUES (3, 1, 3);
INSERT INTO `sys_role_menu` VALUES (4, 1, 4);
INSERT INTO `sys_role_menu` VALUES (5, 1, 5);
INSERT INTO `sys_role_menu` VALUES (6, 1, 6);
INSERT INTO `sys_role_menu` VALUES (7, 1, 7);
INSERT INTO `sys_role_menu` VALUES (8, 1, 8);
INSERT INTO `sys_role_menu` VALUES (9, 1, 9);
INSERT INTO `sys_role_menu` VALUES (10, 1, 10);
INSERT INTO `sys_role_menu` VALUES (11, 1, 11);
INSERT INTO `sys_role_menu` VALUES (12, 1, 12);
INSERT INTO `sys_role_menu` VALUES (13, 1, 13);
INSERT INTO `sys_role_menu` VALUES (14, 1, 14);
INSERT INTO `sys_role_menu` VALUES (15, 1, 15);
INSERT INTO `sys_role_menu` VALUES (38, 1, 16);
INSERT INTO `sys_role_menu` VALUES (16, 2, 1);
INSERT INTO `sys_role_menu` VALUES (17, 2, 2);
INSERT INTO `sys_role_menu` VALUES (18, 2, 3);
INSERT INTO `sys_role_menu` VALUES (19, 2, 4);
INSERT INTO `sys_role_menu` VALUES (20, 2, 5);
INSERT INTO `sys_role_menu` VALUES (21, 2, 6);
INSERT INTO `sys_role_menu` VALUES (22, 2, 7);
INSERT INTO `sys_role_menu` VALUES (23, 2, 8);
INSERT INTO `sys_role_menu` VALUES (24, 2, 9);
INSERT INTO `sys_role_menu` VALUES (25, 2, 10);
INSERT INTO `sys_role_menu` VALUES (26, 2, 11);
INSERT INTO `sys_role_menu` VALUES (27, 2, 12);
INSERT INTO `sys_role_menu` VALUES (28, 2, 13);
INSERT INTO `sys_role_menu` VALUES (29, 2, 14);
INSERT INTO `sys_role_menu` VALUES (30, 2, 15);
INSERT INTO `sys_role_menu` VALUES (39, 2, 16);
INSERT INTO `sys_role_menu` VALUES (31, 3, 1);
INSERT INTO `sys_role_menu` VALUES (32, 3, 5);
INSERT INTO `sys_role_menu` VALUES (33, 3, 8);
INSERT INTO `sys_role_menu` VALUES (34, 3, 10);
INSERT INTO `sys_role_menu` VALUES (35, 3, 12);
INSERT INTO `sys_role_menu` VALUES (36, 3, 15);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色：ADMIN/PROPERTY_MANAGER/OWNER',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'admin123', 'ADMIN', '系统管理员', '13900000000', 'admin@property.com', NULL, 1, '2026-06-23 09:42:38', '2026-06-23 09:42:38');
INSERT INTO `sys_user` VALUES (2, 'propmgr01', '123456', 'PROPERTY_MANAGER', '张管家', '13900000001', 'propmgr01@property.com', NULL, 1, '2026-06-23 09:42:38', '2026-06-23 09:42:38');
INSERT INTO `sys_user` VALUES (3, 'owner001', '123456', 'OWNER', '李明', '13800000001', 'liming@property.com', NULL, 1, '2026-06-23 09:42:38', '2026-06-23 09:42:38');
INSERT INTO `sys_user` VALUES (4, 'owner002', '123456', 'OWNER', '王芳', '13800000002', 'wangfang@property.com', NULL, 0, '2026-06-23 09:42:38', '2026-06-25 16:49:20');
INSERT INTO `sys_user` VALUES (5, 'owner003', '123456', 'OWNER', '赵强', '13800000003', 'zhaoqiang@property.com', NULL, 1, '2026-06-23 09:42:38', '2026-06-23 09:42:38');
INSERT INTO `sys_user` VALUES (6, 'owner004', '123456', 'OWNER', '刘洋', '13800000004', 'liuyang@property.com', NULL, 1, '2026-06-23 09:42:38', '2026-06-23 09:42:38');
INSERT INTO `sys_user` VALUES (7, 'owner005', '123456', 'OWNER', '陈静', '13800000005', 'chenjing@property.com', NULL, 0, '2026-06-23 09:42:38', '2026-06-25 16:49:20');
INSERT INTO `sys_user` VALUES (8, 'owner006', '123456', 'OWNER', '周磊', '13800000006', 'zhoulei@property.com', NULL, 1, '2026-06-23 09:42:38', '2026-06-23 09:42:38');
INSERT INTO `sys_user` VALUES (9, 'propmgr02', '123456', 'PROPERTY_MANAGER', '李管家', '13900000007', 'liguanjia@property.com', NULL, 1, '2026-06-23 16:29:12', '2026-06-23 16:29:12');
INSERT INTO `sys_user` VALUES (10, 'owner007', '123456', 'OWNER', '吴敏', '13800000007', 'wumin@property.com', NULL, 1, '2026-06-23 16:29:12', '2026-06-23 16:29:12');
INSERT INTO `sys_user` VALUES (12, 'owner024', '123456', 'OWNER', '谢格逸', '18800000001', 'xiegeyi@property.com', NULL, 1, '2026-06-25 08:43:49', '2026-06-25 16:25:27');
INSERT INTO `sys_user` VALUES (13, 'owner008', '123456', 'OWNER', '郑涛', '13800000008', 'zhengtao@property.com', NULL, 1, '2026-06-25 15:31:49', '2026-06-25 15:31:49');
INSERT INTO `sys_user` VALUES (14, 'owner011', '123456', 'OWNER', '李芳', '13800000011', 'lifang@property.com', NULL, 1, '2026-06-25 15:31:49', '2026-06-25 15:31:49');
INSERT INTO `sys_user` VALUES (15, 'owner013', '123456', 'OWNER', '张丽', '13800000013', 'zhangli@property.com', NULL, 1, '2026-06-25 15:31:49', '2026-06-25 15:31:49');
INSERT INTO `sys_user` VALUES (16, 'owner014', '123456', 'OWNER', '刘伟', '13800000014', 'liuwei@property.com', NULL, 1, '2026-06-25 15:31:49', '2026-06-25 15:31:49');
INSERT INTO `sys_user` VALUES (17, 'owner016', '123456', 'OWNER', '赵岩', '13800000016', 'zhaoyan@property.com', NULL, 1, '2026-06-25 15:31:49', '2026-06-25 15:31:49');
INSERT INTO `sys_user` VALUES (18, 'owner017', '123456', 'OWNER', '黄静', '13800000017', 'huangjing@property.com', NULL, 1, '2026-06-25 15:31:49', '2026-06-25 15:31:49');
INSERT INTO `sys_user` VALUES (20, 'owner025', '123456', 'OWNER', '田琪', '18964568965', 'tianqi@property.com', NULL, 1, '2026-06-25 16:17:41', '2026-06-25 16:42:03');
INSERT INTO `sys_user` VALUES (21, 'owner026', '123456', 'OWNER', '陈晨', '15623458963', 'chenchen@property.com', NULL, 1, '2026-06-25 16:50:46', '2026-06-25 16:56:20');
INSERT INTO `sys_user` VALUES (22, 'owner027', '123456', 'OWNER', '李立', '15678964523', 'lili@property.com', NULL, 1, '2026-06-25 16:57:11', '2026-06-25 16:57:11');
INSERT INTO `sys_user` VALUES (23, 'owner028', '123456', 'OWNER', '田婷', '15623547896', 'tianting@property.com', NULL, 1, '2026-06-25 16:58:44', '2026-06-25 16:58:44');

-- ----------------------------
-- Table structure for visitor_record
-- ----------------------------
DROP TABLE IF EXISTS `visitor_record`;
CREATE TABLE `visitor_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_id` bigint(20) NOT NULL COMMENT '业主ID',
  `visitor_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '访客姓名',
  `visitor_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '访客电话',
  `visit_date` date NULL DEFAULT NULL COMMENT '来访日期',
  `visit_time` time NULL DEFAULT NULL COMMENT '来访时间',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来访事由',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/COMPLETED/待审批/已批准/已拒绝/已完成',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_visit_date`(`visit_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '来访登记' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of visitor_record
-- ----------------------------
INSERT INTO `visitor_record` VALUES (1, 1, '李父', '13700000001', '2025-03-10', '14:30:00', '亲属探望', 'COMPLETED', '2026-06-23 09:42:38');
INSERT INTO `visitor_record` VALUES (2, 2, '王同事', '13700000002', '2025-03-12', '10:00:00', '朋友来访', 'PENDING', '2026-06-23 09:42:38');
INSERT INTO `visitor_record` VALUES (3, 3, '赵母', '13700000003', '2026-06-21', '15:30:00', '亲属探望', 'APPROVED', '2026-06-23 16:31:44');
INSERT INTO `visitor_record` VALUES (4, 4, '刘同学', '13700000004', '2026-06-22', '10:00:00', '同学来访', 'PENDING', '2026-06-23 16:31:44');
INSERT INTO `visitor_record` VALUES (5, 7, '吴经理', '13700000007', '2026-06-20', '09:30:00', '商务洽谈', 'COMPLETED', '2026-06-23 16:31:44');
INSERT INTO `visitor_record` VALUES (6, 1, '李母', '13700000010', '2026-06-08', '09:00:00', '亲属探望', 'APPROVED', '2026-06-23 16:35:26');
INSERT INTO `visitor_record` VALUES (7, 2, '王姐', '13700000011', '2026-06-11', '14:30:00', '朋友来访', 'REJECTED', '2026-06-23 16:35:26');
INSERT INTO `visitor_record` VALUES (8, 3, '赵哥', '13700000012', '2026-06-13', '16:00:00', '同事来访', 'APPROVED', '2026-06-23 16:35:26');
INSERT INTO `visitor_record` VALUES (9, 5, '陈姐', '13700000013', '2026-06-15', '10:30:00', '亲属探望', 'COMPLETED', '2026-06-23 16:35:26');
INSERT INTO `visitor_record` VALUES (10, 7, '周先生', '13700000014', '2026-06-17', '11:00:00', '朋友来访', 'APPROVED', '2026-06-23 16:35:26');
INSERT INTO `visitor_record` VALUES (11, 11, '李总', '13700000015', '2026-06-19', '09:30:00', '商务洽谈', 'PENDING', '2026-06-23 16:35:26');
INSERT INTO `visitor_record` VALUES (12, 13, '张工', '13700000016', '2026-06-20', '15:00:00', '设备维修', 'APPROVED', '2026-06-23 16:35:26');
INSERT INTO `visitor_record` VALUES (13, 15, '王姐', '13700000017', '2026-06-22', '14:00:00', '朋友来访', 'PENDING', '2026-06-23 16:35:26');

SET FOREIGN_KEY_CHECKS = 1;
