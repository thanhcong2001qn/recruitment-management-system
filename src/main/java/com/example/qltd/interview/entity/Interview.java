package com.example.qltd.interview.entity;

import com.example.qltd.application.entity.Application;
import com.example.qltd.common.base.BaseEntity;
import com.example.qltd.interview.enums.InterviewStatus;
import com.example.qltd.interview.enums.InterviewType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "interviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_interview_application_round", columnNames = {
                "application_id",
                "round_number"
        })
})
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, foreignKey = @ForeignKey(name = "fk_interview_application"))
    private Application application;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false, length = 30)
    private InterviewType interviewType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(length = 500)
    private String location;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private Integer rating;

    @PrePersist
    protected void onCreateInterview() {

        if (this.status == null) {
            this.status = InterviewStatus.SCHEDULED;
        }
    }
}
