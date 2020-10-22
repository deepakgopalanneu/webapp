package com.deepak.project.model;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
public class Answer {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String answer_id;


    @ReadOnlyProperty
    private String question_id;

    @ReadOnlyProperty
    private String created_timestamp;

    @ReadOnlyProperty
    private String updated_timestamp;

    @ReadOnlyProperty
    private String userId;

    @NotNull(message = "answer_text has to be present")
    @NotBlank(message = "answer_text has to be present")
    private String answer_text;

    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "answer_id")
    private List<File> attachments;

    public List<File> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<File> attachments) {
        this.attachments = attachments;
    }


    public String getAnswer_id() {
        return answer_id;
    }

    public void setAnswer_id(String answer_id) {
        this.answer_id = answer_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getCreated_timestamp() {
        return created_timestamp;
    }

    public void setCreated_timestamp(String created_timestamp) {
        this.created_timestamp = created_timestamp;
    }

    public String getUpdated_timestamp() {
        return updated_timestamp;
    }

    public void setUpdated_timestamp(String updated_timestamp) {
        this.updated_timestamp = updated_timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAnswer_text() {
        return answer_text;
    }

    public void setAnswer_text(String answer_text) {
        this.answer_text = answer_text;
    }
}
