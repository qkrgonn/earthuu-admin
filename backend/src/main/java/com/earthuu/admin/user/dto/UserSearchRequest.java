package com.earthuu.admin.user.dto;

import com.earthuu.admin.auth.entity.UserRole;
import com.earthuu.admin.auth.entity.UserStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UserSearchRequest {
    private String query;
    private UserStatus status;
    private UserRole role = UserRole.USER;
    @Min(0) private int page = 0;
    @Min(1) @Max(100) private int size = 20;
    private UserSort sort = UserSort.NEWEST;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public UserSort getSort() { return sort; }
    public void setSort(UserSort sort) { this.sort = sort; }
}
