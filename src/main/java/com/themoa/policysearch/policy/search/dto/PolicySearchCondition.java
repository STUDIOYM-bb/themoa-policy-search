package com.themoa.policysearch.policy.search.dto;

import java.util.ArrayList;
import java.util.List;

public class PolicySearchCondition {
    private String region;
    private Integer age;
    private String ageGroup;
    private List<String> targetGroups = new ArrayList<>();
    private String employmentStatus;
    private Boolean studentStatus;
    private String incomeCondition;
    private String housingStatus;
    private String householdStatus;
    private String category;
    private List<String> keywords = new ArrayList<>();
    private String requestedApplicationStatus = "OPEN";

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
    public List<String> getTargetGroups() { return targetGroups; }
    public void setTargetGroups(List<String> targetGroups) { this.targetGroups = targetGroups; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    public Boolean getStudentStatus() { return studentStatus; }
    public void setStudentStatus(Boolean studentStatus) { this.studentStatus = studentStatus; }
    public String getIncomeCondition() { return incomeCondition; }
    public void setIncomeCondition(String incomeCondition) { this.incomeCondition = incomeCondition; }
    public String getHousingStatus() { return housingStatus; }
    public void setHousingStatus(String housingStatus) { this.housingStatus = housingStatus; }
    public String getHouseholdStatus() { return householdStatus; }
    public void setHouseholdStatus(String householdStatus) { this.householdStatus = householdStatus; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public String getRequestedApplicationStatus() { return requestedApplicationStatus; }
    public void setRequestedApplicationStatus(String requestedApplicationStatus) { this.requestedApplicationStatus = requestedApplicationStatus; }
}
