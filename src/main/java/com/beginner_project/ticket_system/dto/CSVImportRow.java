package com.beginner_project.ticket_system.dto;

public class CSVImportRow {

    private int rowNumber;
    private String customerReference;
    private String title;
    private String description;
    private String priority;
    public CSVImportRow(int rowNumber, String customerReference, String title, String description, String priority) {
        this.rowNumber = rowNumber;
        this.customerReference = customerReference;
        this.title = title;
        this.description = description;
        this.priority = priority;
    }
    public int getRowNumber() {
        return rowNumber;
    }
    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }
    public String getCustomerReference() {
        return customerReference;
    }
    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }
}