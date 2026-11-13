package com.bss.customer.support;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bss.customer.audit.AuditRecorder;
import com.bss.customer.common.ApiException;
import com.bss.customer.security.LoginAccount;

@Service
public class MemberLookupService {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "login_id", "A.LOGIN_ID",
            "created_at", "A.CREATED_AT",
            "status", "A.STATUS");

    private final MemberLookupDao dao;
    private final AuditRecorder auditRecorder;

    public MemberLookupService(MemberLookupDao dao, AuditRecorder auditRecorder) {
        this.dao = dao;
        this.auditRecorder = auditRecorder;
    }

    public List<MemberLookupItem> lookup(LoginAccount actor, String loginIdPrefix, String sort) {
        if (loginIdPrefix == null || loginIdPrefix.isBlank() || sort == null) {
            throw ApiException.badRequest();
        }
        List<MemberLookupItem> items = dao.findByLoginIdPrefix(loginIdPrefix, sort + ", A.ACCOUNT_ID");
        auditRecorder.success(actor, "MEMBER_LOOKUP", "ACCOUNT", null, null, null);
        return items;
    }
}
