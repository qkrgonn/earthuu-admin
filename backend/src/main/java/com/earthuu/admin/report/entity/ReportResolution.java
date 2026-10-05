package com.earthuu.admin.report.entity;

import java.util.EnumSet;

public enum ReportResolution {
    NO_ACTION,
    GUIDANCE,
    WARNING,
    CONTENT_HIDDEN,
    EVENT_SUSPENDED,
    EVENT_DISCARDED,
    HOST_RESTRICTED,
    HOST_SUSPENDED;

    private static final EnumSet<ReportResolution> COMMON = EnumSet.of(NO_ACTION, GUIDANCE, WARNING);
    private static final EnumSet<ReportResolution> EVENT_ONLY = EnumSet.of(CONTENT_HIDDEN, EVENT_SUSPENDED, EVENT_DISCARDED);
    private static final EnumSet<ReportResolution> HOST_ONLY = EnumSet.of(HOST_RESTRICTED, HOST_SUSPENDED);

    public boolean supports(ReportTargetType targetType) {
        return COMMON.contains(this)
                || targetType == ReportTargetType.EVENT && EVENT_ONLY.contains(this)
                || targetType == ReportTargetType.HOST && HOST_ONLY.contains(this);
    }
}
