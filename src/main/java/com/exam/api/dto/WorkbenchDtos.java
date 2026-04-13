package com.exam.api.dto;

public final class WorkbenchDtos {
    private WorkbenchDtos() {
    }

    public record TeacherStats(
            int paperCount,
            long publishedCount,
            int studentCount
    ) {
    }

    public record StudentStats(
            int publishedPaperCount,
            int recordCount,
            long submittedCount,
            double averageScore
    ) {
    }

    public record TeacherWorkbenchPayload(
            AuthUserResponse user,
            TeacherStats stats
    ) {
    }

    public record StudentWorkbenchPayload(
            AuthUserResponse user,
            StudentStats stats,
            StudentWorkspaceDtos.StudentRecordCard ongoingRecord
    ) {
    }
}
