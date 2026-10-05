package com.earthuu.admin.notification.repository;

import com.earthuu.admin.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, UUID> {}
