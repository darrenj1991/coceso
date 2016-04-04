/**
 * CoCeSo
 * Client JS - ko/extenders/isvalue
 * Copyright (c) WRK\Coceso-Team
 *
 * Licensed under the GNU General Public License, version 3 (GPL-3.0)
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright Copyright (c) 2016 WRK\Coceso-Team
 * @link https://sourceforge.net/projects/coceso/
 * @license GPL-3.0 http://opensource.org/licenses/GPL-3.0
 */

define(["knockout"], function(ko) {
  "use strict";

  ko.extenders.isValue = function(target, value) {
    var ret = ko.pureComputed(function() {
      return (target() === ko.utils.unwrapObservable(value));
    });

    ret.state = ko.pureComputed(function() {
      return this() ? "active" : "";
    }, ret);

    ret.set = function() {
      target(ko.utils.unwrapObservable(value));
    };

    return ret;
  };
});
