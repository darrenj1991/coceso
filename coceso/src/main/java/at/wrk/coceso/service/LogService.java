package at.wrk.coceso.service;

import at.wrk.coceso.dao.LogDao;
import at.wrk.coceso.entity.*;
import at.wrk.coceso.entity.enums.LogEntryType;
import at.wrk.coceso.entity.enums.TaskState;
import at.wrk.coceso.entity.helper.JsonContainer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LogService {

  private final static Logger LOG = Logger.getLogger(LogService.class);

  @Autowired
  private LogDao logDao;

  /**
   * Concern level logging
   *
   * @param user
   * @param type
   * @param concernId
   * @param changes
   */
  public void logAuto(Operator user, LogEntryType type, int concernId, JsonContainer changes) {
    createLog(user, type, concernId, null, null, null, changes, true);
  }

  /**
   * Unit/incident level logging For changing of properties, without TaskState
   *
   * @param user
   * @param type
   * @param concernId
   * @param unit
   * @param incident
   * @param changes
   */
  public void logAuto(Operator user, LogEntryType type, int concernId, Unit unit, Incident incident, JsonContainer changes) {
    createLog(user, type, concernId, unit, incident, null, changes, true);
  }

  /**
   * Unit/incident level logging For logging of TaskState, without JSON for properties
   *
   * @param user
   * @param type
   * @param concernId
   * @param unit
   * @param incident
   * @param state
   */
  public void logAuto(Operator user, LogEntryType type, int concernId, Unit unit, Incident incident, TaskState state) {
    createLog(user, type, concernId, unit, incident, state, null, true);
  }

  /**
   * Unit/incident level logging For logging of TaskState and changed properties
   *
   * @param user
   * @param type
   * @param concernId
   * @param unit
   * @param incident
   * @param state
   * @param changes
   */
  public void logAuto(Operator user, LogEntryType type, int concernId, Unit unit, Incident incident, TaskState state, JsonContainer changes) {
    createLog(user, type, concernId, unit, incident, state, changes, true);
  }

  /**
   * Custom log entry
   *
   * @param user
   * @param text
   * @param concernId
   * @param unit
   * @param incident
   */
  public void logCustom(Operator user, String text, int concernId, Unit unit, Incident incident) {
    createLog(user, LogEntryType.CUSTOM.customMessage(text), concernId, unit, incident, null, null, false);
  }

  private void createLog(Operator user, LogEntryType type, int concernId, Unit unit, Incident incident, TaskState state, JsonContainer changes, boolean auto) {
    if (user == null) {
      LOG.error("LogService called without 'user'!");
      return;
    }

    LogEntry logEntry = new LogEntry();

    logEntry.setUser(user);
    logEntry.setText(type.getMessage());
    logEntry.setType(type);
    logEntry.setUnit(unit);
    logEntry.setIncident(incident);
    logEntry.setAutoGenerated(auto);
    logEntry.setConcern(new Concern(concernId));
    logEntry.setState(state);
    logEntry.setChanges(changes);

    logDao.add(logEntry);
  }

  public List<LogEntry> getLast(int case_id, int count) {
    if (count < 1 || case_id < 1) {
      return null;
    }
    return logDao.getLast(case_id, count);
  }

  public List<LogEntry> getByIncidentId(int inc_id) {
    if (inc_id < 1) {
      return null;
    }
    return logDao.getByIncidentId(inc_id);
  }

  public List<LogEntry> getCustom(int concern_id) {
    if (concern_id < 1) {
      return null;
    }
    return logDao.getCustom(concern_id);
  }

  public List<LogEntry> getByUnitId(int id) {
    return logDao.getByUnitId(id);
  }

  public List<LogEntry> getByIncidentAndUnit(int incident_id, int unit_id) {
    return logDao.getByIncidentAndUnit(incident_id, unit_id);
  }

  public Map<Unit, TaskState> getRelatedUnits(int incident_id) {
    return logDao.getRelatedUnits(incident_id);
  }

  public List<LogEntry> getAll(int concern_id) {
    return logDao.getAll(concern_id);
  }

  public List<LogEntry> getLimitedByUnitId(int unit, int limit) {
    return logDao.getLimitedByUnitId(unit, limit);
  }
}
