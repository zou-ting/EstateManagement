package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.PropAnnouncement;
import com.autogen.propmgmt.repository.PropAnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropAnnouncementService {

    private final PropAnnouncementRepository announcementRepository;

    // ✅ 支持按状态筛选和关键词搜索
    public List<PropAnnouncement> list(String status, String keyword) {
        List<PropAnnouncement> list = announcementRepository.findAll();

        // 按状态筛选
        if (StringUtils.hasText(status)) {
            list = list.stream()
                    .filter(a -> status.equals(a.getStatus()))
                    .collect(Collectors.toList());
        }

        // 按关键词搜索标题
        if (StringUtils.hasText(keyword)) {
            list = list.stream()
                    .filter(a -> a.getTitle() != null && a.getTitle().contains(keyword))
                    .collect(Collectors.toList());
        }

        return list;
    }

    public PropAnnouncement getById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告通知不存在"));
    }

    public PropAnnouncement save(PropAnnouncement entity) {
        if (!StringUtils.hasText(entity.getTitle())) {
            throw new BusinessException("公告标题不能为空");
        }
        return announcementRepository.save(entity);
    }

    public void delete(Long id) {
        announcementRepository.deleteById(id);
    }
}