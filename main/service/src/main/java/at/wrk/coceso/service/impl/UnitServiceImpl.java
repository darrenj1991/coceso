package at.wrk.coceso.service.impl;

import at.wrk.coceso.entity.*;
import at.wrk.coceso.entity.enums.*;
import at.wrk.coceso.entity.helper.BatchUnits;
import at.wrk.coceso.entity.helper.Changes;
import at.wrk.coceso.exceptions.ErrorsException;
import at.wrk.coceso.importer.UnitImporter;
import at.wrk.coceso.repository.UnitRepository;
import at.wrk.coceso.entityevent.NotifyList;
import at.wrk.coceso.service.IncidentService;
import at.wrk.coceso.service.LogService;
import at.wrk.coceso.service.PointService;
import at.wrk.coceso.service.TaskService;
import at.wrk.coceso.service.UnitService;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import at.wrk.coceso.service.UserService;
import org.springframework.data.domain.Sort;

@Service
@Transactional
class UnitServiceImpl implements UnitService {

  private final static Logger LOG = LoggerFactory.getLogger(UnitServiceImpl.class);

  @Autowired
  private UnitRepository unitRepository;

  @Autowired
  private LogService logService;

  @Autowired
  private IncidentService incidentService;

  @Autowired
  private TaskService taskService;

  @Autowired
  private PointService pointService;

  @Autowired
  private UserService userService;

  @Autowired
  private UnitImporter unitImporter;

  @Override
  public Unit getById(int id) {
    return unitRepository.findOne(id);
  }

  @Override
  public List<Unit> getAll(Concern concern) {
    return unitRepository.findByConcern(concern);
  }

  @Override
  public List<Unit> getAllSorted(Concern concern) {
    return unitRepository.findByConcern(concern, new Sort(Sort.Direction.ASC, "id"));
  }

  @Override
  public List<Unit> getByUser(User user, Collection<UnitType> types) {
    return user == null ? null : unitRepository.findByUser(user, types);
  }

  @Override
  public List<Unit> getByConcernUser(Concern concern, User user) {
    return unitRepository.findByConcernUser(concern, user);
  }

  @Override
  public Map<Unit, TaskState> getRelated(Incident incident) {
    return unitRepository.findByIdIn(unitRepository.findRelated(incident)).stream().collect(Collectors.toMap(
        Function.identity(), u -> u.getIncidents().getOrDefault(incident, TaskState.Detached)));
  }

  @Override
  public Unit updateMain(Unit unit, User user, NotifyList notify) {
    if (unit.getId() == null) {
      LOG.warn("{}: Tried to create unit with wrong method", user);
      throw new ErrorsException(Errors.UnitCreateNotAllowed);
    }

    LOG.info("{}: Triggered update of unit {}", user, unit);

    Map<Incident, TaskState> incidents = unit.getIncidents();

    Unit save = getById(unit.getId());
    if (save == null) {
      // Unit missing, should be checked by validator!
      throw new ErrorsException(Errors.EntityMissing);
    }

    if (save.getConcern().isClosed()) {
      LOG.warn("{}: Tried to update unit {} in closed concern", user, unit);
      throw new ErrorsException(Errors.ConcernClosed);
    }

    // Set updateable properties
    Changes changes = new Changes("unit");
    if (unit.getState() != null && unit.getState() != save.getState()) {
      changes.put("state", save.getState(), unit.getState());
      save.setState(unit.getState());
    }
    if (unit.getInfo() != null && !unit.getInfo().equals(save.getInfo())) {
      changes.put("info", save.getInfo(), unit.getInfo());
      save.setInfo(unit.getInfo());
    }
    if (unit.getPosition() != null && !Point.infoEquals(unit.getPosition(), save.getPosition())) {
      Point p = pointService.createIfNotExists(unit.getPosition(), save.getConcern());
      changes.put("position", Point.toStringOrNull(save.getPosition()), Point.toStringOrNull(p));
      save.setPosition(p);
    }

    unit = unitRepository.save(save);
    logService.logAuto(user, LogEntryType.UNIT_UPDATE, unit.getConcern(), unit, null, changes);
    notify.add(unit);

    if (incidents != null) {
      final Unit u = unit;
      incidents.forEach((incident, state) -> taskService.changeState(incidentService.getById(incident.getId()), u, state, user, notify));
    }

    return unit;
  }

  @Override
  public Unit updateEdit(Unit unit, Concern concern, User user, NotifyList notify) {
    Changes changes = new Changes("unit");
    Unit save;
    if (unit.getId() == null) {
      LOG.info("{}: Triggered unit create", user);

      if (Concern.isClosed(concern)) {
        LOG.warn("{}: Tried to create unit without open concern", user);
        throw new ErrorsException(Errors.ConcernClosed);
      }

      save = new Unit();

      // Set updated properties
      save.setConcern(concern);

      changes.put("call", null, unit.getCall());
      save.setCall(unit.getCall());

      if (StringUtils.isNotBlank(unit.getAni())) {
        changes.put("ani", null, unit.getAni());
        save.setAni(unit.getAni());
      }

      if (StringUtils.isNotBlank(unit.getInfo())) {
        changes.put("info", null, unit.getInfo());
        save.setInfo(unit.getInfo());
      }

      if (!Point.isEmpty(unit.getHome())) {
        Point p = pointService.createIfNotExists(unit.getHome());
        changes.put("home", null, Point.toStringOrNull(p));
        save.setHome(p);
      }

      if (unit.getType() != null) {
        changes.put("type", null, unit.getType());
        save.setType(unit.getType());
      }

      changes.put("withDoc", null, unit.isWithDoc());
      save.setWithDoc(unit.isWithDoc());

      changes.put("portable", null, unit.isPortable());
      save.setPortable(unit.isPortable());

      changes.put("transportVehicle", null, unit.isTransportVehicle());
      save.setTransportVehicle(unit.isTransportVehicle());

      save.setLocked(false);
    } else {
      LOG.info("{}: Triggered update of unit {}", user, unit);

      save = getById(unit.getId());
      if (save == null) {
        // Unit missing, should be checked by validator!
        throw new ErrorsException(Errors.EntityMissing);
      }

      if (save.getConcern().isClosed()) {
        LOG.warn("{}: Tried to update unit {} in closed concern", user, unit);
        throw new ErrorsException(Errors.ConcernClosed);
      }

      // Set updateable properties
      if (unit.getCall() != null && !unit.getCall().equals(save.getCall())) {
        changes.put("call", save.getCall(), unit.getCall());
        save.setCall(unit.getCall());
      }

      if (!Objects.equals(save.getAni(), unit.getAni())) {
        changes.put("ani", save.getAni(), unit.getAni());
        save.setAni(unit.getAni());
      }

      if (!Objects.equals(save.getInfo(), unit.getInfo())) {
        changes.put("info", save.getInfo(), unit.getInfo());
        save.setInfo(unit.getInfo());
      }

      if (!Point.infoEquals(unit.getHome(), save.getHome())) {
        Point p = pointService.createIfNotExists(unit.getHome());
        changes.put("home", Point.toStringOrNull(save.getHome()), Point.toStringOrNull(p));
        save.setHome(p);
      }

      if (unit.getType() != save.getType()) {
        changes.put("type", save.getType(), unit.getType());
        save.setType(unit.getType());
      }

      if (unit.isWithDoc() != save.isWithDoc()) {
        changes.put("withDoc", save.isWithDoc(), unit.isWithDoc());
        save.setWithDoc(unit.isWithDoc());
      }

      if (unit.isPortable() != save.isPortable()) {
        changes.put("portable", save.isPortable(), unit.isPortable());
        save.setPortable(unit.isPortable());
      }

      if (unit.isTransportVehicle() != save.isTransportVehicle()) {
        changes.put("transportVehicle", save.isTransportVehicle(), unit.isTransportVehicle());
        save.setTransportVehicle(unit.isTransportVehicle());
      }
    }

    if (unit.getSection() == null || !save.getConcern().containsSection(unit.getSection())) {
      save.setSection(null);
    } else {
      save.setSection(unit.getSection());
    }

    unit = unitRepository.save(save);
    logService.logAuto(user, LogEntryType.UNIT_CREATE, unit.getConcern(), unit, null, changes);

    notify.add(unit);

    return unit;
  }

  @Override
  public List<Integer> batchCreate(BatchUnits batch, Concern concern, User user, NotifyList notify) {
    List<Integer> ids = new LinkedList<>();

    Unit unit = new Unit();
    unit.setId(null);
    unit.setPortable(batch.isPortable());
    unit.setWithDoc(batch.isWithDoc());
    unit.setTransportVehicle(batch.isTransportVehicle());
    unit.setHome(batch.getHome());

    for (int i = batch.getFrom(); i <= batch.getTo(); i++) {
      unit.setCall(batch.getCall() + i);
      Unit added = updateEdit(unit, concern, user, notify);
      ids.add(added.getId());
    }

    return ids;
  }

  @Override
  public Unit doRemove(int unitId, User user) {
    Unit unit = getById(unitId);
    if (unit == null) {
      LOG.info("{}: Tried to remove non-existing Unit #{}", user, unitId);
      throw new ErrorsException(Errors.EntityMissing);
    }
    if (unit.isLocked()) {
      LOG.warn("{}: Tried to remove non-deletable Unit #{}", user, unit.getId());
      throw new ErrorsException(Errors.UnitLocked);
    }

    logService.updateForRemoval(unit);
    unitRepository.delete(unit);
    return unit;
  }

  @Override
  public void sendHome(int unitId, User user, NotifyList notify) {
    Unit unit = getById(unitId);
    if (unit == null) {
      throw new ErrorsException(Errors.EntityMissing);
    }
    if (unit.getConcern().isClosed()) {
      throw new ErrorsException(Errors.ConcernClosed);
    }

    if (unit.getIncidents().keySet().stream()
        .anyMatch(i -> (i.getType() != IncidentType.Standby && i.getType() != IncidentType.HoldPosition))) {
      throw new ErrorsException(Errors.IncidentNotAllowed);
    }

    Incident inc = new Incident();
    inc.setState(IncidentState.Dispo);
    inc.setType(IncidentType.ToHome);
    inc.setCaller(user.getUsername());
    inc.setAo(unit.getHome());
    inc.setBo(unit.getPosition());

    inc = incidentService.update(inc, unit.getConcern(), user, notify);
    taskService.changeState(inc, unit, TaskState.Assigned, user, notify);
  }

  @Override
  public void holdPosition(int unitId, User user, NotifyList notify) {
    Unit unit = getById(unitId);
    if (unit == null) {
      throw new ErrorsException(Errors.EntityMissing);
    }
    if (unit.getConcern().isClosed()) {
      throw new ErrorsException(Errors.ConcernClosed);
    }
    if (!unit.getIncidents().isEmpty()) {
      throw new ErrorsException(Errors.IncidentNotAllowed);
    }

    incidentService.createHoldPosition(unit.getPosition(), unit, TaskState.Assigned, user, notify);
  }

  @Override
  public void standby(int unitId, User user, NotifyList notify) {
    Unit unit = getById(unitId);
    if (unit == null) {
      throw new ErrorsException(Errors.EntityMissing);
    }
    if (unit.getConcern().isClosed()) {
      throw new ErrorsException(Errors.ConcernClosed);
    }

    if (unit.getIncidents().keySet().stream()
        .anyMatch(i -> (i.getType() != IncidentType.ToHome && i.getType() != IncidentType.HoldPosition))) {
      throw new ErrorsException(Errors.IncidentNotAllowed);
    }

    Incident inc = new Incident();
    inc.setState(IncidentState.Dispo);
    inc.setType(IncidentType.Standby);
    inc.setCaller(user.getUsername());
    inc.setAo(unit.getHome());

    inc = incidentService.update(inc, unit.getConcern(), user, notify);
    taskService.changeState(inc, unit, TaskState.Assigned, user, notify);
  }

  @Override
  public void removeCrew(int unit_id, int user_id, NotifyList notify) {
    Unit unit = getById(unit_id);
    User user = userService.getById(user_id);
    if (unit == null || user == null) {
      throw new ErrorsException(Errors.EntityMissing);
    }
    if (unit.getConcern().isClosed()) {
      throw new ErrorsException(Errors.ConcernMissing);
    }

    unit.removeCrew(user);
    unitRepository.save(unit);
    notify.add(unit);
  }

  @Override
  public void addCrew(int unit_id, int user_id, NotifyList notify) {
    Unit unit = getById(unit_id);
    User user = userService.getById(user_id);
    if (unit == null || user == null) {
      throw new ErrorsException(Errors.EntityMissing);
    }
    if (unit.getConcern().isClosed()) {
      throw new ErrorsException(Errors.ConcernMissing);
    }

    unit.addCrew(user);
    unitRepository.save(unit);
    notify.add(unit);
  }

  @Override
  public int importUnits(String data, Concern concern, User user, NotifyList notify) {
    LOG.info("{}: started import of units", user.getUsername());

    Map<Unit, Changes> units = unitImporter.importUnits(data, concern, getAll(concern));
    units.forEach((u, c) -> {
      u = unitRepository.save(u);
      logService.logAuto(user, LogEntryType.UNIT_CREATE, u.getConcern(), u, null, c);
      notify.add(u);
    });
    return units.size();
  }

}
