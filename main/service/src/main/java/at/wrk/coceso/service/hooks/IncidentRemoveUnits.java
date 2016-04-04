package at.wrk.coceso.service.hooks;

import at.wrk.coceso.entityevent.NotifyList;
import at.wrk.coceso.entity.Incident;
import at.wrk.coceso.entity.User;
import at.wrk.coceso.entity.enums.IncidentState;
import at.wrk.coceso.entity.enums.LogEntryType;
import at.wrk.coceso.entity.enums.TaskState;
import at.wrk.coceso.repository.IncidentRepository;
import at.wrk.coceso.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class IncidentRemoveUnits implements IncidentStateHook {

  private final static Logger LOG = LoggerFactory.getLogger(IncidentRemoveUnits.class);

  @Autowired
  private IncidentRepository incidentRepository;

  @Autowired
  private LogService logService;

  @Override
  public void call(final Incident incident, final IncidentState state, final User user, final NotifyList notify) {
    if (state == IncidentState.Done) {
      if (incident.getUnits() != null && !incident.getUnits().isEmpty()) {
        incident.getUnits().keySet().stream()
            .forEach(u -> {
              LOG.debug("{}: Auto-detach unit #{}, incident #{}", user, u.getId(), incident.getId());
              logService.logAuto(user, LogEntryType.UNIT_AUTO_DETACH, u.getConcern(), u, incident, TaskState.Detached);
              u.removeIncident(incident);
              notify.add(u);
            });

        incidentRepository.save(incident);
        notify.add(incident);
      }
    }
  }

}
