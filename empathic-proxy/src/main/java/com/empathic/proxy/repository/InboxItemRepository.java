package com.empathic.proxy.repository;

import com.empathic.proxy.model.InboxItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InboxItemRepository extends JpaRepository<InboxItem, Long> {
    List<InboxItem> findByUserIdAndTeamIdAndProcessedFalseOrderByCreatedAtDesc(String userId, String teamId);
    InboxItem findByTeamIdAndChannelIdAndMessageTs(String teamId, String channelId, String messageTs);
}

