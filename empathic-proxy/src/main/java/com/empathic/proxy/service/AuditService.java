package com.empathic.proxy.service;

import com.empathic.proxy.model.AuditLog;
import com.empathic.proxy.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(String userId, String teamId, AuditLog.ActionType actionType, 
                   String channelId, String messageTs, String details) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setTeamId(teamId);
        log.setActionType(actionType);
        log.setChannelId(channelId);
        log.setMessageTs(messageTs);
        log.setDetails(details);
        repository.save(log);
    }
}

