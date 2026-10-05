package com.earthuu.admin.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청값을 확인해 주세요."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "관리자 계정을 찾을 수 없습니다."),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."),
    EVENT_VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "심사할 이벤트 버전을 찾을 수 없습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "신청일 조회 범위가 올바르지 않습니다."),
    INVALID_REVIEW_STATUS(HttpStatus.CONFLICT, "현재 상태에서는 요청한 심사 처리를 할 수 없습니다."),
    REVIEW_ALREADY_STARTED(HttpStatus.CONFLICT, "이미 심사가 시작된 이벤트입니다."),
    REJECT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "반려 사유는 필수입니다."),
    CONCURRENT_EVENT_UPDATE(HttpStatus.CONFLICT, "다른 관리자가 먼저 이벤트를 처리했습니다. 새로고침 후 다시 확인해 주세요."),
    INVALID_EVENT_OPERATION(HttpStatus.CONFLICT, "현재 운영 상태에서는 요청한 이벤트 조치를 적용할 수 없습니다."),
    EVENT_CONTENT_ALREADY_HIDDEN(HttpStatus.CONFLICT, "이미 숨김 처리된 이벤트입니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."),
    REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 대상을 찾을 수 없습니다."),
    INVALID_REPORT_DATE_RANGE(HttpStatus.BAD_REQUEST, "신고일 조회 범위가 올바르지 않습니다."),
    INVALID_REPORT_STATUS(HttpStatus.CONFLICT, "현재 상태에서는 요청한 신고 처리를 할 수 없습니다."),
    INVALID_REPORT_RESOLUTION(HttpStatus.BAD_REQUEST, "신고 대상에 적용할 수 없는 조치입니다."),
    REPORT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "신고 처리 사유는 필수입니다."),
    CANNOT_SANCTION_ADMIN(HttpStatus.FORBIDDEN, "관리자 계정에는 사용자 제재를 적용할 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_USER_STATUS(HttpStatus.CONFLICT, "현재 계정 상태에서는 요청한 처리를 할 수 없습니다."),
    USER_ACTION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "사용자 처리 사유는 필수입니다."),
    RESTRICTION_NOT_FOUND(HttpStatus.NOT_FOUND, "활동 제한 내역을 찾을 수 없습니다."),
    RESTRICTION_NOT_ACTIVE(HttpStatus.CONFLICT, "현재 활성 상태인 활동 제한이 아닙니다."),
    CONCURRENT_USER_UPDATE(HttpStatus.CONFLICT, "다른 관리자가 먼저 사용자 정보를 처리했습니다. 새로고침 후 다시 확인해 주세요."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "학생 인증 신청을 찾을 수 없습니다."),
    UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND, "대학 정보를 찾을 수 없습니다."),
    INVALID_VERIFICATION_STATUS(HttpStatus.CONFLICT, "현재 상태에서는 학생 인증을 처리할 수 없습니다."),
    VERIFICATION_EMAIL_NOT_VERIFIED(HttpStatus.CONFLICT, "학교 이메일 인증이 완료되지 않았습니다."),
    VERIFICATION_DOMAIN_NOT_VERIFIED(HttpStatus.CONFLICT, "승인된 학교 이메일 도메인이 아닙니다."),
    VERIFICATION_REJECT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "학생 인증 반려 사유는 필수입니다."),
    INVALID_VERIFICATION_DATE_RANGE(HttpStatus.BAD_REQUEST, "학생 인증 신청일 조회 범위가 올바르지 않습니다."),
    CONCURRENT_VERIFICATION_UPDATE(HttpStatus.CONFLICT, "다른 관리자가 먼저 학생 인증을 처리했습니다. 새로고침 후 다시 확인해 주세요."),
    INVALID_STATISTICS_DATE_RANGE(HttpStatus.BAD_REQUEST, "통계 조회 기간은 시작일이 종료일보다 빠르고 1년 이내여야 합니다."),
    MASTER_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "기준정보를 찾을 수 없습니다."),
    MASTER_DATA_ALREADY_EXISTS(HttpStatus.CONFLICT, "동일한 기준정보가 이미 존재합니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
    OUTBOX_NOT_FOUND(HttpStatus.NOT_FOUND, "Outbox 메시지를 찾을 수 없습니다."),
    OUTBOX_NOT_FAILED(HttpStatus.CONFLICT, "실패 상태의 Outbox 메시지만 재시도할 수 있습니다."),
    HOST_ALREADY_RESTRICTED(HttpStatus.CONFLICT, "이미 활동 제한 중인 사용자입니다."),
    INVALID_RESTRICTION_PERIOD(HttpStatus.BAD_REQUEST, "활동 제한 종료 시각이 올바르지 않습니다."),
    CONCURRENT_REPORT_UPDATE(HttpStatus.CONFLICT, "다른 관리자가 먼저 신고를 처리했습니다. 새로고침 후 다시 확인해 주세요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
