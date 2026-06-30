package com.autogen.propmgmt.service;

import com.autogen.propmgmt.entity.ComplaintReply;
import com.autogen.propmgmt.repository.ComplaintReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintReplyService {

    private final ComplaintReplyRepository repository;

    public List<ComplaintReply> listByComplaintId(Long complaintId) {
        return repository.findByComplaintIdOrderByCreatedAtAsc(complaintId);
    }

    public ComplaintReply save(ComplaintReply entity) {
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public int countAdminUnread() {
        return repository.countByAdminReadFalse();
    }

    public int countOwnerUnread(Long ownerId) {
        return repository.countByOwnerReadFalseAndOwnerId(ownerId);
    }

    public int countComplaintAdminUnread(Long complaintId) {
        return repository.countByComplaintIdAndAdminReadFalse(complaintId);
    }

    public int countComplaintOwnerUnread(Long complaintId, Long ownerId) {
        return repository.countByComplaintIdAndOwnerReadFalseAndOwnerId(complaintId, ownerId);
    }

    @Transactional
    public void markAllAdminRead() {
        List<ComplaintReply> unread = repository.findAll().stream()
                .filter(r -> Boolean.FALSE.equals(r.getAdminRead()))
                .toList();
        unread.forEach(r -> r.setAdminRead(true));
        repository.saveAll(unread);
    }

    @Transactional
    public void markAllOwnerRead(Long ownerId) {
        List<ComplaintReply> unread = repository.findAll().stream()
                .filter(r -> ownerId.equals(r.getOwnerId()) && Boolean.FALSE.equals(r.getOwnerRead()))
                .toList();
        unread.forEach(r -> r.setOwnerRead(true));
        repository.saveAll(unread);
    }

    @Transactional
    public void markComplaintAdminRead(Long complaintId) {
        List<ComplaintReply> unread = repository.findByComplaintIdOrderByCreatedAtAsc(complaintId).stream()
                .filter(r -> Boolean.FALSE.equals(r.getAdminRead()))
                .toList();
        unread.forEach(r -> r.setAdminRead(true));
        repository.saveAll(unread);
    }

    @Transactional
    public void markComplaintOwnerRead(Long complaintId, Long ownerId) {
        List<ComplaintReply> unread = repository.findByComplaintIdOrderByCreatedAtAsc(complaintId).stream()
                .filter(r -> ownerId.equals(r.getOwnerId()) && Boolean.FALSE.equals(r.getOwnerRead()))
                .toList();
        unread.forEach(r -> r.setOwnerRead(true));
        repository.saveAll(unread);
    }
}