package at.wrk.coceso.service;

import at.wrk.coceso.entity.Incident;
import at.wrk.coceso.entity.User;
import at.wrk.coceso.entity.Unit;
import at.wrk.coceso.entity.enums.TaskState;
import at.wrk.coceso.entityevent.NotifyList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface TaskService {

  void changeState(int incident_id, int unit_id, TaskState state, User user, NotifyList notify);

  void changeState(Incident i, Unit u, TaskState state, User user, NotifyList notify);

  void uncheckedChangeState(Incident i, Unit u, TaskState state, User user, NotifyList notify);

}
