package com.deepak.project.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class File {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String fileId;
    @ReadOnlyProperty
    private String fileName;
    @ReadOnlyProperty
    private String s3ObjectName;
    @ReadOnlyProperty
    private String created_date;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String question_id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String answer_id;

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getAnswer_id() {
        return answer_id;
    }

    public void setAnswer_id(String answer_id) {
        this.answer_id = answer_id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getS3ObjectName() {
        return s3ObjectName;
    }

    public void setS3ObjectName(String s3ObjectName) {
        this.s3ObjectName = s3ObjectName;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }
}
