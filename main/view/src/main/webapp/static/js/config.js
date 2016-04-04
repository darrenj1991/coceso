/**
 * CoCeSo
 * Client JS - config
 * Copyright (c) WRK\Coceso-Team
 *
 * Licensed under the GNU General Public License, version 3 (GPL-3.0)
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright Copyright (c) 2016 WRK\Coceso-Team
 * @link https://sourceforge.net/projects/coceso/
 * @license GPL-3.0 http://opensource.org/licenses/GPL-3.0
 */

define(function() {
  require.config({
    paths: {
      bootstrap: "assets/bootstrap",
      jquery: "assets/jquery/jquery.min",
      "jquery-i18n": "assets/jquery-i18n-properties/jquery.i18n.properties.min",
      "jquery-ui": "assets/jquery-ui",
      "jqueryui-touch-punch": "assets/jqueryui-touch-punch/jquery.ui.touch-punch.min",
      "js-cookie": "assets/js-cookie/js.cookie",
      knockout: "assets/knockout/knockout",
      "knockout-sortable": "assets/knockout-sortable/knockout-sortable.min",
      leaflet: "assets/leaflet/leaflet",
      stomp: "assets/stomp-websocket/stomp.min",
      "typeahead-bundle": "assets/typeahead.js/typeahead.bundle.min"
    },
    bundles: {
      "typeahead-bundle": ["bloodhound", "typeahead.js"]
    },
    shim: {
      "bootstrap/alert": {deps: ["jquery", "bootstrap/transition"]},
      "bootstrap/collapse": {deps: ["jquery", "bootstrap/transition"]},
      "bootstrap/dropdown": {deps: ["jquery"]},
      "bootstrap/modal": {deps: ["jquery", "bootstrap/transition"]},
      "bootstrap/popover": {deps: ["jquery", "bootstrap/tooltip"]},
      "bootstrap/tab": {deps: ["jquery", "bootstrap/transition"]},
      "bootstrap/tooltip": {deps: ["jquery", "bootstrap/transition"]},
      "bootstrap/transition": {deps: ["jquery"]},
      "jquery-i18n": {deps: ["jquery"]},
      "jqueryui-touch-punch": {deps: ["jquery", "jquery-ui/widget", "jquery-ui/mouse"]},
      stomp: {exports: "Stomp"}
    }
  });
});
