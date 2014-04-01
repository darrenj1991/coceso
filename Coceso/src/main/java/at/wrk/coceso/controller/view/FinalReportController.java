package at.wrk.coceso.controller.view;

import at.wrk.coceso.dao.ConcernDao;
import at.wrk.coceso.dao.PatientDao;
import at.wrk.coceso.entity.*;
import at.wrk.coceso.entity.enums.IncidentType;
import at.wrk.coceso.entity.enums.LogEntryType;
import at.wrk.coceso.entity.enums.TaskState;
import at.wrk.coceso.entity.helper.JsonContainer;
import at.wrk.coceso.service.IncidentService;
import at.wrk.coceso.service.LogService;
import at.wrk.coceso.service.PatientService;
import at.wrk.coceso.service.UnitService;
import at.wrk.coceso.utils.Logger;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/finalReport")
public class FinalReportController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    ConcernDao concernDao;

    @Autowired
    UnitService unitService;

    @Autowired
    IncidentService incidentService;

    @Autowired
    LogService logService;

    @Autowired
    private PatientService patientService;

    private Concern concern;
    private Operator user;
    private java.util.List<Unit> unitList;
    private java.util.List<Incident> incidentList;

    private Locale locale;

    private static String dateFormat = "HH:mm:ss";

    @RequestMapping(value = "report.pdf", produces = "application/pdf")
    public void print(HttpServletResponse response,
                      @RequestParam(value = "id") int id,
                      @RequestParam(value = "fullDate", required = false) Boolean fullDate,
                      Principal principal,
                      Locale locale)
    {

        if(fullDate != null && fullDate) {
            dateFormat = "dd.MM.yy HH:mm:ss";
        }

        user = (Operator) ((UsernamePasswordAuthenticationToken)principal).getPrincipal();

        this.locale = locale;

        concern = concernDao.getById(id);
        if(concern == null) {
            throw new ConcernNotFoundException();
        }


        unitList = unitService.getAll(id);
        Collections.sort(unitList, new Comparator<Unit>() {
            @Override
            public int compare(Unit o1, Unit o2) {
                if(o1 == null || o1.getCall() == null) {
                    return 1;
                }
                return o1.getCall().compareTo(o2.getCall());
            }
        });
        incidentList = incidentService.getAll(id);

        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            addMeta(document);
            addFrontPage(document);
            addUnitStats(document);
            addIncidentStats(document);
            addStatistics(document);

            // TODO Custom Log History
        }
        catch(IOException e) {
            Logger.error("FinalReportController:print(): " + e.getMessage());
        }
        catch(DocumentException e) {
            Logger.error("FinalReportController:print(): "+e.getMessage());
        }
        finally {
            document.close();
        }

    }

    private static Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD);
    private static Font subTitleFont = new Font(Font.FontFamily.TIMES_ROMAN, 18);

    private static Font title2Font = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static Font descrFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);

    private static Font defFont = new Font(Font.FontFamily.TIMES_ROMAN, 11);

    private void addMeta(Document document) throws DocumentException {
        document.addTitle("Abschlussbericht der Ambulanz\n" + concern.getName());
        document.addAuthor("CoCeSo");
        document.addCreator("CoCeSo - " + user.getUsername());
    }

    private void addFrontPage(Document document) throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);

        Paragraph p0 = new Paragraph("Abschlussbericht der Ambulanz " + concern.getName(), titleFont);
        p0.setAlignment(Element.ALIGN_CENTER);
        preface.add(p0);

        addEmptyLine(preface, 1);

        Paragraph p1 = new Paragraph("Report generated by: " + user.getGiven_name() + " " + user.getSur_name(), subTitleFont);
        p1.setAlignment(Element.ALIGN_CENTER);
        preface.add(p1);

        addEmptyLine(preface, 3);
        preface.add(new Paragraph("To be filled with Information ", defFont));

        document.add(preface);
        document.newPage();
    }

    private void addStatistics(Document document) throws DocumentException {

        int task, taskBlue, transport, transportBlue, relocation, relocationBlue, other, otherBlue;
        task = taskBlue = transport = transportBlue = relocation = relocationBlue = other = otherBlue = 0;

        for(Incident incident : incidentList) {
            switch (incident.getType()) {
                case Task:
                    task++;
                    if(incident.getBlue())
                        taskBlue++;
                    break;
                case Transport:
                    transport++;
                    if(incident.getBlue())
                        transportBlue++;
                    break;
                case Relocation:
                    relocation++;
                    if(incident.getBlue())
                        relocationBlue++;
                    break;
                default:
                    other++;
                    if(incident.getBlue())
                        otherBlue++;
                    break;
            }
        }

        PdfPTable table = new PdfPTable(new float[] {3, 1, 1});
        table.setWidthPercentage(100);

        table.addCell("");
        table.addCell(messageSource.getMessage("label.report.total", null, locale));
        table.addCell(messageSource.getMessage("label.report.stat_blue", null, locale));

        table.addCell(messageSource.getMessage("label.incident.type.task", null, locale) + " / " + messageSource.getMessage("label.incident.type.task.blue", null, locale));
        table.addCell(""+task);
        table.addCell(""+taskBlue);

        table.addCell(messageSource.getMessage("label.incident.type.transport", null, locale));
        table.addCell(""+transport);
        table.addCell(""+transportBlue);

        table.addCell(messageSource.getMessage("label.incident.type.relocation", null, locale));
        table.addCell(""+relocation);
        table.addCell(""+relocationBlue);

        table.addCell(messageSource.getMessage("label.report.incident.other", null, locale));
        table.addCell(""+other);
        table.addCell(""+otherBlue);


        document.add(new Paragraph(messageSource.getMessage("label.report.statistics", null, locale), titleFont));
        document.add(new Paragraph(" "));
        document.add(table);



        document.add(new Paragraph(" "));
        document.newPage();
    }

    private void addUnitStats(Document document) throws DocumentException {

        ObjectMapper mapper = new ObjectMapper();

        document.add(new Paragraph(messageSource.getMessage("label.units", null, locale), titleFont));
        document.add(new Paragraph(" "));

        for(Unit unit : unitList) {
            java.util.List<LogEntry> logs = logService.getByUnitId(unit.getId());
            Collections.reverse(logs);

            Paragraph p = new Paragraph();

            Paragraph h = new Paragraph(unit.getCall() + " - #" + unit.getId(), title2Font);

            Paragraph s = new Paragraph((unit.getAni() == null || unit.getAni().isEmpty() ? "" : ("ANI: " + unit.getAni()) + "\n") +
                    messageSource.getMessage("label.unit.home", null, locale) + ": " + (unit.getHome() == null ? "N/A" : unit.getHome()));
            p.add(h);
            p.add(s);

            PdfPTable table = new PdfPTable(new float[] {2, 3, 4, 1, 4, 4, 5, 1});
            table.setWidthPercentage(100);

            table.addCell(messageSource.getMessage("label.log.timestamp", null, locale));
            table.addCell(messageSource.getMessage("label.operator", null, locale));
            table.addCell(messageSource.getMessage("label.log.text", null, locale));
            table.addCell(""); //messageSource.getMessage("label.unit.state", null, locale));
            table.addCell(messageSource.getMessage("label.unit.position", null, locale));
            table.addCell(messageSource.getMessage("label.unit.info", null, locale));
            table.addCell(messageSource.getMessage("label.incident", null, locale));
            table.addCell(""); //messageSource.getMessage("label.task.state", null, locale));

            String lastPosition = "";
            String lastInfo = "";
            String lastState = "";

            for(LogEntry log : logs) {
                if(log.getJson() == null || log.getType() == LogEntryType.CUSTOM) {
                    table.addCell(new java.text.SimpleDateFormat(dateFormat).format(log.getTimestamp()));
                    table.addCell(log.getUser().getUsername());

                    PdfPCell cell = new PdfPCell(new Phrase((log.getType() == LogEntryType.CUSTOM || log.getType() == null) ? log.getText() :
                            messageSource.getMessage("descr."+log.getType().name(), null, locale)));
                    cell.setColspan(4);
                    table.addCell(cell);

                    if(log.getIncident() == null) {
                        table.addCell("");
                        table.addCell("");
                    } else {
                        table.addCell(incidentTitle(log.getIncident()));
                        table.addCell(log.getState() == null ? "-" : log.getState().name());
                    }

                    continue;
                }

                JsonContainer jsonContainer;
                try {
                    jsonContainer = mapper.readValue(log.getJson(), JsonContainer.class);
                    Unit tUnit = jsonContainer.getUnit();
                    Incident tIncident = jsonContainer.getIncident();

                    if(tUnit == null) {
                        Logger.debug("FinalReportController: Parsing Error???");
                        continue;
                    }


                    table.addCell(new java.text.SimpleDateFormat(dateFormat).format(log.getTimestamp()));
                    table.addCell(log.getUser().getUsername());
                    table.addCell(messageSource.getMessage("descr."+log.getType().name(), null, locale));
                    table.addCell(tUnit.getState() == null ? "" : ( tUnit.getState().name().equals(lastState) ? "" : ( lastState = tUnit.getState().name() ) ) );
                    table.addCell(tUnit.getPosition() == null ? "" : ( tUnit.getPosition().toString().equals(lastPosition) ? "" : ( lastPosition = tUnit.getPosition()+"" ) ) );
                    table.addCell(tUnit.getInfo() == null ? "" : ( tUnit.getInfo().equals(lastInfo) ? "" : ( lastInfo = tUnit.getInfo() ) ) );

                    if(tIncident == null) {
                        table.addCell("");
                        table.addCell("");
                    } else {
                        table.addCell(incidentTitle(tIncident));
                        table.addCell(log.getState() == null ? "-" : messageSource.getMessage("label.task.state." + log.getState().name().toLowerCase(), null, locale));
                    }

                } catch (IOException e) {
                    Logger.warning(e.getMessage());
                }


            }

            p.add(table);
            // Empty Line
            p.add(new Paragraph(" "));
            document.add(p);
        }
        document.newPage();
    }

    private String incidentTitle(Incident inc) {
        String position;
        String type;

        if(inc == null)
            return "null";

        if(inc.getType() == null)
            return "#" + inc.getId();

        if(inc.getType() == IncidentType.ToHome || inc.getType() == IncidentType.HoldPosition ||
                inc.getType() == IncidentType.Standby || inc.getType() == IncidentType.Relocation) {
            position = formatPoint(inc.getAo());
        } else {
            position = formatPoint(inc.getBo());
        }

        type = humanreadableIncidentType(inc);

        return "#" + inc.getId() + " - " + type + "\n" + position ;
    }

    private String formatPoint(Point point) {
        int maxLength = 30;

        if(point == null || point.getInfo() == null || point.getInfo().isEmpty()) {
            return "N/A";
        }

        String info = point.getInfo();
        if(info.length() > maxLength) {
            info = info.substring(0, 26) + "...";
        }

        return info.split("\n")[0];
    }

    private String humanreadableIncidentType(Incident inc) {
        String type;
        if(inc.getType() == IncidentType.Task) {
            if(inc.getBlue() == null || !inc.getBlue()) {
                type = messageSource.getMessage("label.incident.type.task", null, locale);
            } else {
                type = messageSource.getMessage("label.incident.type.task.blue", null, locale);
            }

        } else {
            type = messageSource.getMessage("label.incident.type." + inc.getType().name().toLowerCase(), null, locale);
        }
        return type;
    }

    // TODO History of Patientdata changes
    private void addIncidentStats(Document document) throws DocumentException {
        ObjectMapper mapper = new ObjectMapper();

        document.add(new Paragraph(messageSource.getMessage("label.incidents", null, locale), titleFont));
        document.add(new Paragraph(" "));

        for(Incident incident : incidentList) {
            // Single Unit Incidents are fully logged by UnitStats
            if(incident.getType().isSingleUnit())
                continue;

            java.util.List<LogEntry> logs = logService.getByIncidentId(incident.getId());
            Collections.reverse(logs);

            Paragraph p = new Paragraph();

            Paragraph h = new Paragraph("#" + incident.getId() + " - " + humanreadableIncidentType(incident),
                    title2Font);

            Paragraph s = new Paragraph("BO: " + (incident.getBo() == null ? "N/A" : incident.getBo()) + "\n" +
                    "AO: " + (incident.getAo() == null ? "N/A" : incident.getAo()), descrFont);
            p.add(h);
            p.add(s);

            Patient patient = patientService.getById(incident.getId());
            if(patient != null) {
                Paragraph pat = new Paragraph(messageSource.getMessage("label.patient", null, locale) + ": "+
                        patient.getGiven_name() + "" + patient.getSur_name() );
                        /*messageSource.getMessage("label.patient.sex", null, locale) + ": " + patient.getSex() + "\n" +
                        messageSource.getMessage("label.patient.insurance_number", null, locale) + ": " + patient.getSur_name() + "\n" +
                        messageSource.getMessage("label.patient.externalID", null, locale) + ": " + patient.getExternalID() + "\n" +
                        messageSource.getMessage("label.patient.diagnosis", null, locale) + ": " + patient.getDiagnosis() + "\n" +
                        messageSource.getMessage("label.patient.erType", null, locale) + ": " + patient.getErType() + "\n" +
                        messageSource.getMessage("label.patient.info", null, locale) + ": " + patient.getInfo() + "\n" );*/

                p.add(pat);
            }

            PdfPTable table = new PdfPTable(new float[] {2, 3, 4, 2, 4, 4, 5, 3, 1});
            PdfPTable table2 = new PdfPTable(new float[] {2, 3, 5, 5, 1});
            table.setWidthPercentage(100);
            table2.setWidthPercentage(100);

            table.addCell(messageSource.getMessage("label.log.timestamp", null, locale));
            table.addCell(messageSource.getMessage("label.operator", null, locale));
            table.addCell(messageSource.getMessage("label.log.text", null, locale));
            table.addCell(messageSource.getMessage("label.incident.state", null, locale));
            table.addCell(messageSource.getMessage("label.incident.bo", null, locale));
            table.addCell(messageSource.getMessage("label.incident.ao", null, locale));
            table.addCell(messageSource.getMessage("label.incident.info", null, locale));
            table.addCell(messageSource.getMessage("label.incident.casus", null, locale));
            table.addCell(messageSource.getMessage("label.incident.blue", null, locale));

            table2.addCell(messageSource.getMessage("label.log.timestamp", null, locale));
            table2.addCell(messageSource.getMessage("label.operator", null, locale));
            table2.addCell(messageSource.getMessage("label.log.text", null, locale));
            table2.addCell(messageSource.getMessage("label.unit", null, locale));
            table2.addCell(""); //messageSource.getMessage("label.task.state", null, locale));


            String lastBO = "";
            String lastAO = "";
            String lastInfo = "";
            String lastState = "";
            String lastCasus = "";

            for(LogEntry log : logs)
            {
                if(log.getJson() == null || log.getType() == LogEntryType.CUSTOM) {
                    table.addCell(new java.text.SimpleDateFormat(dateFormat).format(log.getTimestamp()));
                    table.addCell(log.getUser().getUsername());

                    PdfPCell cell = new PdfPCell(new Phrase((log.getType() == LogEntryType.CUSTOM || log.getType() == null) ? log.getText() :
                            messageSource.getMessage("descr."+log.getType().name(), null, locale)));
                    cell.setColspan(7);
                    table.addCell(cell);

                    continue;
                }

                JsonContainer jsonContainer;
                try {
                    jsonContainer = mapper.readValue(log.getJson(), JsonContainer.class);
                    Unit tUnit = jsonContainer.getUnit();
                    Incident tIncident = jsonContainer.getIncident();

                    if(tIncident == null) {
                        Logger.debug("FinalReportController: Parsing Error???");
                        continue;
                    }

                    if(tUnit == null) {
                        table.addCell(new java.text.SimpleDateFormat(dateFormat).format(log.getTimestamp()));
                        table.addCell(log.getUser().getUsername());
                        table.addCell(messageSource.getMessage("descr." + log.getType().name(), null, locale));
                        table.addCell(tIncident.getState() == null ? "" : ( tIncident.getState().name().equals(lastState) ? "" :
                                ( messageSource.getMessage("label.incident.state." + ( lastState = tIncident.getState().name() ).toLowerCase(), null, locale) ) ) );
                        table.addCell(tIncident.getBo() == null ? "" : ( tIncident.getBo().toString().equals(lastBO) ? "" : ( lastBO = tIncident.getBo()+"" ) ) );
                        table.addCell(tIncident.getAo() == null ? "" : ( tIncident.getAo().toString().equals(lastAO) ? "" : ( lastAO = tIncident.getAo()+"" ) ) );
                        table.addCell(tIncident.getInfo() == null ? "" : ( tIncident.getInfo().equals(lastInfo) ? "" : ( lastInfo = tIncident.getInfo() ) ) );
                        table.addCell(tIncident.getCasusNr() == null ? "" : ( tIncident.getCasusNr().equals(lastCasus) ? "" : ( lastCasus = tIncident.getCasusNr() ) ) );
                        table.addCell(tIncident.getBlue() == null ? "" : ( tIncident.getBlue() ? "J" : "N" ) );
                    } else {
                        table2.addCell(new java.text.SimpleDateFormat(dateFormat).format(log.getTimestamp()));
                        table2.addCell(log.getUser().getUsername());
                        table2.addCell(messageSource.getMessage("descr." + log.getType().name(), null, locale));
                        table2.addCell("#" + tUnit.getId() + (tUnit.getCall() !=  null ? ": " + tUnit.getCall() : ""));
                        table2.addCell(log.getState() + "");

                    }

                } catch (IOException e) {
                    Logger.warning(e.getMessage());
                }


            }

            p.add(table);
            p.add(new Paragraph(messageSource.getMessage("label.unit.movement", null, locale), descrFont));
            p.add(table2);
            // Empty Line
            p.add(new Paragraph(" "));
            document.add(p);
        }
        document.newPage();
    }

    private void addCustomLog(Document document) {
        //TODO !
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    @ExceptionHandler(ConcernNotFoundException.class)
    public String error() {
        return "redirect:/welcome";
    }

    private class ConcernNotFoundException extends Error {
    }
}
