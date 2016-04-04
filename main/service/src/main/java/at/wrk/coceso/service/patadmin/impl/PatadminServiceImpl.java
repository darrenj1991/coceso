package at.wrk.coceso.service.patadmin.impl;

import at.wrk.coceso.auth.AuthorizationProvider;
import at.wrk.coceso.entity.Concern;
import at.wrk.coceso.entity.Patient;
import at.wrk.coceso.entity.Unit;
import at.wrk.coceso.entity.User;
import at.wrk.coceso.entity.enums.AccessLevel;
import at.wrk.coceso.entity.enums.Errors;
import at.wrk.coceso.entity.enums.LogEntryType;
import at.wrk.coceso.entity.enums.UnitState;
import at.wrk.coceso.entity.enums.UnitType;
import at.wrk.coceso.entity.helper.Changes;
import at.wrk.coceso.entityevent.NotifyList;
import at.wrk.coceso.exceptions.ErrorsException;
import at.wrk.coceso.form.Group;
import at.wrk.coceso.repository.PatientRepository;
import at.wrk.coceso.repository.UnitRepository;
import at.wrk.coceso.service.LogService;
import at.wrk.coceso.utils.DataAccessLogger;
import at.wrk.coceso.service.patadmin.PatadminService;
import at.wrk.coceso.specification.PatientSearchSpecification;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

@Service
@Transactional
public class PatadminServiceImpl implements PatadminService {

  private final static Logger LOG = LoggerFactory.getLogger(PatadminServiceImpl.class);

  @Autowired
  private AuthorizationProvider auth;

  @Autowired
  private PatientRepository patientRepository;

  @Autowired
  private UnitRepository unitRepository;

  @Autowired
  private LogService logService;

  @Override
  public boolean[] getAccessLevels(Concern concern) {
    return new boolean[]{
      auth.hasPermission(concern, AccessLevel.PatadminRoot),
      auth.hasPermission(concern, AccessLevel.PatadminTriage),
      auth.hasPermission(concern, AccessLevel.PatadminPostprocessing),
      auth.hasPermission(concern, AccessLevel.PatadminInfo)
    };
  }

  @Override
  public void addAccessLevels(ModelMap map, Concern concern) {
    map.addAttribute("accessLevels", getAccessLevels(concern));
  }

  @Override
  public List<Patient> getAllInTreatment(Concern concern, User user) {
    List<Patient> patients = patientRepository.findInTreatment(concern);
    DataAccessLogger.logPatientAccess(patients, concern, user);
    return patients;
  }

  @Override
  public List<Patient> getPatientsByQuery(Concern concern, String query, boolean showDone, User user) {
    query = query.trim();
    if (query.length() < 1) {
      return Collections.emptyList();
    }

    List<Patient> patients = patientRepository.findAll(new PatientSearchSpecification(query, concern, showDone));
    DataAccessLogger.logPatientAccess(patients, concern, user);
    return patients;
  }

  @Override
  public List<Unit> getGroups(Concern concern) {
    List<Unit> groups = unitRepository.findByConcernAndTypeIn(concern, UnitType.treatment);
    Collections.sort(groups);
    return groups;
  }

  @Override
  public Unit getGroup(int id) {
    Unit group = unitRepository.findOne(id);
    if (group == null) {
      throw new ErrorsException(Errors.EntityMissing);
    }
    if (group.getConcern().isClosed()) {
      throw new ErrorsException(Errors.ConcernClosed);
    }
    if (!group.getType().isTreatment()) {
      throw new ErrorsException(Errors.NotTreatment);
    }
    return group;
  }

  @Override
  public List<Unit> update(List<Group> groups, Concern concern, User user, NotifyList notify) {
    Set<Unit> save = groups.stream()
        .map(group -> {
          Unit unit = getGroup(group.getId());
          if (!unit.getConcern().equals(concern)) {
            LOG.warn("{}: Tried to update group {} of wrong concern", user, unit);
            return null;
          }

          LOG.info("{}: Triggered update of unit #{}", user, unit.getId());
          Changes changes = new Changes("unit");

          if (group.isActive() != (unit.getState() == UnitState.EB)) {
            // Group active is equivalent to UnitState EB in database
            UnitState state = group.isActive() ? UnitState.EB : UnitState.NEB;
            changes.put("state", unit.getState(), state);
            unit.setState(state);
          }

          if (!Objects.equals(group.getCapacity(), unit.getCapacity())) {
            changes.put("capacity", unit.getCapacity(), group.getCapacity());
            unit.setCapacity(group.getCapacity());
          }

          String imgsrc = StringUtils.trimToNull(group.getImgsrc());
          if (!Objects.equals(imgsrc, unit.getImgsrc())) {
            changes.put("imgsrc", unit.getImgsrc(), imgsrc);
            unit.setImgsrc(imgsrc);
          }

          if (!changes.isEmpty()) {
            logService.logAuto(user, LogEntryType.UNIT_UPDATE, unit.getConcern(), unit, null, changes);
            notify.add(unit);
            return unit;
          }

          return null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    return unitRepository.save(save);
  }

  @Override
  public int removeMedinfos(Concern concern) {
    return patientRepository.removeMedinfos(concern);
  }

}
