/**
 * CoCeSo
 * Client JS - main
 * Copyright (c) WRK\Coceso-Team
 *
 * Licensed under the GNU General Public License, version 3 (GPL-3.0)
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright Copyright (c) 2016 WRK\Coceso-Team
 * @link https://sourceforge.net/projects/coceso/
 * @license GPL-3.0 http://opensource.org/licenses/GPL-3.0
 */

require(["config"], function() {
  require(["jquery", "knockout", "data/load", "data/store/incidents", "data/store/patients", "data/store/units",
    "main/confirm", "main/navigation", "main/models/incident", "main/models/patient", "main/models/unit",
    "utils/conf", "utils/lock", "utils/plugins", "utils/i18n", "utils/misc", "bootstrap/collapse", "bootstrap/dropdown"],
    function($, ko, load, incidentsStore, patientsStore, unitsStore, confirm, navigation, Incident, Patient, Unit, conf, lock, getPlugins, _) {
      "use strict";

      conf.set("error", navigation.connectionError);

      //Lock concern changing
      lock.lock();

      //Preload incidents, patients and units
      load({
        url: "incident/main.json",
        stomp: "/topic/incident/main/{c}",
        model: Incident,
        store: incidentsStore.models
      });
      load({
        url: "patient/main.json",
        stomp: "/topic/patient/main/{c}",
        model: Patient,
        store: patientsStore.models
      });
      load({
        url: "unit/main.json",
        stomp: "/topic/unit/main/{c}",
        model: Unit,
        store: unitsStore.models
      });

      //Load Bindings for Notifications
      ko.applyBindings(navigation, $("#navbar")[0]);

      //Load Bindings for status confirmation window
      ko.applyBindings(confirm.data, $("#next-state-confirm")[0]);

      navigation.openHierarchyUnits();
      navigation.openIncidents({filter: ['overview', 'active'], title: _("main.incident.active")}, {position: {at: "left+70% top"}});

      getPlugins("main", function(plugin) {
        plugin();
      });
    }
  );
});
