package com.workfusion.odf2.example.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import com.workfusion.odf2.core.orm.Datastore;

import static com.workfusion.odf2.core.orm.DatastoreType.GLOBAL;
import static com.workfusion.odf2.core.orm.DatastoreType.NON_VERSIONED;

@DatabaseTable(tableName = "searches_analytics")
@Datastore(type = NON_VERSIONED)
public class SearchesAnalytics {

    @DatabaseField(id = true, columnName = "transaction_id", canBeNull = false, index = true)
    private UUID transactionUUID;

    @DatabaseField(columnName = "version")
    private String version;

    @DatabaseField(columnName = "bp_locale")
    private String bpLocale;

    @DatabaseField(columnName = "search_request" )
    private String searchRequest;

    @DatabaseField(columnName = "status")
    private RecordProcessingStatus status;

    @DatabaseField(columnName = "is_purged", canBeNull = false, index = true, width = 63)
    private String isPurged;

    @DatabaseField(columnName = "search_date", canBeNull = false, index = true, width = 63)
    private String searchDate;

    @DatabaseField(columnName = "timestamp")
    private String timestamp;

    @DatabaseField(columnName = "is_investigation_triggered")
    private String investigationTriggered;

    @DatabaseField(columnName = "campaign_uuid")
    private UUID campaignUUID;

    @DatabaseField(columnName = "run_uuid", canBeNull = false, index = true)
    private UUID runUUID;

    @DatabaseField(columnName = "input_starttime")
    private String inputStarttime;

    @DatabaseField(columnName = "input_endtime")
    private String inputEndtime;

    @DatabaseField(columnName = "processing_starttime")
    private String processingStarttime;

    @DatabaseField(columnName = "processing_endtime")
    private String processingEndtime;

    @DatabaseField(columnName = "review_starttime")
    private String reviewStarttime;

    @DatabaseField(columnName = "review_endtime")
    private String reviewEndtime;

    @DatabaseField(columnName = "output_starttime")
    private String outputStarttime;

    @DatabaseField(columnName = "output_endtime")
    private String outputEndtime;

    @DatabaseField(columnName = "worker_id")
    private String workerId;

    @DatabaseField(columnName = "worker_full_name" )
    private String workerFullName;

    @DatabaseField(columnName = "total_transaction_processing_time" )
    private Long totalTransactionProcessingTime;

    @DatabaseField(columnName = "manual_review_processing_time" )
    private Long manualReviewProcessingTime;

    @DatabaseField(columnName = "transaction_automated_processing_time" )
    private Long transactionAutomatedProcessingTime;

    public enum RecordProcessingStatus {
        IN_PROGRESS,
        READY_FOR_REVIEW,
        RETRYING,
        FAILED,
        COMPLETED,
        STOPPED
    }

    public SearchesAnalytics() {
    }

    public SearchesAnalytics(UUID transactionUUID, String version, String bpLocale, String searchRequest, RecordProcessingStatus status, String isPurged, String searchDate, String timestamp, String investigationTriggered, UUID campaignUUID, UUID runUUID, String inputStarttime, String inputEndtime, String processingStarttime, String processingEndtime, String reviewStarttime, String reviewEndtime, String outputStarttime, String outputEndtime, String workerId, String workerFullName, Long totalTransactionProcessingTime, Long manualReviewProcessingTime, Long transactionAutomatedProcessingTime) {
        this.transactionUUID = transactionUUID;
        this.version = version;
        this.bpLocale = bpLocale;
        this.searchRequest = searchRequest;
        this.status = status;
        this.isPurged = isPurged;
        this.searchDate = searchDate;
        this.timestamp = timestamp;
        this.investigationTriggered = investigationTriggered;
        this.campaignUUID = campaignUUID;
        this.runUUID = runUUID;
        this.inputStarttime = inputStarttime;
        this.inputEndtime = inputEndtime;
        this.processingStarttime = processingStarttime;
        this.processingEndtime = processingEndtime;
        this.reviewStarttime = reviewStarttime;
        this.reviewEndtime = reviewEndtime;
        this.outputStarttime = outputStarttime;
        this.outputEndtime = outputEndtime;
        this.workerId = workerId;
        this.workerFullName = workerFullName;
        this.totalTransactionProcessingTime = totalTransactionProcessingTime;
        this.manualReviewProcessingTime = manualReviewProcessingTime;
        this.transactionAutomatedProcessingTime = transactionAutomatedProcessingTime;
    }

    public UUID getTransactionUUID() {
        return transactionUUID;
    }

    public void setTransactionUUID(UUID transactionUUID) {
        this.transactionUUID = transactionUUID;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBpLocale() {
        return bpLocale;
    }

    public void setBpLocale(String bpLocale) {
        this.bpLocale = bpLocale;
    }

    public String getSearchRequest() {
        return searchRequest;
    }

    public void setSearchRequest(String searchRequest) {
        this.searchRequest = searchRequest;
    }

    public RecordProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(RecordProcessingStatus status) {
        this.status = status;
    }

    public String getIsPurged() {
        return isPurged;
    }

    public void setIsPurged(String isPurged) {
        this.isPurged = isPurged;
    }

    public String getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(String searchDate) {
        this.searchDate = searchDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getInvestigationTriggered() {
        return investigationTriggered;
    }

    public void setInvestigationTriggered(String investigationTriggered) {
        this.investigationTriggered = investigationTriggered;
    }

    public UUID getCampaignUUID() {
        return campaignUUID;
    }

    public void setCampaignUUID(UUID campaignUUID) {
        this.campaignUUID = campaignUUID;
    }

    public UUID getRunUUID() {
        return runUUID;
    }

    public void setRunUUID(UUID runUUID) {
        this.runUUID = runUUID;
    }

    public String getInputStarttime() {
        return inputStarttime;
    }

    public void setInputStarttime(String inputStarttime) {
        this.inputStarttime = inputStarttime;
    }

    public String getInputEndtime() {
        return inputEndtime;
    }

    public void setInputEndtime(String inputEndtime) {
        this.inputEndtime = inputEndtime;
    }

    public String getProcessingStarttime() {
        return processingStarttime;
    }

    public void setProcessingStarttime(String processingStarttime) {
        this.processingStarttime = processingStarttime;
    }

    public String getProcessingEndtime() {
        return processingEndtime;
    }

    public void setProcessingEndtime(String processingEndtime) {
        this.processingEndtime = processingEndtime;
    }

    public String getReviewStarttime() {
        return reviewStarttime;
    }

    public void setReviewStarttime(String reviewStarttime) {
        this.reviewStarttime = reviewStarttime;
    }

    public String getReviewEndtime() {
        return reviewEndtime;
    }

    public void setReviewEndtime(String reviewEndtime) {
        this.reviewEndtime = reviewEndtime;
    }

    public String getOutputStarttime() {
        return outputStarttime;
    }

    public void setOutputStarttime(String outputStarttime) {
        this.outputStarttime = outputStarttime;
    }

    public String getOutputEndtime() {
        return outputEndtime;
    }

    public void setOutputEndtime(String outputEndtime) {
        this.outputEndtime = outputEndtime;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getWorkerFullName() {
        return workerFullName;
    }

    public void setWorkerFullName(String workerFullName) {
        this.workerFullName = workerFullName;
    }

    public Long getTotalTransactionProcessingTime() {
        return totalTransactionProcessingTime;
    }

    public void setTotalTransactionProcessingTime(Long totalTransactionProcessingTime) {
        this.totalTransactionProcessingTime = totalTransactionProcessingTime;
    }

    public Long getManualReviewProcessingTime() {
        return manualReviewProcessingTime;
    }

    public void setManualReviewProcessingTime(Long manualReviewProcessingTime) {
        this.manualReviewProcessingTime = manualReviewProcessingTime;
    }

    public Long getTransactionAutomatedProcessingTime() {
        return transactionAutomatedProcessingTime;
    }

    public void setTransactionAutomatedProcessingTime(Long transactionAutomatedProcessingTime) {
        this.transactionAutomatedProcessingTime = transactionAutomatedProcessingTime;
    }

}
