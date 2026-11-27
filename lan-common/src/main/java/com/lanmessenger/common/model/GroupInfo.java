package com.lanmessenger.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Information describing a chat group and its members.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupInfo {

    private String id;
    private String name;
    private String createdBy;
    private List<String> members = new ArrayList<>();
    private Long createdAt;

    public GroupInfo() {
    }

    public GroupInfo(String id, String name, String createdBy, List<String> members, Long createdAt) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.members = (members != null) ? new ArrayList<>(members) : new ArrayList<>();
        this.createdAt = (createdAt != null) ? createdAt : System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "GroupInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", members=" + members +
                ", createdAt=" + createdAt +
                '}';
    }
}
