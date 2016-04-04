/**
 * CoCeSo
 * Client JS - home
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
  require(["knockout", "home/viewmodel", "bootstrap/collapse", "bootstrap/dropdown", "bootstrap/alert", "utils/misc"],
    function(ko, Home) {
      "use strict";
      ko.applyBindings(new Home());
    });
}
);
