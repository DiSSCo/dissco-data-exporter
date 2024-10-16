package eu.dissco.dataexporter.repository;

import static eu.dissco.dataexporter.database.jooq.Tables.EXPORT_QUEUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.domain.ExportJob;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DataExporterRepository {
  private final DSLContext context;
  private final ObjectMapper mapper;

  public void addJobToQueue(ExportJob job) throws InvalidRequestException {
    context.insertInto(EXPORT_QUEUE)
        .set(EXPORT_QUEUE.ID, job.id())
        .set(EXPORT_QUEUE.PARAMS, mapToJSONB(job.params()))
        .set(EXPORT_QUEUE.JOB_STATE, job.jobState())
        .set(EXPORT_QUEUE.CREATOR, job.creator())
        .set(EXPORT_QUEUE.TIME_SCHEDULED, job.timeScheduled())
        .set(EXPORT_QUEUE.EXPORT_TYPE, job.exportType())
        .set(EXPORT_QUEUE.HASHED_PARAMS, job.hashedParameters())
        .set(EXPORT_QUEUE.DESTINATION_EMAIL, job.destinationEmail())
        .execute();
  }

  public void markJobAsRunning(UUID id){
    context.update(EXPORT_QUEUE)
        .set(EXPORT_QUEUE.JOB_STATE, JobState.RUNNING)
        .set(EXPORT_QUEUE.TIME_STARTED, Instant.now())
        .where(EXPORT_QUEUE.ID.eq(id))
        .execute();
  }

  private JSONB mapToJSONB(JsonNode params) throws InvalidRequestException {
    try {
      return JSONB.valueOf(mapper.writeValueAsString(params));
    } catch (JsonProcessingException e){
      log.error("Unable to parse params to JSONB", e);
      throw new InvalidRequestException("Unable to parse params");
    }
  }

}
