package org.example.javaspringbootjooqsample.infrastructure.compensation;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTask;
import org.example.javaspringbootjooqsample.domain.compensation.repository.CompensationTaskRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.CompensationTasks.COMPENSATION_TASKS;

@Repository
@RequiredArgsConstructor
public class CompensationTaskJooqRepositoryAdapter implements CompensationTaskRepository {

    private final DSLContext dsl;

    @Override
    public CompensationTask save(CompensationTask task) {
        if (task.getId() == null) {
            Long insertedId = dsl.insertInto(COMPENSATION_TASKS)
                    .set(COMPENSATION_TASKS.TASK_TYPE, task.getTaskType())
                    .set(COMPENSATION_TASKS.PAYLOAD, task.getPayload())
                    .set(COMPENSATION_TASKS.STATUS, task.getStatus())
                    .set(COMPENSATION_TASKS.RETRY_COUNT, task.getRetryCount())
                    .set(COMPENSATION_TASKS.NEXT_ATTEMPT_AT, task.getNextAttemptAt())
                    .set(COMPENSATION_TASKS.LAST_ERROR, task.getLastError())
                    .set(COMPENSATION_TASKS.CREATED_AT, task.getCreatedAt())
                    .set(COMPENSATION_TASKS.UPDATED_AT, task.getUpdatedAt())
                    .returning(COMPENSATION_TASKS.ID)
                    .fetchOne(COMPENSATION_TASKS.ID);
            task.setId(insertedId);
            return task;
        }

        dsl.update(COMPENSATION_TASKS)
                .set(COMPENSATION_TASKS.STATUS, task.getStatus())
                .set(COMPENSATION_TASKS.RETRY_COUNT, task.getRetryCount())
                .set(COMPENSATION_TASKS.NEXT_ATTEMPT_AT, task.getNextAttemptAt())
                .set(COMPENSATION_TASKS.LAST_ERROR, task.getLastError())
                .set(COMPENSATION_TASKS.UPDATED_AT, task.getUpdatedAt())
                .where(COMPENSATION_TASKS.ID.eq(task.getId()))
                .execute();

        return task;
    }

    @Override
    public CompensationTask findById(Long id) {
        if (id == null) {
            return null;
        }

        return dsl.selectFrom(COMPENSATION_TASKS)
                .where(COMPENSATION_TASKS.ID.eq(id))
                .fetchOne(this::toTask);
    }

    @Override
    public List<CompensationTask> findPendingForUpdate(LocalDateTime now, int limit) {
        return dsl.selectFrom(COMPENSATION_TASKS)
                .where(COMPENSATION_TASKS.STATUS.eq(org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskStatus.PENDING))
                .and(COMPENSATION_TASKS.NEXT_ATTEMPT_AT.le(now))
                .orderBy(COMPENSATION_TASKS.ID.asc())
                .limit(limit)
                .forUpdate()
                .skipLocked()
                .fetch(this::toTask);
    }

    private CompensationTask toTask(Record record) {
        Integer retryCount = record.get(COMPENSATION_TASKS.RETRY_COUNT);
        return CompensationTask.builder()
                .id(record.get(COMPENSATION_TASKS.ID))
                .taskType(record.get(COMPENSATION_TASKS.TASK_TYPE))
                .payload(record.get(COMPENSATION_TASKS.PAYLOAD))
                .status(record.get(COMPENSATION_TASKS.STATUS))
                .retryCount(retryCount == null ? 0 : retryCount)
                .nextAttemptAt(record.get(COMPENSATION_TASKS.NEXT_ATTEMPT_AT))
                .lastError(record.get(COMPENSATION_TASKS.LAST_ERROR))
                .createdAt(record.get(COMPENSATION_TASKS.CREATED_AT))
                .updatedAt(record.get(COMPENSATION_TASKS.UPDATED_AT))
                .build();
    }
}
