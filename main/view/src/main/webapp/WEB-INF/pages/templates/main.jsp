<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%--
/**
 * CoCeSo
 * Client HTML main templates
 * Copyright (c) WRK\Coceso-Team
 *
 * Licensed under the GNU General Public License, version 3 (GPL-3.0)
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright Copyright (c) 2014 WRK\Coceso-Team
 * @link https://github.com/wrk-fmd/CoCeSo
 * @license GPL-3.0 http://opensource.org/licenses/GPL-3.0
 */
--%>
<script type="text/html" id="unit-list-entry-template">
  <li class="dropdown">
    <!-- ko if: portable -->
    <a href="#" class="unit_state dropdown-toggle last-child-corner-right" data-bind="draggable: dragOptions, popover: popover" data-toggle="dropdown" oncontextmenu="this.click(); return false;">
      <span class="ui-corner-left" data-bind="text: call, css: stateCss"></span><!--
      --><!-- ko if: incidentCount() === 0
      --><span data-bind="css: isFree() ? 'unit_state_free' : stateCss()">
        <span class="glyphicon" data-bind="css: isHome() ? 'glyphicon-home' : 'glyphicon-exclamation-sign'"></span>
      </span><!-- /ko
      --><!-- ko foreach: incidents --><span data-bind="html: taskText, css: incident() && incident().typeCss(), click: nextState, clickBubble: false"></span><!-- /ko -->
    </a>
    <!-- /ko -->
    <!-- ko ifnot: portable -->
    <a href="#" class="unit_state dropdown-toggle" data-toggle="dropdown" oncontextmenu="this.click(); return false;">
      <span class="ui-corner-all" data-bind="text: call, css: stateCss"></span>
    </a>
    <!-- /ko -->
    <ul class="dropdown-menu">
      <li class="dropdown-header"><spring:message code="unit.state.set"/></li>
      <!-- ko ifnot: isNEB -->
      <li><a href="#"
             title="<spring:message code="unit.state.set"/>: <spring:message code="unit.state.neb"/>"
             data-bind="click: setNEB"><spring:message code="unit.state.neb"/></a></li>
      <!-- /ko -->
      <!-- ko ifnot: isEB -->
      <li><a href="#"
             title="<spring:message code="unit.state.set"/>: <spring:message code="unit.state.eb"/>"
             data-bind="click: setEB"><spring:message code="unit.state.eb"/></a></li>
      <!-- /ko -->
      <!-- ko ifnot: isAD -->
      <li><a href="#"
             title="<spring:message code="unit.state.set"/>: <spring:message code="unit.state.ad"/>"
             data-bind="click: setAD"><spring:message code="unit.state.ad"/></a></li>
      <!-- /ko -->

      <!-- ko if: portable && !( disableSendHome() && disableStandby() && disableHoldPosition() )  -->
      <li class="divider"></li>
      <li class="dropdown-header"><spring:message code="actions"/></li>
      <!-- ko ifnot: disableSendHome -->
      <li><a href="#" title="<spring:message code="unit.sendhome"/>" data-bind="click: sendHome"><spring:message code="unit.sendhome"/></a></li>
      <!-- /ko -->
      <!-- ko ifnot: disableStandby -->
      <li><a href="#" title="<spring:message code="incident.type.standby"/>" data-bind="click: standby"><spring:message code="incident.type.standby"/></a></li>
      <!-- /ko -->
      <!-- ko ifnot: disableHoldPosition -->
      <li><a href="#" title="<spring:message code="incident.type.holdposition"/>" data-bind="click: holdPosition"><spring:message
          code="incident.type.holdposition"/></a></li>
      <!-- /ko -->
      <!-- /ko -->

      <!-- ko if: ani -->
      <li class="divider"></li>
      <li><a href="#" title="<spring:message code="radio.send"/>" data-bind="click: sendCall"><spring:message code="radio.send"/></a></li>
      <!-- /ko -->

      <!-- ko if: portable && dropdownActive  -->
      <li class="divider"></li>
      <li class="dropdown-header"><spring:message code="incidents"/></li>
      <!-- ko foreach: dropdownIncidents -->
      <li><a href="#" title="<spring:message code="incident.edit"/>"
             data-bind="click: incident() && incident().openForm, html: incident() && incident().dropdownTitle"></a></li>
      <!-- /ko -->
      <!-- /ko -->

      <li class="divider"></li>
      <!-- ko if: portable -->
      <li><a href="#" title="<spring:message code="unit.incident.new"/>" data-bind="click: addIncident"><spring:message code="unit.incident.new"/>...</a></li>
      <li><a href="#" title="<spring:message code="unit.relocation.new"/>" data-bind="click: addRelocation"><spring:message code="unit.relocation.new"/>...</a></li>
      <!-- /ko -->
      <li><a href="#" title="<spring:message code="unit.incident.report"/>" data-bind="click: reportIncident"><spring:message code="unit.incident.report"/>...</a></li>
      <li><a href="#" title="<spring:message code="log.add"/>" data-bind="click: addLog"><spring:message code="log.add"/>...</a></li>

      <li class="divider"></li>
      <li><a href="#" title="<spring:message code="unit.details"/>" data-bind="click: openDetails"><spring:message code="unit.details"/>...</a></li>
      <li><a href="#" title="<spring:message code="unit.edit"/>" data-bind="click: openForm"><spring:message code="unit.edit"/>...</a></li>
      <li>
        <a href="#" target="_blank" title="<spring:message code="log.view"/>"
             data-bind="attr: {href: '<c:url value="/dashboard?uid="/>' + id}">
          <spring:message code="log.view"/> <span class="glyphicon glyphicon-new-window"></span>
        </a>
      </li>
    </ul>
  </li>
</script>

<script type="text/html" id="container-template">
  <%-- UNIT LIST --%>
  <ul class="unit_list" data-bind="template: {name: 'unit-list-entry-template', foreach: units}"></ul>
  <%-- /UNIT LIST --%>

  <div class="unit_container" data-bind="foreach: subContainer">
    <!-- ko if: totalCounter -->
    <div class="panel panel-default unit-container" data-bind="toggle: {id: id, hide: $root.toggleContainer, isHidden: $root.isHidden}">
      <div class="toggle-handle panel-heading clearfix">
        <span data-bind="text: name"></span>
        <span class="pull-left">
          <span class="toggle-indicator glyphicon glyphicon-chevron-up"></span>
        </span>
        <span class="pull-right">
          <span class="badge notification-ok" data-bind="text: availableCounter"></span>
          <span class="badge" data-bind="text: totalCounter"></span>
        </span>
      </div>
      <div class="toggle-body panel-body">
        <div data-bind="template: 'container-template'"></div>
      </div>
    </div>
    <!-- /ko -->
  </div>
</script>
<%-- END TEMPLATE DEFINITIONs --%>

<div id="next-state-confirm" class="modal" tabindex="-1" role="dialog" aria-hidden="true" style="display: none">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" data-bind="html: title"></h4>
      </div>
      <div class="modal-body">
        <p data-bind="html: info_text"></p>

        <dl class="dl-horizontal">
          <!-- ko foreach: elements -->
          <dt data-bind="text: key"></dt>
          <dd class="clearfix"><span class="pre" data-bind="text: val"></span></dd>
          <!-- /ko -->
        </dl>

      </div>
      <div class="modal-footer">
        <button type="button" id="next-state-confirm-no" class="btn btn-danger btn-lg pull-left" data-dismiss="modal" autofocus><spring:message
            code="no"/></button>
        <button type="button" id="next-state-confirm-yes" class="btn btn-success btn-lg pull-right" data-dismiss="modal"
                data-bind="click: save, text: button_text"></button>
      </div>
    </div>
  </div>
</div>
