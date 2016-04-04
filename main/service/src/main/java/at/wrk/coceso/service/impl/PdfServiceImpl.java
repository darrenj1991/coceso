package at.wrk.coceso.service.impl;

import at.wrk.coceso.entity.Concern;
import at.wrk.coceso.entity.Incident;
import at.wrk.coceso.entity.LogEntry;
import at.wrk.coceso.entity.Unit;
import at.wrk.coceso.entity.User;
import at.wrk.coceso.entity.enums.TaskState;
import at.wrk.coceso.service.IncidentService;
import at.wrk.coceso.service.LogService;
import at.wrk.coceso.service.PatientService;
import at.wrk.coceso.service.PdfService;
import at.wrk.coceso.service.UnitService;
import at.wrk.coceso.utils.PdfDocument;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class PdfServiceImpl implements PdfService {

  private final static Logger LOG = LoggerFactory.getLogger(PdfServiceImpl.class);

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LogService logService;

  @Autowired
  private IncidentService incidentService;

  @Autowired
  private PatientService patientService;

  @Autowired
  private UnitService unitService;

  @Override
  public void generateReport(Concern concern, boolean fullDate, HttpServletResponse response, Locale locale, User user) {
    try (PdfDocument doc = new PdfDocument(PageSize.A4.rotate(), fullDate, this, messageSource, locale)) {
      doc.start(response);
      doc.addFrontPage("pdf.report.header", concern, user);
      doc.addStatistics(incidentService.getAll(concern));
      doc.addCustomLog(logService.getCustomAsc(concern));
      doc.addUnitsLog(unitService.getAllSorted(concern));
      doc.addIncidentsLog(incidentService.getAllForReport(concern));
      doc.addLastPage();

      LOG.info("{}: PDF for concern {} completely written", user, concern);
    } catch (IOException | DocumentException e) {
      LOG.error("{}: Error on printing pdf for concern {}", user, concern, e);
    }
  }

  @Override
  public void generateDump(Concern concern, boolean fullDate, HttpServletResponse response, Locale locale, User user) {
    try (PdfDocument doc = new PdfDocument(PageSize.A4.rotate(), fullDate, this, messageSource, locale)) {
      doc.start(response);
      doc.addFrontPage("pdf.dump.header", concern, user);
      doc.addUnitsCurrent(unitService.getAllSorted(concern));
      doc.addIncidentsCurrent(incidentService.getAllForDump(concern));
      doc.addLastPage();

      LOG.info("{}: PDF for concern {} completely written", user, concern);
    } catch (IOException | DocumentException e) {
      LOG.error("{}: Error on printing pdf for concern {}", user, concern, e);
    }
  }

  @Override
  public void generateTransport(Concern concern, boolean fullDate, HttpServletResponse response, Locale locale, User user) {
    try (PdfDocument doc = new PdfDocument(PageSize.A4.rotate(), fullDate, this, messageSource, locale)) {
      doc.start(response);
      doc.addFrontPage("pdf.transport.header", concern, user);
      doc.addTransports(incidentService.getAllTransports(concern));
      doc.addLastPage();

      LOG.info("{}: PDF for concern {} completely written", user, concern);
    } catch (IOException | DocumentException e) {
      LOG.error("{}: Error on printing pdf for concern {}", user, concern, e);
    }
  }

  @Override
  public void generatePatients(Concern concern, HttpServletResponse response, Locale locale, User user) {
    try (PdfDocument doc = new PdfDocument(PageSize.A4.rotate(), false, this, messageSource, locale)) {
      doc.start(response);
      doc.addFrontPage("pdf.patients.header", concern, user);
      doc.addPatients(patientService.getAllSorted(concern, user));
      doc.addLastPage();

      LOG.info("{}: PDF for concern {} completely written", user, concern);
    } catch (IOException | DocumentException e) {
      LOG.error("{}: Error on printing pdf for concern {}", user, concern, e);
    }
  }

  @Override
  public List<LogEntry> getLogByIncident(Incident incident) {
    return logService.getByIncidentAsc(incident);
  }

  @Override
  public List<LogEntry> getLogByUnit(Unit unit) {
    return logService.getByUnitAsc(unit);
  }

  @Override
  public Map<Unit, TaskState> getRelatedUnits(Incident incident) {
    return unitService.getRelated(incident);
  }

  @Override
  public Timestamp getLastUpdate(Incident incident, Unit unit) {
    return logService.getLastTaskStateUpdate(incident, unit);
  }

}
