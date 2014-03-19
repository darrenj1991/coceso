package at.wrk.coceso.controller.view;

import at.wrk.coceso.dao.ConcernDao;
import at.wrk.coceso.entity.Concern;
import at.wrk.coceso.entity.Operator;
import at.wrk.coceso.entity.Point;
import at.wrk.coceso.entity.Unit;
import at.wrk.coceso.service.ConcernService;
import at.wrk.coceso.service.UnitService;
import at.wrk.coceso.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/edit")
public class ConcernEditController {

    private static Set<Integer> allowedErrors;

    static {
        allowedErrors = new HashSet<Integer>();
        allowedErrors.add(3);
    }

    @Autowired
    //ConcernDao concernService;
    ConcernService concernService;

    //@Autowired
    //LogService logService;

    @Autowired
    UnitService unitService;

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String create(HttpServletRequest request, Principal principal)
    {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        Operator user = (Operator) token.getPrincipal();

        Concern concern = new Concern();
        concern.setName(request.getParameter("name"));
        concern.setInfo(request.getParameter("info"));

        int ret = concernService.add(concern, user);

        return "redirect:/welcome" + (ret == -3 ? "?error=3" : "");
    }

    @RequestMapping("")
    public String edit(@CookieValue(value = "active_case") String c_id, ModelMap map,
                       @RequestParam(value = "error", required = false) Integer error_id) {

        final String return_address_error = "redirect:/welcome?error=1";

        if(c_id == null || c_id.isEmpty()) {
            return return_address_error;
        }

        // Write Error Code to ModelMap
        if(error_id != null && allowedErrors.contains(error_id)) {
            map.addAttribute("error", error_id);
        }

        int id;
        try {
            id = Integer.parseInt(c_id);
        } catch(NumberFormatException nfe) {
            Logger.debug("ConcernEditController: "+nfe.getMessage());
            return return_address_error;
        }

        Concern caze = concernService.getById(id);
        List<Unit> unit_list = unitService.getAll(id);
        Set<Integer> nonDeletables = unitService.getNonDeletable(id);

        //Logger.debug("unit_list.size(): "+unit_list.size());

        map.addAttribute("caze", caze);
        map.addAttribute("unit_list", unit_list);

        HashMap<Integer, Boolean> locked = new HashMap<Integer, Boolean>();
        for(Unit u : unit_list) {
            locked.put(u.getId(), nonDeletables.contains(u.getId()));
        }

        map.addAttribute("locked", locked);

        return "edit";
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public String updateCase(@RequestParam("id") int id, @RequestParam("name") String name,
                         @RequestParam("info") String info,
                         @RequestParam("pax") int pax, Principal principal)
    {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        Operator user = (Operator) token.getPrincipal();

        Concern concern = new Concern();
        concern.setId(id);
        concern.setName(name);
        concern.setPax(pax);
        concern.setInfo(info);

        boolean success = concernService.update(concern, user);

        return "redirect:/edit" + (success ? "" : "?error=3");
    }


    @RequestMapping(value = "updateUnit", method = RequestMethod.POST)
    public String updateUnit(HttpServletRequest request, Principal principal)
    {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        Operator user = (Operator) token.getPrincipal();



        Unit unit = unitService.getById(Integer.parseInt(request.getParameter("id")));

        unit.setCall(request.getParameter("call"));
        unit.setAni(request.getParameter("ani"));
        unit.setInfo(request.getParameter("info"));
        unit.setPortable(request.getParameter("portable") != null);
        unit.setWithDoc(request.getParameter("withDoc") != null);
        unit.setTransportVehicle(request.getParameter("transportVehicle") != null);

        String home = request.getParameter("home");
        if(home != null)
            unit.setHome(new Point(home));


        if(request.getParameter("update") != null) {
            unitService.updateFull(unit, user);
        }
        else if(request.getParameter("remove") != null) {
            unitService.remove(unit, user);
        }
        else {
            Logger.error("CreateController: updateUnit wrong submit button");
        }

        return "redirect:/edit";
    }

    @RequestMapping(value = "createUnit", method = RequestMethod.POST)
    public String createUnit(HttpServletRequest request,
                             @CookieValue("active_case") int case_id, Principal principal)
    {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        Operator user = (Operator) token.getPrincipal();

        Unit unit = new Unit();
        unit.setConcern(case_id);

        unit.setId(-1);
        unit.setCall(request.getParameter("call"));
        unit.setAni(request.getParameter("ani"));
        unit.setInfo(request.getParameter("info"));
        unit.setPortable(request.getParameter("portable") != null);
        unit.setWithDoc(request.getParameter("withDoc") != null);
        unit.setTransportVehicle(request.getParameter("transportVehicle") != null);

        String home = request.getParameter("home");
        if(home != null)
            unit.setHome(new Point(home));

        unitService.add(unit, user);

        //Logger.debug("createUnit: Unit: "+ unit.getId() +", "+ unit.getCall());

        return "redirect:/edit";
    }

    @RequestMapping(value = "createUnitBatch", method = RequestMethod.POST)
    public String createUnitBatch(HttpServletRequest request,
                             @CookieValue("active_case") int case_id,
                             Principal principal)
    {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        Operator user = (Operator) token.getPrincipal();

        Unit unit = new Unit();
        unit.setConcern(case_id);

        unit.setId(-1);
        unit.setPortable(request.getParameter("portable") != null);
        unit.setWithDoc(request.getParameter("withDoc") != null);
        unit.setTransportVehicle(request.getParameter("transportVehicle") != null);
        unit.setHome(new Point(request.getParameter("home")));

        int from = Integer.parseInt(request.getParameter("from"));
        int to = Integer.parseInt(request.getParameter("to"));

        for(int i = from; i <= to; i++){
            unit.setCall(request.getParameter("call_pre")+i);
            unitService.add(unit, user);
        }

        return "redirect:/edit";
    }

    @RequestMapping("container")
    public String editContainer(@CookieValue(value = "active_case") String c_id, ModelMap map) {

        final String return_address_error = "redirect:/welcome";

        if(c_id == null || c_id.isEmpty()) {
            //TODO Show Error Message
            return return_address_error;
        }

        int id;
        try {
            id = Integer.parseInt(c_id);
        } catch(NumberFormatException nfe) {
            Logger.debug("ConcernEditController: "+nfe.getMessage());
            return return_address_error;
        }

        Concern concern = concernService.getById(id);

        map.addAttribute("concern", concern);

        return "edit_container";
    }

}
